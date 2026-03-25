import { isRouteErrorResponse, Link, useRouteError } from 'react-router-dom'

function getErrorMessage(error: unknown) {
  if (isRouteErrorResponse(error)) {
    if (typeof error.data === 'string' && error.data.trim()) {
      return error.data
    }

    return error.statusText || 'Something went wrong.'
  }

  if (error instanceof Error) {
    return error.message
  }

  return 'Something went wrong.'
}

export function ErrorPage() {
  const error = useRouteError()
  const message = getErrorMessage(error)

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-md rounded-lg bg-white p-8 text-center shadow-sm">
        <h1 className="text-2xl font-semibold text-slate-900">
          Something went wrong
        </h1>
        <p className="mt-3 text-sm text-slate-600">{message}</p>
        <div className="mt-6 flex justify-center gap-3">
          <button
            type="button"
            onClick={() => window.location.reload()}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white"
          >
            Reload page
          </button>
          <Link
            to="/"
            className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700"
          >
            Go home
          </Link>
        </div>
      </div>
    </div>
  )
}
