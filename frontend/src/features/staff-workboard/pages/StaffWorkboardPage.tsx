import { useMemo, useState } from "react";
import { toast } from "react-toastify";

import {
  useGetMyAssignedTasksQuery,
  useGetMyWorkloadQuery,
  useMarkTaskCompleteMutation,
} from "../api";
import { AssignedTasksList } from "../components/AssignedTasksList";
import { WorkboardHeader } from "../components/WorkboardHeader";
import { WorkloadSummaryCard } from "../components/WorkloadSummaryCard";
import { BottomNav } from "../../staff-portal/attendance/components/BottomNav";

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

export function StaffWorkboardPage() {
  const [markCompleteLoadingTaskId, setMarkCompleteLoadingTaskId] = useState<
    string | null
  >(null);
  const {
    data: tasksData,
    isLoading: tasksLoading,
    isFetching: tasksFetching,
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
  const [markTaskComplete] = useMarkTaskCompleteMutation();

  const errorMessage = useMemo(() => {
    if (tasksError) {
      return resolveErrorMessage(tasksErrorData);
    }

    if (workloadError) {
      return resolveErrorMessage(workloadErrorData);
    }

    return null;
  }, [tasksError, tasksErrorData, workloadError, workloadErrorData]);

  const handleMarkComplete = async (taskId: string) => {
    setMarkCompleteLoadingTaskId(taskId);

    try {
      const response = await markTaskComplete(taskId).unwrap();
      toast.success(response.message);
    } catch (error) {
      toast.error(resolveErrorMessage(error));
      refetchTasks();
      refetchWorkload();
    } finally {
      setMarkCompleteLoadingTaskId(null);
    }
  };

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
    <main className="min-h-screen bg-[#f4f3f8] pb-36 text-slate-700">
      <div className="mx-auto max-w-md px-4 py-5">
        <WorkboardHeader date={tasksData.date} />
        <WorkloadSummaryCard workload={workload} />

        {tasksFetching ? (
          <p className="mt-3 text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">
            Refreshing workboard...
          </p>
        ) : null}

        <AssignedTasksList
          tasks={tasksData.tasks}
          markCompleteLoadingTaskId={markCompleteLoadingTaskId}
          onMarkComplete={handleMarkComplete}
        />
      </div>

      <BottomNav activeTab="tasks" />
    </main>
  );
}
