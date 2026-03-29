import { skipToken } from '@reduxjs/toolkit/query'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'

import type { AppDispatch, RootState } from '../../../../app/store'
import { baseApi } from '../../../../shared/api/baseApi'
import { clearPersistedAuthState, logout } from '../../../auth/slice'
import { DashboardHeader } from '../../dashboard/components/DashboardHeader'
import { DashboardSidebar } from '../../dashboard/components/DashboardSidebar'
import {
  useGetAdminAttendanceLogsQuery,
  useGetAdminAttendanceSummaryQuery,
} from '../api'
import { AttendanceDateRangeFilter } from '../components/AttendanceDateRangeFilter'
import { AttendanceLogsHeader } from '../components/AttendanceLogsHeader'
import { AttendanceLogsTable } from '../components/AttendanceLogsTable'
import { AttendancePagination } from '../components/AttendancePagination'
import { AttendanceSummaryCards } from '../components/AttendanceSummaryCards'
import {
  setAdminAttendanceFromDate,
  setAdminAttendancePage,
  setAdminAttendanceToDate,
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
    return 'Unable to load attendance logs.'
  }

  const candidate = error as {
    data?: { message?: string; error?: { message?: string } }
    error?: string
  }

  return (
    candidate.data?.message ??
    candidate.data?.error?.message ??
    candidate.error ??
    'Unable to load attendance logs.'
  )
}

export function AdminAttendanceLogsPage() {
  const dispatch = useDispatch<AppDispatch>()
  const navigate = useNavigate()
  const { username } = useSelector((state: RootState) => state.auth)
  const { fromDate, toDate, page, size } = useSelector(
    (state: RootState) => state.adminAttendanceLogsUi,
  )

  const hasInvalidDateRange = Boolean(fromDate && toDate && fromDate > toDate)
  const rangeArgs = hasInvalidDateRange ? skipToken : { fromDate, toDate }
  const summaryQuery = useGetAdminAttendanceSummaryQuery(rangeArgs)
  const logsQuery = useGetAdminAttendanceLogsQuery(
    hasInvalidDateRange ? skipToken : { fromDate, toDate, page, size },
  )

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

            <section className="flex-1 space-y-5 px-4 py-5 sm:px-5 md:px-6 lg:px-7">
              {(summaryQuery.isFetching || logsQuery.isFetching) &&
              (summaryQuery.data || logsQuery.data) ? (
                <p className="text-[0.7rem] font-bold uppercase tracking-[0.18em] text-[#8e9aaa]">
                  Refreshing attendance logs...
                </p>
              ) : null}

              <AttendanceLogsHeader />

              <AttendanceDateRangeFilter
                fromDate={fromDate}
                toDate={toDate}
                loading={summaryQuery.isFetching || logsQuery.isFetching}
                validationMessage={
                  hasInvalidDateRange
                    ? 'To date must be on or after from date.'
                    : null
                }
                onFromDateChange={(value) =>
                  dispatch(setAdminAttendanceFromDate(value))
                }
                onToDateChange={(value) => dispatch(setAdminAttendanceToDate(value))}
              />

              <AttendanceSummaryCards
                summary={summaryQuery.data}
                isLoading={summaryQuery.isLoading}
                isError={summaryQuery.isError}
                errorMessage={resolveErrorMessage(summaryQuery.error)}
                onRetry={() => summaryQuery.refetch()}
              />

              <AttendanceLogsTable
                items={logsQuery.data?.items ?? []}
                isLoading={logsQuery.isLoading}
                isError={logsQuery.isError}
                errorMessage={resolveErrorMessage(logsQuery.error)}
                onRetry={() => logsQuery.refetch()}
              />

              <AttendancePagination
                page={logsQuery.data?.page ?? page}
                size={logsQuery.data?.size ?? size}
                totalElements={logsQuery.data?.totalElements ?? 0}
                totalPages={logsQuery.data?.totalPages ?? 0}
                loading={logsQuery.isFetching}
                onPageChange={(nextPage) =>
                  dispatch(setAdminAttendancePage(nextPage))
                }
              />
            </section>
          </div>
        </div>
      </div>
    </main>
  )
}
