package com.ibe.housekeeping.leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplyLeaveRequest(
        @NotBlank(message = "userId is required")
        String userId,
        @NotBlank(message = "fromDate is required")
        String fromDate,
        @NotBlank(message = "toDate is required")
        String toDate,
        @NotBlank(message = "leaveType is required")
        String leaveType,
        @Size(max = 500, message = "reason must be at most 500 characters")
        String reason
) {
}
