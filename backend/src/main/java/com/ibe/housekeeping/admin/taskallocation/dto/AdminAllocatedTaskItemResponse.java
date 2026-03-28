package com.ibe.housekeeping.admin.taskallocation.dto;

import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.common.enums.TaskType;
import java.util.UUID;

public record AdminAllocatedTaskItemResponse(
        UUID taskId,
        UUID roomId,
        String roomNumber,
        String floorLabel,
        String roomTypeLabel,
        TaskType taskType,
        UUID assignedStaffId,
        String assignedStaffName,
        TaskStatus status,
        String priorityLabel,
        Integer estimatedMinutes,
        String shiftName
) {
}
