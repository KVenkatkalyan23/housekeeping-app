export interface AdminDashboardShortfallAlert {
  shortfallPercent: number
  shortfallMessage: string
  additionalStaffRequired: number
  isCritical: boolean
}

export interface AdminDashboardInventoryStatus {
  totalRooms: number
  occupiedRooms: number
  vacantRooms: number
  occupancyRate: number
}

export interface AdminDashboardWorkforceEfficiency {
  utilizationPercent: number
  description: string
}

export interface AdminDashboardSlaPerformance {
  completionRate: number
  deltaVsYesterday: number
  completedTasks: number
  totalAssignedTasks: number
}

export interface AdminDashboardCapacityWorkloadPoint {
  label: string
  availableHours: number
  requiredHours: number
  peak: boolean
}

export interface AdminDashboardCapacityWorkload {
  availableTotalHours: number
  requiredTotalHours: number
  timeSeries: AdminDashboardCapacityWorkloadPoint[]
}

export interface AdminDashboardResourceDiscrepancy {
  requiredHours: number
  availableHours: number
  deltaHours: number
  impactMessage: string
}

export interface AdminDashboardCurrentAdmin {
  displayName: string
  roleLabel: string
  avatarUrl: string | null
}

export interface AdminDashboardData {
  shortfallAlert: AdminDashboardShortfallAlert
  inventoryStatus: AdminDashboardInventoryStatus
  workforceEfficiency: AdminDashboardWorkforceEfficiency
  slaPerformance: AdminDashboardSlaPerformance
  capacityVsWorkload: AdminDashboardCapacityWorkload
  resourceDiscrepancy: AdminDashboardResourceDiscrepancy
  currentAdmin: AdminDashboardCurrentAdmin
}
