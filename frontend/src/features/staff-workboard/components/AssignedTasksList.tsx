import type { MyAssignedTaskItemResponse } from "../types";
import { AssignedTaskCard } from "./AssignedTaskCard";

interface AssignedTasksListProps {
  tasks: MyAssignedTaskItemResponse[];
  markCompleteLoadingTaskId: string | null;
  onMarkComplete: (taskId: string) => void;
}

export function AssignedTasksList({
  tasks,
  markCompleteLoadingTaskId,
  onMarkComplete,
}: AssignedTasksListProps) {
  if (tasks.length === 0) {
    return (
      <section className="mt-5 rounded-[1.75rem] bg-white p-6 text-center shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
        <h2 className="text-lg font-semibold text-slate-900">
          No tasks assigned for today
        </h2>
        <p className="mt-2 text-sm leading-6 text-slate-500">
          Your workboard will update automatically when new tasks are assigned.
        </p>
      </section>
    );
  }

  return (
    <section className="mt-5 space-y-3.5">
      {tasks.map((task) => (
        <AssignedTaskCard
          key={task.taskId}
          task={task}
          isCompleting={markCompleteLoadingTaskId === task.taskId}
          onMarkComplete={onMarkComplete}
        />
      ))}
    </section>
  );
}

