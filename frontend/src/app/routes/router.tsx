import { useSelector } from 'react-redux'
import { createBrowserRouter, Navigate } from 'react-router-dom'

import { ErrorPage } from './ErrorPage'
import type { RootState } from '../store'
import { HomePage } from '../../features/dashboard/pages/HomePage'
import { LoginPage } from '../../features/auth/pages/LoginPage'
import { ProtectedRoute } from '../../features/auth/components/ProtectedRoute'
import { getDefaultRouteForRole } from '../../features/auth/routing'
import { AttendanceHistoryPage } from '../../features/attendance/pages/AttendanceHistoryPage'
import { StaffAttendancePage } from '../../features/attendance/pages/StaffAttendancePage'
import { LeaveHistoryPage } from '../../features/leave/pages/LeaveHistoryPage'
import { ProfilePage } from '../../features/profile/pages/ProfilePage'

function RoleHomeRedirect() {
  const role = useSelector((state: RootState) => state.auth.role)
  return <Navigate to={getDefaultRouteForRole(role)} replace />
}

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
    errorElement: <ErrorPage />,
  },
  {
    path: '/',
    element: <ProtectedRoute />,
    errorElement: <ErrorPage />,
    children: [
      {
        index: true,
        element: <RoleHomeRedirect />,
      },
    ],
  },
  {
    path: '/admin',
    element: <ProtectedRoute allowedRoles={['ADMIN']} />,
    errorElement: <ErrorPage />,
    children: [
      {
        index: true,
        element: <HomePage />,
      },
    ],
  },
  {
    path: '/staff',
    element: <ProtectedRoute allowedRoles={['STAFF']} />,
    errorElement: <ErrorPage />,
    children: [
      {
        index: true,
        element: <RoleHomeRedirect />,
      },
      {
        path: 'attendance',
        element: <StaffAttendancePage />,
      },
      {
        path: 'attendance/history',
        element: <AttendanceHistoryPage />,
      },
      {
        path: 'leave',
        element: <LeaveHistoryPage />,
      },
      {
        path: 'profile',
        element: <ProfilePage />,
      },
    ],
  },
])
