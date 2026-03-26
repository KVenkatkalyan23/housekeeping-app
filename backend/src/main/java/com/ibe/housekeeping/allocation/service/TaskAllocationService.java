package com.ibe.housekeeping.allocation.service;

import com.ibe.housekeeping.allocation.dto.AllocationResultSummaryResponse;
import com.ibe.housekeeping.allocation.dto.RunAllocationResponse;
import com.ibe.housekeeping.allocation.dto.TaskAssignmentItemResponse;
import com.ibe.housekeeping.allocation.dto.UnassignedTaskItemResponse;
import com.ibe.housekeeping.allocation.repository.TaskAssignmentRepository;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.common.enums.TaskType;
import com.ibe.housekeeping.entity.CleaningTask;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.TaskAssignment;
import com.ibe.housekeeping.leave.repository.LeaveRequestRepository;
import com.ibe.housekeeping.shift.repository.ShiftRepository;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import com.ibe.housekeeping.task.repository.CleaningTaskRepository;
import com.ibe.housekeeping.task.service.CleaningTaskService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskAllocationService {

    private static final int MAX_SHIFT_MINUTES = 240;
    private static final String APPROVED_LEAVE_STATUS = "APPROVED";
    private static final EnumSet<TaskStatus> EXCLUDED_STATUSES = EnumSet.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED);
    private static final LocalTime MORNING_START = LocalTime.of(8, 0);
    private static final LocalTime MORNING_END = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_START = LocalTime.of(13, 0);
    private static final LocalTime AFTERNOON_END = LocalTime.of(17, 0);

    private final CleaningTaskRepository cleaningTaskRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final ShiftRepository shiftRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final CleaningTaskService cleaningTaskService;

    public TaskAllocationService(
            CleaningTaskRepository cleaningTaskRepository,
            StaffProfileRepository staffProfileRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            ShiftRepository shiftRepository,
            LeaveRequestRepository leaveRequestRepository,
            CleaningTaskService cleaningTaskService
    ) {
        this.cleaningTaskRepository = cleaningTaskRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.shiftRepository = shiftRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.cleaningTaskService = cleaningTaskService;
    }

    @Transactional
    public RunAllocationResponse runAllocation(LocalDate taskDate) {
        cleaningTaskService.generateTasks(taskDate);
        List<Shift> allocationShifts = getAllocationShifts();
        List<CleaningTask> unassignedTasks = cleaningTaskRepository.findUnassignedEligibleTasksForAllocation(taskDate, EXCLUDED_STATUSES);
        List<UUID> shiftIds = allocationShifts.stream().map(Shift::getId).toList();
        List<TaskAssignment> existingAssignments = taskAssignmentRepository.findAllByTaskDateAndShiftIds(taskDate, shiftIds);
        List<StaffProfile> eligibleStaff = allocationShifts.stream()
                .flatMap(shift -> getEligibleStaff(shift.getId(), taskDate).stream())
                .toList();

        Map<UUID, StaffAllocationState> staffStates = initializeStaffStates(eligibleStaff, existingAssignments);
        List<CleaningTask> deepCleanTasks = new ArrayList<>();
        List<CleaningTask> dailyTasks = new ArrayList<>();
        List<CleaningTask> vacantTasks = new ArrayList<>();

        for (CleaningTask task : unassignedTasks) {
            if (!isTaskValid(task)) {
                continue;
            }
            if (task.getTaskType() == TaskType.DEEP_CLEAN) {
                deepCleanTasks.add(task);
            } else if (task.getTaskType() == TaskType.DAILY_CLEAN) {
                dailyTasks.add(task);
            } else if (task.getTaskType() == TaskType.VACANT_CLEAN) {
                vacantTasks.add(task);
            }
        }

        List<TaskAssignment> newAssignments = new ArrayList<>();

        allocateTasks(deepCleanTasks, staffStates, newAssignments);
        allocateTasks(dailyTasks, staffStates, newAssignments);
        allocateTasks(vacantTasks, staffStates, newAssignments);

        if (!newAssignments.isEmpty()) {
            taskAssignmentRepository.saveAll(newAssignments);
            staffProfileRepository.saveAll(eligibleStaff);
        }

        List<TaskAssignment> allAssignments = taskAssignmentRepository.findAllByTaskDateAndShiftIds(taskDate, shiftIds);
        List<CleaningTask> remainingUnassigned = cleaningTaskRepository.findUnassignedEligibleTasksForResult(taskDate, EXCLUDED_STATUSES);

        return new RunAllocationResponse(
                buildSummary(taskDate, allAssignments.size(), remainingUnassigned.size()),
                allAssignments.stream().map(this::toAssignmentItem).toList(),
                remainingUnassigned.stream().map(this::toUnassignedItem).toList()
        );
    }

    @Transactional(readOnly = true)
    public RunAllocationResponse getAllocation(LocalDate taskDate) {
        List<UUID> shiftIds = getAllocationShifts().stream().map(Shift::getId).toList();
        List<TaskAssignment> assignments = taskAssignmentRepository.findAllByTaskDateAndShiftIds(taskDate, shiftIds);
        List<CleaningTask> unassignedTasks = cleaningTaskRepository.findUnassignedEligibleTasksForResult(taskDate, EXCLUDED_STATUSES);

        return new RunAllocationResponse(
                buildSummary(taskDate, assignments.size(), unassignedTasks.size()),
                assignments.stream().map(this::toAssignmentItem).toList(),
                unassignedTasks.stream().map(this::toUnassignedItem).toList()
        );
    }

    private List<StaffProfile> getEligibleStaff(UUID shiftId, LocalDate taskDate) {
        return staffProfileRepository
                .findAllByCurrentShiftIdOrderByIdAsc(shiftId)
                .stream()
                .filter(staff -> !leaveRequestRepository
                        .existsByStaffIdAndStatusIgnoreCaseAndLeaveStartDateLessThanEqualAndLeaveEndDateGreaterThanEqual(
                                staff.getId(),
                                APPROVED_LEAVE_STATUS,
                                taskDate,
                                taskDate
                        ))
                .toList();
    }

    private Map<UUID, StaffAllocationState> initializeStaffStates(
            List<StaffProfile> eligibleStaff,
            List<TaskAssignment> existingAssignments
    ) {
        Map<UUID, StaffAllocationState> staffStates = new HashMap<>();
        for (StaffProfile staff : eligibleStaff) {
            staffStates.put(
                    staff.getId(),
                    new StaffAllocationState(staff, safeMinutes(staff.getTotalMinutesWorked()))
            );
        }

        for (TaskAssignment assignment : existingAssignments) {
            if (assignment.getStaff() == null) {
                continue;
            }
            StaffAllocationState state = staffStates.get(assignment.getStaff().getId());
            if (state == null) {
                continue;
            }

            int estimatedMinutes = safeMinutes(assignment.getCleaningTask().getEstimatedMinutes());
            state.allocatedMinutes += estimatedMinutes;
            if (assignment.getCleaningTask().getTaskType() == TaskType.DEEP_CLEAN) {
                state.deepCleanAssigned = true;
            }
        }

        return staffStates;
    }

    private void allocateTasks(
            List<CleaningTask> tasks,
            Map<UUID, StaffAllocationState> staffStates,
            List<TaskAssignment> newAssignments
    ) {
        for (CleaningTask task : tasks) {
            Optional<StaffAllocationState> candidate = selectBestStaff(task, staffStates);
            if (candidate.isEmpty()) {
                continue;
            }

            StaffAllocationState state = candidate.get();
            task.setShift(state.staff.getCurrentShift());
            task.setTaskStatus(TaskStatus.ASSIGNED);
            newAssignments.add(TaskAssignment.builder()
                    .cleaningTask(task)
                    .staff(state.staff)
                    .build());

            state.allocatedMinutes += task.getEstimatedMinutes();
            if (task.getTaskType() == TaskType.DEEP_CLEAN) {
                state.deepCleanAssigned = true;
            }
            state.staff.setTotalMinutesWorked(safeMinutes(state.staff.getTotalMinutesWorked()) + task.getEstimatedMinutes());
        }
    }

    private Optional<StaffAllocationState> selectBestStaff(
            CleaningTask task,
            Map<UUID, StaffAllocationState> staffStates
    ) {
        return staffStates.values().stream()
                .filter(state -> canFitTask(task, state))
                .min(Comparator
                        .comparingInt((StaffAllocationState state) -> state.allocatedMinutes)
                        .thenComparingInt(state -> state.historicalMinutes)
                        .thenComparing(state -> state.staff.getId()));
    }

    private boolean canFitTask(CleaningTask task, StaffAllocationState state) {
        if (state.allocatedMinutes + task.getEstimatedMinutes() > MAX_SHIFT_MINUTES) {
            return false;
        }

        return task.getTaskType() != TaskType.DEEP_CLEAN || !state.deepCleanAssigned;
    }

    private boolean isTaskValid(CleaningTask task) {
        return task.getEstimatedMinutes() != null && task.getEstimatedMinutes() > 0;
    }

    private AllocationResultSummaryResponse buildSummary(
            LocalDate taskDate,
            int assignedCount,
            int unassignedCount
    ) {
        return new AllocationResultSummaryResponse(
                taskDate,
                assignedCount + unassignedCount,
                assignedCount,
                unassignedCount
        );
    }

    private TaskAssignmentItemResponse toAssignmentItem(TaskAssignment assignment) {
        CleaningTask task = assignment.getCleaningTask();
        Shift shift = task.getShift();
        StaffProfile staff = assignment.getStaff();

        return new TaskAssignmentItemResponse(
                task.getId(),
                task.getRoom().getRoomNumber(),
                task.getTaskType(),
                task.getEstimatedMinutes(),
                staff != null ? staff.getId() : null,
                staff != null ? staff.getFullName() : "Unassigned",
                shift != null ? shift.getId() : null,
                shift != null ? shift.getShiftCode() : null,
                shift != null ? shift.getShiftName() : null
        );
    }

    private UnassignedTaskItemResponse toUnassignedItem(CleaningTask task) {
        String reason = task.getTaskType() == TaskType.DEEP_CLEAN
                ? "No staff capacity available for deep clean in this shift."
                : "No staff capacity available in this shift.";

        return new UnassignedTaskItemResponse(
                task.getId(),
                task.getRoom().getRoomNumber(),
                task.getTaskType(),
                task.getEstimatedMinutes(),
                reason
        );
    }

    private List<Shift> getAllocationShifts() {
        Shift morningShift = requireShiftByWindow(MORNING_START, MORNING_END, "Morning");
        Shift afternoonShift = requireShiftByWindow(AFTERNOON_START, AFTERNOON_END, "Afternoon");
        return List.of(morningShift, afternoonShift);
    }

    private Shift requireShiftByWindow(LocalTime startTime, LocalTime endTime, String label) {
        return shiftRepository.findByStartTimeAndEndTime(startTime, endTime)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "%s shift is not configured.".formatted(label)
                ));
    }

    private int safeMinutes(Integer minutes) {
        return minutes == null ? 0 : minutes;
    }

    private static final class StaffAllocationState {
        private final StaffProfile staff;
        private final int historicalMinutes;
        private int allocatedMinutes;
        private boolean deepCleanAssigned;

        private StaffAllocationState(StaffProfile staff, int historicalMinutes) {
            this.staff = staff;
            this.historicalMinutes = historicalMinutes;
            this.allocatedMinutes = 0;
            this.deepCleanAssigned = false;
        }
    }
}
