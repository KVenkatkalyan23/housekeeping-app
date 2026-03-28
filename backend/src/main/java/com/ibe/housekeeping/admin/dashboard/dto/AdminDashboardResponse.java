package com.ibe.housekeeping.admin.dashboard.dto;

import java.util.List;

public record AdminDashboardResponse(
        ShortfallAlert shortfallAlert,
        InventoryStatus inventoryStatus,
        WorkforceEfficiency workforceEfficiency,
        SlaPerformance slaPerformance,
        CapacityWorkload capacityVsWorkload,
        ResourceDiscrepancy resourceDiscrepancy,
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
            double requiredTotalHours,
            List<CapacityWorkloadPoint> timeSeries
    ) {
    }

    public record CapacityWorkloadPoint(
            String label,
            double availableHours,
            double requiredHours,
            boolean peak
    ) {
    }

    public record ResourceDiscrepancy(
            double requiredHours,
            double availableHours,
            double deltaHours,
            String impactMessage
    ) {
    }

    public record CurrentAdmin(
            String displayName,
            String roleLabel,
            String avatarUrl
    ) {
    }
}
