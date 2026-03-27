import { useMemo } from "react";

import {
  useGetMyAssignedTasksQuery,
  useGetMyWorkloadQuery,
} from "../api";

function resolveErrorMessage(error: unknown) {
  if (!error || typeof error !== "object") {
    return "Unable to load your tasks.";
  }

  const candidate = error as {
    data?: { message?: string; error?: { message?: string } };
    error?: string;
  };

  return (
    candidate.data?.message ??
    candidate.data?.error?.message ??
    candidate.error ??
    "Unable to load your tasks."
  );
}

function formatDateLabel(date: string | undefined) {
  if (!date) {
    return "Today";
  }

  return new Intl.DateTimeFormat("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
  }).format(new Date(`${date}T00:00:00`));
}

function getTaskTypeLabel(taskType: string) {
  switch (taskType) {
    case "DEEP_CLEAN":
      return "Checkout Cleaning";
    case "DAILY_CLEAN":
      return "Daily Cleaning";
    case "VACANT_CLEAN":
      return "Vacant Cleaning";
    default:
      return taskType;
  }
}

function getStatusTone(status: string) {
  switch (status) {
    case "COMPLETED":
      return "bg-[#dcfce7] text-[#166534]";
    case "IN_PROGRESS":
      return "bg-[#dbeafe] text-[#1d4ed8]";
    case "ASSIGNED":
      return "bg-[#fef3c7] text-[#92400e]";
    default:
      return "bg-slate-100 text-slate-500";
  }
}

export function StaffWorkboardPage() {
  const {
    data: tasksData,
    isLoading: tasksLoading,
    isError: tasksError,
    error: tasksErrorData,
    refetch: refetchTasks,
  } = useGetMyAssignedTasksQuery();
  const {
    data: workload,
    isLoading: workloadLoading,
    isError: workloadError,
    error: workloadErrorData,
    refetch: refetchWorkload,
  } = useGetMyWorkloadQuery();

  const errorMessage = useMemo(() => {
    if (tasksError) {
      return resolveErrorMessage(tasksErrorData);
    }

    if (workloadError) {
      return resolveErrorMessage(workloadErrorData);
    }

    return null;
  }, [tasksError, tasksErrorData, workloadError, workloadErrorData]);

  if (tasksLoading || workloadLoading) {
    return (
      <main className="min-h-screen bg-[#f4f3f8] px-4 py-6 text-slate-700">
        <div className="mx-auto max-w-md animate-pulse space-y-4">
          <div className="h-16 rounded-[1.5rem] bg-white" />
          <div className="h-24 rounded-[1.75rem] bg-white" />
          <div className="h-28 rounded-[1.75rem] bg-white" />
          <div className="h-28 rounded-[1.75rem] bg-white" />
        </div>
      </main>
    );
  }

  if (errorMessage || !tasksData || !workload) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-[#f4f3f8] px-4">
        <div className="w-full max-w-sm rounded-[1.75rem] bg-white p-6 text-center shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
          <h1 className="text-xl font-semibold text-slate-900">
            Unable to load workboard
          </h1>
          <p className="mt-2 text-sm leading-6 text-slate-500">
            {errorMessage ?? "Unable to load your tasks."}
          </p>
          <button
            type="button"
            onClick={() => {
              refetchTasks();
              refetchWorkload();
            }}
            className="mt-5 h-11 rounded-full bg-[#1664c0] px-5 text-sm font-semibold text-white"
          >
            Retry
          </button>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-[#f4f3f8] pb-24 text-slate-700">
      <div className="mx-auto max-w-md px-4 py-5">
        <header className="rounded-[1.9rem] bg-white px-5 py-5 shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
          <p className="text-[0.68rem] font-bold uppercase tracking-[0.22em] text-[#7aa4e6]">
            Staff Workboard
          </p>
          <h1 className="mt-2 text-[2rem] font-semibold tracking-[-0.05em] text-slate-900">
            My Tasks
          </h1>
          <p className="mt-2 text-sm text-slate-500">
            Assigned work for {formatDateLabel(tasksData.date)}.
          </p>
        </header>

        <section className="mt-5 rounded-[1.9rem] bg-white p-5 shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
          <div className="flex items-end justify-between gap-4">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">
                Assigned Minutes
              </p>
              <p className="mt-2 text-[2.2rem] font-semibold tracking-[-0.06em] text-slate-900">
                {workload.assignedMinutes}
              </p>
            </div>
            <div className="rounded-full bg-[#e0ecff] px-4 py-2 text-sm font-semibold text-[#1664c0]">
              {workload.completionPercentage}% complete
            </div>
          </div>

          <div className="mt-5 grid grid-cols-2 gap-3 text-sm">
            <div className="rounded-[1.3rem] bg-[#f8fafc] px-4 py-3">
              <p className="text-slate-400">Completed</p>
              <p className="mt-1 font-semibold text-slate-800">
                {workload.completedMinutes} min · {workload.completedTaskCount} tasks
              </p>
            </div>
            <div className="rounded-[1.3rem] bg-[#f8fafc] px-4 py-3">
              <p className="text-slate-400">Pending</p>
              <p className="mt-1 font-semibold text-slate-800">
                {workload.pendingMinutes} min · {workload.pendingTaskCount} tasks
              </p>
            </div>
          </div>
        </section>

        {tasksData.tasks.length === 0 ? (
          <section className="mt-5 rounded-[1.75rem] bg-white p-6 text-center shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
            <h2 className="text-lg font-semibold text-slate-900">
              No tasks assigned for today
            </h2>
            <p className="mt-2 text-sm leading-6 text-slate-500">
              Your workboard will update automatically when new tasks are assigned.
            </p>
          </section>
        ) : (
          <section className="mt-5 space-y-3.5">
            {tasksData.tasks.map((task) => (
              <article
                key={task.taskId}
                className="rounded-[1.6rem] bg-white px-4 py-4 shadow-[0_14px_40px_rgba(15,23,42,0.08)]"
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="flex items-center gap-2">
                      <h2 className="text-[1.85rem] font-semibold leading-none text-slate-900">
                        {task.roomNumber}
                      </h2>
                      <span
                        className={`rounded-full px-2.5 py-1 text-[0.65rem] font-bold uppercase tracking-[0.16em] ${getStatusTone(
                          task.taskStatus
                        )}`}
                      >
                        {task.taskStatus}
                      </span>
                    </div>
                    <p className="mt-2 text-sm font-semibold text-slate-700">
                      {getTaskTypeLabel(task.taskType)}
                    </p>
                    <p className="mt-2 text-xs uppercase tracking-[0.14em] text-slate-400">
                      {task.estimatedMinutes} min
                      {task.shiftName ? ` · ${task.shiftName}` : ""}
                    </p>
                  </div>
                </div>
              </article>
            ))}
          </section>
        )}
      </div>
    </main>
  );
}
