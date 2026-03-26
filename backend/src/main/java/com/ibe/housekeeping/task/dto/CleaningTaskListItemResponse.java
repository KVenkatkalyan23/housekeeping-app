package com.ibe.housekeeping.task.dto;

import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.common.enums.TaskType;
import java.time.LocalDate;
import java.util.UUID;

public record CleaningTaskListItemResponse(
        UUID taskId,
        UUID roomId,
        Integer roomNumber,
        LocalDate taskDate,
        TaskType taskType,
        Integer estimatedMinutes,
        Integer priorityOrder,
        TaskStatus taskStatus,
        UUID shiftId,
        UUID sourceStayId
) {
}
