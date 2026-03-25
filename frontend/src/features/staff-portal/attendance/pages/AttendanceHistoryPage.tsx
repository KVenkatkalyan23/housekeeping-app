import { useState } from 'react'

import { useGetWeeklyAttendanceHistoryQuery } from '../api'
import { AttendanceHeader } from '../components/AttendanceHeader'
import { AttendanceHistoryPagination } from '../components/AttendanceHistoryPagination'
import { BottomNav } from '../components/BottomNav'
import { WeeklyLogsTimeline } from '../components/WeeklyLogsTimeline'
import { WeeklyOverviewCards } from '../components/WeeklyOverviewCards'

function resolveErrorMessage(error: unknown) {
  if (!error || typeof error !== 'object') {
    return 'Unable to load attendance history.'
  }

  const candidate = error as {
    data?: { message?: string; error?: { message?: string } }
    error?: string
  }

  return (
    candidate.data?.message ??
    candidate.data?.error?.message ??
    candidate.error ??
    'Unable to load attendance history.'
  )
}

export function AttendanceHistoryPage() {
  const [page, setPage] = useState(0)
  const { data, isLoading, isError, error, refetch, isFetching } =
    useGetWeeklyAttendanceHistoryQuery({ page, size: 7 })

  if (isLoading) {
    return (
      <main className="min-h-screen bg-[#f5f6fa] px-4 py-5 pb-36">
        <div className="mx-auto max-w-md animate-pulse space-y-4">
          <div className="h-14 rounded-[1.3rem] bg-white" />
          <div className="h-44 rounded-[1.5rem] bg-white" />
          <div className="h-40 rounded-[1.5rem] bg-white" />
        </div>
      </main>
    )
  }

  if (isError || !data) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-[#f5f6fa] px-4">
        <div className="w-full max-w-sm rounded-[1.75rem] bg-white p-6 text-center shadow-[0_14px_36px_rgba(15,23,42,0.08)]">
          <h1 className="text-xl font-semibold text-[#23324d]">Unable to load attendance history</h1>
          <p className="mt-2 text-sm leading-6 text-slate-500">{resolveErrorMessage(error)}</p>
          <button
            type="button"
            onClick={() => {
              refetch()
            }}
            className="mt-5 h-11 rounded-full bg-[#2d63cb] px-5 text-sm font-semibold text-white"
          >
            Retry
          </button>
        </div>
      </main>
    )
  }

  return (
    <main className="min-h-screen bg-[#f5f6fa] pb-36 text-slate-700">
      <div className="mx-auto max-w-md px-4 py-5">
        <AttendanceHeader />
        <WeeklyOverviewCards summary={data.summary} />
        <AttendanceHistoryPagination
          currentPage={data.pagination.page}
          hasPrevious={data.pagination.hasPrevious}
          hasNext={data.pagination.hasNext}
          onPrevious={() => setPage((currentPage) => Math.max(currentPage - 1, 0))}
          onNext={() => setPage((currentPage) => currentPage + 1)}
        />
        {isFetching ? (
          <p className="mt-3 text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">
            Loading another week...
          </p>
        ) : null}
        {data.logs.length > 0 ? (
          <WeeklyLogsTimeline logs={data.logs} />
        ) : (
          <section className="mt-8 rounded-[1.5rem] bg-white p-6 text-center shadow-[0_14px_36px_rgba(15,23,42,0.08)]">
            <h2 className="text-lg font-semibold text-[#23324d]">No attendance records</h2>
            <p className="mt-2 text-sm leading-6 text-slate-500">
              No attendance records for this week.
            </p>
          </section>
        )}
      </div>

      <BottomNav activeTab="attendance" />
    </main>
  )
}
