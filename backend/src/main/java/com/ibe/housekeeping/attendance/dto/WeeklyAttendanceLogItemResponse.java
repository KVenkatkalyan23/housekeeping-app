package com.ibe.housekeeping.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WeeklyAttendanceLogItemResponse(
        LocalDate date,
        String dayLabel,
        LocalDateTime clockInTime,
        LocalDateTime clockOutTime,
        Integer workedMinutes,
        String statusLabel,
        String statusType
) {
}
