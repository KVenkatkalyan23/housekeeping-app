export function AttendanceLogsHeader() {
  return (
    <section className="mb-5 flex flex-col gap-2">
      <p className="text-[0.72rem] font-bold uppercase tracking-[0.22em] text-[#1a7a77]">
        Workforce Attendance
      </p>
      <div className="flex flex-col gap-2 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <h1 className="text-[2rem] font-semibold tracking-[-0.04em] text-[#243648]">
            Attendance Logs
          </h1>
          <p className="mt-2 max-w-2xl text-sm text-[#708093]">
            Review attendance coverage, shift duration consistency, and late check-ins across the selected date range.
          </p>
        </div>
      </div>
    </section>
  )
}
