import { createSlice, type PayloadAction } from '@reduxjs/toolkit'

import type { AuthState, LoginResponse } from './types'

const AUTH_STORAGE_KEY = 'housekeeping.auth'

const unauthenticatedState: AuthState = {
  accessToken: null,
  tokenType: null,
  userId: null,
  username: null,
  role: null,
  isAuthenticated: false,
}

function isValidAuthState(value: unknown): value is AuthState {
  if (!value || typeof value !== 'object') {
    return false
  }

  const candidate = value as Partial<AuthState>

  return (
    typeof candidate.accessToken === 'string' &&
    typeof candidate.tokenType === 'string' &&
    typeof candidate.userId === 'string' &&
    typeof candidate.username === 'string' &&
    (candidate.role === 'ADMIN' || candidate.role === 'STAFF') &&
    typeof candidate.isAuthenticated === 'boolean'
  )
}

export function persistAuthState(state: AuthState) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(state))
}

export function clearPersistedAuthState() {
  localStorage.removeItem(AUTH_STORAGE_KEY)
}

export function readPersistedAuthState(): AuthState | null {
  const storedValue = localStorage.getItem(AUTH_STORAGE_KEY)

  if (!storedValue) {
    return null
  }

  try {
    const parsedValue = JSON.parse(storedValue) as unknown

    if (isValidAuthState(parsedValue) && parsedValue.isAuthenticated) {
      return parsedValue
    }
  } catch {
    clearPersistedAuthState()
  }

  return null
}

export function createAuthStateFromLoginResponse(
  payload: LoginResponse,
): AuthState {
  return {
    accessToken: payload.accessToken,
    tokenType: payload.tokenType,
    userId: payload.userId,
    username: payload.username,
    role: payload.role,
    isAuthenticated: true,
  }
}

const authSlice = createSlice({
  name: 'auth',
  initialState: unauthenticatedState,
  reducers: {
    loginSuccess: (_, action: PayloadAction<LoginResponse>) => {
      return createAuthStateFromLoginResponse(action.payload)
    },
    hydrateFromStorage: (_, action: PayloadAction<AuthState | null>) => {
      return action.payload ?? unauthenticatedState
    },
    logout: () => unauthenticatedState,
  },
})

export const { hydrateFromStorage, loginSuccess, logout } = authSlice.actions
export const authReducer = authSlice.reducer
