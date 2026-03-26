package com.ibe.housekeeping.task.service;

import com.ibe.housekeeping.common.enums.RoomStatus;
import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.common.enums.TaskType;
import com.ibe.housekeeping.entity.CleaningTask;
import com.ibe.housekeeping.entity.Room;
import com.ibe.housekeeping.entity.RoomStay;
import com.ibe.housekeeping.room.repository.RoomRepository;
import com.ibe.housekeeping.roomstay.repository.RoomStayRepository;
import com.ibe.housekeeping.task.dto.CleaningTaskListItemResponse;
import com.ibe.housekeeping.task.dto.GenerateTasksResponse;
import com.ibe.housekeeping.task.repository.CleaningTaskRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CleaningTaskService {

    private static final int DEEP_CLEAN_ESTIMATED_MINUTES = 120;
    private static final int DAILY_CLEAN_ESTIMATED_MINUTES = 30;
    private static final int VACANT_CLEAN_ESTIMATED_MINUTES = 15;

    private final CleaningTaskRepository cleaningTaskRepository;
    private final RoomRepository roomRepository;
    private final RoomStayRepository roomStayRepository;

    public CleaningTaskService(
            CleaningTaskRepository cleaningTaskRepository,
            RoomRepository roomRepository,
            RoomStayRepository roomStayRepository
    ) {
        this.cleaningTaskRepository = cleaningTaskRepository;
        this.roomRepository = roomRepository;
        this.roomStayRepository = roomStayRepository;
    }

    @Transactional
    public GenerateTasksResponse generateTasks(LocalDate taskDate) {
        List<Room> rooms = roomRepository.findAllByRoomStatusOrderByRoomNumberAsc(RoomStatus.ACTIVE);
        int generatedCount = 0;
        int skippedCount = 0;

        for (Room room : rooms) {
            if (cleaningTaskRepository.existsByRoomIdAndTaskDate(room.getId(), taskDate)) {
                skippedCount++;
                continue;
            }

            RoomStay stay = roomStayRepository
                    .findTopByRoomIdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualOrderByCheckOutDateDescCreatedAtDesc(
                            room.getId(),
                            taskDate,
                            taskDate
                    )
                    .orElse(null);

            TaskType taskType = deriveTaskType(stay, taskDate);
            CleaningTask task = CleaningTask.builder()
                    .room(room)
                    .taskDate(taskDate)
                    .shift(null)
                    .taskType(taskType)
                    .priorityOrder(calculatePriority(taskType))
                    .estimatedMinutes(calculateEstimatedMinutes(taskType))
                    .taskStatus(TaskStatus.PENDING)
                    .sourceStay(stay)
                    .build();

            try {
                cleaningTaskRepository.save(task);
                generatedCount++;
            } catch (DataIntegrityViolationException ex) {
                skippedCount++;
            }
        }

        return new GenerateTasksResponse(generatedCount, skippedCount);
    }

    @Transactional(readOnly = true)
    public List<CleaningTaskListItemResponse> getTasksByDate(LocalDate taskDate, TaskStatus taskStatus) {
        List<CleaningTask> tasks = taskStatus == null
                ? cleaningTaskRepository.findAllByTaskDateOrderByPriorityOrderAscRoomRoomNumberAsc(taskDate)
                : cleaningTaskRepository.findAllByTaskDateAndTaskStatusOrderByPriorityOrderAscRoomRoomNumberAsc(
                        taskDate,
                        taskStatus
                );

        return tasks.stream()
                .map(this::toListItemResponse)
                .toList();
    }

    TaskType deriveTaskType(RoomStay stay, LocalDate taskDate) {
        if (stay == null) {
            return TaskType.VACANT_CLEAN;
        }

        if (stay.getCheckOutDate().isEqual(taskDate)) {
            return TaskType.DEEP_CLEAN;
        }

        if (stay.getCheckOutDate().isAfter(taskDate)) {
            return TaskType.DAILY_CLEAN;
        }

        return TaskType.VACANT_CLEAN;
    }

    int calculateEstimatedMinutes(TaskType taskType) {
        return switch (taskType) {
            case DEEP_CLEAN -> DEEP_CLEAN_ESTIMATED_MINUTES;
            case DAILY_CLEAN -> DAILY_CLEAN_ESTIMATED_MINUTES;
            case VACANT_CLEAN -> VACANT_CLEAN_ESTIMATED_MINUTES;
        };
    }

    int calculatePriority(TaskType taskType) {
        return switch (taskType) {
            case DEEP_CLEAN -> 1;
            case DAILY_CLEAN -> 2;
            case VACANT_CLEAN -> 3;
        };
    }

    private CleaningTaskListItemResponse toListItemResponse(CleaningTask task) {
        return new CleaningTaskListItemResponse(
                task.getId(),
                task.getRoom().getId(),
                task.getRoom().getRoomNumber(),
                task.getTaskDate(),
                task.getTaskType(),
                task.getEstimatedMinutes(),
                task.getPriorityOrder(),
                task.getTaskStatus(),
                task.getShift() != null ? task.getShift().getId() : null,
                task.getSourceStay() != null ? task.getSourceStay().getId() : null
        );
    }
}
