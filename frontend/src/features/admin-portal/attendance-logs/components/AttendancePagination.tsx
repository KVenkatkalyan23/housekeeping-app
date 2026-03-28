function buildVisiblePages(page: number, totalPages: number) {
  if (totalPages <= 3) {
    return Array.from({ length: totalPages }, (_, index) => index)
  }

  const start = Math.max(0, Math.min(page - 1, totalPages - 3))
  return Array.from({ length: 3 }, (_, index) => start + index)
}

export function AttendancePagination({
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
  const hasPrevious = page > 0
  const hasNext = totalPages > 0 && page < totalPages - 1
  const startEntry = totalElements === 0 ? 0 : page * size + 1
  const endEntry = totalElements === 0 ? 0 : Math.min((page + 1) * size, totalElements)
  const visiblePages = buildVisiblePages(page, totalPages)

  return (
    <div className="flex flex-col gap-4 rounded-[1.2rem] bg-white px-4 py-4 shadow-[0_18px_45px_rgba(25,39,52,0.06)] lg:flex-row lg:items-center lg:justify-between">
      <p className="text-sm font-medium text-[#5b6978]">
        Showing {startEntry} - {endEntry} of {totalElements} entries
      </p>

      <div className="flex flex-wrap items-center gap-2 self-end lg:self-auto">
        <button
          type="button"
          disabled={!hasPrevious || loading}
          onClick={() => onPageChange(page - 1)}
          className="rounded-full border border-[#e4ebf2] px-4 py-2 text-sm font-semibold text-[#6d7e90] disabled:opacity-50"
        >
          Previous
        </button>

        {visiblePages.map((visiblePage) => (
          <button
            key={visiblePage}
            type="button"
            disabled={loading}
            onClick={() => onPageChange(visiblePage)}
            className={`h-10 min-w-10 rounded-full px-3 text-sm font-semibold ${
              visiblePage === page
                ? 'bg-[#145f78] text-white'
                : 'border border-[#e4ebf2] text-[#6d7e90]'
            }`}
          >
            {visiblePage + 1}
          </button>
        ))}

        <button
          type="button"
          disabled={!hasNext || loading}
          onClick={() => onPageChange(page + 1)}
          className="rounded-full border border-[#e4ebf2] px-4 py-2 text-sm font-semibold text-[#6d7e90] disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </div>
  )
}
