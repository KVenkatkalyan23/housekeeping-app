import { useGetLeaveHistoryQuery } from '../api'
import { BottomNav } from '../components/BottomNav'
import { LeaveFab } from '../components/LeaveFab'
import { LeaveHistoryHeader } from '../components/LeaveHistoryHeader'
import { LeaveHistoryList } from '../components/LeaveHistoryList'

function resolveErrorMessage(error: unknown) {
  if (!error || typeof error !== 'object') {
    return 'Unable to load leave history.'
  }

  const candidate = error as {
    data?: { message?: string; error?: { message?: string } }
    error?: string
  }

  return (
    candidate.data?.message ??
    candidate.data?.error?.message ??
    candidate.error ??
    'Unable to load leave history.'
  )
}

export function LeaveHistoryPage() {
  const { data, isLoading, isError, error, refetch } = useGetLeaveHistoryQuery()

  if (isLoading) {
    return (
      <main className="min-h-screen bg-[#fcfcfd] px-4 py-5 pb-32">
        <div className="mx-auto max-w-md animate-pulse space-y-4">
          <div className="h-14 rounded-[1.3rem] bg-white" />
          <div className="h-20 rounded-[1.5rem] bg-white" />
          <div className="h-24 rounded-[1.5rem] bg-white" />
          <div className="h-24 rounded-[1.5rem] bg-white" />
        </div>
      </main>
    )
  }

  if (isError || !data) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-[#fcfcfd] px-4">
        <div className="w-full max-w-sm rounded-[1.75rem] bg-white p-6 text-center shadow-[0_14px_36px_rgba(15,23,42,0.08)]">
          <h1 className="text-xl font-semibold text-[#26324d]">Unable to load leave history</h1>
          <p className="mt-2 text-sm leading-6 text-slate-500">{resolveErrorMessage(error)}</p>
          <button
            type="button"
            onClick={() => refetch()}
            className="mt-5 h-11 rounded-full bg-[#2849c7] px-5 text-sm font-semibold text-white"
          >
            Retry
          </button>
        </div>
      </main>
    )
  }

  return (
    <main className="min-h-screen bg-[#fcfcfd] pb-36 text-slate-700">
      <div className="mx-auto max-w-md px-4 py-5">
        <LeaveHistoryHeader />

        <section className="mt-6">
          <h1 className="text-[2.2rem] font-semibold tracking-[-0.06em] text-[#2849c7]">
            Leave History
          </h1>
          <p className="mt-2 max-w-xs text-sm leading-6 text-slate-500">
            View your past time off applications and their status.
          </p>
        </section>

        {data.length > 0 ? (
          <LeaveHistoryList items={data} />
        ) : (
          <section className="mt-8 rounded-[1.5rem] bg-white p-6 text-center shadow-[0_14px_36px_rgba(15,23,42,0.08)]">
            <h2 className="text-lg font-semibold text-[#26324d]">No leave history</h2>
            <p className="mt-2 text-sm leading-6 text-slate-500">
              You do not have any leave requests yet.
            </p>
          </section>
        )}
      </div>

      <LeaveFab />
      <BottomNav />
    </main>
  )
}
