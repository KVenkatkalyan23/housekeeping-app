package com.ibe.housekeeping.leave.dto;

import com.ibe.housekeeping.common.enums.LeaveStatus;
import com.ibe.housekeeping.common.enums.LeaveType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeaveListItemResponse(
        UUID leaveId,
        UUID userId,
        String username,
        UUID staffId,
        String staffName,
        LocalDate fromDate,
        LocalDate toDate,
        LeaveType leaveType,
        LeaveStatus status,
        long durationDays,
        String reason,
        LocalDateTime requestedAt
) {
}
