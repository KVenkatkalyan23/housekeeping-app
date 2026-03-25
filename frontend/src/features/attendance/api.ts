import { baseApi } from '../../shared/api/baseApi'
import type {
  AttendanceStatusResponse,
  ClockInResponse,
  ClockOutResponse,
  WeeklyAttendanceHistoryResponse,
} from './types'

export const attendanceApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getCurrentAttendance: builder.query<AttendanceStatusResponse, void>({
      query: () => ({
        url: '/attendance/current',
        method: 'GET',
      }),
      providesTags: ['Attendance'],
    }),
    clockIn: builder.mutation<ClockInResponse, void>({
      query: () => ({
        url: '/attendance/clock-in',
        method: 'POST',
      }),
      invalidatesTags: ['Attendance'],
    }),
    clockOut: builder.mutation<ClockOutResponse, void>({
      query: () => ({
        url: '/attendance/clock-out',
        method: 'POST',
      }),
      invalidatesTags: ['Attendance'],
    }),
    getWeeklyAttendanceHistory: builder.query<
      WeeklyAttendanceHistoryResponse,
      { weekStart?: string } | void
    >({
      query: (params) => ({
        url: '/attendance/weekly-history',
        method: 'GET',
        params: params?.weekStart ? { weekStart: params.weekStart } : undefined,
      }),
      providesTags: ['Attendance'],
    }),
  }),
})

export const {
  useClockInMutation,
  useClockOutMutation,
  useGetCurrentAttendanceQuery,
  useGetWeeklyAttendanceHistoryQuery,
} = attendanceApi
