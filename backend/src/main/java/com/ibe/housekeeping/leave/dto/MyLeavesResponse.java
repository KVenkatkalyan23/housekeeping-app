package com.ibe.housekeeping.leave.dto;

import java.util.List;

public record MyLeavesResponse(
        List<LeaveListItemResponse> items,
        LeavePaginationResponse pagination
) {
}
