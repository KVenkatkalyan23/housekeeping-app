import type { AdminStaffDirectoryItem } from '../types'
import { StaffDirectoryRow } from './StaffDirectoryRow'

function TableLoadingRows() {
  return (
    <div className="space-y-0">
      {Array.from({ length: 4 }).map((_, index) => (
        <div
          key={index}
          className="grid grid-cols-1 gap-4 border-t border-[#edf1f5] px-4 py-4 md:grid-cols-[2fr_1.35fr_1.9fr_0.45fr] md:items-center md:px-6"
        >
          <div className="flex items-center gap-4">
            <div className="h-12 w-12 animate-pulse rounded-2xl bg-[#eef2f6]" />
            <div className="space-y-2">
              <div className="h-4 w-36 animate-pulse rounded bg-[#eef2f6]" />
              <div className="h-3 w-24 animate-pulse rounded bg-[#eef2f6]" />
            </div>
          </div>
          <div className="space-y-2">
            <div className="h-3 w-24 animate-pulse rounded bg-[#eef2f6] md:hidden" />
            <div className="h-7 w-24 animate-pulse rounded-full bg-[#eef2f6]" />
          </div>
          <div className="space-y-2">
            <div className="h-3 w-24 animate-pulse rounded bg-[#eef2f6] md:hidden" />
            <div className="h-4 w-40 animate-pulse rounded bg-[#eef2f6]" />
            <div className="h-4 w-32 animate-pulse rounded bg-[#eef2f6]" />
          </div>
          <div className="flex items-center md:justify-end">
            <div className="mr-3 h-3 w-16 animate-pulse rounded bg-[#eef2f6] md:hidden" />
            <div className="h-10 w-10 animate-pulse rounded-full bg-[#eef2f6] md:ml-auto" />
          </div>
        </div>
      ))}
    </div>
  )
}

export function StaffDirectoryTable({
  items,
  isLoading,
  isError,
  errorMessage,
  onRetry,
}: {
  items: AdminStaffDirectoryItem[]
  isLoading: boolean
  isError: boolean
  errorMessage?: string
  onRetry: () => void
}) {
  return (
    <section className="overflow-hidden rounded-[1.9rem] bg-white shadow-[0_22px_50px_rgba(25,39,52,0.06)]">
      <div className="hidden grid-cols-[2fr_1.35fr_1.9fr_0.45fr] gap-4 bg-[#f5f7f9] px-6 py-4 text-[0.72rem] font-bold uppercase tracking-[0.2em] text-[#758396] md:grid">
        <p>Personnel</p>
        <p>Current Status</p>
        <p>Contact Info</p>
        <p className="text-right">Actions</p>
      </div>

      {isLoading ? <TableLoadingRows /> : null}

      {!isLoading && isError ? (
        <div className="px-6 py-14 text-center">
          <p className="text-[0.72rem] font-bold uppercase tracking-[0.18em] text-[#c53b37]">
            Directory Unavailable
          </p>
          <h3 className="mt-3 text-xl font-semibold tracking-[-0.04em] text-[#223346]">
            Unable to load staff records
          </h3>
          <p className="mx-auto mt-3 max-w-lg text-sm leading-6 text-[#6f7b8b]">
            {errorMessage ?? 'Please try again.'}
          </p>
          <button
            type="button"
            onClick={onRetry}
            className="mt-6 rounded-full bg-[#119c98] px-5 py-3 text-sm font-semibold text-white"
          >
            Retry
          </button>
        </div>
      ) : null}

      {!isLoading && !isError && items.length === 0 ? (
        <div className="px-6 py-14 text-center">
          <p className="text-[0.72rem] font-bold uppercase tracking-[0.18em] text-[#7c8c9d]">
            No Matching Staff
          </p>
          <h3 className="mt-3 text-xl font-semibold tracking-[-0.04em] text-[#223346]">
            No employees match this search or filter.
          </h3>
          <p className="mt-3 text-sm leading-6 text-[#6f7b8b]">
            Adjust the search term or choose a different status tab.
          </p>
        </div>
      ) : null}

      {!isLoading && !isError && items.length > 0 ? (
        <div>
          {items.map((item) => (
            <StaffDirectoryRow key={item.staffId} item={item} />
          ))}
        </div>
      ) : null}
    </section>
  )
}
