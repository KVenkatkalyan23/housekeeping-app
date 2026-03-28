import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'

interface DashboardSidebarProps {
  onLogout: () => void
}

interface NavigationItem {
  label: string
  path?: string
  icon: (className?: string) => ReactNode
}

function DashboardIcon(className = 'h-4 w-4') {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className} aria-hidden="true">
      <path d="M4 4h7v7H4V4Zm9 0h7v5h-7V4ZM4 13h7v7H4v-7Zm9-2h7v9h-7v-9Z" fill="currentColor" />
    </svg>
  )
}

function RoomsIcon(className = 'h-4 w-4') {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className} aria-hidden="true">
      <path d="M5 8h14v11H5V8Zm2-3h10v3H7V5Zm2 8h2v2H9v-2Zm4 0h2v2h-2v-2Z" fill="currentColor" />
    </svg>
  )
}

function StaffIcon(className = 'h-4 w-4') {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className} aria-hidden="true">
      <path
        d="M12 12a3 3 0 1 0 0-6 3 3 0 0 0 0 6Zm-6 7a5 5 0 0 1 12 0H6Zm12-8a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Zm1.8 8a4 4 0 0 0-2.17-3.57A5.8 5.8 0 0 1 20 19h-.2Z"
        fill="currentColor"
      />
    </svg>
  )
}

function TasksIcon(className = 'h-4 w-4') {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className} aria-hidden="true">
      <path d="M8 6h11v2H8V6Zm0 5h11v2H8v-2Zm0 5h11v2H8v-2ZM5 7.5 6.5 9 9 5.5 7.8 4.7 6.4 6.7 5.9 6.2 5 7.1Zm0 5 1.5 1.5L9 10.5l-1.2-.8-1.4 2-.5-.5-.9.8Zm0 5 1.5 1.5L9 15.5l-1.2-.8-1.4 2-.5-.5-.9.8Z" fill="currentColor" />
    </svg>
  )
}

function AttendanceIcon(className = 'h-4 w-4') {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className} aria-hidden="true">
      <path d="M7 3h2v2h6V3h2v2h3v16H4V5h3V3Zm11 7H6v9h12v-9Zm-6 2h2v3h3v2h-5v-5Z" fill="currentColor" />
    </svg>
  )
}

function LogsIcon(className = 'h-4 w-4') {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className} aria-hidden="true">
      <path d="M12 6V3L8 7l4 4V8a4 4 0 1 1-4 4H6a6 6 0 1 0 6-6Z" fill="currentColor" />
    </svg>
  )
}

function LeaveIcon(className = 'h-4 w-4') {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className} aria-hidden="true">
      <path d="M7 3h10v3H7V3Zm11 5v13H6V8h12Zm-8 3H8v2h2v-2Zm0 4H8v2h2v-2Zm6-4h-4v6h4v-6Z" fill="currentColor" />
    </svg>
  )
}

function HelpIcon(className = 'h-4 w-4') {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className} aria-hidden="true">
      <path
        d="M12 4a8 8 0 1 0 0 16 8 8 0 0 0 0-16Zm0 11.5a1 1 0 1 1 0 2 1 1 0 0 1 0-2Zm1.7-3.6-.8.55c-.53.37-.9.8-.9 1.55V14h-2v-.5c0-1.16.55-2.1 1.45-2.73l1.1-.77c.42-.3.65-.73.65-1.2 0-.86-.69-1.55-1.55-1.55-.85 0-1.55.69-1.55 1.55H8a3.55 3.55 0 1 1 7.1 0c0 1.11-.53 2.14-1.4 2.74Z"
        fill="currentColor"
      />
    </svg>
  )
}

function LogoutIcon(className = 'h-4 w-4') {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className} aria-hidden="true">
      <path
        d="M10 4H5v16h5v-2H7V6h3V4Zm6.59 4.59L18 10l-4 4-1.41-1.41L14.17 11H9V9h5.17l-1.58-1.59L14 6l4 4-1.41 1.41Z"
        fill="currentColor"
      />
    </svg>
  )
}

const navigationItems: NavigationItem[] = [
  { label: 'Dashboard', path: '/admin/dashboard', icon: DashboardIcon },
  { label: 'Rooms', icon: RoomsIcon },
  { label: 'Staff', icon: StaffIcon },
  { label: 'Tasks', icon: TasksIcon },
  { label: 'Attendance', icon: AttendanceIcon },
  { label: 'Logs', icon: LogsIcon },
  { label: 'Leave', icon: LeaveIcon },
]

export function DashboardSidebar({ onLogout }: DashboardSidebarProps) {
  return (
    <aside className="w-full border-b border-[#e4ebf3] bg-[#f5f9fe] px-4 py-5 text-[#6b7a90] xl:min-h-screen xl:max-w-[238px] xl:border-b-0 xl:border-r xl:px-5 xl:py-7">
      <div className="flex items-start justify-between gap-4 xl:block">
        <div>
          <p className="text-[1.2rem] font-semibold tracking-[-0.04em] text-[#15717a] xl:text-[1.4rem]">
            Linen &amp; Logic
          </p>
          <p className="mt-1 text-[0.62rem] font-semibold uppercase tracking-[0.22em] text-[#94a7bc]">
            Operational Hub
          </p>
        </div>

        <div className="hidden items-center gap-2 sm:flex xl:hidden">
          <button
            type="button"
            className="flex items-center gap-2 rounded-xl px-3 py-2 text-sm font-semibold text-[#7a8a9f]"
          >
            {HelpIcon()}
            <span>Help</span>
          </button>
          <button
            type="button"
            onClick={onLogout}
            className="flex items-center gap-2 rounded-xl bg-white px-3 py-2 text-sm font-semibold text-[#7a8a9f]"
          >
            {LogoutIcon()}
            <span>Logout</span>
          </button>
        </div>
      </div>

      <nav className="mt-5 grid grid-cols-2 gap-2 sm:grid-cols-4 xl:mt-8 xl:grid-cols-1 xl:space-y-1.5 xl:gap-0">
        {navigationItems.map((item) =>
          item.path ? (
            <NavLink
              key={item.label}
              to={item.path}
              className={({ isActive }) =>
                [
                  'flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition',
                  isActive
                    ? 'bg-white text-[#15717a] shadow-[0_14px_35px_rgba(34,74,116,0.08)]'
                    : 'text-[#71829a] hover:bg-white/80 hover:text-[#23364d]',
                ].join(' ')
              }
            >
              {item.icon()}
              <span>{item.label}</span>
            </NavLink>
          ) : (
            <button
              key={item.label}
              type="button"
              className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold text-[#93a1b3]"
            >
              {item.icon()}
              <span>{item.label}</span>
            </button>
          ),
        )}
      </nav>

      <div className="mt-5 hidden space-y-2 xl:block xl:pt-6">
        <button
          type="button"
          className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold text-[#7a8a9f] hover:bg-white/80"
        >
          {HelpIcon()}
          <span>Help Center</span>
        </button>
        <button
          type="button"
          onClick={onLogout}
          className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold text-[#7a8a9f] hover:bg-white/80"
        >
          {LogoutIcon()}
          <span>Logout</span>
        </button>
      </div>
    </aside>
  )
}
