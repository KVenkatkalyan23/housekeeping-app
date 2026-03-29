package com.ibe.housekeeping.admin.attendance.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AdminAttendanceLogItemResponse(
        UUID attendanceId,
        UUID staffId,
        String staffName,
        String staffRoleLabel,
        String staffInitials,
        LocalDate workDate,
        String clockInTime,
        String clockOutTime,
        double totalWorkedHours,
        String shiftReference,
        boolean lateCheckIn,
        String lateCheckInLabel,
        boolean overtimeFlag,
        String statusTag
) {
}
