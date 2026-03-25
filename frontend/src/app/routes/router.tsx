import { createBrowserRouter } from 'react-router-dom'

import { ErrorPage } from './ErrorPage'
import { HomePage } from '../../features/dashboard/pages/HomePage'
import { LoginPage } from '../../features/auth/pages/LoginPage'
import { ProtectedRoute } from '../../features/auth/components/ProtectedRoute'

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
        element: <HomePage />,
      },
    ],
  },
])
