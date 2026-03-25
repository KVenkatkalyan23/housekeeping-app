export type Role = 'ADMIN' | 'STAFF'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  userId: string
  username: string
  role: Role
}

export interface AuthState {
  accessToken: string | null
  tokenType: string | null
  userId: string | null
  username: string | null
  role: Role | null
  isAuthenticated: boolean
}
