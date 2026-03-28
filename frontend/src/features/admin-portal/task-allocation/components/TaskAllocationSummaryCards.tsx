import type { AdminTaskAllocationSummary } from '../types'
import { TaskAllocationSummaryCard } from './TaskAllocationSummaryCard'

function SummaryCardsSkeleton() {
  return (
    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      {Array.from({ length: 4 }).map((_, index) => (
        <div
          key={index}
          className="h-36 animate-pulse rounded-[1.45rem] bg-white shadow-[0_18px_45px_rgba(25,39,52,0.06)]"
        />
      ))}
    </div>
  )
}

export function TaskAllocationSummaryCards({
  summary,
  isLoading,
  isError,
  errorMessage,
  onRetry,
}: {
  summary?: AdminTaskAllocationSummary
  isLoading: boolean
  isError: boolean
  errorMessage: string
  onRetry: () => void
}) {
  if (isLoading && !summary) {
    return <SummaryCardsSkeleton />
  }

  if (isError && !summary) {
    return (
      <section className="rounded-[1.45rem] border border-[#f0d9d8] bg-white p-5 shadow-[0_18px_45px_rgba(25,39,52,0.06)]">
        <p className="text-sm text-[#a34a47]">{errorMessage}</p>
        <button
          type="button"
          onClick={onRetry}
          className="mt-4 rounded-full bg-[#145f78] px-4 py-2 text-sm font-semibold text-white"
        >
          Retry
        </button>
      </section>
    )
  }

  const data = summary

  if (!data) {
    return null
  }

  return (
    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      <TaskAllocationSummaryCard
        label="Total Active"
        value={data.totalActiveTasks}
        accent="#145f78"
        icon="assignment"
        detail={`${data.inProgressCount} in progress`}
      />
      <TaskAllocationSummaryCard
        label="Checkout Tasks"
        value={data.checkoutTaskCount}
        accent="#365bff"
        icon="exit_to_app"
        detail={`${data.checkoutAssignedCount} assigned • ${data.checkoutPendingCount} pending`}
      />
      <TaskAllocationSummaryCard
        label="Daily Cleaning"
        value={data.dailyTaskCount}
        accent="#1e9e89"
        icon="cleaning_services"
        detail={`${data.dailyAssignedCount} assigned • ${data.dailyPendingCount} pending`}
      />
      <TaskAllocationSummaryCard
        label="Vacant Cleaning"
        value={data.vacantTaskCount}
        accent="#b67a45"
        icon="hotel_class"
        detail={`${data.vacantAssignedCount} assigned • ${data.vacantPendingCount} pending`}
      />
    </div>
  )
}
