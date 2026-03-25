package com.ibe.housekeeping.attendance.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyAttendanceHistoryResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        WeeklyAttendanceSummaryResponse summary,
        List<WeeklyAttendanceLogItemResponse> logs,
        WeeklyAttendancePaginationResponse pagination
) {
}
