package com.ibe.housekeeping.admin.attendance.dto;

import java.time.LocalDate;

public record AdminAttendanceSummaryResponse(
        LocalDate fromDate,
        LocalDate toDate,
        long activeTodayCount,
        double activeTodayDeltaPercent,
        double averageShiftLengthHours,
        String averageShiftLengthLabel,
        long lateCheckInsCount,
        String lateCheckInsLabel
) {
}
