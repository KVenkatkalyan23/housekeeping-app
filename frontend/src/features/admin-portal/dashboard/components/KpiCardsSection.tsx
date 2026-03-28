import type {
  AdminDashboardInventoryStatus,
  AdminDashboardSlaPerformance,
  AdminDashboardWorkforceEfficiency,
} from '../types'
import { KpiCard } from './KpiCard'

interface KpiCardsSectionProps {
  inventoryStatus: AdminDashboardInventoryStatus
  workforceEfficiency: AdminDashboardWorkforceEfficiency
  slaPerformance: AdminDashboardSlaPerformance
}

function formatPercentage(value: number) {
  return `${Math.round(value)}%`
}

function InventoryIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5" aria-hidden="true">
      <path d="M5 7h14l-1 10H6L5 7Zm3-3h8l1 3H7l1-3Z" fill="currentColor" />
    </svg>
  )
}

function EfficiencyIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5" aria-hidden="true">
      <path d="M5 18h2v-4H5v4Zm4 0h2V9H9v9Zm4 0h2v-6h-2v6Zm4 0h2V6h-2v12Zm-1.3-9.3-1.4-1.4-3.3 3.3-2-2-4.6 4.6 1.4 1.4 3.2-3.2 2 2 4.7-4.7Z" fill="currentColor" />
    </svg>
  )
}

function SlaIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5" aria-hidden="true">
      <path
        d="M12 4a8 8 0 1 0 8 8 8.01 8.01 0 0 0-8-8Zm-1 11-3-3 1.4-1.4 1.6 1.6 3.6-3.6L16 10l-5 5Z"
        fill="currentColor"
      />
    </svg>
  )
}

function clampPercentage(value: number) {
  return Math.max(0, Math.min(100, Math.round(value)))
}

export function KpiCardsSection({
  inventoryStatus,
  workforceEfficiency,
  slaPerformance,
}: KpiCardsSectionProps) {
  const utilizationWidth = clampPercentage(workforceEfficiency.utilizationPercent)

  return (
    <section className="grid gap-5 xl:grid-cols-3">
      <KpiCard
        title="Inventory Status"
        value={inventoryStatus.totalRooms.toLocaleString()}
        subtitle="Total Registered Rooms"
        accentClassName="border-[#d5e4ff] shadow-[inset_3px_0_0_0_#2967d5,0_18px_45px_rgba(25,39,52,0.06)]"
        icon={<InventoryIcon />}
      >
        <div className="grid grid-cols-2 gap-4 border-t border-[#eef2f6] pt-4">
          <div>
            <p className="text-[0.68rem] font-bold uppercase tracking-[0.14em] text-[#9aa8b8]">
              Occupied
            </p>
            <p className="mt-1 text-xl font-semibold text-[#2967d5]">
              {inventoryStatus.occupiedRooms.toLocaleString()}
            </p>
          </div>
          <div>
            <p className="text-[0.68rem] font-bold uppercase tracking-[0.14em] text-[#9aa8b8]">
              Vacant
            </p>
            <p className="mt-1 text-xl font-semibold text-[#7c9ecb]">
              {inventoryStatus.vacantRooms.toLocaleString()}
            </p>
          </div>
        </div>
        <div className="mt-4 flex items-center justify-between text-sm text-[#6d7988]">
          <span>Occupancy Rate</span>
          <span className="rounded-full bg-[#edf3ff] px-2.5 py-1 font-semibold text-[#425dc5]">
            {formatPercentage(inventoryStatus.occupancyRate)}
          </span>
        </div>
      </KpiCard>

      <KpiCard
        title="Workforce Efficiency"
        value={formatPercentage(workforceEfficiency.utilizationPercent)}
        subtitle="Active Staff Utilization"
        accentClassName="border-[#d7efe9] shadow-[inset_3px_0_0_0_#15717a,0_18px_45px_rgba(25,39,52,0.06)]"
        icon={<EfficiencyIcon />}
      >
        <div className="rounded-full bg-[#edf3f2] p-1">
          <div
            className="h-2.5 rounded-full bg-[linear-gradient(90deg,#15717a,#33b2a6)]"
            style={{ width: `${utilizationWidth}%` }}
          />
        </div>
        <p className="mt-4 text-sm leading-6 text-[#6d7988]">
          {workforceEfficiency.description}
        </p>
      </KpiCard>

      <KpiCard
        title="SLA Performance"
        value={formatPercentage(slaPerformance.completionRate)}
        subtitle="Task Completion Rate"
        accentClassName="border-[#f2e2cf] shadow-[inset_3px_0_0_0_#bc7a2f,0_18px_45px_rgba(25,39,52,0.06)]"
        icon={<SlaIcon />}
      >
        <div className="flex items-end justify-between gap-4 border-t border-[#eef2f6] pt-4">
          <div>
            <p className="text-sm font-semibold text-[#2a8b87]">
              {slaPerformance.deltaVsYesterday >= 0 ? '+' : ''}
              {slaPerformance.deltaVsYesterday.toFixed(1)}% vs Yesterday
            </p>
            <p className="mt-2 text-sm leading-6 text-[#6d7988]">
              {slaPerformance.completedTasks} tasks completed out of{' '}
              {slaPerformance.totalAssignedTasks} assigned today.
            </p>
          </div>
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-[#ffe0be] text-[#7a4f1d]">
            <SlaIcon />
          </div>
        </div>
      </KpiCard>
    </section>
  )
}
