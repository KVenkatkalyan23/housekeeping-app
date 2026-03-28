import type { ChangeEvent } from 'react'

import type {
  AdminStaffDirectoryFilterStatus,
  StaffDirectorySummary,
} from '../types'

const filterTabs: Array<{
  label: string
  value: AdminStaffDirectoryFilterStatus
}> = [
  { label: 'All Personnel', value: 'ALL' },
  { label: 'On-Duty', value: 'ON_DUTY' },
  { label: 'Off-Duty', value: 'OFF_DUTY' },
  { label: 'Leave', value: 'LEAVE' },
  { label: 'Sick', value: 'SICK' },
]

function SearchIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" aria-hidden="true">
      <path
        d="M10.5 4a6.5 6.5 0 1 0 4.03 11.6l4.44 4.44 1.06-1.06-4.44-4.44A6.5 6.5 0 0 0 10.5 4Zm0 1.5a5 5 0 1 1 0 10 5 5 0 0 1 0-10Z"
        fill="currentColor"
      />
    </svg>
  )
}

export function StaffDirectoryFilters({
  search,
  status,
  summary,
  loading,
  onSearchChange,
  onStatusChange,
}: {
  search: string
  status: AdminStaffDirectoryFilterStatus
  summary?: StaffDirectorySummary
  loading: boolean
  onSearchChange: (value: string) => void
  onStatusChange: (value: AdminStaffDirectoryFilterStatus) => void
}) {
  if (loading && !summary) {
    return (
      <section className="rounded-[1.75rem] bg-white p-3 shadow-[0_18px_45px_rgba(25,39,52,0.06)]">
        <div className="flex flex-col gap-3 xl:flex-row xl:items-center">
          <div className="h-14 animate-pulse rounded-[1.1rem] bg-[#eef2f6] xl:flex-1" />
          <div className="flex flex-col gap-3 xl:flex-1">
            <div className="flex flex-wrap gap-2">
              {Array.from({ length: 5 }).map((_, index) => (
                <div
                  key={index}
                  className="h-10 w-24 animate-pulse rounded-full bg-[#eef2f6]"
                />
              ))}
            </div>
            <div className="h-4 w-32 animate-pulse self-start rounded bg-[#eef2f6] xl:self-end" />
          </div>
        </div>
      </section>
    )
  }

  return (
    <section className="rounded-[1.75rem] bg-white p-3 shadow-[0_18px_45px_rgba(25,39,52,0.06)]">
      <div className="flex flex-col gap-3 xl:flex-row xl:items-center">
        <label className="flex h-14 w-full items-center gap-3 rounded-[1.1rem] border border-[#eff2f5] bg-[#fbfcfd] px-4 text-[#99a6b5] xl:flex-1">
          {SearchIcon()}
          <input
            value={search}
            onChange={(event: ChangeEvent<HTMLInputElement>) =>
              onSearchChange(event.target.value)
            }
            placeholder="Search staff by name..."
            className="h-full w-full bg-transparent text-sm font-medium text-[#223346] outline-none placeholder:text-[#a8b4c1]"
          />
        </label>

        <div className="flex flex-col gap-3 xl:flex-1 xl:flex-row xl:items-center xl:justify-between">
          <div className="flex flex-wrap items-center gap-2">
            {filterTabs.map((tab) => {
              const isActive = status === tab.value

              return (
                <button
                  key={tab.value}
                  type="button"
                  onClick={() => onStatusChange(tab.value)}
                  className={[
                    'rounded-full px-4 py-2.5 text-sm font-semibold transition sm:px-5',
                    isActive
                      ? 'bg-[#119c98] text-white shadow-[0_12px_24px_rgba(17,156,152,0.24)]'
                      : 'text-[#536273] hover:bg-[#f4f7fa]',
                  ].join(' ')}
                >
                  {tab.label}
                </button>
              )
            })}
          </div>

          <div className="text-left xl:text-right">
            <p className="text-sm font-semibold text-[#526274]">
              {loading && !summary
                ? 'Loading employees...'
                : `${summary?.totalEmployees ?? 0} total employees`}
            </p>
          </div>
        </div>
      </div>
    </section>
  )
}
