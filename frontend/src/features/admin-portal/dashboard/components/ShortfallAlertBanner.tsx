import type { AdminDashboardShortfallAlert } from '../types'

interface ShortfallAlertBannerProps {
  shortfallAlert: AdminDashboardShortfallAlert
}

function SupportIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5" aria-hidden="true">
      <path
        d="M10.5 11a3 3 0 1 0 0-6 3 3 0 0 0 0 6ZM5 18a5.5 5.5 0 0 1 11 0H5Zm12-6a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Zm1.9 6a4.5 4.5 0 0 0-2.32-3.93A5.96 5.96 0 0 1 20.5 18H18.9Z"
        fill="currentColor"
      />
    </svg>
  )
}

export function ShortfallAlertBanner({
  shortfallAlert,
}: ShortfallAlertBannerProps) {
  const isCritical = shortfallAlert.isCritical

  return (
    <section
      className={[
        'flex flex-col gap-6 rounded-[1.5rem] px-6 py-6 text-white shadow-[0_22px_50px_rgba(31,41,55,0.12)] xl:flex-row xl:items-center xl:justify-between',
        isCritical
          ? 'bg-[linear-gradient(135deg,#c91f1f,#ef4b3a)]'
          : 'bg-[linear-gradient(135deg,#15717a,#2aa39b)]',
      ].join(' ')}
    >
      <div>
        <span className="inline-flex rounded-full bg-white/15 px-3 py-1 text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-white/85">
          {isCritical ? 'Critical Alert' : 'System Healthy'}
        </span>
        <h2 className="mt-4 text-[2.15rem] font-semibold tracking-[-0.05em]">
          {isCritical
            ? `${shortfallAlert.shortfallPercent}% Staffing Shortfall`
            : 'Team Capacity Stable'}
        </h2>
        <p className="mt-2 max-w-2xl text-sm leading-6 text-white/88">
          {shortfallAlert.shortfallMessage}
        </p>
      </div>

      <button
        type="button"
        className="inline-flex h-16 items-center justify-center gap-3 self-start rounded-full bg-white px-6 text-sm font-semibold text-[#d3332f] shadow-[0_16px_30px_rgba(17,24,39,0.15)]"
      >
        <SupportIcon />
        <span>Request Support</span>
      </button>
    </section>
  )
}
