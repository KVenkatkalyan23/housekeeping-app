import type { AdminDashboardCurrentAdmin } from '../types'

interface DashboardHeaderProps {
  currentAdmin: AdminDashboardCurrentAdmin
  lastUpdatedAt: string | null
}

function BellIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5" aria-hidden="true">
      <path
        d="M12 4a4 4 0 0 1 4 4v1.53c0 .8.32 1.56.88 2.12L18 12.77V14H6v-1.23l1.12-1.12c.56-.56.88-1.32.88-2.12V8a4 4 0 0 1 4-4Zm0 16a2.5 2.5 0 0 0 2.45-2h-4.9A2.5 2.5 0 0 0 12 20Zm6-6v-1.23l-.54-.54A5 5 0 0 1 16 8V8a4 4 0 1 0-8 0v.23a5 5 0 0 1-1.46 3.54l-.54.54V14h12Z"
        fill="currentColor"
      />
      <circle cx="18.2" cy="6.3" r="2" fill="#d3332f" />
    </svg>
  )
}

function getAdminInitial(name: string) {
  return name.trim().charAt(0).toUpperCase() || 'A'
}

function formatUpdatedLabel(lastUpdatedAt: string | null) {
  if (!lastUpdatedAt) {
    return 'Awaiting live sync'
  }

  const differenceInMinutes = Math.max(
    0,
    Math.round((Date.now() - new Date(lastUpdatedAt).getTime()) / 60000),
  )

  if (differenceInMinutes < 1) {
    return 'Updated just now'
  }

  if (differenceInMinutes === 1) {
    return 'Updated 1 minute ago'
  }

  return `Updated ${differenceInMinutes} minutes ago`
}

export function DashboardHeader({
  currentAdmin,
  lastUpdatedAt,
}: DashboardHeaderProps) {
  return (
    <header className="flex flex-col gap-4 border-b border-[#edf1f5] px-6 py-5 md:flex-row md:items-center md:justify-end">
      <div className="mr-auto">
        <h1 className="text-[1.8rem] font-semibold tracking-[-0.04em] text-[#243648]">
          Admin Dashboard
        </h1>
        <p className="mt-1 text-sm text-[#7a8797]">
          {formatUpdatedLabel(lastUpdatedAt)}
        </p>
      </div>

      <button
        type="button"
        className="inline-flex h-11 w-11 items-center justify-center rounded-full border border-[#edf1f5] bg-white text-[#5c6b7b] shadow-[0_14px_35px_rgba(25,39,52,0.05)]"
        aria-label="Notifications"
      >
        <BellIcon />
      </button>

      <div className="flex items-center gap-3 rounded-full bg-white px-3 py-2 shadow-[0_14px_35px_rgba(25,39,52,0.05)]">
        <div className="text-right">
          <p className="text-sm font-semibold text-[#243648]">
            {currentAdmin.displayName}
          </p>
          <p className="text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-[#71a3a2]">
            {currentAdmin.roleLabel}
          </p>
        </div>
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[linear-gradient(135deg,#0f5f78,#f0b468)] text-sm font-semibold text-white">
          {getAdminInitial(currentAdmin.displayName)}
        </div>
      </div>
    </header>
  )
}
