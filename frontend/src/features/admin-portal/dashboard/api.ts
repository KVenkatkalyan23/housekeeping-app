import { baseApi } from '../../../shared/api/baseApi'
import type { AdminDashboardData } from './types'

export const adminDashboardApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getAdminDashboardData: builder.query<AdminDashboardData, void>({
      query: () => ({
        url: '/admin/dashboard',
        method: 'GET',
      }),
      providesTags: ['AdminDashboard'],
    }),
  }),
})

export const { useGetAdminDashboardDataQuery } = adminDashboardApi
