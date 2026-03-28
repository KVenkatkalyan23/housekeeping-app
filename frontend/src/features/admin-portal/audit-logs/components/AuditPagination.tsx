function buildPageNumbers(page: number, totalPages: number) {
  if (totalPages <= 0) {
    return []
  }

  const start = Math.max(0, page - 1)
  const end = Math.min(totalPages - 1, start + 2)
  const adjustedStart = Math.max(0, end - 2)

  return Array.from(
    { length: end - adjustedStart + 1 },
    (_, index) => adjustedStart + index,
  )
}

export function AuditPagination({
  page,
  size,
  totalElements,
  totalPages,
  loading,
  onPageChange,
}: {
  page: number
  size: number
  totalElements: number
  totalPages: number
  loading: boolean
  onPageChange: (page: number) => void
}) {
  const pages = buildPageNumbers(page, totalPages)
  const hasPrevious = page > 0
  const hasNext = totalPages > 0 && page < totalPages - 1
  const start = totalElements === 0 ? 0 : page * size + 1
  const end = totalElements === 0 ? 0 : Math.min((page + 1) * size, totalElements)

  return (
    <div className="flex flex-col gap-4 rounded-[1.6rem] bg-white px-6 py-5 shadow-[0_18px_45px_rgba(25,39,52,0.06)] sm:flex-row sm:items-center sm:justify-between">
      <p className="text-sm text-[#5c6b7b]">
        Showing {start}-{end} of {totalElements} entries
      </p>

      <div className="flex items-center gap-2 self-end sm:self-auto">
        <button
          type="button"
          disabled={!hasPrevious || loading}
          onClick={() => onPageChange(page - 1)}
          className="inline-flex h-9 w-9 items-center justify-center rounded-full text-[#526170] disabled:opacity-40"
          aria-label="Previous page"
        >
          &#8249;
        </button>

        {pages.map((pageNumber) => {
          const active = pageNumber === page
          return (
            <button
              key={pageNumber}
              type="button"
              disabled={loading}
              onClick={() => onPageChange(pageNumber)}
              className={[
                'inline-flex h-9 w-9 items-center justify-center rounded-lg text-sm font-semibold transition',
                active
                  ? 'bg-[#0d7b74] text-white'
                  : 'text-[#526170] hover:bg-[#eef2f5]',
              ].join(' ')}
            >
              {pageNumber + 1}
            </button>
          )
        })}

        <button
          type="button"
          disabled={!hasNext || loading}
          onClick={() => onPageChange(page + 1)}
          className="inline-flex h-9 w-9 items-center justify-center rounded-full text-[#526170] disabled:opacity-40"
          aria-label="Next page"
        >
          &#8250;
        </button>
      </div>
    </div>
  )
}
