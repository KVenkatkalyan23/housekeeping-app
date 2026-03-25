export type AvailabilityStatus = 'ON_DUTY' | 'OFF_DUTY'

export interface AttendanceStatusResponse {
  onDuty: boolean
  attendanceId: string | null
  staffId: string
  shiftId: string | null
  shiftCode: string | null
  shiftName: string | null
  shiftStartTime: string | null
  shiftEndTime: string | null
  workDate: string
  clockInTime: string | null
  clockOutTime: string | null
  workedMinutes: number
  availabilityStatus: AvailabilityStatus
}

export interface ClockInResponse {
  attendanceId: string
  staffId: string
  shiftId: string
  workDate: string
  clockInTime: string
  workedMinutes: number
  availabilityStatus: AvailabilityStatus
}

export interface ClockOutResponse {
  attendanceId: string
  staffId: string
  shiftId: string
  workDate: string
  clockInTime: string
  clockOutTime: string
  workedMinutes: number
  availabilityStatus: AvailabilityStatus
}

export interface WeeklyAttendanceSummaryResponse {
  totalWorkedMinutes: number
  overtimeMinutes: number
  totalBreakMinutes: number
}

export interface WeeklyAttendanceLogItemResponse {
  date: string
  dayLabel: string
  clockInTime: string | null
  clockOutTime: string | null
  workedMinutes: number
  statusLabel: string
  statusType: 'ON_TIME' | 'LATE' | 'OVERTIME'
}

export interface WeeklyAttendanceHistoryResponse {
  weekStart: string
  weekEnd: string
  summary: WeeklyAttendanceSummaryResponse
  logs: WeeklyAttendanceLogItemResponse[]
  pagination: {
    page: number
    size: number
    hasPrevious: boolean
    hasNext: boolean
  }
}
