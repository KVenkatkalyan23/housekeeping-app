import type { AdminAllocatedTaskItem } from '../types'
import { TaskAllocationRow } from './TaskAllocationRow'

function TableSkeleton() {
  return (
    <div className="space-y-3 p-4">
      {Array.from({ length: 4 }).map((_, index) => (
        <div key={index} className="h-20 animate-pulse rounded-[1rem] bg-[#f3f6f8]" />
      ))}
    </div>
  )
}

export function TaskAllocationTable({
  items,
  isLoading,
  isError,
  errorMessage,
  onRetry,
  onReassign,
}: {
  items: AdminAllocatedTaskItem[]
  isLoading: boolean
  isError: boolean
  errorMessage: string
  onRetry: () => void
  onReassign: (item: AdminAllocatedTaskItem) => void
}) {
  return (
    <section className="overflow-hidden rounded-[1.45rem] bg-white shadow-[0_18px_45px_rgba(25,39,52,0.06)]">
      <div className="overflow-x-auto">
        <table className="min-w-full border-separate border-spacing-0">
          <thead>
            <tr className="bg-[#f8fafc] text-left">
              {['Room Number', 'Task Type', 'Assigned Staff', 'Status', 'Priority', 'Action'].map(
                (label) => (
                  <th
                    key={label}
                    className="px-4 py-4 text-[0.68rem] font-bold uppercase tracking-[0.18em] text-[#6b7b8d]"
                  >
                    {label}
                  </th>
                ),
              )}
            </tr>
          </thead>
          <tbody>
            {isLoading && items.length === 0 ? (
              <tr>
                <td colSpan={6}>
                  <TableSkeleton />
                </td>
              </tr>
            ) : null}

            {isError && items.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center">
                  <p className="text-sm text-[#a34a47]">{errorMessage}</p>
                  <button
                    type="button"
                    onClick={onRetry}
                    className="mt-4 rounded-full bg-[#145f78] px-4 py-2 text-sm font-semibold text-white"
                  >
                    Retry
                  </button>
                </td>
              </tr>
            ) : null}

            {!isLoading && !isError && items.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-10 text-center">
                  <p className="text-lg font-semibold text-[#243648]">No tasks found</p>
                  <p className="mt-2 text-sm text-[#758394]">
                    Adjust the current filters or clear the search to see more tasks.
                  </p>
                </td>
              </tr>
            ) : null}

            {items.map((item) => (
              <TaskAllocationRow
                key={item.taskId}
                item={item}
                onReassign={onReassign}
              />
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}
