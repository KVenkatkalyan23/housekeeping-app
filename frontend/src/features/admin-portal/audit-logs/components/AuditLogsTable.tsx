import { AuditLogsRow } from './AuditLogsRow'
import type { AdminAuditLogItem } from '../types'

function AuditLogsLoadingState() {
  return (
    <div className="rounded-[1.6rem] bg-white shadow-[0_18px_45px_rgba(25,39,52,0.06)]">
      <div className="grid grid-cols-6 gap-4 border-b border-[#edf1f5] px-6 py-5">
        {Array.from({ length: 6 }).map((_, index) => (
          <div key={index} className="h-4 animate-pulse rounded bg-[#eef2f5]" />
        ))}
      </div>
      <div className="space-y-4 px-6 py-6">
        {Array.from({ length: 5 }).map((_, index) => (
          <div key={index} className="h-14 animate-pulse rounded-xl bg-[#f4f6f8]" />
        ))}
      </div>
    </div>
  )
}

function AuditLogsErrorState({
  errorMessage,
  onRetry,
}: {
  errorMessage: string
  onRetry: () => void
}) {
  return (
    <section className="rounded-[1.6rem] border border-[#f0d9d8] bg-white px-6 py-10 text-center shadow-[0_18px_45px_rgba(25,39,52,0.06)]">
      <p className="text-[0.72rem] font-bold uppercase tracking-[0.18em] text-[#c23431]">
        Logs Unavailable
      </p>
      <p className="mt-3 text-sm text-[#667585]">{errorMessage}</p>
      <button
        type="button"
        onClick={onRetry}
        className="mt-5 rounded-full bg-[#2158d9] px-5 py-3 text-sm font-semibold text-white"
      >
        Retry
      </button>
    </section>
  )
}

function AuditLogsEmptyState() {
  return (
    <section className="rounded-[1.6rem] border border-dashed border-[#dbe3ea] bg-white px-6 py-12 text-center shadow-[0_18px_45px_rgba(25,39,52,0.06)]">
      <p className="text-[0.72rem] font-bold uppercase tracking-[0.18em] text-[#718193]">
        No Audit Activity
      </p>
      <p className="mt-3 text-sm text-[#667585]">
        No logs match the selected filter yet.
      </p>
    </section>
  )
}

export function AuditLogsTable({
  items,
  isLoading,
  isError,
  errorMessage,
  onRetry,
}: {
  items: AdminAuditLogItem[]
  isLoading: boolean
  isError: boolean
  errorMessage: string
  onRetry: () => void
}) {
  if (isLoading) {
    return <AuditLogsLoadingState />
  }

  if (isError) {
    return <AuditLogsErrorState errorMessage={errorMessage} onRetry={onRetry} />
  }

  if (items.length === 0) {
    return <AuditLogsEmptyState />
  }

  return (
    <div className="overflow-hidden rounded-[1.6rem] bg-white shadow-[0_18px_45px_rgba(25,39,52,0.06)]">
      <div className="overflow-x-auto">
        <table className="min-w-full border-collapse">
          <thead>
            <tr className="border-b border-[#edf1f5] text-left">
              {[
                'Timestamp',
                'Action (Event Code)',
                'Category',
                'Actor',
                'Target',
                'Severity',
              ].map((label) => (
                <th
                  key={label}
                  className="px-6 py-5 text-[0.68rem] font-bold uppercase tracking-[0.16em] text-[#6f7d8d]"
                >
                  {label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <AuditLogsRow key={item.id} item={item} />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
