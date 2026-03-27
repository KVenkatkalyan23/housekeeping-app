import { baseApi } from "../../../shared/api/baseApi";
import type {
  AssignedTaskItem,
  AttendanceStatusResponse,
  ClockInResponse,
  ClockOutResponse,
  MarkTaskCompleteResponse,
  WorkloadSummaryResponse,
  WeeklyAttendanceHistoryResponse,
} from "./types";

export const attendanceApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getCurrentAttendance: builder.query<AttendanceStatusResponse, void>({
      query: () => ({
        url: "/attendance/current",
        method: "GET",
      }),
      providesTags: ["Attendance"],
    }),
    clockIn: builder.mutation<ClockInResponse, void>({
      query: () => ({
        url: "/attendance/clock-in",
        method: "POST",
      }),
      invalidatesTags: ["Attendance"],
    }),
    clockOut: builder.mutation<ClockOutResponse, void>({
      query: () => ({
        url: "/attendance/clock-out",
        method: "POST",
      }),
      invalidatesTags: ["Attendance"],
    }),
    getWeeklyAttendanceHistory: builder.query<
      WeeklyAttendanceHistoryResponse,
      { weekStart?: string; page?: number; size?: number } | void
    >({
      query: (params) => ({
        url: "/attendance/weekly-history",
        method: "GET",
        params: {
          ...(params?.weekStart ? { weekStart: params.weekStart } : {}),
          ...(typeof params?.page === "number" ? { page: params.page } : {}),
          ...(typeof params?.size === "number" ? { size: params.size } : {}),
        },
      }),
      providesTags: ["Attendance"],
    }),
    getTodayAssignedTasks: builder.query<{ date: string; tasks: AssignedTaskItem[] }, void>({
      query: () => ({
        url: "/staff/tasks/today",
        method: "GET",
      }),
      providesTags: ["StaffWorkboard"],
    }),
    getTodayWorkload: builder.query<WorkloadSummaryResponse, void>({
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
  useClockInMutation,
  useClockOutMutation,
  useGetCurrentAttendanceQuery,
  useGetTodayAssignedTasksQuery,
  useGetTodayWorkloadQuery,
  useGetWeeklyAttendanceHistoryQuery,
  useMarkTaskCompleteMutation,
} = attendanceApi;
