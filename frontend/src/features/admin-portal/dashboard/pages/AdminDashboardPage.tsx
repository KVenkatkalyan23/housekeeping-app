import { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'

import type { AppDispatch, RootState } from '../../../../app/store'
import { baseApi } from '../../../../shared/api/baseApi'
import { clearPersistedAuthState, logout } from '../../../auth/slice'
import { useGetAdminDashboardDataQuery } from '../api'
import { CapacityWorkloadChart } from '../components/CapacityWorkloadChart'
import { DashboardHeader } from '../components/DashboardHeader'
import { DashboardSidebar } from '../components/DashboardSidebar'
import { KpiCardsSection } from '../components/KpiCardsSection'
import { ShortfallAlertBanner } from '../components/ShortfallAlertBanner'
import { clearDashboardTimestamp, setDashboardTimestamp } from '../slice'
import type { AdminDashboardData } from '../types'

function resolveErrorMessage(error: unknown) {
  if (!error || typeof error !== 'object') {
    return 'Unable to load the admin dashboard.'
  }

  const candidate = error as {
    data?: { message?: string; error?: { message?: string } }
    error?: string
  }

  return (
    candidate.data?.message ??
    candidate.data?.error?.message ??
    candidate.error ??
    'Unable to load the admin dashboard.'
  )
}

function DashboardLoadingState() {
  return (
    <div className="space-y-5">
      <div className="h-40 animate-pulse rounded-[1.5rem] bg-[#f2f4f8] sm:h-44" />
      <div className="grid gap-5 xl:grid-cols-3">
        <div className="h-64 animate-pulse rounded-[1.4rem] bg-[#f2f4f8] sm:h-72" />
        <div className="h-64 animate-pulse rounded-[1.4rem] bg-[#f2f4f8] sm:h-72" />
        <div className="h-64 animate-pulse rounded-[1.4rem] bg-[#f2f4f8] sm:h-72" />
      </div>
      <div className="h-72 animate-pulse rounded-[1.5rem] bg-[#f2f4f8] sm:h-80" />
    </div>
  )
}

function DashboardErrorState({
  error,
  onRetry,
}: {
  error: unknown
  onRetry: () => void
}) {
  return (
    <section className="rounded-[1.5rem] border border-[#f0d9d8] bg-white p-6 shadow-[0_18px_45px_rgba(25,39,52,0.06)] sm:p-8">
      <p className="text-[0.72rem] font-bold uppercase tracking-[0.18em] text-[#c23431]">
        Dashboard Unavailable
      </p>
      <h2 className="mt-3 text-xl font-semibold tracking-[-0.04em] text-[#243648] sm:text-2xl">
        Unable to load operational metrics
      </h2>
      <p className="mt-3 max-w-xl text-sm leading-6 text-[#6d7988]">
        {resolveErrorMessage(error)}
      </p>
      <button
        type="button"
        onClick={onRetry}
        className="mt-6 rounded-full bg-[#2158d9] px-5 py-3 text-sm font-semibold text-white"
      >
        Retry
      </button>
    </section>
  )
}

function DashboardEmptyState() {
  return (
    <section className="rounded-[1.5rem] border border-dashed border-[#d9e2ec] bg-white p-6 text-center shadow-[0_18px_45px_rgba(25,39,52,0.06)] sm:p-8">
      <p className="text-[0.72rem] font-bold uppercase tracking-[0.18em] text-[#6f8195]">
        No Dashboard Data
      </p>
      <h2 className="mt-3 text-xl font-semibold tracking-[-0.04em] text-[#243648] sm:text-2xl">
        The dashboard is ready but there is nothing to report yet.
      </h2>
      <p className="mt-3 text-sm leading-6 text-[#6d7988]">
        Add rooms, staffing, and active cleaning tasks to populate the live operational view.
      </p>
    </section>
  )
}

function hasEmptyDashboard(data: AdminDashboardData) {
  return (
    data.inventoryStatus.totalRooms === 0 &&
    data.capacityVsWorkload.requiredTotalHours === 0 &&
    data.capacityVsWorkload.availableTotalHours === 0 &&
    data.slaPerformance.totalAssignedTasks === 0
  )
}

export function AdminDashboardPage() {
  const dispatch = useDispatch<AppDispatch>()
  const navigate = useNavigate()
  const lastUpdatedAt = useSelector(
    (state: RootState) => state.adminDashboardUi.lastUpdatedAt,
  )
  const { data, isLoading, isFetching, isError, error, refetch } =
    useGetAdminDashboardDataQuery()

  useEffect(() => {
    if (data) {
      dispatch(setDashboardTimestamp(new Date().toISOString()))
    }
  }, [data, dispatch])

  useEffect(
    () => () => {
      dispatch(clearDashboardTimestamp())
    },
    [dispatch],
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
              currentAdmin={
                data?.currentAdmin ?? {
                  displayName: 'Admin User',
                  roleLabel: 'MASTER ADMIN',
                  avatarUrl: null,
                }
              }
              title="Admin Dashboard"
              lastUpdatedAt={lastUpdatedAt}
            />

            <section className="flex-1 px-4 py-5 sm:px-5 md:px-6 lg:px-7">
              {isFetching && data ? (
                <p className="mb-3 text-[0.7rem] font-bold uppercase tracking-[0.18em] text-[#8e9aaa]">
                  Refreshing dashboard data...
                </p>
              ) : null}

              {isLoading ? <DashboardLoadingState /> : null}
              {isError ? (
                <DashboardErrorState error={error} onRetry={() => refetch()} />
              ) : null}
              {!isLoading && !isError && data ? (
                hasEmptyDashboard(data) ? (
                  <DashboardEmptyState />
                ) : (
                  <div className="space-y-5">
                    <ShortfallAlertBanner shortfallAlert={data.shortfallAlert} />
                    <KpiCardsSection
                      inventoryStatus={data.inventoryStatus}
                      workforceEfficiency={data.workforceEfficiency}
                      slaPerformance={data.slaPerformance}
                    />
                    <CapacityWorkloadChart chart={data.capacityVsWorkload} />
                  </div>
                )
              ) : null}
            </section>
          </div>
        </div>
      </div>
    </main>
  )
}
