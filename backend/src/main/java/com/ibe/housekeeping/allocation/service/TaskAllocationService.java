package com.ibe.housekeeping.allocation.service;

import com.ibe.housekeeping.activitylog.service.ActivityLogService;
import com.ibe.housekeeping.allocation.dto.AllocationResultSummaryResponse;
import com.ibe.housekeeping.allocation.dto.RunAllocationResponse;
import com.ibe.housekeeping.allocation.dto.TaskAssignmentItemResponse;
import com.ibe.housekeeping.allocation.dto.UnassignedTaskItemResponse;
import com.ibe.housekeeping.allocation.repository.TaskAssignmentRepository;
import com.ibe.housekeeping.common.enums.LeaveStatus;
import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.common.enums.TaskType;
import com.ibe.housekeeping.entity.CleaningTask;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.TaskAssignment;
import com.ibe.housekeeping.entity.User;
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
import java.util.LinkedHashMap;
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

    private static final int MAX_DAILY_MINUTES = 240;
    private static final int MAX_BUCKET_MINUTES = 120;
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
    private final TaskDateLockService taskDateLockService;
    private final ActivityLogService activityLogService;

    public TaskAllocationService(
            CleaningTaskRepository cleaningTaskRepository,
            StaffProfileRepository staffProfileRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            ShiftRepository shiftRepository,
            LeaveRequestRepository leaveRequestRepository,
            CleaningTaskService cleaningTaskService,
            TaskDateLockService taskDateLockService,
            ActivityLogService activityLogService
    ) {
        this.cleaningTaskRepository = cleaningTaskRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.shiftRepository = shiftRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.cleaningTaskService = cleaningTaskService;
        this.taskDateLockService = taskDateLockService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public RunAllocationResponse runAllocation(LocalDate taskDate) {
        return taskDateLockService.executeWithTaskDateLock(taskDate, () -> runAllocationInternal(taskDate, true));
    }

    @Transactional
    public RunAllocationResponse runScheduledAllocation(LocalDate taskDate) {
        return taskDateLockService.executeWithTaskDateLock(taskDate, () -> runAllocationInternal(taskDate, true));
    }

    @Transactional(readOnly = true)
    public RunAllocationResponse getAllocation(LocalDate taskDate) {
        List<TaskAssignment> assignments = taskAssignmentRepository.findAllByTaskDate(taskDate);
        List<CleaningTask> unassignedTasks = cleaningTaskRepository.findUnassignedEligibleTasksForResult(taskDate, EXCLUDED_STATUSES);

        return new RunAllocationResponse(
                buildSummary(taskDate, assignments.size(), unassignedTasks.size()),
                assignments.stream().map(this::toAssignmentItem).toList(),
                unassignedTasks.stream().map(this::toUnassignedItem).toList()
        );
    }

    @Transactional(readOnly = true)
    public List<TaskAssignmentItemResponse> getTaskAssignments(LocalDate taskDate) {
        return taskAssignmentRepository.findAllByTaskDate(taskDate)
                .stream()
                .map(this::toAssignmentItem)
                .toList();
    }

    private RunAllocationResponse runAllocationInternal(LocalDate taskDate, boolean generateTasks) {
        if (generateTasks) {
            cleaningTaskService.generateTasks(taskDate);
        }

        List<CleaningTask> unassignedTasks = cleaningTaskRepository.findUnassignedEligibleTasksForAllocation(taskDate, EXCLUDED_STATUSES);
        ShiftAllocationConfig shiftConfig = getAllocationShiftConfig();
        List<TaskAssignment> existingAssignments = taskAssignmentRepository.findAllByTaskDate(taskDate);
        List<StaffProfile> eligibleStaff = getEligibleStaff(taskDate);

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

        allocateTasks(deepCleanTasks, staffStates, shiftConfig, newAssignments);
        allocateTasks(dailyTasks, staffStates, shiftConfig, newAssignments);
        allocateTasks(vacantTasks, staffStates, shiftConfig, newAssignments);

        if (!newAssignments.isEmpty()) {
            taskAssignmentRepository.saveAll(newAssignments);
            newAssignments.forEach(activityLogService::logTaskAssigned);
        }

        List<TaskAssignment> allAssignments = taskAssignmentRepository.findAllByTaskDate(taskDate);
        List<CleaningTask> remainingUnassigned = cleaningTaskRepository.findUnassignedEligibleTasksForResult(taskDate, EXCLUDED_STATUSES);

        RunAllocationResponse response = new RunAllocationResponse(
                buildSummary(taskDate, allAssignments.size(), remainingUnassigned.size()),
                allAssignments.stream().map(this::toAssignmentItem).toList(),
                remainingUnassigned.stream().map(this::toUnassignedItem).toList()
        );
        activityLogService.logAllocationRun(
                taskDate,
                allAssignments.size(),
                remainingUnassigned.size(),
                allAssignments.size() + remainingUnassigned.size()
        );
        return response;
    }

    private List<StaffProfile> getEligibleStaff(LocalDate taskDate) {
        return staffProfileRepository
                .findAllByOrderByIdAsc()
                .stream()
                .filter(staff -> !leaveRequestRepository
                        .existsByStaffIdAndStatusAndLeaveStartDateLessThanEqualAndLeaveEndDateGreaterThanEqual(
                                staff.getId(),
                                LeaveStatus.APPROVED,
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
            AllocationBucket bucket = resolveBucket(assignment.getCleaningTask());
            if (bucket == null) {
                continue;
            }
            state.allocatedMinutes += estimatedMinutes;
            state.allocatedTaskCount += 1;
            state.bucketMinutes.merge(bucket, estimatedMinutes, Integer::sum);
        }

        return staffStates;
    }

    private void allocateTasks(
            List<CleaningTask> tasks,
            Map<UUID, StaffAllocationState> staffStates,
            ShiftAllocationConfig shiftConfig,
            List<TaskAssignment> newAssignments
    ) {
        for (CleaningTask task : tasks) {
            Optional<CandidatePlacement> candidate = selectBestPlacement(task, staffStates, shiftConfig);
            if (candidate.isEmpty()) {
                continue;
            }

            CandidatePlacement placement = candidate.get();
            StaffAllocationState state = placement.staffState();
            task.setShift(placement.bucket().resolveShift(shiftConfig));
            task.setTaskStatus(TaskStatus.ASSIGNED);
            newAssignments.add(TaskAssignment.builder()
                    .cleaningTask(task)
                    .staff(state.staff)
                    .build());

            int estimatedMinutes = safeMinutes(task.getEstimatedMinutes());
            state.allocatedMinutes += estimatedMinutes;
            state.allocatedTaskCount += 1;
            state.bucketMinutes.merge(placement.bucket(), estimatedMinutes, Integer::sum);
        }
    }

    private Optional<CandidatePlacement> selectBestPlacement(
            CleaningTask task,
            Map<UUID, StaffAllocationState> staffStates,
            ShiftAllocationConfig shiftConfig
    ) {
        List<CandidatePlacement> matchingCandidates = new ArrayList<>();
        List<CandidatePlacement> fallbackCandidates = new ArrayList<>();

        for (StaffAllocationState state : staffStates.values()) {
            for (AllocationBucket bucket : AllocationBucket.allowedBucketsFor(task.getTaskType())) {
                if (!canFitTask(task, state, bucket)) {
                    continue;
                }

                CandidatePlacement placement = new CandidatePlacement(
                        state,
                        bucket,
                        isPreferredShiftMatch(state.staff, bucket, shiftConfig)
                );

                if (placement.preferredShiftMatched()) {
                    matchingCandidates.add(placement);
                } else {
                    fallbackCandidates.add(placement);
                }
            }
        }

        List<CandidatePlacement> rankedCandidates = matchingCandidates.isEmpty() ? fallbackCandidates : matchingCandidates;
        return rankedCandidates.stream()
                .min(Comparator
                        .comparingInt((CandidatePlacement placement) -> placement.staffState().allocatedMinutes)
                        .thenComparingInt(placement -> placement.staffState().historicalMinutes)
                        .thenComparingInt(placement -> placement.staffState().allocatedTaskCount)
                        .thenComparing(placement -> placement.staffState().staff.getId())
                        .thenComparingInt(placement -> placement.bucket().sortOrder));
    }

    private boolean canFitTask(CleaningTask task, StaffAllocationState state, AllocationBucket bucket) {
        int estimatedMinutes = safeMinutes(task.getEstimatedMinutes());

        if (estimatedMinutes <= 0) {
            return false;
        }

        if (!bucket.allowedTaskTypes.contains(task.getTaskType())) {
            return false;
        }

        if (state.allocatedMinutes + estimatedMinutes > MAX_DAILY_MINUTES) {
            return false;
        }

        return state.bucketMinutes.getOrDefault(bucket, 0) + estimatedMinutes <= MAX_BUCKET_MINUTES;
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
        User user = staff != null ? staff.getUser() : null;

        return new TaskAssignmentItemResponse(
                task.getId(),
                task.getRoom().getRoomNumber(),
                task.getTaskType(),
                task.getEstimatedMinutes(),
                staff != null ? staff.getId() : null,
                user != null ? user.getUsername() : null,
                staff != null ? staff.getFullName() : "Unassigned",
                shift != null ? shift.getId() : null,
                shift != null ? shift.getShiftCode() : null,
                shift != null ? shift.getShiftName() : null,
                staff != null && shift != null && staff.getPreferredShift() != null && shift.getId().equals(staff.getPreferredShift().getId())
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

    private ShiftAllocationConfig getAllocationShiftConfig() {
        Shift morningShift = requireShiftByWindow(MORNING_START, MORNING_END, "Morning");
        Shift afternoonShift = requireShiftByWindow(AFTERNOON_START, AFTERNOON_END, "Afternoon");
        return new ShiftAllocationConfig(morningShift, afternoonShift);
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

    private boolean isPreferredShiftMatch(StaffProfile staff, AllocationBucket bucket, ShiftAllocationConfig shiftConfig) {
        Shift preferredShift = staff.getPreferredShift();
        if (preferredShift == null) {
            return false;
        }

        return preferredShift.getId().equals(bucket.resolveShift(shiftConfig).getId());
    }

    private AllocationBucket resolveBucket(CleaningTask task) {
        Shift shift = task.getShift();
        if (shift == null || task.getTaskType() == null) {
            return null;
        }

        boolean morningShift = MORNING_START.equals(shift.getStartTime()) && MORNING_END.equals(shift.getEndTime());
        boolean afternoonShift = AFTERNOON_START.equals(shift.getStartTime()) && AFTERNOON_END.equals(shift.getEndTime());

        if (task.getTaskType() == TaskType.DEEP_CLEAN) {
            if (morningShift) {
                return AllocationBucket.B;
            }
            if (afternoonShift) {
                return AllocationBucket.C;
            }
        }

        if (task.getTaskType() == TaskType.DAILY_CLEAN || task.getTaskType() == TaskType.VACANT_CLEAN) {
            if (morningShift) {
                return AllocationBucket.A;
            }
            if (afternoonShift) {
                return AllocationBucket.D;
            }
        }

        return null;
    }

    private record ShiftAllocationConfig(Shift morningShift, Shift afternoonShift) {
    }

    private enum AllocationBucket {
        A(0, EnumSet.of(TaskType.DAILY_CLEAN, TaskType.VACANT_CLEAN)) {
            @Override
            Shift resolveShift(ShiftAllocationConfig shiftConfig) {
                return shiftConfig.morningShift();
            }
        },
        D(1, EnumSet.of(TaskType.DAILY_CLEAN, TaskType.VACANT_CLEAN)) {
            @Override
            Shift resolveShift(ShiftAllocationConfig shiftConfig) {
                return shiftConfig.afternoonShift();
            }
        },
        B(2, EnumSet.of(TaskType.DEEP_CLEAN, TaskType.DAILY_CLEAN, TaskType.VACANT_CLEAN)) {
            @Override
            Shift resolveShift(ShiftAllocationConfig shiftConfig) {
                return shiftConfig.morningShift();
            }
        },
        C(3, EnumSet.of(TaskType.DEEP_CLEAN, TaskType.DAILY_CLEAN, TaskType.VACANT_CLEAN)) {
            @Override
            Shift resolveShift(ShiftAllocationConfig shiftConfig) {
                return shiftConfig.afternoonShift();
            }
        };

        private final int sortOrder;
        private final EnumSet<TaskType> allowedTaskTypes;

        AllocationBucket(int sortOrder, EnumSet<TaskType> allowedTaskTypes) {
            this.sortOrder = sortOrder;
            this.allowedTaskTypes = allowedTaskTypes;
        }

        abstract Shift resolveShift(ShiftAllocationConfig shiftConfig);

        private static List<AllocationBucket> allowedBucketsFor(TaskType taskType) {
            return switch (taskType) {
                case DEEP_CLEAN -> List.of(B, C);
                case DAILY_CLEAN, VACANT_CLEAN -> List.of(A, D, B, C);
            };
        }
    }

    private record CandidatePlacement(
            StaffAllocationState staffState,
            AllocationBucket bucket,
            boolean preferredShiftMatched
    ) {
    }

    private static final class StaffAllocationState {
        private final StaffProfile staff;
        private final int historicalMinutes;
        private int allocatedMinutes;
        private int allocatedTaskCount;
        private final Map<AllocationBucket, Integer> bucketMinutes;

        private StaffAllocationState(StaffProfile staff, int historicalMinutes) {
            this.staff = staff;
            this.historicalMinutes = historicalMinutes;
            this.allocatedMinutes = 0;
            this.allocatedTaskCount = 0;
            this.bucketMinutes = new LinkedHashMap<>();
        }
    }
}
