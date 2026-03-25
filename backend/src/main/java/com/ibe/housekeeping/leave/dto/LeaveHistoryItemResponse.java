package com.ibe.housekeeping.leave.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeaveHistoryItemResponse(
        UUID leaveRequestId,
        String leaveType,
        LocalDate leaveStartDate,
        LocalDate leaveEndDate,
        String status,
        long durationDays,
        String reason,
        LocalDateTime requestedAt
) {
}
