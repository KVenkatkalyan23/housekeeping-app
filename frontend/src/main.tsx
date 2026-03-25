import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { Provider } from 'react-redux'
import 'react-toastify/dist/ReactToastify.css'
import './index.css'
import App from './App.tsx'
import { store } from './app/store'
import { hydrateFromStorage, readPersistedAuthState } from './features/auth/slice'

store.dispatch(hydrateFromStorage(readPersistedAuthState()))

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Provider store={store}>
      <App />
    </Provider>
  </StrictMode>,
)
