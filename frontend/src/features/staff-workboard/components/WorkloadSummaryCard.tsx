import type { WorkloadSummaryResponse } from "../types";

interface WorkloadSummaryCardProps {
  workload: WorkloadSummaryResponse;
}

export function WorkloadSummaryCard({
  workload,
}: WorkloadSummaryCardProps) {
  return (
    <section className="mt-5 rounded-[1.9rem] bg-white p-5 shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
      <div className="flex items-end justify-between gap-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">
            Assigned Minutes
          </p>
          <p className="mt-2 text-[2.2rem] font-semibold tracking-[-0.06em] text-slate-900">
            {workload.assignedMinutes}
          </p>
        </div>
        <div className="rounded-full bg-[#e0ecff] px-4 py-2 text-sm font-semibold text-[#1664c0]">
          {workload.completionPercentage}% complete
        </div>
      </div>

      <div className="mt-5 grid grid-cols-2 gap-3 text-sm">
        <div className="rounded-[1.3rem] bg-[#f8fafc] px-4 py-3">
          <p className="text-slate-400">Completed</p>
          <p className="mt-1 font-semibold text-slate-800">
            {workload.completedMinutes} min · {workload.completedTaskCount} tasks
          </p>
        </div>
        <div className="rounded-[1.3rem] bg-[#f8fafc] px-4 py-3">
          <p className="text-slate-400">Pending</p>
          <p className="mt-1 font-semibold text-slate-800">
            {workload.pendingMinutes} min · {workload.pendingTaskCount} tasks
          </p>
        </div>
      </div>
    </section>
  );
}

