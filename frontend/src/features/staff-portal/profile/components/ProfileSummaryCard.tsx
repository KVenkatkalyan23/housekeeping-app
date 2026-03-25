import type { StaffProfileResponse } from '../types'

interface ProfileSummaryCardProps {
  profile: StaffProfileResponse
}

export function ProfileSummaryCard({ profile }: ProfileSummaryCardProps) {
  return (
    <section className="mt-6 text-center">
      <div className="mx-auto flex h-32 w-32 items-center justify-center rounded-[1.8rem] bg-linear-to-br from-[#7bc7c1] to-[#4ea9b7] shadow-[0_18px_38px_rgba(15,23,42,0.12)]">
        <svg viewBox="0 0 120 120" fill="none" className="h-24 w-24 text-white/95">
          <circle cx="60" cy="34" r="16" fill="currentColor" />
          <path
            d="M34 94c4-18 18-28 26-28s22 10 26 28"
            fill="currentColor"
            opacity="0.92"
          />
        </svg>
      </div>

      <h1 className="mt-6 text-[2.15rem] font-semibold tracking-[-0.06em] text-[#173b9f]">
        {profile.fullName}
      </h1>
      <p className="mt-1 text-base text-slate-500">{profile.displayName}</p>

      <div className="mt-5 inline-flex items-center rounded-full bg-[#2649c7] px-5 py-2 text-sm font-semibold text-white shadow-[0_14px_24px_rgba(38,73,199,0.22)]">
        <span className="mr-2 h-2 w-2 rounded-full bg-white/90" />
        {profile.currentShift}
      </div>
    </section>
  )
}
