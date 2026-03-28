package com.ibe.housekeeping.admin.taskallocation.service;

import com.ibe.housekeeping.admin.taskallocation.dto.AdminAllocatedTaskItemResponse;
import com.ibe.housekeeping.admin.taskallocation.dto.AdminAllocatedTaskListResponse;
import com.ibe.housekeeping.admin.taskallocation.dto.AdminTaskAllocationSummaryResponse;
import com.ibe.housekeeping.allocation.repository.TaskAssignmentRepository;
import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.common.enums.TaskType;
import com.ibe.housekeeping.entity.CleaningTask;
import com.ibe.housekeeping.entity.TaskAssignment;
import com.ibe.housekeeping.task.repository.CleaningTaskRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminTaskAllocationService {

    private static final Set<TaskStatus> INACTIVE_STATUSES = EnumSet.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED);

    private final CleaningTaskRepository cleaningTaskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final Clock clock;

    public AdminTaskAllocationService(
            CleaningTaskRepository cleaningTaskRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            Clock clock
    ) {
        this.cleaningTaskRepository = cleaningTaskRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AdminTaskAllocationSummaryResponse getAllocationSummary(LocalDate taskDate) {
        LocalDate selectedDate = resolveTaskDate(taskDate);
        List<CleaningTask> tasks = cleaningTaskRepository.findAllForAdminAllocationByTaskDate(selectedDate);
        Set<UUID> assignedTaskIds = findAssignedTaskIds(selectedDate);

        return new AdminTaskAllocationSummaryResponse(
                selectedDate,
                tasks.stream().filter(this::isActiveTask).count(),
                tasks.stream().filter(task -> task.getTaskStatus() == TaskStatus.IN_PROGRESS).count(),
                countActiveByType(tasks, TaskType.DEEP_CLEAN),
                countAssignedActiveByType(tasks, assignedTaskIds, TaskType.DEEP_CLEAN),
                countPendingActiveByType(tasks, assignedTaskIds, TaskType.DEEP_CLEAN),
                countActiveByType(tasks, TaskType.DAILY_CLEAN),
                countAssignedActiveByType(tasks, assignedTaskIds, TaskType.DAILY_CLEAN),
                countPendingActiveByType(tasks, assignedTaskIds, TaskType.DAILY_CLEAN),
                countActiveByType(tasks, TaskType.VACANT_CLEAN),
                countAssignedActiveByType(tasks, assignedTaskIds, TaskType.VACANT_CLEAN),
                countPendingActiveByType(tasks, assignedTaskIds, TaskType.VACANT_CLEAN)
        );
    }

    @Transactional(readOnly = true)
    public AdminAllocatedTaskListResponse getAllocatedTasks(
            LocalDate taskDate,
            int page,
            int size,
            String search,
            String taskType,
            String status
    ) {
        validatePagination(page, size);

        LocalDate selectedDate = resolveTaskDate(taskDate);
        TaskType requestedTaskType = parseTaskType(taskType);
        TaskStatus requestedStatus = parseTaskStatus(status);
        String normalizedSearch = normalizeSearch(search);

        List<CleaningTask> tasks = cleaningTaskRepository.findAllForAdminAllocationByTaskDate(selectedDate);
        Map<UUID, TaskAssignment> assignmentsByTaskId = findAssignmentsByTaskId(selectedDate);

        List<AdminAllocatedTaskItemResponse> filteredItems = tasks.stream()
                .map(task -> toItemResponse(task, assignmentsByTaskId.get(task.getId())))
                .filter(matchesTaskType(requestedTaskType))
                .filter(matchesStatus(requestedStatus))
                .filter(matchesSearch(normalizedSearch))
                .toList();

        int fromIndex = Math.min(page * size, filteredItems.size());
        int toIndex = Math.min(fromIndex + size, filteredItems.size());
        int totalPages = filteredItems.isEmpty() ? 0 : (int) Math.ceil(filteredItems.size() / (double) size);

        return new AdminAllocatedTaskListResponse(
                filteredItems.subList(fromIndex, toIndex),
                page,
                size,
                filteredItems.size(),
                totalPages
        );
    }

    private Set<UUID> findAssignedTaskIds(LocalDate taskDate) {
        return taskAssignmentRepository.findAllByTaskDate(taskDate).stream()
                .map(assignment -> assignment.getCleaningTask().getId())
                .collect(Collectors.toSet());
    }

    private Map<UUID, TaskAssignment> findAssignmentsByTaskId(LocalDate taskDate) {
        return taskAssignmentRepository.findAllByTaskDate(taskDate).stream()
                .collect(Collectors.toMap(
                        assignment -> assignment.getCleaningTask().getId(),
                        assignment -> assignment,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private long countActiveByType(List<CleaningTask> tasks, TaskType taskType) {
        return tasks.stream()
                .filter(this::isActiveTask)
                .filter(task -> task.getTaskType() == taskType)
                .count();
    }

    private long countAssignedActiveByType(List<CleaningTask> tasks, Set<UUID> assignedTaskIds, TaskType taskType) {
        return tasks.stream()
                .filter(this::isActiveTask)
                .filter(task -> task.getTaskType() == taskType)
                .filter(task -> assignedTaskIds.contains(task.getId()))
                .count();
    }

    private long countPendingActiveByType(List<CleaningTask> tasks, Set<UUID> assignedTaskIds, TaskType taskType) {
        return tasks.stream()
                .filter(this::isActiveTask)
                .filter(task -> task.getTaskType() == taskType)
                .filter(task -> !assignedTaskIds.contains(task.getId()))
                .count();
    }

    private boolean isActiveTask(CleaningTask task) {
        return !INACTIVE_STATUSES.contains(task.getTaskStatus());
    }

    private Predicate<AdminAllocatedTaskItemResponse> matchesTaskType(TaskType taskType) {
        return item -> taskType == null || item.taskType() == taskType;
    }

    private Predicate<AdminAllocatedTaskItemResponse> matchesStatus(TaskStatus status) {
        return item -> status == null || item.status() == status;
    }

    private Predicate<AdminAllocatedTaskItemResponse> matchesSearch(String search) {
        if (!StringUtils.hasText(search)) {
            return item -> true;
        }

        String normalizedSearch = search.toLowerCase(Locale.ENGLISH);
        return item -> StringUtils.hasText(item.assignedStaffName())
                && item.assignedStaffName().toLowerCase(Locale.ENGLISH).contains(normalizedSearch);
    }

    private AdminAllocatedTaskItemResponse toItemResponse(CleaningTask task, TaskAssignment assignment) {
        return new AdminAllocatedTaskItemResponse(
                task.getId(),
                task.getRoom().getId(),
                String.valueOf(task.getRoom().getRoomNumber()),
                null,
                null,
                task.getTaskType(),
                assignment != null && assignment.getStaff() != null ? assignment.getStaff().getId() : null,
                assignment != null && assignment.getStaff() != null ? assignment.getStaff().getFullName() : null,
                task.getTaskStatus(),
                toPriorityLabel(task.getPriorityOrder()),
                task.getEstimatedMinutes(),
                task.getShift() != null ? task.getShift().getShiftName() : null
        );
    }

    private String toPriorityLabel(Integer priorityOrder) {
        if (priorityOrder == null || priorityOrder <= 1) {
            return "HIGH";
        }

        if (priorityOrder == 2) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private LocalDate resolveTaskDate(LocalDate taskDate) {
        return taskDate != null ? taskDate : LocalDate.now(clock);
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be 0 or greater.");
        }

        if (size < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be at least 1.");
        }
    }

    private TaskType parseTaskType(String taskType) {
        if (!StringUtils.hasText(taskType)) {
            return null;
        }

        try {
            return TaskType.valueOf(taskType.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "taskType must be one of DEEP_CLEAN, DAILY_CLEAN, or VACANT_CLEAN."
            );
        }
    }

    private TaskStatus parseTaskStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }

        try {
            return TaskStatus.valueOf(status.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "status must be one of PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, or CANCELLED."
            );
        }
    }

    private String normalizeSearch(String search) {
        return StringUtils.hasText(search) ? search.trim() : null;
    }
}
