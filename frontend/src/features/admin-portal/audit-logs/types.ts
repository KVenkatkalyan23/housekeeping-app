export type AuditLogCategory = 'TASK' | 'ATTENDANCE' | 'LEAVE' | 'SYSTEM'
export type AuditLogCategoryFilter = 'ALL' | AuditLogCategory
export type AuditSeverity = 'SUCCESS' | 'INFO' | 'WARNING' | 'ERROR'

export interface AdminAuditLogItem {
  id: string
  createdAt: string
  eventCode: string
  eventTitle: string
  eventMessage: string
  eventCategory: AuditLogCategory
  severity: AuditSeverity
  actorName: string
  actorSubtitle: string | null
  targetLabel: string
  targetSubLabel: string | null
}

export interface AdminAuditLogsResponse {
  items: AdminAuditLogItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
