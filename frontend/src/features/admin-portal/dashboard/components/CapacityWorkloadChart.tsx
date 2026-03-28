import type { AdminDashboardCapacityWorkload } from '../types'

interface CapacityWorkloadChartProps {
  chart: AdminDashboardCapacityWorkload
}

function formatHours(value: number) {
  return `${value.toFixed(1).replace('.0', '')}h`
}

function formatPercentage(value: number) {
  return `${Math.round(value)}%`
}

function clampPercentage(value: number) {
  return Math.max(0, Math.min(100, Math.round(value)))
}

export function CapacityWorkloadChart({
  chart,
}: CapacityWorkloadChartProps) {
  const utilization = chart.availableTotalHours
    ? (chart.requiredTotalHours / chart.availableTotalHours) * 100
    : 0
  const discrepancy = chart.requiredTotalHours - chart.availableTotalHours
  const isOverloaded = discrepancy > 0
  const coverageRatio = chart.requiredTotalHours
    ? (chart.availableTotalHours / chart.requiredTotalHours) * 100
    : 0
  const filledWidth = clampPercentage(utilization)

  return (
    <section className="rounded-[1.5rem] bg-white px-5 py-5 shadow-[0_18px_45px_rgba(25,39,52,0.06)] sm:px-6 sm:py-6">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h2 className="text-[1.55rem] font-semibold tracking-[-0.05em] text-[#243648] sm:text-[1.85rem]">
            Capacity vs. Workload
          </h2>
          <p className="mt-1 text-sm leading-6 text-[#6d7988]">
            Current staffing hours against today&apos;s operational demand.
          </p>
        </div>
      </div>

      <div className="mt-6 grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
        <div className="rounded-[1.25rem] border border-[#edf1f5] p-4 sm:p-5">
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="rounded-[1.1rem] bg-[#f5fbfb] p-4">
              <p className="text-[0.7rem] font-bold uppercase tracking-[0.14em] text-[#769297]">
                Available Hours
              </p>
              <p className="mt-3 text-3xl font-semibold tracking-[-0.05em] text-[#15717a]">
                {formatHours(chart.availableTotalHours)}
              </p>
            </div>

            <div className="rounded-[1.1rem] bg-[#f4f7fe] p-4">
              <p className="text-[0.7rem] font-bold uppercase tracking-[0.14em] text-[#7f8fb0]">
                Required Hours
              </p>
              <p className="mt-3 text-3xl font-semibold tracking-[-0.05em] text-[#2f6ed9]">
                {formatHours(chart.requiredTotalHours)}
              </p>
            </div>
          </div>

          <div className="mt-5">
            <div className="flex items-center justify-between gap-3 text-sm font-semibold text-[#526273]">
              <span>Workload Utilization</span>
              <span>{formatPercentage(utilization)}</span>
            </div>
            <div className="mt-3 rounded-full bg-[#edf1f5] p-1">
              <div
                className={[
                  'h-3 rounded-full transition-all',
                  isOverloaded
                    ? 'bg-[linear-gradient(90deg,#d3332f,#ef6a4b)]'
                    : 'bg-[linear-gradient(90deg,#15717a,#33b2a6)]',
                ].join(' ')}
                style={{ width: `${filledWidth}%` }}
              />
            </div>
          </div>
        </div>

        <div className="rounded-[1.25rem] border border-[#edf1f5] p-4 sm:p-5">
          <p className="text-[0.7rem] font-bold uppercase tracking-[0.14em] text-[#7b8796]">
            Status Summary
          </p>
          <p
            className={[
              'mt-3 text-[1.9rem] font-semibold tracking-[-0.05em] sm:text-3xl',
              isOverloaded ? 'text-[#d3332f]' : 'text-[#15717a]',
            ].join(' ')}
          >
            {isOverloaded ? 'Over Capacity' : 'Within Capacity'}
          </p>
          <p className="mt-3 text-sm leading-6 text-[#6d7988]">
            {isOverloaded
              ? `${formatHours(discrepancy)} additional staffing hours are needed to fully cover the current workload.`
              : `${formatHours(Math.abs(discrepancy))} staffing hours remain available after covering the current workload.`}
          </p>

          <div className="mt-5 rounded-[1rem] bg-[#f8fafc] p-4">
            <p className="text-[0.68rem] font-bold uppercase tracking-[0.14em] text-[#8a97a8]">
              Coverage Ratio
            </p>
            <p className="mt-2 text-2xl font-semibold text-[#243648]">
              {formatPercentage(coverageRatio)}
            </p>
          </div>
        </div>
      </div>
    </section>
  )
}
