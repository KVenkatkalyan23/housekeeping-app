import { useDispatch, useSelector } from 'react-redux'

import type { RootState } from '../../../../app/store'
import { useGetMyLeavesQuery } from '../api'
import { BottomNav } from '../components/BottomNav'
import { LeaveHistoryHeader } from '../components/LeaveHistoryHeader'
import { LeaveHistoryList } from '../components/LeaveHistoryList'
import { setMyLeavePage } from '../slice'

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

function LeavePagination({
  currentPage,
  hasPrevious,
  hasNext,
  onPrevious,
  onNext,
}: {
  currentPage: number
  hasPrevious: boolean
  hasNext: boolean
  onPrevious: () => void
  onNext: () => void
}) {
  return (
    <div className="mt-6 flex items-center justify-between rounded-[1.4rem] bg-white px-4 py-3 shadow-[0_12px_30px_rgba(15,23,42,0.07)]">
      <button
        type="button"
        onClick={onPrevious}
        disabled={!hasPrevious}
        className="rounded-full border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 disabled:cursor-not-allowed disabled:opacity-45"
      >
        Newer
      </button>
      <span className="text-sm font-semibold text-[#23324d]">Page {currentPage + 1}</span>
      <button
        type="button"
        onClick={onNext}
        disabled={!hasNext}
        className="rounded-full bg-[#2d63cb] px-4 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-45"
      >
        Older
      </button>
    </div>
  )
}

export function LeaveHistoryPage() {
  const dispatch = useDispatch()
  const userId = useSelector((state: RootState) => state.auth.userId)
  const currentPage = useSelector((state: RootState) => state.leaveUi.myPage)
  const { data, isLoading, isError, error, refetch } = useGetMyLeavesQuery(
    {
      userId: userId ?? '',
      page: currentPage,
      size: 5,
    },
    {
      skip: !userId,
    },
  )

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
            My Leaves
          </h1>
          <p className="mt-2 max-w-xs text-sm leading-6 text-slate-500">
            Review approved leave requests and the dates already reserved for you.
          </p>
        </section>

        {data.items.length > 0 ? (
          <>
            <LeaveHistoryList items={data.items} />
            <LeavePagination
              currentPage={data.pagination.page}
              hasPrevious={data.pagination.hasPrevious}
              hasNext={data.pagination.hasNext}
              onPrevious={() => dispatch(setMyLeavePage(data.pagination.page - 1))}
              onNext={() => dispatch(setMyLeavePage(data.pagination.page + 1))}
            />
          </>
        ) : (
          <section className="mt-8 rounded-[1.5rem] bg-white p-6 text-center shadow-[0_14px_36px_rgba(15,23,42,0.08)]">
            <h2 className="text-lg font-semibold text-[#26324d]">No leave history</h2>
            <p className="mt-2 text-sm leading-6 text-slate-500">
              You do not have any leave requests yet.
            </p>
          </section>
        )}
      </div>

      <BottomNav />
    </main>
  )
}
