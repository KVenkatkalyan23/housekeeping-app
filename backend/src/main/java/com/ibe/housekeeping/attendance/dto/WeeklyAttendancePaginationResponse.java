package com.ibe.housekeeping.attendance.dto;

public record WeeklyAttendancePaginationResponse(
        int page,
        int size,
        boolean hasPrevious,
        boolean hasNext
) {
}
