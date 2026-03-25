import { createBrowserRouter } from 'react-router-dom'

import { ErrorPage } from './ErrorPage'
import { HomePage } from '../../features/dashboard/pages/HomePage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <HomePage />,
    errorElement: <ErrorPage />,
  },
])
