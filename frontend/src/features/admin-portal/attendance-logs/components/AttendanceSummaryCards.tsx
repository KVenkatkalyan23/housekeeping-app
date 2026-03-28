import type { AdminAttendanceSummaryResponse } from '../types'
import { AttendanceSummaryCard } from './AttendanceSummaryCard'

function SummaryErrorState({
  errorMessage,
  onRetry,
}: {
  errorMessage: string
  onRetry: () => void
}) {
  return (
    <section className="rounded-[1.45rem] border border-[#f0d9d8] bg-white px-6 py-8 text-center shadow-[0_18px_45px_rgba(25,39,52,0.06)]">
      <p className="text-sm text-[#8d4b47]">{errorMessage}</p>
      <button
        type="button"
        onClick={onRetry}
        className="mt-4 rounded-full bg-[#145f78] px-5 py-3 text-sm font-semibold text-white"
      >
        Retry
      </button>
    </section>
  )
}

export function AttendanceSummaryCards({
  summary,
  isLoading,
  isError,
  errorMessage,
  onRetry,
}: {
  summary?: AdminAttendanceSummaryResponse
  isLoading: boolean
  isError: boolean
  errorMessage: string
  onRetry: () => void
}) {
  if (isError && !summary) {
    return <SummaryErrorState errorMessage={errorMessage} onRetry={onRetry} />
  }

  return (
    <section className="grid gap-4 lg:grid-cols-3">
      <AttendanceSummaryCard
        label="Active Today"
        value={summary ? `${summary.activeTodayCount}` : '--'}
        supportingText={
          summary
            ? `${summary.activeTodayDeltaPercent >= 0 ? '+' : ''}${summary.activeTodayDeltaPercent}% vs previous range`
            : 'Comparing current range activity'
        }
        tone="teal"
        isLoading={isLoading && !summary}
      />
      <AttendanceSummaryCard
        label="Avg. Shift Length"
        value={summary ? `${summary.averageShiftLengthHours.toFixed(1)} hrs` : '--'}
        supportingText={summary?.averageShiftLengthLabel ?? 'Measuring completed shifts'}
        tone="amber"
        isLoading={isLoading && !summary}
      />
      <AttendanceSummaryCard
        label="Late Check-Ins"
        value={summary ? `${summary.lateCheckInsCount}` : '--'}
        supportingText={summary?.lateCheckInsLabel ?? 'Tracking punctuality exceptions'}
        tone="rose"
        isLoading={isLoading && !summary}
      />
    </section>
  )
}
