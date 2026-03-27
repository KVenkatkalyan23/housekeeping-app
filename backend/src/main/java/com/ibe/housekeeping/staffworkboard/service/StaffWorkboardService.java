package com.ibe.housekeeping.staffworkboard.service;

import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.entity.CleaningTask;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import com.ibe.housekeeping.staffworkboard.dto.MyAssignedTaskItemResponse;
import com.ibe.housekeeping.staffworkboard.dto.MyAssignedTasksResponse;
import com.ibe.housekeeping.staffworkboard.dto.WorkloadSummaryResponse;
import com.ibe.housekeeping.task.repository.CleaningTaskRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StaffWorkboardService {

    private final CleaningTaskRepository cleaningTaskRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final UserRepository userRepository;

    public StaffWorkboardService(
            CleaningTaskRepository cleaningTaskRepository,
            StaffProfileRepository staffProfileRepository,
            UserRepository userRepository
    ) {
        this.cleaningTaskRepository = cleaningTaskRepository;
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
