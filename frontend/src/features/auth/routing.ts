import type { Role } from './types'

export function getDefaultRouteForRole(role: Role | null) {
  if (role === 'STAFF') {
    return '/staff/attendance'
  }

  return '/admin'
}
