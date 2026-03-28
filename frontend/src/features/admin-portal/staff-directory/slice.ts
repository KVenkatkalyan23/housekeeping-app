import { createSlice, type PayloadAction } from '@reduxjs/toolkit'

import type { AdminStaffDirectoryFilterStatus } from './types'

interface AdminStaffDirectoryUiState {
  page: number
  size: number
  search: string
  status: AdminStaffDirectoryFilterStatus
}

const initialState: AdminStaffDirectoryUiState = {
  page: 0,
  size: 10,
  search: '',
  status: 'ALL',
}

const adminStaffDirectorySlice = createSlice({
  name: 'adminStaffDirectoryUi',
  initialState,
  reducers: {
    setDirectoryPage: (state, action: PayloadAction<number>) => {
      state.page = action.payload
    },
    setDirectorySize: (state, action: PayloadAction<number>) => {
      state.size = action.payload
      state.page = 0
    },
    setDirectorySearch: (state, action: PayloadAction<string>) => {
      state.search = action.payload
      state.page = 0
    },
    setDirectoryStatus: (
      state,
      action: PayloadAction<AdminStaffDirectoryFilterStatus>,
    ) => {
      state.status = action.payload
      state.page = 0
    },
  },
})

export const {
  setDirectoryPage,
  setDirectorySearch,
  setDirectorySize,
  setDirectoryStatus,
} = adminStaffDirectorySlice.actions
export const adminStaffDirectoryReducer = adminStaffDirectorySlice.reducer
