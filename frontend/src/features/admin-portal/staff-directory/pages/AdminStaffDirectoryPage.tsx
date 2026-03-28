import { useDeferredValue } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'

import type { AppDispatch, RootState } from '../../../../app/store'
import { baseApi } from '../../../../shared/api/baseApi'
import { clearPersistedAuthState, logout } from '../../../auth/slice'
import { DashboardHeader } from '../../dashboard/components/DashboardHeader'
import { DashboardSidebar } from '../../dashboard/components/DashboardSidebar'
import { useGetAdminStaffDirectoryQuery } from '../api'
import { StaffDirectoryFilters } from '../components/StaffDirectoryFilters'
import { StaffDirectoryHeader } from '../components/StaffDirectoryHeader'
import { StaffDirectoryPagination } from '../components/StaffDirectoryPagination'
import { StaffDirectoryTable } from '../components/StaffDirectoryTable'
import {
  setDirectoryPage,
  setDirectorySearch,
  setDirectorySize,
  setDirectoryStatus,
} from '../slice'

function resolveErrorMessage(error: unknown) {
  if (!error || typeof error !== 'object') {
    return 'Unable to load the staff directory.'
  }

  const candidate = error as {
    data?: { message?: string; error?: { message?: string } }
    error?: string
  }

  return (
    candidate.data?.message ??
    candidate.data?.error?.message ??
    candidate.error ??
    'Unable to load the staff directory.'
  )
}

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

export function AdminStaffDirectoryPage() {
  const dispatch = useDispatch<AppDispatch>()
  const navigate = useNavigate()
  const { username } = useSelector((state: RootState) => state.auth)
  const { page, size, search, status } = useSelector(
    (state: RootState) => state.adminStaffDirectoryUi,
  )
  const deferredSearch = useDeferredValue(search.trim())

  const { data, isLoading, isFetching, isError, error, refetch } =
    useGetAdminStaffDirectoryQuery({
      page,
      size,
      search: deferredSearch || undefined,
      status,
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
                  Refreshing staff directory...
                </p>
              ) : null}

              <StaffDirectoryHeader />

              <div className="space-y-5">
                <StaffDirectoryFilters
                  search={search}
                  status={status}
                  summary={data?.summary}
                  loading={isLoading || isFetching}
                  onSearchChange={(value) => dispatch(setDirectorySearch(value))}
                  onStatusChange={(value) => dispatch(setDirectoryStatus(value))}
                />

                <StaffDirectoryTable
                  items={data?.items ?? []}
                  isLoading={isLoading}
                  isError={isError}
                  errorMessage={resolveErrorMessage(error)}
                  onRetry={() => refetch()}
                />

                <StaffDirectoryPagination
                  page={data?.page ?? page}
                  size={data?.size ?? size}
                  totalPages={data?.totalPages ?? 0}
                  totalElements={data?.totalElements ?? 0}
                  isLoading={isLoading || isFetching}
                  onPageChange={(value) => dispatch(setDirectoryPage(value))}
                  onSizeChange={(value) => dispatch(setDirectorySize(value))}
                />
              </div>
            </section>
          </div>
        </div>
      </div>
    </main>
  )
}
