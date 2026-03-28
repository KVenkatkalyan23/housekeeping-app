interface AttendanceHistoryPaginationProps {
  currentPage: number;
  hasPrevious: boolean;
  hasNext: boolean;
  onPrevious: () => void;
  onNext: () => void;
}

export function AttendanceHistoryPagination({
  currentPage,
  hasPrevious,
  hasNext,
  onPrevious,
  onNext,
}: AttendanceHistoryPaginationProps) {
  return (
    <div className="mt-6 flex items-center justify-between rounded-[1.4rem] bg-white px-4 py-3 shadow-[0_12px_30px_rgba(15,23,42,0.07)]">
      <button
        type="button"
        onClick={onPrevious}
        disabled={!hasPrevious}
        className="rounded-full border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 disabled:cursor-not-allowed disabled:opacity-45"
      >
        Newer Week
      </button>
      <span className="text-sm font-semibold text-[#23324d]">
        Week {currentPage + 1}
      </span>
      <button
        type="button"
        onClick={onNext}
        disabled={!hasNext}
        className="rounded-full bg-[#2d63cb] px-4 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-45"
      >
        Older Week
      </button>
    </div>
  );
}
