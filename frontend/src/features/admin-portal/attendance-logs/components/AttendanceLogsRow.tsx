import type { AdminAttendanceLogItem } from '../types'

function ActionIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" aria-hidden="true">
      <path
        d="M12 5.5a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3Zm0 5a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3Zm0 5a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3Z"
        fill="currentColor"
      />
    </svg>
  )
}

function formatDisplayDate(value: string) {
  const date = new Date(`${value}T00:00:00`)
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(date)
}

export function AttendanceLogsRow({ item }: { item: AdminAttendanceLogItem }) {
  return (
    <tr className="border-t border-[#edf1f5] align-top">
      <td className="px-5 py-5">
        <div className="flex items-center gap-3">
          <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#0c6970,#f0b465)] text-sm font-semibold text-white">
            {item.staffInitials}
          </div>
          <div>
            <p className="text-sm font-semibold text-[#243648]">{item.staffName}</p>
            <p className="mt-1 text-xs font-medium text-[#7a8795]">
              {item.staffRoleLabel}
            </p>
          </div>
        </div>
      </td>
      <td className="px-5 py-5 text-sm font-medium text-[#425366]">
        {formatDisplayDate(item.workDate)}
      </td>
      <td className="px-5 py-5">
        <p className="text-sm font-semibold text-[#243648]">{item.clockInTime}</p>
        <p
          className={`mt-2 inline-flex rounded-full px-2.5 py-1 text-[0.68rem] font-bold uppercase tracking-[0.12em] ${
            item.lateCheckIn
              ? 'bg-[#fdeceb] text-[#c43d38]'
              : 'bg-[#e7f5ef] text-[#197a56]'
          }`}
        >
          {item.lateCheckInLabel}
        </p>
      </td>
      <td className="px-5 py-5">
        <p className="text-sm font-semibold text-[#243648]">{item.clockOutTime}</p>
        <p className="mt-2 text-xs font-medium text-[#7a8795]">{item.statusTag}</p>
      </td>
      <td className="px-5 py-5">
        <p className="text-sm font-semibold text-[#243648]">
          {item.totalWorkedHours.toFixed(1)} hrs
        </p>
        {item.overtimeFlag ? (
          <p className="mt-2 inline-flex rounded-full bg-[#fff2df] px-2.5 py-1 text-[0.68rem] font-bold uppercase tracking-[0.12em] text-[#ac6b16]">
            Overtime
          </p>
        ) : (
          <p className="mt-2 text-xs font-medium text-[#7a8795]">Within planned shift</p>
        )}
      </td>
      <td className="px-5 py-5">
        <span className="inline-flex rounded-full bg-[#eef3f8] px-3 py-1.5 text-xs font-semibold text-[#4e6175]">
          {item.shiftReference}
        </span>
      </td>
      <td className="px-5 py-5 text-right">
        <button
          type="button"
          aria-label={`More actions for ${item.staffName}`}
          className="inline-flex h-10 w-10 items-center justify-center rounded-full border border-[#e5ebf2] text-[#6e7f90]"
        >
          <ActionIcon />
        </button>
      </td>
    </tr>
  )
}
