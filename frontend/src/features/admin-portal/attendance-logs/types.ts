export interface AdminAttendanceSummaryResponse {
  fromDate: string
  toDate: string
  activeTodayCount: number
  activeTodayDeltaPercent: number
  averageShiftLengthHours: number
  averageShiftLengthLabel: string
  lateCheckInsCount: number
  lateCheckInsLabel: string
}

export interface AdminAttendanceLogItem {
  attendanceId: string
  staffId: string
  staffName: string
  staffRoleLabel: string
  staffInitials: string
  workDate: string
  clockInTime: string
  clockOutTime: string
  totalWorkedHours: number
  shiftReference: string
  lateCheckIn: boolean
  lateCheckInLabel: string
  overtimeFlag: boolean
  statusTag: string
}

export interface AdminAttendanceLogListResponse {
  items: AdminAttendanceLogItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
