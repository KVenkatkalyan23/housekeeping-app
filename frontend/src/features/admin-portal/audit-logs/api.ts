import { baseApi } from '../../../shared/api/baseApi'
import type { AdminAuditLogsResponse, AuditLogCategoryFilter } from './types'

export interface GetAdminAuditLogsParams {
  page: number
  size: number
  category: AuditLogCategoryFilter
}

export const adminAuditLogsApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getAdminAuditLogs: builder.query<AdminAuditLogsResponse, GetAdminAuditLogsParams>({
      query: ({ page, size, category }) => ({
        url: '/admin/logs',
        method: 'GET',
        params: {
          page,
          size,
          ...(category !== 'ALL' ? { category } : {}),
        },
      }),
      providesTags: ['AdminAuditLogs'],
    }),
  }),
})

export const { useGetAdminAuditLogsQuery } = adminAuditLogsApi
