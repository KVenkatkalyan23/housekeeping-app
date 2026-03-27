import { createSlice, type PayloadAction } from '@reduxjs/toolkit'

interface LeaveUiState {
  myPage: number
  adminPage: number
}

const initialState: LeaveUiState = {
  myPage: 0,
  adminPage: 0,
}

const leaveSlice = createSlice({
  name: 'leaveUi',
  initialState,
  reducers: {
    setMyLeavePage: (state, action: PayloadAction<number>) => {
      state.myPage = Math.max(action.payload, 0)
    },
    setAdminLeavePage: (state, action: PayloadAction<number>) => {
      state.adminPage = Math.max(action.payload, 0)
    },
    resetLeavePages: () => initialState,
  },
})

export const { resetLeavePages, setAdminLeavePage, setMyLeavePage } = leaveSlice.actions
export const leaveReducer = leaveSlice.reducer
