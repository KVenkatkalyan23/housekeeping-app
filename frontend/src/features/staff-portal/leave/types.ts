export type LeaveType = 'PLANNED' | 'SICK'
export type LeaveStatus = 'APPROVED'

export interface ApplyLeaveRequest {
  userId: string
  fromDate: string
  toDate: string
  leaveType: LeaveType
  reason?: string
}

export interface LeaveListItem {
  leaveId: string
  userId: string
  username: string
  staffId: string
  staffName: string
  fromDate: string
  toDate: string
  leaveType: LeaveType
  status: LeaveStatus
  durationDays: number
  reason: string | null
  requestedAt: string | null
}

export interface LeavePagination {
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasPrevious: boolean
  hasNext: boolean
}

export interface MyLeavesResponse {
  items: LeaveListItem[]
  pagination: LeavePagination
}

export interface AdminLeavesResponse {
  items: LeaveListItem[]
  pagination: LeavePagination
}

export interface LeaveFormErrors {
  fromDate?: string
  toDate?: string
  leaveType?: string
  reason?: string
  form?: string
}
