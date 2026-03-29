import type { AdminAllocatedTaskItem } from '../types'
import { TaskPriorityBadge } from './TaskPriorityBadge'
import { TaskStatusBadge } from './TaskStatusBadge'

function getTaskTypeLabel(taskType: AdminAllocatedTaskItem['taskType']) {
  switch (taskType) {
    case 'DEEP_CLEAN':
      return 'Checkout'
    case 'DAILY_CLEAN':
      return 'Daily'
    case 'VACANT_CLEAN':
      return 'Vacant'
  }
}

function getTaskTypeTone(taskType: AdminAllocatedTaskItem['taskType']) {
  switch (taskType) {
    case 'DEEP_CLEAN':
      return 'bg-[#eef0f2] text-[#7b838b]'
    case 'DAILY_CLEAN':
      return 'bg-[#eef6f2] text-[#5f8374]'
    case 'VACANT_CLEAN':
      return 'bg-[#f4f3eb] text-[#8a805c]'
  }
}

function getAccent(taskType: AdminAllocatedTaskItem['taskType']) {
  switch (taskType) {
    case 'DEEP_CLEAN':
      return 'bg-[#3156df]'
    case 'DAILY_CLEAN':
      return 'bg-[#f0f2f4]'
    case 'VACANT_CLEAN':
      return 'bg-[#0d8c7d]'
  }
}

function getInitial(name: string | null) {
  return name?.trim().charAt(0).toUpperCase() || 'U'
}

function canReassignTask(item: AdminAllocatedTaskItem) {
  return (
    Boolean(item.assignedStaffId) &&
    item.status !== 'COMPLETED' &&
    item.status !== 'CANCELLED'
  )
}

export function TaskAllocationRow({
  item,
  onReassign,
}: {
  item: AdminAllocatedTaskItem
  onReassign: (item: AdminAllocatedTaskItem) => void
}) {
  const reassignable = canReassignTask(item)

  return (
    <tr className="border-t border-[#edf1f4]">
      <td className="px-4 py-5">
        <div className="flex items-start gap-3">
          <span className={`mt-1 h-7 w-1 rounded-full ${getAccent(item.taskType)}`} />
          <div>
            <p className="text-[1.05rem] font-semibold tracking-[-0.04em] text-[#243648]">
              Room {item.roomNumber}
            </p>
            <p className="mt-1 text-xs leading-5 text-[#7b8897]">
              {[item.floorLabel, item.roomTypeLabel].filter(Boolean).join(' - ') ||
                'Active room task'}
            </p>
          </div>
        </div>
      </td>
      <td className="px-4 py-5">
        <span
          className={`inline-flex rounded-full px-3 py-1.5 text-xs font-semibold ${getTaskTypeTone(item.taskType)}`}
        >
          {getTaskTypeLabel(item.taskType)}
        </span>
      </td>
      <td className="px-4 py-5">
        {item.assignedStaffName ? (
          <div className="flex items-center gap-3">
            <span className="flex h-9 w-9 items-center justify-center rounded-full bg-[linear-gradient(135deg,#17395c,#f0b468)] text-xs font-semibold text-white">
              {getInitial(item.assignedStaffName)}
            </span>
            <div>
              <p className="text-sm font-semibold text-[#243648]">
                {item.assignedStaffName}
              </p>
              <p className="text-xs text-[#7b8897]">
                {item.shiftName ?? 'Shift pending'}
              </p>
            </div>
          </div>
        ) : (
          <span className="text-sm font-medium text-[#9aa5b2]">Unassigned</span>
        )}
      </td>
      <td className="px-4 py-5">
        <TaskStatusBadge status={item.status} />
      </td>
      <td className="px-4 py-5">
        <TaskPriorityBadge priority={item.priorityLabel} />
      </td>
      <td className="px-4 py-5 text-right">
        {reassignable ? (
          <button
            type="button"
            onClick={() => onReassign(item)}
            className="rounded-full bg-[#0b8b7d] px-4 py-2 text-sm font-semibold text-white shadow-[0_12px_24px_rgba(11,139,125,0.18)] transition hover:bg-[#0a7b70]"
          >
            Reassign
          </button>
        ) : (
          <span className="inline-flex rounded-full border border-[#edf1f5] bg-white px-3 py-2 text-xs font-semibold uppercase tracking-[0.16em] text-[#9aa5b2]">
            Unavailable
          </span>
        )}
      </td>
    </tr>
  )
}
