package com.ibe.housekeeping.leave.dto;

public record LeavePaginationResponse(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasPrevious,
        boolean hasNext
) {
}
