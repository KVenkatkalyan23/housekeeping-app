export type AdminStaffDirectoryFilterStatus =
  | 'ALL'
  | 'ON_DUTY'
  | 'OFF_DUTY'
  | 'LEAVE'
  | 'SICK'

export type AdminStaffDirectoryItemStatus =
  Exclude<AdminStaffDirectoryFilterStatus, 'ALL'>

export interface StaffDirectorySummary {
  totalEmployees: number
  onDutyCount: number
  offDutyCount: number
  leaveCount: number
  sickCount: number
}

export interface AdminStaffDirectoryItem {
  staffId: string
  staffCode: string
  fullName: string
  email: string | null
  phone: string | null
  status: AdminStaffDirectoryItemStatus
  userId: string | null
  avatarUrl: string | null
}

export interface AdminStaffDirectoryResponse {
  items: AdminStaffDirectoryItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  summary: StaffDirectorySummary
}
