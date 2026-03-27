package com.ibe.housekeeping.staffworkboard.service;

import com.ibe.housekeeping.allocation.repository.TaskAssignmentRepository;
import com.ibe.housekeeping.auth.repository.UserRepository;
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
import com.ibe.housekeeping.task.repository.CleaningTaskRepository;
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

    private final CleaningTaskRepository cleaningTaskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final UserRepository userRepository;

    public StaffWorkboardService(
            CleaningTaskRepository cleaningTaskRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            StaffProfileRepository staffProfileRepository,
            UserRepository userRepository
    ) {
        this.cleaningTaskRepository = cleaningTaskRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public MyAssignedTasksResponse getMyAssignedTasks(String username, LocalDate taskDate) {
        StaffProfile staffProfile = loadStaffProfile(username);
        List<CleaningTask> tasks = cleaningTaskRepository.findAllAssignedToStaffByTaskDate(staffProfile.getId(), taskDate);

        return new MyAssignedTasksResponse(
                taskDate,
                tasks.stream().map(this::toTaskItemResponse).toList()
        );
    }

    @Transactional(readOnly = true)
    public WorkloadSummaryResponse getMyWorkload(String username, LocalDate taskDate) {
        StaffProfile staffProfile = loadStaffProfile(username);
        List<CleaningTask> tasks = cleaningTaskRepository.findAllAssignedToStaffByTaskDate(staffProfile.getId(), taskDate);

        int assignedMinutes = tasks.stream()
                .map(CleaningTask::getEstimatedMinutes)
                .filter(minutes -> minutes != null && minutes > 0)
                .mapToInt(Integer::intValue)
                .sum();

        int completedMinutes = tasks.stream()
                .filter(task -> task.getTaskStatus() == TaskStatus.COMPLETED)
                .map(CleaningTask::getEstimatedMinutes)
                .filter(minutes -> minutes != null && minutes > 0)
                .mapToInt(Integer::intValue)
                .sum();

        int totalTaskCount = tasks.size();
        int completedTaskCount = (int) tasks.stream()
                .filter(task -> task.getTaskStatus() == TaskStatus.COMPLETED)
                .count();
        int pendingTaskCount = totalTaskCount - completedTaskCount;
        int pendingMinutes = assignedMinutes - completedMinutes;
        int completionPercentage = totalTaskCount == 0
                ? 0
                : Math.toIntExact(Math.round((completedTaskCount * 100.0) / totalTaskCount));

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
        StaffProfile staffProfile = loadStaffProfile(username);
        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found."));

        TaskAssignment assignment = taskAssignmentRepository.findByCleaningTaskIdAndStaffId(taskId, staffProfile.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only complete tasks assigned to you."
                ));

        TaskStatus taskStatus = task.getTaskStatus();
        if (taskStatus == TaskStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task is already completed.");
        }
        if (taskStatus == TaskStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancelled tasks cannot be completed.");
        }
        if (taskStatus != TaskStatus.ASSIGNED && taskStatus != TaskStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only assigned or in-progress tasks can be completed.");
        }

        LocalDateTime completedAt = LocalDateTime.now();
        task.setTaskStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(completedAt);
        cleaningTaskRepository.save(task);

        return new MarkTaskCompleteResponse(
                assignment.getCleaningTask().getId(),
                task.getTaskStatus(),
                task.getCompletedAt(),
                "Task marked as completed."
        );
    }

    private StaffProfile loadStaffProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));

        return staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff profile not found for this user."));
    }

    private MyAssignedTaskItemResponse toTaskItemResponse(CleaningTask task) {
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
