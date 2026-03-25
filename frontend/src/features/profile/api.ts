import { baseApi } from '../../shared/api/baseApi'
import type { StaffProfileResponse } from './types'

export const profileApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getCurrentStaffProfile: builder.query<StaffProfileResponse, void>({
      query: () => ({
        url: '/staff/profile',
        method: 'GET',
      }),
    }),
  }),
})

export const { useGetCurrentStaffProfileQuery } = profileApi
