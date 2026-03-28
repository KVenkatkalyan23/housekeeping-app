package com.ibe.housekeeping.staffworkboard.service;

import com.ibe.housekeeping.activitylog.service.ActivityLogService;
import com.ibe.housekeeping.allocation.repository.TaskAssignmentRepository;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.entity.CleaningTask;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.TaskAssignment;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import com.ibe.housekeeping.staffworkboard.dto.MarkTaskCompleteResponse;
import com.ibe.housekeeping.staffworkboard.dto.MyAssignedTaskItemResponse;
import com.ibe.housekeeping.staffworkboard.dto.MyAssignedTasksResponse;
import com.ibe.housekeeping.staffworkboard.dto.WorkloadSummaryResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StaffWorkboardService {

    private final TaskAssignmentRepository taskAssignmentRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public StaffWorkboardService(
            TaskAssignmentRepository taskAssignmentRepository,
            StaffProfileRepository staffProfileRepository,
            UserRepository userRepository,
            ActivityLogService activityLogService
    ) {
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public MyAssignedTasksResponse getMyAssignedTasks(String username, LocalDate taskDate) {
        StaffProfile staffProfile = loadStaffProfile(username);
        List<TaskAssignment> assignments = taskAssignmentRepository.findAllByStaffIdAndTaskDate(
                staffProfile.getId(),
                taskDate
        );

        return new MyAssignedTasksResponse(
                taskDate,
                assignments.stream()
                        .map(TaskAssignment::getCleaningTask)
                        .map(this::toAssignedTaskItemResponse)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public WorkloadSummaryResponse getMyWorkload(String username, LocalDate taskDate) {
        StaffProfile staffProfile = loadStaffProfile(username);
        List<CleaningTask> tasks = taskAssignmentRepository.findAllByStaffIdAndTaskDate(
                        staffProfile.getId(),
                        taskDate
                ).stream()
                .map(TaskAssignment::getCleaningTask)
                .toList();

        int assignedMinutes = tasks.stream()
                .mapToInt(CleaningTask::getEstimatedMinutes)
                .sum();
        int completedMinutes = tasks.stream()
                .filter(task -> task.getTaskStatus() == TaskStatus.COMPLETED)
                .mapToInt(CleaningTask::getEstimatedMinutes)
                .sum();
        int totalTaskCount = tasks.size();
        int completedTaskCount = (int) tasks.stream()
                .filter(task -> task.getTaskStatus() == TaskStatus.COMPLETED)
                .count();
        int pendingMinutes = assignedMinutes - completedMinutes;
        int pendingTaskCount = totalTaskCount - completedTaskCount;
        int completionPercentage = totalTaskCount == 0
                ? 0
                : (completedTaskCount * 100) / totalTaskCount;

        return new WorkloadSummaryResponse(
                assignedMinutes,
                completedMinutes,
                pendingMinutes,
                totalTaskCount,
                completedTaskCount,
                pendingTaskCount,
                completionPercentage
        );
    }

    @Transactional
    public MarkTaskCompleteResponse markTaskComplete(String username, UUID taskId) {
        User user = loadUser(username);
        StaffProfile staffProfile = loadStaffProfile(user);

        if (staffProfile.getAvailabilityStatus() != AvailabilityStatus.ON_DUTY) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You must be on duty to complete tasks."
            );
        }

        TaskAssignment assignment = taskAssignmentRepository.findByCleaningTaskIdAndStaffId(taskId, staffProfile.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only complete tasks assigned to you."
                ));

        CleaningTask task = assignment.getCleaningTask();

        if (task.getTaskStatus() == TaskStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task is already completed.");
        }

        if (task.getTaskStatus() == TaskStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancelled tasks cannot be completed.");
        }

        if (task.getTaskStatus() != TaskStatus.ASSIGNED && task.getTaskStatus() != TaskStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only assigned or in-progress tasks can be completed."
            );
        }

        LocalDateTime completedAt = LocalDateTime.now();
        task.setTaskStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(completedAt);
        activityLogService.logTaskCompleted(task, staffProfile, user);

        return new MarkTaskCompleteResponse(
                task.getId(),
                task.getTaskStatus(),
                task.getCompletedAt(),
                "Task marked complete."
        );
    }

    private StaffProfile loadStaffProfile(String username) {
        return loadStaffProfile(loadUser(username));
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }

    private StaffProfile loadStaffProfile(User user) {
        return staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Staff profile not found for this user."
                ));
    }

    private MyAssignedTaskItemResponse toAssignedTaskItemResponse(CleaningTask task) {
        Shift shift = task.getShift();

        return new MyAssignedTaskItemResponse(
                task.getId(),
                task.getRoom().getId(),
                task.getRoom().getRoomNumber(),
                task.getTaskType(),
                task.getTaskStatus(),
                task.getEstimatedMinutes(),
                task.getPriorityOrder(),
                shift != null ? shift.getId() : null,
                shift != null ? shift.getShiftCode() : null,
                shift != null ? shift.getShiftName() : null,
                task.getCompletedAt(),
                task.getSourceStay() != null ? task.getSourceStay().getId() : null
        );
    }
}



