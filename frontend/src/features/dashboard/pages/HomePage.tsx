import { useDispatch, useSelector } from 'react-redux'

import type { RootState } from '../../../app/store'
import { useGetAdminLeavesQuery } from '../../staff-portal/leave/api'
import { LeaveHistoryList } from '../../staff-portal/leave/components/LeaveHistoryList'
import { setAdminLeavePage } from '../../staff-portal/leave/slice'

function resolveErrorMessage(error: unknown) {
  if (!error || typeof error !== 'object') {
    return 'Unable to load leave requests.'
  }

  const candidate = error as {
    data?: { message?: string; error?: { message?: string } }
    error?: string
  }

  return (
    candidate.data?.message ??
    candidate.data?.error?.message ??
    candidate.error ??
    'Unable to load leave requests.'
  )
}

export function HomePage() {
  const dispatch = useDispatch()
  const currentPage = useSelector((state: RootState) => state.leaveUi.adminPage)
  const { data, isLoading, isError, error, refetch } = useGetAdminLeavesQuery({
    page: currentPage,
    size: 8,
  })

  return (
    <main className="min-h-screen bg-[#f4f3f8] px-4 py-8 text-slate-700">
      <div className="mx-auto max-w-4xl">
        <section className="rounded-[2rem] bg-[linear-gradient(135deg,#0f172a,#2849c7)] px-6 py-8 text-white shadow-[0_24px_60px_rgba(15,23,42,0.22)]">
          <p className="text-xs font-semibold uppercase tracking-[0.22em] text-blue-100">
            Admin Dashboard
          </p>
          <h1 className="mt-3 text-3xl font-semibold tracking-[-0.05em]">
            Leave Overview
          </h1>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-blue-100">
            Review auto-approved leave requests across the team and page through the latest submissions.
          </p>
        </section>

        {isLoading ? (
          <div className="mt-6 grid gap-4">
            <div className="h-28 animate-pulse rounded-[1.75rem] bg-white" />
            <div className="h-28 animate-pulse rounded-[1.75rem] bg-white" />
            <div className="h-28 animate-pulse rounded-[1.75rem] bg-white" />
          </div>
        ) : null}

        {isError ? (
          <section className="mt-6 rounded-[1.75rem] bg-white p-6 shadow-[0_14px_36px_rgba(15,23,42,0.08)]">
            <h2 className="text-lg font-semibold text-slate-900">Unable to load admin leaves</h2>
            <p className="mt-2 text-sm leading-6 text-slate-500">{resolveErrorMessage(error)}</p>
            <button
              type="button"
              onClick={() => refetch()}
              className="mt-5 rounded-full bg-[#2849c7] px-5 py-3 text-sm font-semibold text-white"
            >
              Retry
            </button>
          </section>
        ) : null}

        {!isLoading && !isError && data ? (
          <>
            {data.items.length > 0 ? (
              <>
                <LeaveHistoryList items={data.items} showStaffDetails />
                <div className="mt-6 flex items-center justify-between rounded-[1.4rem] bg-white px-4 py-3 shadow-[0_12px_30px_rgba(15,23,42,0.07)]">
                  <button
                    type="button"
                    onClick={() => dispatch(setAdminLeavePage(data.pagination.page - 1))}
                    disabled={!data.pagination.hasPrevious}
                    className="rounded-full border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 disabled:cursor-not-allowed disabled:opacity-45"
                  >
                    Newer
                  </button>
                  <span className="text-sm font-semibold text-[#23324d]">
                    Page {data.pagination.page + 1}
                  </span>
                  <button
                    type="button"
                    onClick={() => dispatch(setAdminLeavePage(data.pagination.page + 1))}
                    disabled={!data.pagination.hasNext}
                    className="rounded-full bg-[#2d63cb] px-4 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-45"
                  >
                    Older
                  </button>
                </div>
              </>
            ) : (
              <section className="mt-6 rounded-[1.75rem] bg-white p-6 text-center shadow-[0_14px_36px_rgba(15,23,42,0.08)]">
                <h2 className="text-lg font-semibold text-slate-900">No leave requests</h2>
                <p className="mt-2 text-sm leading-6 text-slate-500">
                  There are no leave requests to display yet.
                </p>
              </section>
            )}
          </>
        ) : null}
      </div>
    </main>
  )
}
