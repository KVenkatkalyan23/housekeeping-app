import { useSelector } from "react-redux";
import { Navigate } from "react-router-dom";

import type { RootState } from "../../../app/store";
import { LoginForm } from "../components/LoginForm";
import { getDefaultRouteForRole } from '../routing'

function BrandMark() {
  return (
    <div className="flex items-center gap-3 sm:gap-4">
      <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-linear-to-br from-[#2f6ee8] to-[#4f8af6] shadow-[0_18px_35px_rgba(61,115,240,0.25)] sm:h-14 sm:w-14">
        <svg viewBox="0 0 24 24" fill="none" className="h-7 w-7 text-white">
          <rect
            x="5.2"
            y="4.7"
            width="13.6"
            height="14.6"
            rx="2.2"
            stroke="currentColor"
            strokeWidth="1.8"
          />
          <path
            d="M9 9h6M9 12h6M9 15h4"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
          />
        </svg>
      </div>
      <span className="text-[1.55rem] font-semibold tracking-[0.24em] text-slate-800 sm:text-[2rem] sm:tracking-[0.28em]">
        HYATT
      </span>
    </div>
  );
}

export function LoginPage() {
  const isAuthenticated = useSelector(
    (state: RootState) => state.auth.isAuthenticated,
  );
  const role = useSelector((state: RootState) => state.auth.role)

  if (isAuthenticated) {
    return <Navigate to={getDefaultRouteForRole(role)} replace />;
  }

  return (
    <main className="relative min-h-screen overflow-hidden bg-[radial-gradient(circle_at_top_left,_rgba(96,165,250,0.22),_transparent_30%),linear-gradient(180deg,#f8fbff_0%,#eef4fb_100%)] px-4 py-5 text-slate-700 sm:px-6 sm:py-8 lg:px-10 xl:px-16">
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute left-[-8rem] top-[-5rem] h-56 w-56 rounded-full bg-blue-200/35 blur-3xl sm:h-72 sm:w-72" />
        <div className="absolute bottom-[-7rem] right-[-4rem] h-64 w-64 rounded-full bg-cyan-100/70 blur-3xl sm:h-80 sm:w-80" />
        <div className="absolute right-[18%] top-[20%] hidden h-40 w-40 rounded-full border border-white/40 bg-white/20 lg:block" />
      </div>

      <div className="relative mx-auto grid min-h-[calc(100vh-2.5rem)] w-full max-w-7xl items-center gap-8 lg:grid-cols-[minmax(0,1.1fr)_minmax(22rem,28rem)] lg:gap-14">
        <section className="order-2 space-y-8 pb-4 lg:order-1 lg:space-y-10 lg:pl-6 lg:pr-6 ">
          <div className="max-md:flex w-full items-center justify-center">
            <BrandMark />
          </div>

          <div className="max-w-2xl space-y-4 sm:space-y-5">
            <div className="inline-flex items-center rounded-full border border-blue-100 bg-white/80 px-4 py-2 text-xs font-semibold uppercase tracking-[0.22em] text-blue-600 shadow-[0_12px_30px_rgba(148,163,184,0.12)] backdrop-blur max-md:flex  max-md:justify-center">
              Housekeeping Control Center
            </div>
            <h1 className="max-w-xl text-4xl font-semibold tracking-[-0.05em] text-slate-800 sm:text-5xl lg:text-6xl max-md:flex  max-md:text-center w-full">
              Welcome back to your operations desk
            </h1>
            <p className="max-w-xl text-base leading-7 text-slate-500 sm:text-lg sm:leading-8 lg:text-xl max-md:text-center">
              Sign in to manage rooms, staff activity, daily assignments, and
              live housekeeping operations from a single workspace.
            </p>
          </div>

          <div className="grid gap-3 sm:grid-cols-3">
            <div className="rounded-2xl border border-white/70 bg-white/80 p-4 shadow-[0_16px_35px_rgba(148,163,184,0.12)] backdrop-blur">
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-blue-500">
                Task Flow
              </p>
              <p className="mt-2 text-sm leading-6 text-slate-500">
                Assign, rebalance, and monitor room work without losing shift
                visibility.
              </p>
            </div>
            <div className="rounded-2xl border border-white/70 bg-white/80 p-4 shadow-[0_16px_35px_rgba(148,163,184,0.12)] backdrop-blur">
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-blue-500">
                Attendance
              </p>
              <p className="mt-2 text-sm leading-6 text-slate-500">
                Track staffing coverage, gaps, and leave impact in real time.
              </p>
            </div>
            <div className="rounded-2xl border border-white/70 bg-white/80 p-4 shadow-[0_16px_35px_rgba(148,163,184,0.12)] backdrop-blur">
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-blue-500">
                Secure Access
              </p>
              <p className="mt-2 text-sm leading-6 text-slate-500">
                Role-based access keeps staff and admin workflows separated.
              </p>
            </div>
          </div>
        </section>

        <section className="order-1 mx-auto w-full max-w-md lg:order-2 lg:max-w-[28rem]">
          <div className="rounded-[1.9rem] border border-white/80 bg-white/90 p-3 shadow-[0_30px_80px_rgba(15,23,42,0.12)] backdrop-blur-sm sm:p-4">
            <div className="rounded-[1.55rem] bg-linear-to-r from-[#2f6ee8] via-[#427ef2] to-[#6da0f8] px-5 py-7 text-center shadow-[inset_0_1px_0_rgba(255,255,255,0.28)] sm:px-6 sm:py-8">
              <p className="text-xs font-semibold uppercase tracking-[0.24em] text-blue-100">
                Secure Access
              </p>
              <h2 className="mt-3 text-[2rem] font-semibold tracking-[-0.03em] text-white sm:text-[2.35rem]">
                Sign In
              </h2>
              <p className="mt-2 text-sm text-blue-100/90 sm:text-base">
                Access your housekeeping dashboard
              </p>
            </div>

            <div className="px-3 pb-4 pt-6 sm:px-4 sm:pb-5 sm:pt-8">
              <LoginForm />
            </div>
          </div>
        </section>
      </div>
    </main>
  );
}
