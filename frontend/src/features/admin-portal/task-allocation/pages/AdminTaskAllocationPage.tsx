import { useDeferredValue } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'

import type { AppDispatch, RootState } from '../../../../app/store'
import { baseApi } from '../../../../shared/api/baseApi'
import { clearPersistedAuthState, logout } from '../../../auth/slice'
import { DashboardHeader } from '../../dashboard/components/DashboardHeader'
import { DashboardSidebar } from '../../dashboard/components/DashboardSidebar'
import {
  useGetAdminTaskAllocationListQuery,
  useGetAdminTaskAllocationSummaryQuery,
} from '../api'
import { TaskAllocationFilters } from '../components/TaskAllocationFilters'
import { TaskAllocationHeader } from '../components/TaskAllocationHeader'
import { TaskAllocationPagination } from '../components/TaskAllocationPagination'
import { TaskAllocationSummaryCards } from '../components/TaskAllocationSummaryCards'
import { TaskAllocationTable } from '../components/TaskAllocationTable'
import {
  resetTaskAllocationFilters,
  setTaskAllocationPage,
  setTaskAllocationSearch,
  setTaskAllocationStatus,
  setTaskAllocationTaskType,
} from '../slice'

function resolveErrorMessage(error: unknown) {
  if (!error || typeof error !== 'object') {
    return 'Unable to load task allocation data.'
  }

  const candidate = error as {
    data?: { message?: string; error?: { message?: string } }
    error?: string
  }

  return (
    candidate.data?.message ??
    candidate.data?.error?.message ??
    candidate.error ??
    'Unable to load task allocation data.'
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

export function AdminTaskAllocationPage() {
  const dispatch = useDispatch<AppDispatch>()
  const navigate = useNavigate()
  const { username } = useSelector((state: RootState) => state.auth)
  const { page, size, search, taskType, status } = useSelector(
    (state: RootState) => state.adminTaskAllocationUi,
  )
  const deferredSearch = useDeferredValue(search.trim())

  const summaryQuery = useGetAdminTaskAllocationSummaryQuery()
  const listQuery = useGetAdminTaskAllocationListQuery({
    page,
    size,
    search: deferredSearch || undefined,
    taskType,
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
              {(summaryQuery.isFetching || listQuery.isFetching) &&
              (summaryQuery.data || listQuery.data) ? (
                <p className="mb-3 text-[0.7rem] font-bold uppercase tracking-[0.18em] text-[#8e9aaa]">
                  Refreshing task allocation...
                </p>
              ) : null}

              <TaskAllocationHeader />

              <div className="space-y-5">
                <TaskAllocationSummaryCards
                  summary={summaryQuery.data}
                  isLoading={summaryQuery.isLoading}
                  isError={summaryQuery.isError}
                  errorMessage={resolveErrorMessage(summaryQuery.error)}
                  onRetry={() => summaryQuery.refetch()}
                />

                <TaskAllocationFilters
                  search={search}
                  taskType={taskType}
                  status={status}
                  loading={listQuery.isFetching}
                  onSearchChange={(value) => dispatch(setTaskAllocationSearch(value))}
                  onTaskTypeChange={(value) => dispatch(setTaskAllocationTaskType(value))}
                  onStatusChange={(value) => dispatch(setTaskAllocationStatus(value))}
                  onReset={() => dispatch(resetTaskAllocationFilters())}
                />

                <TaskAllocationTable
                  items={listQuery.data?.items ?? []}
                  isLoading={listQuery.isLoading}
                  isError={listQuery.isError}
                  errorMessage={resolveErrorMessage(listQuery.error)}
                  onRetry={() => listQuery.refetch()}
                />

                <TaskAllocationPagination
                  page={listQuery.data?.page ?? page}
                  size={listQuery.data?.size ?? size}
                  totalElements={listQuery.data?.totalElements ?? 0}
                  totalPages={listQuery.data?.totalPages ?? 0}
                  loading={listQuery.isFetching}
                  onPageChange={(nextPage) => dispatch(setTaskAllocationPage(nextPage))}
                />
              </div>
            </section>
          </div>
        </div>
      </div>
    </main>
  )
}
