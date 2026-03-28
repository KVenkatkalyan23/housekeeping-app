import { createSlice, type PayloadAction } from '@reduxjs/toolkit'

import type { AdminTaskStatusFilter, AdminTaskTypeFilter } from './types'

interface AdminTaskAllocationUiState {
  page: number
  size: number
  search: string
  taskType: AdminTaskTypeFilter
  status: AdminTaskStatusFilter
}

const initialState: AdminTaskAllocationUiState = {
  page: 0,
  size: 4,
  search: '',
  taskType: 'ALL',
  status: 'ALL',
}

const adminTaskAllocationSlice = createSlice({
  name: 'adminTaskAllocationUi',
  initialState,
  reducers: {
    setTaskAllocationPage: (state, action: PayloadAction<number>) => {
      state.page = action.payload
    },
    setTaskAllocationSize: (state, action: PayloadAction<number>) => {
      state.size = action.payload
      state.page = 0
    },
    setTaskAllocationSearch: (state, action: PayloadAction<string>) => {
      state.search = action.payload
      state.page = 0
    },
    setTaskAllocationTaskType: (
      state,
      action: PayloadAction<AdminTaskTypeFilter>,
    ) => {
      state.taskType = action.payload
      state.page = 0
    },
    setTaskAllocationStatus: (
      state,
      action: PayloadAction<AdminTaskStatusFilter>,
    ) => {
      state.status = action.payload
      state.page = 0
    },
    resetTaskAllocationFilters: () => initialState,
  },
})

export const {
  resetTaskAllocationFilters,
  setTaskAllocationPage,
  setTaskAllocationSearch,
  setTaskAllocationSize,
  setTaskAllocationStatus,
  setTaskAllocationTaskType,
} = adminTaskAllocationSlice.actions
export const adminTaskAllocationReducer = adminTaskAllocationSlice.reducer
