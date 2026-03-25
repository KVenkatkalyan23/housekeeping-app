export interface StaffProfileResponse {
  staffId: string
  userId: string
  fullName: string
  displayName: string
  email: string | null
  phone: string | null
  currentShift: string
  assignedWing: string
  availabilityStatus: 'ON_DUTY' | 'OFF_DUTY'
}
