import { configureStore } from '@reduxjs/toolkit'

import { adminDashboardReducer } from '../../features/admin-portal/dashboard/slice'
import { adminStaffDirectoryReducer } from '../../features/admin-portal/staff-directory/slice'
import { authReducer } from '../../features/auth/slice'
import { leaveReducer } from '../../features/staff-portal/leave/slice'
import { baseApi } from '../../shared/api/baseApi'

export const store = configureStore({
  reducer: {
    adminDashboardUi: adminDashboardReducer,
    adminStaffDirectoryUi: adminStaffDirectoryReducer,
    auth: authReducer,
    leaveUi: leaveReducer,
    [baseApi.reducerPath]: baseApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(baseApi.middleware),
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
