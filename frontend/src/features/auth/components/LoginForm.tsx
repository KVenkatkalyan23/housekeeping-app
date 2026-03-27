import { type FormEvent, useMemo, useState } from 'react'
import { useDispatch } from 'react-redux'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { toast } from 'react-toastify'

import type { AppDispatch } from '../../../app/store'
import { baseApi } from '../../../shared/api/baseApi'
import { getDefaultRouteForRole } from '../routing'
import { createAuthStateFromLoginResponse, loginSuccess, persistAuthState } from '../slice'
import { useLoginMutation } from '../api'

interface ValidationErrors {
  username?: string
  password?: string
}

function UserIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5 text-slate-400">
      <path
        d="M12 12a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7Z"
        stroke="currentColor"
        strokeWidth="1.8"
      />
      <path
        d="M5.5 19a6.5 6.5 0 0 1 13 0"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function LockIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5 text-slate-400">
      <path
        d="M8 10V7.75A4 4 0 0 1 12 4a4 4 0 0 1 4 3.75V10"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
      <path
        d="M6.5 10h11A1.5 1.5 0 0 1 19 11.5v7a1.5 1.5 0 0 1-1.5 1.5h-11A1.5 1.5 0 0 1 5 18.5v-7A1.5 1.5 0 0 1 6.5 10Z"
        stroke="currentColor"
        strokeWidth="1.8"
      />
    </svg>
  )
}

function resolveErrorMessage(error: unknown) {
  if (!error || typeof error !== 'object') {
    return 'Unable to sign in. Please try again.'
  }

  const candidate = error as {
    data?: { message?: string; error?: string | { message?: string } }
    error?: string
  }

  const nestedErrorMessage =
    typeof candidate.data?.error === 'object'
      ? candidate.data.error?.message
      : undefined
  const directErrorMessage =
    typeof candidate.data?.error === 'string' ? candidate.data.error : undefined

  return (
    candidate.data?.message ??
    nestedErrorMessage ??
    directErrorMessage ??
    candidate.error ??
    'Unable to sign in. Please try again.'
  )
}

export function LoginForm() {
  const dispatch = useDispatch<AppDispatch>()
  const navigate = useNavigate()
  const location = useLocation()
  const [login, { isLoading }] = useLoginMutation()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [validationErrors, setValidationErrors] = useState<ValidationErrors>({})
  const [submitError, setSubmitError] = useState<string | null>(null)

  const fromPath = useMemo(() => {
    const state = location.state as { from?: { pathname?: string } } | null
    return state?.from?.pathname
  }, [location.state])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const nextErrors: ValidationErrors = {}

    if (!username.trim()) {
      nextErrors.username = 'Username is required.'
    }

    if (!password.trim()) {
      nextErrors.password = 'Password is required.'
    }

    setValidationErrors(nextErrors)
    setSubmitError(null)

    if (Object.keys(nextErrors).length > 0) {
      return
    }

    try {
      const response = await login({
        username: username.trim(),
        password,
      }).unwrap()

      dispatch(baseApi.util.resetApiState())
      persistAuthState(createAuthStateFromLoginResponse(response))
      dispatch(loginSuccess(response))
      toast.success(`Signed in as ${response.username}`)

      navigate(fromPath ?? getDefaultRouteForRole(response.role), { replace: true })
    } catch (error) {
      setSubmitError(resolveErrorMessage(error))
    }
  }

  return (
    <form className="space-y-5 sm:space-y-6" onSubmit={handleSubmit}>
      <div className="space-y-4 sm:space-y-5">
        <label className="block">
          <span className="mb-2 block text-sm font-medium text-slate-600">
            Username
          </span>
          <div className="flex h-12 items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50/90 px-4 shadow-[inset_0_1px_0_rgba(255,255,255,0.7)] transition focus-within:border-blue-400 focus-within:bg-white focus-within:shadow-[0_0_0_4px_rgba(96,165,250,0.12)] sm:h-13">
            <UserIcon />
            <input
              type="text"
              autoComplete="username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              placeholder="Username"
              className="h-full w-full border-0 bg-transparent text-[15px] text-slate-700 outline-none placeholder:text-slate-400 sm:text-base"
            />
          </div>
          {validationErrors.username ? (
            <p className="mt-2 text-sm text-rose-500">{validationErrors.username}</p>
          ) : null}
        </label>

        <label className="block">
          <span className="mb-2 block text-sm font-medium text-slate-600">
            Password
          </span>
          <div className="flex h-12 items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50/90 px-4 shadow-[inset_0_1px_0_rgba(255,255,255,0.7)] transition focus-within:border-blue-400 focus-within:bg-white focus-within:shadow-[0_0_0_4px_rgba(96,165,250,0.12)] sm:h-13">
            <LockIcon />
            <input
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Password"
              className="h-full w-full border-0 bg-transparent text-[15px] text-slate-700 outline-none placeholder:text-slate-400 sm:text-base"
            />
          </div>
          {validationErrors.password ? (
            <p className="mt-2 text-sm text-rose-500">{validationErrors.password}</p>
          ) : null}
        </label>
      </div>

      <div className="flex justify-end">
        <Link
          to="/login"
          className="text-sm font-medium text-blue-500 underline underline-offset-2 sm:text-[15px]"
        >
          Forgot password?
        </Link>
      </div>

      {submitError ? (
        <div className="rounded-2xl border border-rose-100 bg-rose-50 px-4 py-3 text-sm text-rose-600">
          {submitError}
        </div>
      ) : null}

      <div className="rounded-2xl border border-slate-100 bg-slate-50/80 p-3 text-xs leading-5 text-slate-500 sm:text-sm">
        Use your assigned housekeeping username and password to continue.
      </div>

      <button
        type="submit"
        disabled={isLoading}
        className="flex h-12 w-full items-center justify-center rounded-2xl bg-linear-to-r from-[#2f6ee8] to-[#6b9df7] text-sm font-semibold tracking-[0.12em] text-white transition hover:brightness-105 focus:outline-none focus:ring-4 focus:ring-blue-200 disabled:cursor-not-allowed disabled:opacity-70 sm:h-13 sm:text-base"
      >
        {isLoading ? 'SIGNING IN...' : 'SUBMIT'}
      </button>

      <div className="space-y-3 rounded-2xl border border-slate-100 bg-white px-4 py-4 text-center shadow-[inset_0_1px_0_rgba(255,255,255,0.65)]">
        <p className="text-sm text-slate-500">
          Don&apos;t have an account?{' '}
          <Link to="/login" className="font-medium text-blue-500 underline underline-offset-2">
            Contact admin
          </Link>
        </p>
      </div>
    </form>
  )
}
