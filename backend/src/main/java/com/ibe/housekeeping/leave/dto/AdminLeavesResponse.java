package com.ibe.housekeeping.leave.dto;

import java.util.List;

public record AdminLeavesResponse(
        List<LeaveListItemResponse> items,
        LeavePaginationResponse pagination
) {
}
