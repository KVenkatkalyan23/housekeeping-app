import { RouterProvider } from 'react-router-dom'
import { ToastContainer } from 'react-toastify'

import { router } from './app/routes/router'

function App() {
  return (
    <>
      <RouterProvider router={router} />
      <ToastContainer position="top-right" autoClose={3000} />
    </>
  )
}

export default App
