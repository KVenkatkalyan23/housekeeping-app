import { useSelector, useDispatch } from 'react-redux'
import { useNavigate } from 'react-router-dom'

import type { AppDispatch, RootState } from '../../../../app/store'
import { baseApi } from '../../../../shared/api/baseApi'
import { clearPersistedAuthState, logout } from '../../../auth/slice'
import { DashboardHeader } from '../../dashboard/components/DashboardHeader'
import { DashboardSidebar } from '../../dashboard/components/DashboardSidebar'
import { useGetAdminAuditLogsQuery } from '../api'
import { AuditLogsFilters } from '../components/AuditLogsFilters'
import { AuditLogsHeader } from '../components/AuditLogsHeader'
import { AuditLogsTable } from '../components/AuditLogsTable'
import { AuditPagination } from '../components/AuditPagination'
import {
  setAdminAuditLogsCategory,
  setAdminAuditLogsPage,
} from '../slice'

function formatDisplayName(value: string | null) {
  if (!value) {
    return 'Admin User'
  }

  return value
    .replace(/[_-]+/g, ' ')
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .map(
      (segment) =>
        segment.charAt(0).toUpperCase() + segment.slice(1).toLowerCase(),
    )
    .join(' ')
}

function resolveErrorMessage(error: unknown) {
  if (!error || typeof error !== 'object') {
    return 'Unable to load audit logs.'
  }

  const candidate = error as {
    data?: { message?: string; error?: { message?: string } }
    error?: string
  }

  return (
    candidate.data?.message ??
    candidate.data?.error?.message ??
    candidate.error ??
    'Unable to load audit logs.'
  )
}

export function AdminAuditLogsPage() {
  const dispatch = useDispatch<AppDispatch>()
  const navigate = useNavigate()
  const { username } = useSelector((state: RootState) => state.auth)
  const { page, size, category } = useSelector(
    (state: RootState) => state.adminAuditLogsUi,
  )
  const { data, isLoading, isFetching, isError, error, refetch } =
    useGetAdminAuditLogsQuery({
      page,
      size,
      category,
    })

  const handleLogout = () => {
    clearPersistedAuthState()
    dispatch(baseApi.util.resetApiState())
    dispatch(logout())
    navigate('/login', { replace: true })
  }

  return (
    <main className="min-h-screen bg-[#edf3f7] text-[#243648]">
      <div className="flex min-h-screen flex-col xl:flex-row">
        <DashboardSidebar onLogout={handleLogout} />

        <div className="min-w-0 flex-1">
          <div className="mx-auto flex min-h-screen max-w-[1280px] flex-col bg-[#fbfdff]">
            <DashboardHeader
              currentAdmin={{
                displayName: formatDisplayName(username),
                roleLabel: 'MASTER ADMIN',
                avatarUrl: null,
              }}
              title={null}
              subtitle={null}
            />

            <section className="flex-1 px-4 py-5 sm:px-5 md:px-6 lg:px-7">
              {isFetching && data ? (
                <p className="mb-3 text-[0.7rem] font-bold uppercase tracking-[0.18em] text-[#8e9aaa]">
                  Refreshing audit logs...
                </p>
              ) : null}

              <AuditLogsHeader />
              <AuditLogsFilters
                value={category}
                loading={isLoading || isFetching}
                onChange={(value) => dispatch(setAdminAuditLogsCategory(value))}
              />
              <div className="space-y-5">
                <AuditLogsTable
                  items={data?.items ?? []}
                  isLoading={isLoading}
                  isError={isError}
                  errorMessage={resolveErrorMessage(error)}
                  onRetry={() => refetch()}
                />
                <AuditPagination
                  page={data?.page ?? page}
                  size={data?.size ?? size}
                  totalElements={data?.totalElements ?? 0}
                  totalPages={data?.totalPages ?? 0}
                  loading={isFetching}
                  onPageChange={(value) => dispatch(setAdminAuditLogsPage(value))}
                />
              </div>
            </section>
          </div>
        </div>
      </div>
    </main>
  )
}
