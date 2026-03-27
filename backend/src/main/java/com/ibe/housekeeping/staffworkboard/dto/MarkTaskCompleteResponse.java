package com.ibe.housekeeping.staffworkboard.dto;

import com.ibe.housekeeping.common.enums.TaskStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record MarkTaskCompleteResponse(
        UUID taskId,
        TaskStatus taskStatus,
        LocalDateTime completedAt,
        String message
) {
}
