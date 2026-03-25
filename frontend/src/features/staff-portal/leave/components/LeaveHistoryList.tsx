import type { LeaveHistoryItemResponse } from '../types'
import { LeaveHistoryCard } from './LeaveHistoryCard'

interface LeaveHistoryListProps {
  items: LeaveHistoryItemResponse[]
}

export function LeaveHistoryList({ items }: LeaveHistoryListProps) {
  return (
    <div className="mt-6 space-y-3.5">
      {items.map((item) => (
        <LeaveHistoryCard key={item.leaveRequestId} item={item} />
      ))}
    </div>
  )
}
