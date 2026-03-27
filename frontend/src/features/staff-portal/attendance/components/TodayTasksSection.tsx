import { toast } from 'react-toastify'

import {
  useGetTodayAssignedTasksQuery,
  useGetTodayWorkloadQuery,
  useMarkTaskCompleteMutation,
} from '../api'
import type { AssignedTaskItem, StaffTaskStatus, StaffTaskType } from '../types'

function resolveErrorMessage(error: unknown) {
  if (!error || typeof error !== 'object') {
    return "Unable to load today's tasks."
  }

  const candidate = error as {
    data?: { message?: string; error?: { message?: string } }
    error?: string
  }

  return (
    candidate.data?.message ??
    candidate.data?.error?.message ??
    candidate.error ??
    "Unable to load today's tasks."
  )
}

function formatTaskType(taskType: StaffTaskType) {
  if (taskType === 'DEEP_CLEAN') {
    return 'Checkout Cleaning'
  }

  if (taskType === 'VACANT_CLEAN') {
    return 'Vacant Cleaning'
  }

  return 'Daily Cleaning'
}

function formatStatus(status: StaffTaskStatus) {
  if (status === 'COMPLETED') {
    return 'DONE'
  }

  if (status === 'IN_PROGRESS') {
    return 'IN PROGRESS'
  }

  return status
}

function formatCompletedAt(value: string | null) {
  if (!value) {
    return ''
  }

  return `Completed ${new Intl.DateTimeFormat('en-US', {
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(value))}`
}

function TaskCard({
  task,
  isSubmitting,
  onMarkComplete,
}: {
  task: AssignedTaskItem
  isSubmitting: boolean
  onMarkComplete: (taskId: string) => void
}) {
  const isDone = task.taskStatus === 'COMPLETED'
  const canComplete =
    task.taskStatus === 'ASSIGNED' || task.taskStatus === 'IN_PROGRESS'

  return (
    <article
      className={`rounded-[1.5rem] bg-white px-4 py-4 shadow-[0_14px_40px_rgba(15,23,42,0.08)] ${
        isDone ? 'opacity-45' : ''
      }`}
    >
      <div className="flex items-start justify-between gap-4">
        <div className="flex gap-3">
          <span className="mt-0.5 h-[4.5rem] w-1 rounded-full bg-[#1664c0]" />
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-[1.8rem] font-semibold leading-none text-slate-800">
                {task.roomNumber}
              </h3>
              <span className="rounded-full bg-[#dfeeff] px-2 py-0.5 text-[0.58rem] font-bold tracking-[0.16em] text-[#5a86ce]">
                {formatStatus(task.taskStatus)}
              </span>
            </div>
            <p className="mt-1.5 text-sm font-semibold text-slate-600">
              {formatTaskType(task.taskType)}
            </p>
            <div className="mt-3 flex flex-wrap items-center gap-3 text-[0.72rem] font-medium text-slate-400">
              <span>{task.estimatedMinutes} min</span>
              {task.shiftName ? <span>{task.shiftName}</span> : null}
              {task.completedAt ? <span>{formatCompletedAt(task.completedAt)}</span> : null}
            </div>
          </div>
        </div>

        {canComplete ? (
          <button
            type="button"
            onClick={() => onMarkComplete(task.taskId)}
            disabled={isSubmitting}
            className="rounded-full border border-[#cfe1fb] bg-white px-3 py-2 text-[0.65rem] font-semibold uppercase tracking-[0.08em] text-[#6ea4dc] disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isSubmitting ? 'Saving' : 'Done'}
          </button>
        ) : (
          <div
            className={`flex h-8 w-8 items-center justify-center rounded-full border ${
              isDone
                ? 'border-[#6ea4dc] bg-[#6ea4dc] text-white'
                : 'border-[#cfe1fb] bg-white text-[#6ea4dc]'
            }`}
          >
            <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4">
              <path
                d="M6.5 12.5 10 16l7.5-8"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </div>
        )}
      </div>
    </article>
  )
}

export function TodayTasksSection() {
  const { data, isLoading, isError, error, refetch } = useGetTodayAssignedTasksQuery()
  const { data: workload } = useGetTodayWorkloadQuery()
  const [markTaskComplete, { isLoading: isMarkingComplete, originalArgs }] =
    useMarkTaskCompleteMutation()

  const handleMarkComplete = async (taskId: string) => {
    try {
      await markTaskComplete(taskId).unwrap()
      toast.success('Task marked complete.')
    } catch (mutationError) {
      toast.error(resolveErrorMessage(mutationError))
    }
  }

  if (isLoading) {
    return (
      <section className="mt-7">
        <div className="h-40 animate-pulse rounded-[1.5rem] bg-white shadow-[0_14px_40px_rgba(15,23,42,0.08)]" />
      </section>
    )
  }

  if (isError || !data) {
    return (
      <section className="mt-7 rounded-[1.5rem] bg-white p-6 text-center shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
        <h2 className="text-lg font-semibold text-slate-900">Unable to load tasks</h2>
        <p className="mt-2 text-sm leading-6 text-slate-500">{resolveErrorMessage(error)}</p>
        <button
          type="button"
          onClick={() => refetch()}
          className="mt-4 rounded-full bg-[#1664c0] px-4 py-2 text-sm font-semibold text-white"
        >
          Retry
        </button>
      </section>
    )
  }

  const pendingCount =
    workload?.pendingTaskCount ??
    data.tasks.filter((task) => task.taskStatus !== 'COMPLETED').length

  return (
    <section className="mt-7">
      <div className="flex items-center justify-between">
        <h2 className="text-[1.7rem] font-semibold tracking-[-0.04em] text-slate-900">
          Today&apos;s Tasks
        </h2>
        <span className="text-right text-sm font-semibold text-[#4c78c8]">
          {workload ? `${workload.assignedMinutes} min` : `${pendingCount} Left`}
        </span>
      </div>

      {workload ? (
        <div className="mt-3 flex flex-wrap items-center gap-3 text-[0.72rem] font-medium text-slate-400">
          <span>{workload.completedTaskCount} completed</span>
          <span>{workload.pendingTaskCount} pending</span>
          <span>
            {workload.completedMinutes} / {workload.assignedMinutes} min done
          </span>
        </div>
      ) : null}

      {data.tasks.length > 0 ? (
        <div className="mt-4 space-y-3.5">
          {data.tasks.map((task) => (
            <TaskCard
              key={task.taskId}
              task={task}
              isSubmitting={isMarkingComplete && originalArgs === task.taskId}
              onMarkComplete={handleMarkComplete}
            />
          ))}
        </div>
      ) : (
        <section className="mt-4 rounded-[1.5rem] bg-white px-4 py-6 text-center shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
          <h3 className="text-lg font-semibold text-slate-900">No tasks assigned</h3>
          <p className="mt-2 text-sm text-slate-500">You have no assigned rooms for today.</p>
        </section>
      )}
    </section>
  )
}
