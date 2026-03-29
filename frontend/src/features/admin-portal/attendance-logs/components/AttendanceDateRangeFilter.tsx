interface AttendanceDateRangeFilterProps {
  fromDate: string
  toDate: string
  loading: boolean
  validationMessage: string | null
  onFromDateChange: (value: string) => void
  onToDateChange: (value: string) => void
}

export function AttendanceDateRangeFilter({
  fromDate,
  toDate,
  loading,
  validationMessage,
  onFromDateChange,
  onToDateChange,
}: AttendanceDateRangeFilterProps) {
  return (
    <section className="rounded-[1.45rem] bg-white p-4 shadow-[0_18px_45px_rgba(25,39,52,0.06)] sm:p-5">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p className="text-sm font-semibold text-[#243648]">Date range</p>
          <p className="mt-1 text-sm text-[#748395]">
            Summary cards and attendance rows update automatically when the range changes.
          </p>
        </div>

        <div className="grid gap-3 sm:grid-cols-2">
          <label className="flex min-w-[180px] flex-col gap-2">
            <span className="text-[0.72rem] font-bold uppercase tracking-[0.18em] text-[#738295]">
              From Date
            </span>
            <input
              type="date"
              value={fromDate}
              disabled={loading}
              onChange={(event) => onFromDateChange(event.target.value)}
              className="rounded-2xl border border-[#dbe4ee] bg-[#f9fbfd] px-4 py-3 text-sm font-medium text-[#243648] outline-none transition focus:border-[#1a7a77]"
            />
          </label>

          <label className="flex min-w-[180px] flex-col gap-2">
            <span className="text-[0.72rem] font-bold uppercase tracking-[0.18em] text-[#738295]">
              To Date
            </span>
            <input
              type="date"
              value={toDate}
              disabled={loading}
              onChange={(event) => onToDateChange(event.target.value)}
              className="rounded-2xl border border-[#dbe4ee] bg-[#f9fbfd] px-4 py-3 text-sm font-medium text-[#243648] outline-none transition focus:border-[#1a7a77]"
            />
          </label>
        </div>
      </div>

      {validationMessage ? (
        <p className="mt-3 text-sm font-medium text-[#c43d38]">{validationMessage}</p>
      ) : null}
    </section>
  )
}
