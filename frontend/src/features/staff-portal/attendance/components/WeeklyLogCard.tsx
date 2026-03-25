import type { WeeklyAttendanceLogItemResponse } from '../types'

function formatTime(value: string | null) {
  if (!value) {
    return '--:--'
  }

  return new Intl.DateTimeFormat('en-US', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: true,
  }).format(new Date(value))
}

function formatWorkedMinutes(minutes: number) {
  const hours = Math.floor(minutes / 60)
  const remainingMinutes = minutes % 60

  if (remainingMinutes === 0) {
    return `${hours}h`
  }

  return `${hours}h ${remainingMinutes}m`
}

function getStatusClasses(statusType: WeeklyAttendanceLogItemResponse['statusType']) {
  if (statusType === 'LATE') {
    return 'text-[#d18c4b]'
  }

  if (statusType === 'OVERTIME') {
    return 'text-[#3c65c7]'
  }

  return 'text-[#2d63cb]'
}

interface WeeklyLogCardProps {
  log: WeeklyAttendanceLogItemResponse
}

export function WeeklyLogCard({ log }: WeeklyLogCardProps) {
  return (
    <article className="relative rounded-[1.4rem] bg-white px-4 py-3.5 shadow-[0_14px_32px_rgba(15,23,42,0.08)]">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h3 className="text-sm font-semibold text-[#23324d]">{log.dayLabel}</h3>
          <p className="mt-1 text-[0.78rem] text-slate-400">
            {formatTime(log.clockInTime)} - {formatTime(log.clockOutTime)}
          </p>
        </div>
        <div className="text-right">
          <p className="text-sm font-semibold text-[#3c65c7]">
            {formatWorkedMinutes(log.workedMinutes)}
          </p>
          <p className={`mt-1 text-[0.68rem] font-bold uppercase tracking-[0.14em] ${getStatusClasses(log.statusType)}`}>
            {log.statusLabel}
          </p>
        </div>
      </div>
    </article>
  )
}
