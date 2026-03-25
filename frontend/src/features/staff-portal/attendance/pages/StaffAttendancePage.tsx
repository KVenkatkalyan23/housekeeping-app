import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { toast } from "react-toastify";

import type { AppDispatch, RootState } from "../../../../app/store";
import { clearPersistedAuthState, logout } from "../../../auth/slice";
import {
  useClockInMutation,
  useClockOutMutation,
  useGetCurrentAttendanceQuery,
} from "../api";
import { BottomNav } from "../components/BottomNav";
import { ClockControls } from "../components/ClockControls";
import { LeaveSection } from "../components/LeaveSection";
import { LogoutConfirmModal } from "../components/LogoutConfirmModal";
import { ShiftStatusCard } from "../components/ShiftStatusCard";
import { TodayTasksSection } from "../components/TodayTasksSection";

function resolveErrorMessage(error: unknown) {
  if (!error || typeof error !== "object") {
    return "Something went wrong. Please try again.";
  }

  const candidate = error as {
    data?: { message?: string; error?: { message?: string } };
    error?: string;
  };

  return (
    candidate.data?.message ??
    candidate.data?.error?.message ??
    candidate.error ??
    "Something went wrong. Please try again."
  );
}

function PortalHeader({
  username,
  status,
  onLogout,
}: {
  username: string | null;
  status: string;
  onLogout: () => void;
}) {
  return (
    <header className="flex items-start justify-between gap-4">
      <div>
        <p className="text-[0.68rem] font-bold uppercase tracking-[0.2em] text-[#7aa4e6]">
          Housekeeping AI
        </p>
        <h1 className="text-[2rem] font-semibold tracking-[-0.05em] text-slate-900">
          Staff Portal
        </h1>
        <p className="mt-1 text-sm text-slate-500">
          Current duty status:{" "}
          <span className="font-semibold text-slate-700">{status}</span>
        </p>
      </div>

      <div className="flex items-center gap-2">
        <button
          type="button"
          className="flex h-10 w-10 items-center justify-center rounded-full bg-white text-slate-500 shadow-[0_12px_25px_rgba(15,23,42,0.08)]"
          aria-label="Notifications"
        >
          <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5">
            <path
              d="M12 5a4 4 0 0 0-4 4v2.2c0 .7-.24 1.37-.67 1.92L6 14.8h12l-1.33-1.68a3.1 3.1 0 0 1-.67-1.92V9a4 4 0 0 0-4-4Z"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <path
              d="M10 18a2 2 0 0 0 4 0"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
            />
          </svg>
        </button>
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[#e7f0ff] text-sm font-semibold text-[#1664c0] shadow-[0_12px_25px_rgba(15,23,42,0.08)]">
          {(username ?? "S").slice(0, 1).toUpperCase()}
        </div>
        <button
          type="button"
          onClick={onLogout}
          className="rounded-full bg-white px-3 py-2 text-xs font-semibold uppercase tracking-[0.12em] text-slate-500 shadow-[0_12px_25px_rgba(15,23,42,0.08)]"
        >
          Logout
        </button>
      </div>
    </header>
  );
}

export function StaffAttendancePage() {
  const dispatch = useDispatch<AppDispatch>();
  const username = useSelector((state: RootState) => state.auth.username);
  const { data, isLoading, isFetching, isError, error, refetch } =
    useGetCurrentAttendanceQuery();
  const [clockIn, { isLoading: isClockingIn }] = useClockInMutation();
  const [clockOut, { isLoading: isClockingOut }] = useClockOutMutation();
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false);
  const [showClockOutConfirm, setShowClockOutConfirm] = useState(false);

  const handleLogout = () => {
    if (data?.onDuty) {
      setShowLogoutConfirm(true);
      return;
    }

    clearPersistedAuthState();
    dispatch(logout());
  };

  const confirmLogout = () => {
    setShowLogoutConfirm(false);
    clearPersistedAuthState();
    dispatch(logout());
  };

  const handleClockIn = async () => {
    try {
      await clockIn().unwrap();
      toast.success("Clock-in successful.");
    } catch (mutationError) {
      toast.error(resolveErrorMessage(mutationError));
    }
  };

  const confirmClockOut = async () => {
    setShowClockOutConfirm(false);

    try {
      await clockOut().unwrap();
      toast.success("Clock-out successful.");
    } catch (mutationError) {
      toast.error(resolveErrorMessage(mutationError));
    }
  };

  const handleClockOut = () => {
    setShowClockOutConfirm(true);
  };

  if (isLoading) {
    return (
      <main className="min-h-screen bg-[#f4f3f8] px-4 py-6 text-slate-700">
        <div className="mx-auto max-w-md animate-pulse space-y-4">
          <div className="h-16 rounded-[1.5rem] bg-white" />
          <div className="h-44 rounded-[1.75rem] bg-white" />
          <div className="h-28 rounded-[1.75rem] bg-white" />
          <div className="h-36 rounded-[1.75rem] bg-white" />
        </div>
      </main>
    );
  }

  if (isError || !data) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-[#f4f3f8] px-4">
        <div className="w-full max-w-sm rounded-[1.75rem] bg-white p-6 text-center shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
          <h1 className="text-xl font-semibold text-slate-900">
            Unable to load attendance
          </h1>
          <p className="mt-2 text-sm leading-6 text-slate-500">
            {resolveErrorMessage(error)}
          </p>
          <button
            type="button"
            onClick={() => refetch()}
            className="mt-5 h-11 rounded-full bg-[#1664c0] px-5 text-sm font-semibold text-white"
          >
            Retry
          </button>
        </div>
      </main>
    );
  }

  return (
    <>
      <main className="min-h-screen bg-[#f4f3f8] pb-36 text-slate-700">
        <div className="mx-auto max-w-md px-3 py-5">
          <PortalHeader
            username={username}
            status={data.onDuty ? "On Duty" : "Off Duty"}
            onLogout={handleLogout}
          />

          <div className="mt-5 space-y-4">
            <ShiftStatusCard attendance={data} />
            <ClockControls
              isOnDuty={data.onDuty}
              isClockingIn={isClockingIn}
              isClockingOut={isClockingOut}
              onClockIn={handleClockIn}
              onClockOut={handleClockOut}
            />
          </div>

          {isFetching ? (
            <p className="mt-3 text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">
              Refreshing portal status...
            </p>
          ) : null}

          <TodayTasksSection />
          <LeaveSection />
        </div>

        <BottomNav activeTab="tasks" />
      </main>

      <LogoutConfirmModal
        open={showLogoutConfirm}
        onCancel={() => setShowLogoutConfirm(false)}
        onConfirm={confirmLogout}
      />
      <LogoutConfirmModal
        open={showClockOutConfirm}
        title="Clock out now?"
        message="Are you sure you want to clock out from your current shift?"
        confirmLabel="Clock Out"
        onCancel={() => setShowClockOutConfirm(false)}
        onConfirm={confirmClockOut}
      />
    </>
  );
}
