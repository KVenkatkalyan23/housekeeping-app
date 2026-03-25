import type { LeaveHistoryItemResponse } from '../types'

function formatLeaveType(value: string) {
  return value
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date(value))
}

function formatDuration(days: number) {
  return `${days} ${days === 1 ? 'Day' : 'Days'}`
}

function getStatusClasses(status: string) {
  if (status === 'APPROVED') {
    return 'text-[#375fca]'
  }

  if (status === 'DENIED') {
    return 'text-[#d35d63]'
  }

  return 'text-slate-500'
}

function LeaveIcon() {
  return (
    <div className="flex h-11 w-11 items-center justify-center rounded-full bg-[#f6f8ff] text-[#375fca]">
      <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5">
        <path
          d="M7 12h10M12 7l5 5-5 5"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    </div>
  )
}

interface LeaveHistoryCardProps {
  item: LeaveHistoryItemResponse
}

export function LeaveHistoryCard({ item }: LeaveHistoryCardProps) {
  return (
    <article className="flex items-center justify-between gap-4 rounded-[1.5rem] bg-[#f7f8fb] px-4 py-4 shadow-[0_12px_28px_rgba(15,23,42,0.05)]">
      <div className="flex items-center gap-3">
        <LeaveIcon />
        <div>
          <h3 className="text-sm font-semibold text-[#26324d]">
            {formatLeaveType(item.leaveType)}
          </h3>
          <p className="mt-1 text-[0.78rem] text-slate-400">
            {formatDate(item.leaveStartDate)}
          </p>
        </div>
      </div>

      <div className="text-right">
        <p className={`text-sm font-semibold ${getStatusClasses(item.status)}`}>
          {formatLeaveType(item.status)}
        </p>
        <p className="mt-1 text-[0.72rem] text-slate-400">
          {formatDuration(item.durationDays)}
        </p>
      </div>
    </article>
  )
}
