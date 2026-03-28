package com.ibe.housekeeping.admin.logs.dto;

import java.util.List;

public record AdminAuditLogsResponse(
        List<AdminAuditLogItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
