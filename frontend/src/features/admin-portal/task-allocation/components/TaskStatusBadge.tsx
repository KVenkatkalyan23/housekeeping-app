import type { AdminTaskStatus } from '../types'

function getStatusClasses(status: AdminTaskStatus) {
  switch (status) {
    case 'IN_PROGRESS':
      return 'text-[#1f917e]'
    case 'COMPLETED':
      return 'text-[#3458da]'
    case 'ASSIGNED':
      return 'text-[#b87442]'
    case 'CANCELLED':
      return 'text-[#9e5a5c]'
    default:
      return 'text-[#b87442]'
  }
}

function getStatusLabel(status: AdminTaskStatus) {
  switch (status) {
    case 'IN_PROGRESS':
      return 'In Progress'
    default:
      return status.charAt(0) + status.slice(1).toLowerCase().replace('_', ' ')
  }
}

function getStatusIcon(status: AdminTaskStatus) {
  switch (status) {
    case 'IN_PROGRESS':
      return 'play_circle'
    case 'COMPLETED':
      return 'check_circle'
    case 'ASSIGNED':
      return 'pending_actions'
    case 'CANCELLED':
      return 'cancel'
    default:
      return 'pending_actions'
  }
}

export function TaskStatusBadge({ status }: { status: AdminTaskStatus }) {
  return (
    <span className={`inline-flex items-center gap-2 text-sm font-semibold ${getStatusClasses(status)}`}>
      <span>{getStatusIcon(status)}</span>
      <span className="text-[#5a6777]">{getStatusLabel(status)}</span>
    </span>
  )
}
