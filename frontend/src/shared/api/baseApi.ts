import { createApi } from '@reduxjs/toolkit/query/react'

import { baseQueryWithAuth } from './client'

export const baseApi = createApi({
  reducerPath: 'baseApi',
  baseQuery: baseQueryWithAuth,
  tagTypes: ['Attendance', 'StaffWorkboard'],
  endpoints: () => ({}),
})
