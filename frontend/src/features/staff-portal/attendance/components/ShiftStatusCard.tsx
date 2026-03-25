import type { AttendanceStatusResponse } from '../types'

interface ShiftStatusCardProps {
  attendance: AttendanceStatusResponse
}

function formatShiftTime(value: string | null) {
  if (!value) {
    return '--:--'
  }

  const [hours, minutes] = value.split(':')
  const date = new Date()
  date.setHours(Number(hours), Number(minutes), 0, 0)

  return new Intl.DateTimeFormat('en-US', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: true,
  }).format(date)
}

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

export function ShiftStatusCard({ attendance }: ShiftStatusCardProps) {
  const progress = 75

  return (
    <section className="rounded-[1.75rem] bg-white p-5 shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-[0.7rem] font-semibold uppercase tracking-[0.18em] text-slate-400">
            Shift Status
          </p>
          <div className="mt-2 flex items-center gap-2">
            <span
              className={`h-2.5 w-2.5 rounded-full ${
                attendance.onDuty ? 'bg-[#1664c0]' : 'bg-slate-300'
              }`}
            />
            <p className="text-lg font-semibold text-slate-900">
              {attendance.onDuty ? 'On Duty' : 'Off Duty'}
            </p>
          </div>
        </div>

        <div className="text-right">
          <p className="text-[0.7rem] font-semibold uppercase tracking-[0.18em] text-slate-400">
            Started at
          </p>
          <p className="mt-2 text-sm font-semibold text-[#4c78c8]">
            {formatTime(attendance.clockInTime)}
          </p>
        </div>
      </div>

      <div className="mt-5">
        <div className="flex items-center justify-between text-[0.72rem] font-semibold uppercase tracking-[0.14em] text-slate-400">
          <span>4h Shift Progress</span>
          <span className="text-[#4c78c8]">{progress}% Complete</span>
        </div>
        <div className="mt-3 h-2.5 rounded-full bg-slate-100">
          <div
            className="h-full rounded-full bg-[#1664c0]"
            style={{ width: `${progress}%` }}
          />
        </div>
        <div className="mt-2 flex justify-between text-[0.68rem] font-medium text-slate-300">
          <span>{formatShiftTime(attendance.shiftStartTime)}</span>
          <span>{formatShiftTime(attendance.shiftEndTime)}</span>
        </div>
      </div>
    </section>
  )
}
