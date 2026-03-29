package com.ibe.housekeeping.admin.taskreassignment.dto;

import com.ibe.housekeeping.common.enums.AssignmentSource;
import com.ibe.housekeeping.common.enums.TaskStatus;
import java.util.UUID;

public record ManualTaskReassignmentResponse(
        UUID taskId,
        UUID roomId,
        String roomNumber,
        UUID previousStaffId,
        String previousStaffName,
        UUID newStaffId,
        String newStaffName,
        UUID shiftId,
        String shiftName,
        TaskStatus taskStatus,
        AssignmentSource assignmentSource,
        String successMessage
) {
}
