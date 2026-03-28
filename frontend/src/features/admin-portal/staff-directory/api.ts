import { baseApi } from '../../../shared/api/baseApi'
import type {
  AdminStaffDirectoryFilterStatus,
  AdminStaffDirectoryResponse,
} from './types'

export interface GetAdminStaffDirectoryParams {
  page: number
  size: number
  search?: string
  status: AdminStaffDirectoryFilterStatus
}

export const adminStaffDirectoryApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getAdminStaffDirectory: builder.query<
      AdminStaffDirectoryResponse,
      GetAdminStaffDirectoryParams
    >({
      query: ({ page, size, search, status }) => ({
        url: '/admin/staff',
        method: 'GET',
        params: {
          page,
          size,
          status,
          ...(search ? { search } : {}),
        },
      }),
      providesTags: ['AdminStaffDirectory'],
    }),
  }),
})

export const { useGetAdminStaffDirectoryQuery } = adminStaffDirectoryApi
