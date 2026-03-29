import { createSlice, type PayloadAction } from '@reduxjs/toolkit'

interface AdminAttendanceLogsUiState {
  fromDate: string
  toDate: string
  page: number
  size: number
}

function toDateInputValue(date: Date) {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

function createInitialState(): AdminAttendanceLogsUiState {
  const endDate = new Date()
  const startDate = new Date(endDate)
  startDate.setDate(endDate.getDate() - 6)

  return {
    fromDate: toDateInputValue(startDate),
    toDate: toDateInputValue(endDate),
    page: 0,
    size: 5,
  }
}

const adminAttendanceLogsSlice = createSlice({
  name: 'adminAttendanceLogsUi',
  initialState: createInitialState(),
  reducers: {
    setAdminAttendanceFromDate: (state, action: PayloadAction<string>) => {
      state.fromDate = action.payload
      state.page = 0
    },
    setAdminAttendanceToDate: (state, action: PayloadAction<string>) => {
      state.toDate = action.payload
      state.page = 0
    },
    setAdminAttendancePage: (state, action: PayloadAction<number>) => {
      state.page = action.payload
    },
  },
})

export const {
  setAdminAttendanceFromDate,
  setAdminAttendancePage,
  setAdminAttendanceToDate,
} = adminAttendanceLogsSlice.actions
export const adminAttendanceLogsReducer = adminAttendanceLogsSlice.reducer
