export function TaskAllocationPagination({
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
  const visibleCount = totalElements === 0 ? 0 : Math.min((page + 1) * size, totalElements)

  return (
    <div className="flex flex-col gap-3 rounded-[1.2rem] bg-white px-4 py-4 shadow-[0_18px_45px_rgba(25,39,52,0.06)] sm:flex-row sm:items-center sm:justify-between">
      <p className="text-sm font-medium text-[#5b6978]">
        Showing {visibleCount} of {totalElements} tasks
      </p>

      <div className="flex items-center gap-3 self-end sm:self-auto">
        <p className="text-sm font-medium text-[#7c8998]">
          Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
        </p>
        <button
          type="button"
          disabled={!hasPrevious || loading}
          onClick={() => onPageChange(page - 1)}
          className="rounded-full border border-[#edf1f5] px-4 py-2 text-sm font-semibold text-[#7c8998] disabled:opacity-50"
        >
          Prev
        </button>
        <button
          type="button"
          disabled={!hasNext || loading}
          onClick={() => onPageChange(page + 1)}
          className="rounded-full border border-[#edf1f5] px-4 py-2 text-sm font-semibold text-[#4f5f72] disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </div>
  )
}
