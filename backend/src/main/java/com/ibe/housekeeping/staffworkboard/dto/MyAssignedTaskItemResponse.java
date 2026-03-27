package com.ibe.housekeeping.staffworkboard.dto;

import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.common.enums.TaskType;
import java.time.LocalDateTime;
import java.util.UUID;

public record MyAssignedTaskItemResponse(
        UUID taskId,
        UUID roomId,
        Integer roomNumber,
        TaskType taskType,
        TaskStatus taskStatus,
        Integer estimatedMinutes,
        Integer priorityOrder,
        UUID shiftId,
        String shiftCode,
        String shiftName,
        LocalDateTime completedAt,
        UUID sourceStayId
) {
}
