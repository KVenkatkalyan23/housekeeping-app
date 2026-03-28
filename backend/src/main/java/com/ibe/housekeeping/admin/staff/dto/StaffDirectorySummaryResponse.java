package com.ibe.housekeeping.admin.staff.dto;

public record StaffDirectorySummaryResponse(
        long totalEmployees,
        long onDutyCount,
        long offDutyCount,
        long leaveCount,
        long sickCount
) {
}
