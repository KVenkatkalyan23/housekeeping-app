function ChevronLeftIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" aria-hidden="true">
      <path d="m14.5 6-6 6 6 6 1.06-1.06L10.62 12l4.94-4.94L14.5 6Z" fill="currentColor" />
    </svg>
  )
}

function ChevronRightIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" aria-hidden="true">
      <path d="m9.5 18 6-6-6-6-1.06 1.06L13.38 12l-4.94 4.94L9.5 18Z" fill="currentColor" />
    </svg>
  )
}

function buildVisiblePages(currentPage: number, totalPages: number) {
  if (totalPages <= 0) {
    return []
  }

  const pages = new Set<number>([
    0,
    totalPages - 1,
    currentPage - 1,
    currentPage,
    currentPage + 1,
  ])

  return Array.from(pages)
    .filter((page) => page >= 0 && page < totalPages)
    .sort((left, right) => left - right)
}

export function StaffDirectoryPagination({
  page,
  size,
  totalPages,
  totalElements,
  isLoading,
  onPageChange,
  onSizeChange,
}: {
  page: number
  size: number
  totalPages: number
  totalElements: number
  isLoading: boolean
  onPageChange: (value: number) => void
  onSizeChange: (value: number) => void
}) {
  const visiblePages = buildVisiblePages(page, totalPages)
  const hasPrevious = page > 0
  const hasNext = totalPages > 0 && page < totalPages - 1

  if (isLoading && totalElements === 0) {
    return (
      <div className="flex flex-col gap-4 rounded-[1.6rem] bg-white px-5 py-4 shadow-[0_18px_45px_rgba(25,39,52,0.06)] md:flex-row md:items-center md:justify-between">
        <div className="flex flex-wrap items-center gap-2">
          {Array.from({ length: 5 }).map((_, index) => (
            <div
              key={index}
              className="h-9 w-9 animate-pulse rounded-xl bg-[#eef2f6]"
            />
          ))}
        </div>
        <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
          <div className="h-4 w-28 animate-pulse rounded bg-[#eef2f6]" />
          <div className="h-10 w-36 animate-pulse rounded-xl bg-[#eef2f6]" />
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-4 rounded-[1.6rem] bg-white px-5 py-4 shadow-[0_18px_45px_rgba(25,39,52,0.06)] md:flex-row md:items-center md:justify-between">
      <div className="flex flex-wrap items-center gap-2">
        <button
          type="button"
          onClick={() => hasPrevious && onPageChange(page - 1)}
          disabled={!hasPrevious || isLoading}
          className="inline-flex h-9 w-9 items-center justify-center rounded-xl border border-[#edf1f5] text-[#7d8a99] disabled:cursor-not-allowed disabled:opacity-45"
          aria-label="Previous page"
        >
          {ChevronLeftIcon()}
        </button>

        {visiblePages.map((visiblePage, index) => {
          const previousPage = visiblePages[index - 1]
          const showGap = previousPage !== undefined && visiblePage - previousPage > 1

          return (
            <span key={visiblePage} className="flex items-center gap-2">
              {showGap ? (
                <span className="px-1 text-sm font-semibold text-[#9aa6b5]">...</span>
              ) : null}
              <button
                type="button"
                onClick={() => onPageChange(visiblePage)}
                disabled={isLoading}
                className={[
                  'inline-flex h-9 min-w-9 items-center justify-center rounded-xl px-3 text-sm font-semibold transition',
                  visiblePage === page
                    ? 'bg-[#119c98] text-white'
                    : 'border border-[#edf1f5] text-[#5f7084]',
                ].join(' ')}
              >
                {visiblePage + 1}
              </button>
            </span>
          )
        })}

        <button
          type="button"
          onClick={() => hasNext && onPageChange(page + 1)}
          disabled={!hasNext || isLoading}
          className="inline-flex h-9 w-9 items-center justify-center rounded-xl border border-[#edf1f5] text-[#7d8a99] disabled:cursor-not-allowed disabled:opacity-45"
          aria-label="Next page"
        >
          {ChevronRightIcon()}
        </button>
      </div>

      <div className="flex flex-col gap-2 text-sm text-[#5f7084] sm:flex-row sm:items-center sm:justify-end">
        <p className="font-medium">
          {totalElements === 0
            ? 'No results'
            : `${page * size + 1}-${Math.min((page + 1) * size, totalElements)} of ${totalElements}`}
        </p>
        <label className="flex items-center gap-2 font-medium">
          <span>Results per page:</span>
          <select
            value={size}
            onChange={(event) => onSizeChange(Number(event.target.value))}
            disabled={isLoading}
            className="rounded-xl border border-[#edf1f5] bg-white px-3 py-2 text-sm text-[#223346] outline-none"
          >
            {[10, 20, 30].map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </label>
      </div>
    </div>
  )
}
