import { useSelector } from 'react-redux'
import { Navigate } from 'react-router-dom'

import { getDefaultRouteForRole } from '../../features/auth/routing'
import type { RootState } from '../store'

export function RoleHomeRedirect() {
  const role = useSelector((state: RootState) => state.auth.role)
  return <Navigate to={getDefaultRouteForRole(role)} replace />
}
