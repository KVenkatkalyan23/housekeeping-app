import { createSlice, type PayloadAction } from '@reduxjs/toolkit'

import type { AuditLogCategoryFilter } from './types'

interface AdminAuditLogsUiState {
  page: number
  size: number
  category: AuditLogCategoryFilter
}

const initialState: AdminAuditLogsUiState = {
  page: 0,
  size: 10,
  category: 'ALL',
}

const adminAuditLogsSlice = createSlice({
  name: 'adminAuditLogsUi',
  initialState,
  reducers: {
    setAdminAuditLogsPage: (state, action: PayloadAction<number>) => {
      state.page = action.payload
    },
    setAdminAuditLogsCategory: (
      state,
      action: PayloadAction<AuditLogCategoryFilter>,
    ) => {
      state.category = action.payload
      state.page = 0
    },
  },
})

export const { setAdminAuditLogsCategory, setAdminAuditLogsPage } =
  adminAuditLogsSlice.actions
export const adminAuditLogsReducer = adminAuditLogsSlice.reducer
