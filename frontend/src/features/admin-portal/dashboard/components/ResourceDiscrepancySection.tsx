import type { AdminDashboardResourceDiscrepancy } from '../types'

interface ResourceDiscrepancySectionProps {
  discrepancy: AdminDashboardResourceDiscrepancy
}

function WarningIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-6 w-6" aria-hidden="true">
      <path d="M12 4 3 20h18L12 4Zm0 5 1 6h-2l1-6Zm0 9a1.25 1.25 0 1 0 0-2.5A1.25 1.25 0 0 0 12 18Z" fill="currentColor" />
    </svg>
  )
}

function formatHours(value: number) {
  return `${value.toFixed(1).replace('.0', '')} Hours`
}

function normalizeBarWidth(value: number, maxValue: number) {
  if (maxValue <= 0) {
    return '0%'
  }

  return `${Math.max(10, Math.round((value / maxValue) * 100))}%`
}

export function ResourceDiscrepancySection({
  discrepancy,
}: ResourceDiscrepancySectionProps) {
  const baseline = Math.max(discrepancy.requiredHours, discrepancy.availableHours, 1)
  const isActionRequired = discrepancy.deltaHours > 0

  return (
    <section className="grid gap-4 rounded-[1.5rem] bg-white p-6 shadow-[0_18px_45px_rgba(25,39,52,0.06)] xl:grid-cols-[1.1fr_0.9fr]">
      <div className="pr-0 xl:border-r xl:border-[#edf1f5] xl:pr-6">
        <h2 className="text-[1.45rem] font-semibold tracking-[-0.04em] text-[#243648]">
          Resource Discrepancy Analysis
        </h2>

        <div className="mt-6 space-y-5">
          <div>
            <div className="flex items-center justify-between gap-3 text-sm font-semibold text-[#243648]">
              <span>Cleaning Volume Required</span>
              <span className="text-[#2f6ed9]">{formatHours(discrepancy.requiredHours)}</span>
            </div>
            <div className="mt-2 rounded-full bg-[#edf1f5] p-1">
              <div
                className="h-2.5 rounded-full bg-[#2158d9]"
                style={{ width: normalizeBarWidth(discrepancy.requiredHours, baseline) }}
              />
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between gap-3 text-sm font-semibold text-[#243648]">
              <span>Current Roster Availability</span>
              <span className="text-[#15717a]">{formatHours(discrepancy.availableHours)}</span>
            </div>
            <div className="mt-2 rounded-full bg-[#edf1f5] p-1">
              <div
                className="h-2.5 rounded-full bg-[#15717a]"
                style={{ width: normalizeBarWidth(discrepancy.availableHours, baseline) }}
              />
            </div>
          </div>
        </div>
      </div>

      <div className="flex flex-col items-center justify-center text-center">
        <div className="flex h-16 w-16 items-center justify-center rounded-full bg-[#ffe2de] text-[#b32220]">
          <WarningIcon />
        </div>
        <h3 className="mt-5 text-2xl font-semibold tracking-[-0.04em] text-[#cc2f2a]">
          {isActionRequired ? 'Action Required' : 'Balanced Capacity'}
        </h3>
        <p className="mt-3 max-w-sm text-sm leading-6 text-[#6d7988]">
          {discrepancy.impactMessage}
        </p>
      </div>
    </section>
  )
}
