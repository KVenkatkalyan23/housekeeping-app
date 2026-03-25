import type { StaffProfileResponse } from '../types'
import { ProfileDetailCard } from './ProfileDetailCard'

interface ProfileDetailsListProps {
  profile: StaffProfileResponse
}

export function ProfileDetailsList({ profile }: ProfileDetailsListProps) {
  return (
    <section className="mt-8">
      <h2 className="text-[1.15rem] font-semibold uppercase tracking-[0.18em] text-slate-500">
        Personal Details
      </h2>
      <div className="mt-5 space-y-3.5 rounded-[1.8rem] bg-[#f7f8fb] p-4 shadow-[inset_0_1px_0_rgba(255,255,255,0.7)]">
        <ProfileDetailCard
          label="Work Email"
          value={profile.email || 'Not available'}
          icon="email"
        />
        <ProfileDetailCard
          label="Phone"
          value={profile.phone || 'Not available'}
          icon="phone"
        />
        <ProfileDetailCard
          label="Assigned Wing"
          value={profile.assignedWing}
          icon="location"
        />
      </div>
    </section>
  )
}
