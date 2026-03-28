import { baseApi } from '../../../shared/api/baseApi'
import type {
  AdminAttendanceLogListResponse,
  AdminAttendanceSummaryResponse,
} from './types'

export interface AdminAttendanceRangeParams {
  fromDate: string
  toDate: string
}

export interface GetAdminAttendanceLogsParams extends AdminAttendanceRangeParams {
  page: number
  size: number
  search?: string
}

export const adminAttendanceApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getAdminAttendanceSummary: builder.query<
      AdminAttendanceSummaryResponse,
      AdminAttendanceRangeParams
    >({
      query: ({ fromDate, toDate }) => ({
        url: '/admin/attendance/summary',
        method: 'GET',
        params: { fromDate, toDate },
      }),
      providesTags: ['AdminAttendance'],
    }),
    getAdminAttendanceLogs: builder.query<
      AdminAttendanceLogListResponse,
      GetAdminAttendanceLogsParams
    >({
      query: ({ fromDate, toDate, page, size, search }) => ({
        url: '/admin/attendance/logs',
        method: 'GET',
        params: {
          fromDate,
          toDate,
          page,
          size,
          ...(search ? { search } : {}),
        },
      }),
      providesTags: ['AdminAttendance'],
    }),
  }),
})

export const {
  useGetAdminAttendanceLogsQuery,
  useGetAdminAttendanceSummaryQuery,
} = adminAttendanceApi
