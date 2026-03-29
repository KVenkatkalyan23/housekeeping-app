package com.ibe.housekeeping.admin.attendance.dto;

import java.util.List;

public record AdminAttendanceLogListResponse(
        List<AdminAttendanceLogItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
