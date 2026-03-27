import type { MyAssignedTaskItemResponse } from "../types";
import { TaskStatusBadge } from "./TaskStatusBadge";

interface AssignedTaskCardProps {
  task: MyAssignedTaskItemResponse;
  isCompleting: boolean;
  onMarkComplete: (taskId: string) => void;
}

function getTaskTypeLabel(taskType: MyAssignedTaskItemResponse["taskType"]) {
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

function formatCompletedAt(completedAt: string | null) {
  if (!completedAt) {
    return null;
  }

  return new Intl.DateTimeFormat("en-US", {
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(completedAt));
}

export function AssignedTaskCard({
  task,
  isCompleting,
  onMarkComplete,
}: AssignedTaskCardProps) {
  const completedAtLabel = formatCompletedAt(task.completedAt);
  const canComplete =
    task.taskStatus === "ASSIGNED" || task.taskStatus === "IN_PROGRESS";

  return (
    <article className="rounded-[1.6rem] bg-white px-4 py-4 shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
      <div className="flex items-start justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h3 className="text-[1.85rem] font-semibold leading-none text-slate-900">
              {task.roomNumber}
            </h3>
            <TaskStatusBadge status={task.taskStatus} />
          </div>

          <p className="mt-2 text-sm font-semibold text-slate-700">
            {getTaskTypeLabel(task.taskType)}
          </p>

          <p className="mt-2 text-xs uppercase tracking-[0.14em] text-slate-400">
            {task.estimatedMinutes} min
            {task.shiftName ? ` · ${task.shiftName}` : ""}
          </p>

          {completedAtLabel ? (
            <p className="mt-2 text-sm font-medium text-[#166534]">
              Completed at {completedAtLabel}
            </p>
          ) : null}
        </div>

        {canComplete ? (
          <button
            type="button"
            onClick={() => onMarkComplete(task.taskId)}
            disabled={isCompleting}
            className="rounded-full bg-[#1664c0] px-4 py-2 text-xs font-semibold uppercase tracking-[0.12em] text-white disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isCompleting ? "Saving..." : "Mark Complete"}
          </button>
        ) : null}
      </div>
    </article>
  );
}

