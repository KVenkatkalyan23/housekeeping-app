package com.ibe.housekeeping.admin.taskallocation.dto;

import java.time.LocalDate;

public record AdminTaskAllocationSummaryResponse(
        LocalDate taskDate,
        long totalActiveTasks,
        long inProgressCount,
        long checkoutTaskCount,
        long checkoutAssignedCount,
        long checkoutPendingCount,
        long dailyTaskCount,
        long dailyAssignedCount,
        long dailyPendingCount,
        long vacantTaskCount,
        long vacantAssignedCount,
        long vacantPendingCount
) {
}
