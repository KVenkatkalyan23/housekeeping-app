import type { WeeklyAttendanceSummaryResponse } from "../types";

function formatDecimalHours(minutes: number) {
  return (minutes / 60).toFixed(1);
}

function formatDuration(minutes: number) {
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;

  if (hours === 0) {
    return `${remainingMinutes}m`;
  }

  if (remainingMinutes === 0) {
    return `${hours}h`;
  }

  return `${hours}h ${remainingMinutes}m`;
}

interface WeeklyOverviewCardsProps {
  summary: WeeklyAttendanceSummaryResponse;
}

export function WeeklyOverviewCards({ summary }: WeeklyOverviewCardsProps) {
  return (
    <section className="mt-6">
      <h2 className="text-xl font-semibold tracking-[-0.03em] text-[#23324d]">
        This Week Overview
      </h2>

      <div className="mt-4 grid grid-cols-[1.2fr_1fr] gap-3">
        <article className="rounded-[1.5rem] bg-white p-5 shadow-[0_14px_36px_rgba(15,23,42,0.08)]">
          <div className="flex h-8 w-8 items-center justify-center rounded-full bg-[#e7efff] text-[#295ec8]">
            <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4">
              <path
                d="M12 7v5l3 2"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
              <circle
                cx="12"
                cy="12"
                r="8"
                stroke="currentColor"
                strokeWidth="2"
              />
            </svg>
          </div>
          <p className="mt-8 text-[2rem] font-semibold tracking-[-0.05em] text-[#23324d]">
            {formatDecimalHours(summary.totalWorkedMinutes)}
          </p>
          <p className="mt-1 text-[0.68rem] font-semibold uppercase tracking-[0.16em] text-slate-400">
            Hours Worked
          </p>
        </article>

        <div className="space-y-3">
          <article className="rounded-[1.5rem] bg-[#eef3fd] p-4 shadow-[0_14px_36px_rgba(15,23,42,0.06)]">
            <p className="text-[0.68rem] font-bold uppercase tracking-[0.16em] text-[#597dcd]">
              Overtime
            </p>
            <p className="mt-3 text-[1.6rem] font-semibold tracking-[-0.04em] text-[#23324d]">
              {formatDecimalHours(summary.overtimeMinutes)}h
            </p>
          </article>
          <article className="rounded-[1.5rem] bg-[#f4f4f6] p-4 shadow-[0_14px_36px_rgba(15,23,42,0.06)]">
            <p className="text-[0.68rem] font-bold uppercase tracking-[0.16em] text-slate-400">
              Breaks
            </p>
            <p className="mt-3 text-[1.45rem] font-semibold tracking-[-0.04em] text-[#23324d]">
              {formatDuration(summary.totalBreakMinutes)}
            </p>
          </article>
        </div>
      </div>
    </section>
  );
}
