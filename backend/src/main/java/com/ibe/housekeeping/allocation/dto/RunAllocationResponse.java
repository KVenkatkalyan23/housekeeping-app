package com.ibe.housekeeping.allocation.dto;

import java.util.List;

public record RunAllocationResponse(
        AllocationResultSummaryResponse summary,
        List<TaskAssignmentItemResponse> assignments,
        List<UnassignedTaskItemResponse> unassigned
) {
}
