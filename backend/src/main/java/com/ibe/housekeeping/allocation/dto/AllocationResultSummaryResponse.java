package com.ibe.housekeeping.allocation.dto;

import java.time.LocalDate;

public record AllocationResultSummaryResponse(
        LocalDate taskDate,
        int totalTasks,
        int assignedTasks,
        int unassignedTasks
) {
}
