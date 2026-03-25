import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";

import type { AppDispatch } from "../../../../app/store";
import { clearPersistedAuthState, logout } from "../../../auth/slice";
import { useGetCurrentStaffProfileQuery } from "../api";
import { BottomNav } from "../components/BottomNav";
import { LogoutButton } from "../components/LogoutButton";
import { ProfileDetailsList } from "../components/ProfileDetailsList";
import { ProfileHeader } from "../components/ProfileHeader";
import { ProfileSummaryCard } from "../components/ProfileSummaryCard";

function resolveErrorMessage(error: unknown) {
  if (!error || typeof error !== "object") {
    return "Unable to load profile.";
  }

  const candidate = error as {
    data?: { message?: string; error?: { message?: string } };
    error?: string;
  };

  return (
    candidate.data?.message ??
    candidate.data?.error?.message ??
    candidate.error ??
    "Unable to load profile."
  );
}

export function ProfilePage() {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { data, isLoading, isError, error, refetch } =
    useGetCurrentStaffProfileQuery();

  const handleLogout = () => {
    clearPersistedAuthState();
    dispatch(logout());
    navigate("/login", { replace: true });
  };

  if (isLoading) {
    return (
      <main className="min-h-screen bg-[#fcfcfe] px-4 py-5 pb-36">
        <div className="mx-auto max-w-md animate-pulse space-y-4">
          <div className="h-14 rounded-[1.3rem] bg-white" />
          <div className="h-52 rounded-[1.5rem] bg-white" />
          <div className="h-48 rounded-[1.5rem] bg-white" />
        </div>
      </main>
    );
  }

  if (isError || !data) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-[#fcfcfe] px-4">
        <div className="w-full max-w-sm rounded-[1.75rem] bg-white p-6 text-center shadow-[0_14px_36px_rgba(15,23,42,0.08)]">
          <h1 className="text-xl font-semibold text-[#26324d]">
            Unable to load profile
          </h1>
          <p className="mt-2 text-sm leading-6 text-slate-500">
            {resolveErrorMessage(error)}
          </p>
          <button
            type="button"
            onClick={() => refetch()}
            className="mt-5 h-11 rounded-full bg-[#2649c7] px-5 text-sm font-semibold text-white"
          >
            Retry
          </button>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-[#fcfcfe] pb-36 text-slate-700">
      <div className="mx-auto max-w-md px-4 py-5">
        <ProfileHeader />
        <ProfileSummaryCard profile={data} />
        <ProfileDetailsList profile={data} />
        <LogoutButton onLogout={handleLogout} />
      </div>

      <BottomNav />
    </main>
  );
}
