import { baseApi } from "../../shared/api/baseApi";
import type {
  MarkTaskCompleteResponse,
  MyAssignedTasksResponse,
  WorkloadSummaryResponse,
} from "./types";

export const staffWorkboardApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getMyAssignedTasks: builder.query<MyAssignedTasksResponse, void>({
      query: () => ({
        url: "/staff/tasks/today",
        method: "GET",
      }),
      providesTags: ["StaffWorkboard"],
    }),
    getMyWorkload: builder.query<WorkloadSummaryResponse, void>({
      query: () => ({
        url: "/staff/tasks/workload",
        method: "GET",
      }),
      providesTags: ["StaffWorkboard"],
    }),
    markTaskComplete: builder.mutation<MarkTaskCompleteResponse, string>({
      query: (taskId) => ({
        url: `/staff/tasks/${taskId}/complete`,
        method: "POST",
      }),
      invalidatesTags: ["StaffWorkboard"],
    }),
  }),
});

export const {
  useGetMyAssignedTasksQuery,
  useGetMyWorkloadQuery,
  useMarkTaskCompleteMutation,
} = staffWorkboardApi;
