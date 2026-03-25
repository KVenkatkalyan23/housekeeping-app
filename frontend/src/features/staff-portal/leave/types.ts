export interface LeaveHistoryItemResponse {
  leaveRequestId: string
  leaveType: string
  leaveStartDate: string
  leaveEndDate: string
  status: string
  durationDays: number
  reason: string | null
  requestedAt: string | null
}
