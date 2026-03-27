import type { LeaveListItem } from '../types'

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
  }).format(new Date(`${value}T00:00:00`))
}

function formatDuration(days: number) {
  return `${days} ${days === 1 ? 'Day' : 'Days'}`
}

function getStatusClasses(status: string) {
  if (status === 'APPROVED') {
    return 'bg-[#eef4ff] text-[#375fca]'
  }

  return 'bg-slate-100 text-slate-600'
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
  item: LeaveListItem
  showStaffDetails?: boolean
}

export function LeaveHistoryCard({ item, showStaffDetails = false }: LeaveHistoryCardProps) {
  return (
    <article className="rounded-[1.5rem] bg-[#f7f8fb] px-4 py-4 shadow-[0_12px_28px_rgba(15,23,42,0.05)]">
      <div className="flex items-start justify-between gap-4">
        <div className="flex items-start gap-3">
          <LeaveIcon />
          <div>
            <h3 className="text-sm font-semibold text-[#26324d]">
              {formatLeaveType(item.leaveType)}
            </h3>
            <p className="mt-1 text-[0.78rem] text-slate-500">
              {formatDate(item.fromDate)} to {formatDate(item.toDate)}
            </p>
            {showStaffDetails ? (
              <p className="mt-1 text-[0.78rem] text-slate-400">
                {item.staffName} ({item.username})
              </p>
            ) : null}
          </div>
        </div>

        <div className="text-right">
          <span
            className={`inline-flex rounded-full px-2.5 py-1 text-[0.68rem] font-semibold uppercase tracking-[0.16em] ${getStatusClasses(item.status)}`}
          >
            {formatLeaveType(item.status)}
          </span>
          <p className="mt-2 text-[0.72rem] text-slate-400">
            {formatDuration(item.durationDays)}
          </p>
        </div>
      </div>

      {item.reason ? (
        <p className="mt-3 text-sm leading-6 text-slate-500">{item.reason}</p>
      ) : null}
    </article>
  )
}
