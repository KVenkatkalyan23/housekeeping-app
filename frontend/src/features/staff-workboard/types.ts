export type WorkboardTaskStatus =
  | "ASSIGNED"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "CANCELLED";

export type WorkboardTaskType = "DEEP_CLEAN" | "DAILY_CLEAN" | "VACANT_CLEAN";

export interface MyAssignedTaskItemResponse {
  taskId: string;
  roomId: string;
  roomNumber: number;
  taskType: WorkboardTaskType;
  taskStatus: WorkboardTaskStatus;
  estimatedMinutes: number;
  priorityOrder: number;
  shiftId: string | null;
  shiftCode: string | null;
  shiftName: string | null;
  completedAt: string | null;
  sourceStayId: string | null;
}

export interface MyAssignedTasksResponse {
  date: string;
  tasks: MyAssignedTaskItemResponse[];
}

export interface WorkloadSummaryResponse {
  assignedMinutes: number;
  completedMinutes: number;
  pendingMinutes: number;
  totalTaskCount: number;
  completedTaskCount: number;
  pendingTaskCount: number;
  completionPercentage: number;
}

export interface MarkTaskCompleteResponse {
  taskId: string;
  taskStatus: WorkboardTaskStatus;
  completedAt: string;
  message: string;
}
