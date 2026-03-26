package com.ibe.housekeeping.allocation.dto;

import com.ibe.housekeeping.common.enums.TaskType;
import java.util.UUID;

public record UnassignedTaskItemResponse(
        UUID taskId,
        int roomNumber,
        TaskType taskType,
        int estimatedMinutes,
        String reason
) {
}
