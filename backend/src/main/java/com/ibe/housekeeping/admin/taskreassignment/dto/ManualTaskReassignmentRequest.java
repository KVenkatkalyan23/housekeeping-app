package com.ibe.housekeeping.admin.taskreassignment.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ManualTaskReassignmentRequest(
        @NotNull(message = "taskId is required.")
        UUID taskId,
        @NotNull(message = "targetStaffId is required.")
        UUID targetStaffId
) {
}
