import type { AdminAttendanceLogItem } from '../types'
import { AttendanceLogsRow } from './AttendanceLogsRow'

function LoadingState() {
  return (
    <div className="space-y-3 p-5">
      {Array.from({ length: 5 }).map((_, index) => (
        <div key={index} className="h-20 animate-pulse rounded-[1rem] bg-[#f2f5f8]" />
      ))}
    </div>
  )
}

export function AttendanceLogsTable({
  items,
  isLoading,
  isError,
  errorMessage,
  onRetry,
}: {
  items: AdminAttendanceLogItem[]
  isLoading: boolean
  isError: boolean
  errorMessage: string
  onRetry: () => void
}) {
  return (
    <section className="overflow-hidden rounded-[1.5rem] bg-white shadow-[0_18px_45px_rgba(25,39,52,0.06)]">
      <div className="flex items-center justify-between border-b border-[#edf1f5] px-5 py-4">
        <div>
          <p className="text-sm font-semibold text-[#243648]">Attendance activity</p>
          <p className="mt-1 text-sm text-[#748395]">
            Detailed check-in and shift completion records for the selected range.
          </p>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full border-separate border-spacing-0">
          <thead>
            <tr className="bg-[#f8fafc] text-left">
              {[
                'Staff Name',
                'Date',
                'Clock-In Time',
                'Clock-Out Time',
                'Total Worked',
                'Shift Reference',
                'Action',
              ].map((label) => (
                <th
                  key={label}
                  className="px-5 py-4 text-[0.68rem] font-bold uppercase tracking-[0.18em] text-[#6b7b8d]"
                >
                  {label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {isLoading && items.length === 0 ? (
              <tr>
                <td colSpan={7}>
                  <LoadingState />
                </td>
              </tr>
            ) : null}

            {isError && items.length === 0 ? (
              <tr>
                <td colSpan={7} className="px-5 py-10 text-center">
                  <p className="text-sm text-[#a34a47]">{errorMessage}</p>
                  <button
                    type="button"
                    onClick={onRetry}
                    className="mt-4 rounded-full bg-[#145f78] px-4 py-2 text-sm font-semibold text-white"
                  >
                    Retry
                  </button>
                </td>
              </tr>
            ) : null}

            {!isLoading && !isError && items.length === 0 ? (
              <tr>
                <td colSpan={7} className="px-5 py-12 text-center">
                  <p className="text-lg font-semibold text-[#243648]">No attendance records found</p>
                  <p className="mt-2 text-sm text-[#758394]">
                    Adjust the selected date range to review attendance activity.
                  </p>
                </td>
              </tr>
            ) : null}

            {items.map((item) => (
              <AttendanceLogsRow key={item.attendanceId} item={item} />
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}
