package com.ibe.housekeeping.admin.taskallocation.dto;

import java.util.List;

public record AdminAllocatedTaskListResponse(
        List<AdminAllocatedTaskItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
