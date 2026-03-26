package com.ibe.housekeeping.allocation.dto;

import com.ibe.housekeeping.common.enums.TaskType;
import java.util.UUID;

public record TaskAssignmentItemResponse(
        UUID taskId,
        int roomNumber,
        TaskType taskType,
        int estimatedMinutes,
        UUID staffId,
        String staffName,
        UUID shiftId,
        String shiftCode,
        String shiftName
) {
}
