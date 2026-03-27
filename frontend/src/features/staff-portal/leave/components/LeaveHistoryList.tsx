import type { LeaveListItem } from '../types'
import { LeaveHistoryCard } from './LeaveHistoryCard'

interface LeaveHistoryListProps {
  items: LeaveListItem[]
  showStaffDetails?: boolean
}

export function LeaveHistoryList({ items, showStaffDetails = false }: LeaveHistoryListProps) {
  return (
    <div className="mt-6 space-y-3.5">
      {items.map((item) => (
        <LeaveHistoryCard key={item.leaveId} item={item} showStaffDetails={showStaffDetails} />
      ))}
    </div>
  )
}
