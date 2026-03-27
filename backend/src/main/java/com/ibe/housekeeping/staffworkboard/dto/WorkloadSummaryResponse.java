package com.ibe.housekeeping.staffworkboard.dto;

public record WorkloadSummaryResponse(
        int assignedMinutes,
        int completedMinutes,
        int pendingMinutes,
        int totalTaskCount,
        int completedTaskCount,
        int pendingTaskCount,
        int completionPercentage
) {
}
