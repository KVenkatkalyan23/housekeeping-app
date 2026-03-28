export type AdminTaskTypeFilter = 'ALL' | 'DEEP_CLEAN' | 'DAILY_CLEAN' | 'VACANT_CLEAN'

export type AdminTaskStatusFilter =
  | 'ALL'
  | 'PENDING'
  | 'ASSIGNED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'

export type AdminTaskType = Exclude<AdminTaskTypeFilter, 'ALL'>
export type AdminTaskStatus = Exclude<AdminTaskStatusFilter, 'ALL'>

export interface AdminTaskAllocationSummary {
  taskDate: string
  totalActiveTasks: number
  inProgressCount: number
  checkoutTaskCount: number
  checkoutAssignedCount: number
  checkoutPendingCount: number
  dailyTaskCount: number
  dailyAssignedCount: number
  dailyPendingCount: number
  vacantTaskCount: number
  vacantAssignedCount: number
  vacantPendingCount: number
}

export interface AdminAllocatedTaskItem {
  taskId: string
  roomId: string
  roomNumber: string
  floorLabel: string | null
  roomTypeLabel: string | null
  taskType: AdminTaskType
  assignedStaffId: string | null
  assignedStaffName: string | null
  status: AdminTaskStatus
  priorityLabel: string
  estimatedMinutes: number | null
  shiftName: string | null
}

export interface AdminAllocatedTaskListResponse {
  items: AdminAllocatedTaskItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
