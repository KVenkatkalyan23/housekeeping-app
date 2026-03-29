package com.ibe.housekeeping.admin.taskreassignment.service;

import com.ibe.housekeeping.activitylog.dto.CreateActivityLogRequest;
import com.ibe.housekeeping.activitylog.service.ActivityLogService;
import com.ibe.housekeeping.admin.taskreassignment.dto.ManualTaskReassignmentResponse;
import com.ibe.housekeeping.admin.taskreassignment.dto.ReassignmentCandidateItemResponse;
import com.ibe.housekeeping.allocation.repository.TaskAssignmentRepository;
import com.ibe.housekeeping.allocation.service.TaskDateLockService;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.common.enums.ActorType;
import com.ibe.housekeeping.common.enums.AssignmentSource;
import com.ibe.housekeeping.common.enums.EventCategory;
import com.ibe.housekeeping.common.enums.Severity;
import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.entity.CleaningTask;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.TaskAssignment;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import com.ibe.housekeeping.task.repository.CleaningTaskRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminTaskReassignmentService {

    private static final int MAX_SHIFT_MINUTES = 240;

    private final CleaningTaskRepository cleaningTaskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final UserRepository userRepository;
    private final TaskDateLockService taskDateLockService;
    private final ActivityLogService activityLogService;

    public AdminTaskReassignmentService(
            CleaningTaskRepository cleaningTaskRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            StaffProfileRepository staffProfileRepository,
            UserRepository userRepository,
            TaskDateLockService taskDateLockService,
            ActivityLogService activityLogService
    ) {
        this.cleaningTaskRepository = cleaningTaskRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.userRepository = userRepository;
        this.taskDateLockService = taskDateLockService;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public List<ReassignmentCandidateItemResponse> getCandidates(UUID taskId) {
        CleaningTask task = findTask(taskId);
        return staffProfileRepository.findAllForReassignment().stream()
                .map(staff -> toCandidate(task, staff))
                .filter(ReassignmentCandidateItemResponse::capacityAvailable)
                .toList();
    }

    @Transactional
    public ManualTaskReassignmentResponse reassignTask(UUID taskId, UUID targetStaffId, String actorUsername) {
        CleaningTask task = findTask(taskId);
        return taskDateLockService.executeWithTaskDateLock(
                task.getTaskDate(),
                () -> reassignTaskInternal(taskId, targetStaffId, actorUsername)
        );
    }

    private ManualTaskReassignmentResponse reassignTaskInternal(UUID taskId, UUID targetStaffId, String actorUsername) {
        TaskAssignment assignment = taskAssignmentRepository.findByCleaningTaskIdForReassignment(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Task is not currently assigned."
                ));

        CleaningTask task = assignment.getCleaningTask();
        validateTaskIsReassignable(task);
        Shift shift = requireShift(task);
        StaffProfile currentStaff = requireAssignedStaff(assignment);
        StaffProfile targetStaff = findTargetStaff(targetStaffId);

        validateTargetStaff(currentStaff, targetStaff);
        validateTargetCapacity(task, targetStaff);

        assignment.setStaff(targetStaff);
        assignment.setAssignmentSource(AssignmentSource.MANUAL);
        task.setTaskStatus(TaskStatus.ASSIGNED);
        taskAssignmentRepository.save(assignment);

        User actorUser = findActor(actorUsername);
        activityLogService.logEvent(CreateActivityLogRequest.builder()
                .eventCode("TASK_REASSIGNED")
                .eventTitle("Manual Task Reassignment")
                .eventMessage("Task for room %s reassigned from %s to %s.".formatted(
                        task.getRoom().getRoomNumber(),
                        currentStaff.getFullName(),
                        targetStaff.getFullName()
                ))
                .eventCategory(EventCategory.TASK)
                .severity(Severity.WARNING)
                .actorType(ActorType.USER)
                .actorUserId(actorUser.getId())
                .actorName(resolveActorName(actorUser))
                .targetEntityType("TASK")
                .targetEntityId(task.getId())
                .relatedStaffId(targetStaff.getId())
                .relatedRoomId(task.getRoom().getId())
                .metadata(buildAuditMetadata(task, currentStaff, targetStaff, shift))
                .build());

        return new ManualTaskReassignmentResponse(
                task.getId(),
                task.getRoom().getId(),
                String.valueOf(task.getRoom().getRoomNumber()),
                currentStaff.getId(),
                currentStaff.getFullName(),
                targetStaff.getId(),
                targetStaff.getFullName(),
                shift.getId(),
                shift.getShiftName(),
                task.getTaskStatus(),
                assignment.getAssignmentSource(),
                "Task reassigned successfully."
        );
    }

    private CleaningTask findTask(UUID taskId) {
        return cleaningTaskRepository.findByIdForManualReassignment(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found."));
    }

    private StaffProfile findTargetStaff(UUID targetStaffId) {
        return staffProfileRepository.findByIdForReassignment(targetStaffId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target staff member not found."));
    }

    private StaffProfile requireAssignedStaff(TaskAssignment assignment) {
        if (assignment.getStaff() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task is not currently assigned.");
        }

        return assignment.getStaff();
    }

    private void validateTaskIsReassignable(CleaningTask task) {
        if (task.getTaskStatus() == TaskStatus.COMPLETED || task.getTaskStatus() == TaskStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Completed or cancelled tasks cannot be reassigned."
            );
        }
    }

    private Shift requireShift(CleaningTask task) {
        if (task.getShift() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task shift is not configured.");
        }

        return task.getShift();
    }

    private void validateTargetStaff(StaffProfile currentStaff, StaffProfile targetStaff) {
        if (currentStaff.getId().equals(targetStaff.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selected staff member must be different from the current assignee."
            );
        }
    }

    private void validateTargetCapacity(CleaningTask task, StaffProfile targetStaff) {
        int allocatedMinutes = taskAssignmentRepository.sumAssignedMinutesForStaffOnDate(
                        targetStaff.getId(),
                        task.getTaskDate(),
                        task.getId()
                )
                .orElse(0);

        int estimatedMinutes = task.getEstimatedMinutes() == null ? 0 : task.getEstimatedMinutes();
        if (allocatedMinutes + estimatedMinutes > MAX_SHIFT_MINUTES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selected staff member does not have sufficient remaining capacity for this task."
            );
        }
    }

    private ReassignmentCandidateItemResponse toCandidate(CleaningTask task, StaffProfile staff) {
        int allocatedMinutes = taskAssignmentRepository.sumAssignedMinutesForStaffOnDate(
                        staff.getId(),
                        task.getTaskDate(),
                        task.getId()
                )
                .orElse(0);
        int estimatedMinutes = task.getEstimatedMinutes() == null ? 0 : task.getEstimatedMinutes();
        int remainingMinutes = Math.max(0, MAX_SHIFT_MINUTES - allocatedMinutes);

        return new ReassignmentCandidateItemResponse(
                staff.getId(),
                staff.getFullName(),
                staff.getPreferredShift() != null ? staff.getPreferredShift().getShiftName() : "Shift not set",
                allocatedMinutes,
                remainingMinutes,
                allocatedMinutes + estimatedMinutes <= MAX_SHIFT_MINUTES
        );
    }

    private User findActor(String actorUsername) {
        return userRepository.findByUsername(actorUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Actor user not found."));
    }

    private String resolveActorName(User actorUser) {
        return StringUtils.hasText(actorUser.getUsername()) ? actorUser.getUsername() : "Admin";
    }

    private Map<String, Object> buildAuditMetadata(
            CleaningTask task,
            StaffProfile previousStaff,
            StaffProfile targetStaff,
            Shift shift
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("actorSubtitle", "Admin");
        metadata.put("taskId", task.getId());
        metadata.put("roomId", task.getRoom().getId());
        metadata.put("roomNumber", task.getRoom().getRoomNumber());
        metadata.put("previousStaffId", previousStaff.getId());
        metadata.put("previousStaffName", previousStaff.getFullName());
        metadata.put("newStaffId", targetStaff.getId());
        metadata.put("newStaffName", targetStaff.getFullName());
        metadata.put("shiftId", shift.getId());
        metadata.put("shiftLabel", shift.getShiftName());
        metadata.put("reassignmentSource", AssignmentSource.MANUAL.name());
        metadata.put("targetLabel", "Room " + task.getRoom().getRoomNumber());
        metadata.put("targetSubLabel", "Task ID: " + task.getId().toString().substring(0, 8));
        return metadata;
    }
}
