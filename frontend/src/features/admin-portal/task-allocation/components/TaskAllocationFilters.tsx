import type { ChangeEvent } from 'react'

import type { AdminTaskStatusFilter, AdminTaskTypeFilter } from '../types'

const taskTypeOptions: Array<{ label: string; value: AdminTaskTypeFilter }> = [
  { label: 'All Task Types', value: 'ALL' },
  { label: 'Checkout', value: 'DEEP_CLEAN' },
  { label: 'Daily', value: 'DAILY_CLEAN' },
  { label: 'Vacant', value: 'VACANT_CLEAN' },
]

const statusOptions: Array<{ label: string; value: AdminTaskStatusFilter }> = [
  { label: 'All Statuses', value: 'ALL' },
  { label: 'Pending', value: 'PENDING' },
  { label: 'Assigned', value: 'ASSIGNED' },
  { label: 'In Progress', value: 'IN_PROGRESS' },
  { label: 'Completed', value: 'COMPLETED' },
  { label: 'Cancelled', value: 'CANCELLED' },
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

export function TaskAllocationFilters({
  search,
  taskType,
  status,
  loading,
  onSearchChange,
  onTaskTypeChange,
  onStatusChange,
  onReset,
}: {
  search: string
  taskType: AdminTaskTypeFilter
  status: AdminTaskStatusFilter
  loading: boolean
  onSearchChange: (value: string) => void
  onTaskTypeChange: (value: AdminTaskTypeFilter) => void
  onStatusChange: (value: AdminTaskStatusFilter) => void
  onReset: () => void
}) {
  return (
    <section className="rounded-[1.45rem] bg-white p-4 shadow-[0_18px_45px_rgba(25,39,52,0.06)]">
      <div className="flex flex-col gap-3 xl:flex-row xl:items-center">
        <div className="flex items-center gap-3 text-[0.72rem] font-bold uppercase tracking-[0.18em] text-[#687a8e]">
          <span>Filters:</span>
          <label className="flex h-11 min-w-0 flex-1 items-center gap-3 rounded-[0.95rem] border border-[#ecf1f5] bg-[#fbfcfd] px-3 text-[#98a6b5] xl:min-w-[300px]">
            <SearchIcon />
            <input
              value={search}
              disabled={loading}
              onChange={(event: ChangeEvent<HTMLInputElement>) =>
                onSearchChange(event.target.value)
              }
              placeholder="Search assigned staff..."
              className="h-full w-full bg-transparent text-sm font-medium text-[#223346] outline-none placeholder:text-[#a8b4c1]"
            />
          </label>
        </div>

        <div className="flex flex-1 flex-col gap-3 sm:flex-row sm:items-center sm:justify-end">
          <select
            value={taskType}
            disabled={loading}
            onChange={(event: ChangeEvent<HTMLSelectElement>) =>
              onTaskTypeChange(event.target.value as AdminTaskTypeFilter)
            }
            className="h-11 rounded-[0.95rem] border border-[#ecf1f5] bg-[#fbfcfd] px-4 text-sm font-medium text-[#4d5f73] outline-none"
          >
            {taskTypeOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>

          <select
            value={status}
            disabled={loading}
            onChange={(event: ChangeEvent<HTMLSelectElement>) =>
              onStatusChange(event.target.value as AdminTaskStatusFilter)
            }
            className="h-11 rounded-[0.95rem] border border-[#ecf1f5] bg-[#fbfcfd] px-4 text-sm font-medium text-[#4d5f73] outline-none"
          >
            {statusOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>

          <button
            type="button"
            onClick={onReset}
            disabled={loading}
            className="text-sm font-semibold text-[#15717a] transition hover:text-[#0e5561] disabled:opacity-60"
          >
            Reset
          </button>
        </div>
      </div>
    </section>
  )
}
