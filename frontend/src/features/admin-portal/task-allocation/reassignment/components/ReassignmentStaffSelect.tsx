import type { ReassignmentCandidateItem } from '../../types'

export function ReassignmentStaffSelect({
  candidates,
  value,
  disabled,
  onChange,
}: {
  candidates: ReassignmentCandidateItem[]
  value: string
  disabled: boolean
  onChange: (value: string) => void
}) {
  return (
    <div className="space-y-2">
      <label
        htmlFor="manual-reassignment-staff"
        className="text-[0.72rem] font-bold uppercase tracking-[0.18em] text-[#66798d]"
      >
        Target Staff Member
      </label>
      <select
        id="manual-reassignment-staff"
        value={value}
        disabled={disabled}
        onChange={(event) => onChange(event.target.value)}
        className="w-full rounded-2xl border border-[#d9e6e8] bg-white px-4 py-3 text-sm font-medium text-[#243648] outline-none transition focus:border-[#0c8b7d] focus:ring-4 focus:ring-[#d9f3ee] disabled:cursor-not-allowed disabled:bg-[#f6f8fa] disabled:text-[#8a98a8]"
      >
        <option value="">Select available staff...</option>
        {candidates.map((candidate) => (
          <option key={candidate.staffId} value={candidate.staffId}>
            {candidate.fullName} - {candidate.remainingMinutes} min left
          </option>
        ))}
      </select>
    </div>
  )
}
