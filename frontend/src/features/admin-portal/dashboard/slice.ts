import { createSlice, type PayloadAction } from '@reduxjs/toolkit'

interface AdminDashboardUiState {
  lastUpdatedAt: string | null
}

const initialState: AdminDashboardUiState = {
  lastUpdatedAt: null,
}

const adminDashboardSlice = createSlice({
  name: 'adminDashboardUi',
  initialState,
  reducers: {
    clearDashboardTimestamp: () => initialState,
    setDashboardTimestamp: (state, action: PayloadAction<string>) => {
      state.lastUpdatedAt = action.payload
    },
  },
})

export const { clearDashboardTimestamp, setDashboardTimestamp } =
  adminDashboardSlice.actions
export const adminDashboardReducer = adminDashboardSlice.reducer
