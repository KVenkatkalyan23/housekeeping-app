package com.ibe.housekeeping.admin.staff.dto;

import java.util.List;

public record AdminStaffDirectoryResponse(
        List<AdminStaffDirectoryItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        StaffDirectorySummaryResponse summary
) {
}
