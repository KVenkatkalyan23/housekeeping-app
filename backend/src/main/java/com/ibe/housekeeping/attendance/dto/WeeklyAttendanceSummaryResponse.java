package com.ibe.housekeeping.attendance.dto;

public record WeeklyAttendanceSummaryResponse(
        Integer totalWorkedMinutes,
        Integer overtimeMinutes,
        Integer totalBreakMinutes
) {
}
