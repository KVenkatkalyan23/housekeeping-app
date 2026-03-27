import { configureStore } from '@reduxjs/toolkit'

import { authReducer } from '../../features/auth/slice'
import { leaveReducer } from '../../features/staff-portal/leave/slice'
import { baseApi } from '../../shared/api/baseApi'

export const store = configureStore({
  reducer: {
    auth: authReducer,
    leaveUi: leaveReducer,
    [baseApi.reducerPath]: baseApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(baseApi.middleware),
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
