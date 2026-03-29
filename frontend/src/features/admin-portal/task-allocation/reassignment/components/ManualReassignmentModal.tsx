import { useEffect, useState } from 'react'
import { toast } from 'react-toastify'

import {
  useGetTaskReassignmentCandidatesQuery,
  useReassignTaskMutation,
} from '../../api'
import type {
  AdminAllocatedTaskItem,
  ManualTaskReassignmentResponse,
} from '../../types'
import { ReassignmentStaffSelect } from './ReassignmentStaffSelect'

function resolveErrorMessage(error: unknown) {
  if (!error || typeof error !== 'object') {
    return 'Unable to complete reassignment.'
  }

  const candidate = error as {
    data?: { message?: string; error?: { message?: string } }
    error?: string
  }

  return (
    candidate.data?.message ??
    candidate.data?.error?.message ??
    candidate.error ??
    'Unable to complete reassignment.'
  )
}

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

export function ManualReassignmentModal({
  task,
  open,
  onClose,
  onReassigned,
}: {
  task: AdminAllocatedTaskItem | null
  open: boolean
  onClose: () => void
  onReassigned: (response: ManualTaskReassignmentResponse) => void
}) {
  const [selectedTargetStaffId, setSelectedTargetStaffId] = useState('')
  const [formError, setFormError] = useState<string | null>(null)
  const candidatesQuery = useGetTaskReassignmentCandidatesQuery(
    { taskId: task?.taskId ?? '' },
    { skip: !open || !task },
  )
  const [reassignTask, reassignTaskState] = useReassignTaskMutation()

  useEffect(() => {
    if (!open) {
      setSelectedTargetStaffId('')
      setFormError(null)
      return
    }

    setSelectedTargetStaffId('')
    setFormError(null)
  }, [open, task?.taskId])

  if (!open || !task) {
    return null
  }

  const handleSubmit = async () => {
    if (!selectedTargetStaffId) {
      setFormError('Select a staff member before confirming reassignment.')
      return
    }

    setFormError(null)

    try {
      const response = await reassignTask({
        taskId: task.taskId,
        targetStaffId: selectedTargetStaffId,
      }).unwrap()
      toast.success(response.successMessage)
      onReassigned(response)
      onClose()
    } catch (error) {
      const message = resolveErrorMessage(error)
      setFormError(message)
      toast.error(message)
    }
  }

  const selectedCandidate = candidatesQuery.data?.find(
    (candidate) => candidate.staffId === selectedTargetStaffId,
  )

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[rgba(21,32,43,0.30)] px-4 py-6 backdrop-blur-[2px]">
      <div className="w-full max-w-[32rem] overflow-hidden rounded-[1.6rem] bg-white shadow-[0_24px_70px_rgba(18,30,44,0.22)]">
        <div className="flex items-center justify-between border-b border-[#edf2f4] px-6 py-5">
          <h2 className="text-lg font-semibold tracking-[-0.03em] text-[#243648]">
            Manual Task Reassignment
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="h-9 w-9 rounded-full text-sm font-semibold uppercase text-[#7d8b99] transition hover:bg-[#f3f6f8]"
            aria-label="Close manual reassignment modal"
          >
            Close
          </button>
        </div>

        <div className="space-y-5 px-6 py-5">
          <div className="rounded-[1.2rem] border border-[#e3eeef] bg-[#fbfefd] p-4">
            <p className="text-[0.68rem] font-bold uppercase tracking-[0.18em] text-[#0f9a8c]">
              Task Details
            </p>
            <div className="mt-3 flex items-start justify-between gap-4">
              <div>
                <p className="text-lg font-semibold tracking-[-0.03em] text-[#243648]">
                  Room {task.roomNumber}
                </p>
                <p className="mt-1 text-xs uppercase tracking-[0.16em] text-[#8d9aa7]">
                  {[task.floorLabel, task.roomTypeLabel]
                    .filter(Boolean)
                    .join(' - ') || 'Active room task'}
                </p>
              </div>
              <span className="rounded-full bg-[#eef3f6] px-3 py-1.5 text-xs font-semibold text-[#6f7e89]">
                {getTaskTypeLabel(task.taskType)}
              </span>
            </div>
            <div className="mt-4 rounded-2xl bg-[#f5f8fa] px-4 py-3 text-sm text-[#5f7183]">
              <p>
                Current assignee:{' '}
                <span className="font-semibold text-[#243648]">
                  {task.assignedStaffName ?? 'Unassigned'}
                </span>
              </p>
              <p className="mt-1">
                Shift:{' '}
                <span className="font-semibold text-[#243648]">
                  {task.shiftName ?? 'Not set'}
                </span>
              </p>
            </div>
          </div>

          <ReassignmentStaffSelect
            candidates={candidatesQuery.data ?? []}
            value={selectedTargetStaffId}
            disabled={candidatesQuery.isLoading || reassignTaskState.isLoading}
            onChange={(value) => {
              setSelectedTargetStaffId(value)
              setFormError(null)
            }}
          />

          {candidatesQuery.isLoading ? (
            <div className="rounded-2xl bg-[#f3f8f9] px-4 py-3 text-sm text-[#607385]">
              Loading available staff...
            </div>
          ) : null}

          {candidatesQuery.isError ? (
            <div className="rounded-2xl border border-[#f0d6d2] bg-[#fff5f4] px-4 py-3 text-sm text-[#b24f4a]">
              {resolveErrorMessage(candidatesQuery.error)}
            </div>
          ) : null}

          {!candidatesQuery.isLoading &&
          !candidatesQuery.isError &&
          (candidatesQuery.data?.length ?? 0) === 0 ? (
            <div className="rounded-2xl bg-[#f3f8f9] px-4 py-3 text-sm text-[#607385]">
              No staff with enough remaining daily capacity are available for
              this task.
            </div>
          ) : null}

          {selectedCandidate ? (
            <div className="rounded-2xl border border-[#d5f0ea] bg-[#eefaf7] px-4 py-3 text-sm text-[#1a7d72]">
              Capacity revalidated: Selected staff member has sufficient
              remaining hours for today.
            </div>
          ) : null}

          {formError ? (
            <div className="rounded-2xl border border-[#f0d6d2] bg-[#fff5f4] px-4 py-3 text-sm text-[#b24f4a]">
              {formError}
            </div>
          ) : null}

          <div className="rounded-2xl bg-[#fafcfd] px-4 py-3 text-xs text-[#6d7d8c]">
            This reassignment will be tagged as manual and recorded in the task
            audit log.
          </div>
        </div>

        <div className="flex items-center justify-end gap-3 border-t border-[#edf2f4] px-6 py-4">
          <button
            type="button"
            onClick={onClose}
            disabled={reassignTaskState.isLoading}
            className="rounded-full px-4 py-2.5 text-sm font-semibold text-[#728393] transition hover:bg-[#f4f7f9] disabled:cursor-not-allowed disabled:opacity-60"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={
              reassignTaskState.isLoading ||
              candidatesQuery.isLoading ||
              candidatesQuery.isError ||
              (candidatesQuery.data?.length ?? 0) === 0
            }
            className="rounded-full bg-[#0b8b7d] px-5 py-2.5 text-sm font-semibold text-white shadow-[0_12px_24px_rgba(11,139,125,0.22)] transition hover:bg-[#0a7a6e] disabled:cursor-not-allowed disabled:bg-[#9ccfc8] disabled:shadow-none"
          >
            {reassignTaskState.isLoading
              ? 'Reassigning...'
              : 'Confirm Reassignment'}
          </button>
        </div>
      </div>
    </div>
  )
}
