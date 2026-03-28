package com.ibe.housekeeping.admin.dashboard.dto;

public record AdminDashboardResponse(
        ShortfallAlert shortfallAlert,
        InventoryStatus inventoryStatus,
        WorkforceEfficiency workforceEfficiency,
        SlaPerformance slaPerformance,
        CapacityWorkload capacityVsWorkload,
        CurrentAdmin currentAdmin
) {

    public record ShortfallAlert(
            int shortfallPercent,
            String shortfallMessage,
            int additionalStaffRequired,
            boolean isCritical
    ) {
    }

    public record InventoryStatus(
            long totalRooms,
            long occupiedRooms,
            long vacantRooms,
            int occupancyRate
    ) {
    }

    public record WorkforceEfficiency(
            int utilizationPercent,
            String description
    ) {
    }

    public record SlaPerformance(
            int completionRate,
            double deltaVsYesterday,
            long completedTasks,
            long totalAssignedTasks
    ) {
    }

    public record CapacityWorkload(
            double availableTotalHours,
            double requiredTotalHours
    ) {
    }

    public record CurrentAdmin(
            String displayName,
            String roleLabel,
            String avatarUrl
    ) {
    }
}
