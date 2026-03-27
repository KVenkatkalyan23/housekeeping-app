import { baseApi } from '../../../shared/api/baseApi'
import type {
  AdminLeavesResponse,
  ApplyLeaveRequest,
  LeaveListItem,
  MyLeavesResponse,
} from './types'

export const leaveApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    applyLeave: builder.mutation<LeaveListItem, ApplyLeaveRequest>({
      query: (body) => ({
        url: '/leave/apply',
        method: 'POST',
        body,
      }),
      invalidatesTags: ['Leave', 'StaffWorkboard', 'Attendance'],
    }),
    getMyLeaves: builder.query<
      MyLeavesResponse,
      { userId: string; page?: number; size?: number }
    >({
      query: ({ userId, page = 0, size = 5 }) => ({
        url: '/leave/my',
        method: 'GET',
        params: { userId, page, size },
      }),
      providesTags: ['Leave'],
    }),
    getAdminLeaves: builder.query<AdminLeavesResponse, { page?: number; size?: number } | void>({
      query: (params) => ({
        url: '/leave/admin',
        method: 'GET',
        params: {
          ...(typeof params?.page === 'number' ? { page: params.page } : {}),
          ...(typeof params?.size === 'number' ? { size: params.size } : {}),
        },
      }),
      providesTags: ['Leave'],
    }),
  }),
})

export const {
  useApplyLeaveMutation,
  useGetAdminLeavesQuery,
  useGetMyLeavesQuery,
} = leaveApi
