interface AttendanceSummaryCardProps {
  label: string
  value: string
  supportingText: string
  tone: 'teal' | 'amber' | 'rose'
  isLoading?: boolean
}

const toneClasses = {
  teal: 'bg-[linear-gradient(135deg,#0c6970,#2ca49f)] text-white',
  amber: 'bg-[linear-gradient(135deg,#f2d298,#f0b465)] text-[#3f3014]',
  rose: 'bg-[linear-gradient(135deg,#f3c5c1,#ee9e96)] text-[#4d2220]',
}

export function AttendanceSummaryCard({
  label,
  value,
  supportingText,
  tone,
  isLoading = false,
}: AttendanceSummaryCardProps) {
  return (
    <article
      className={`rounded-[1.55rem] p-5 shadow-[0_18px_45px_rgba(25,39,52,0.06)] ${toneClasses[tone]}`}
    >
      {isLoading ? (
        <div className="space-y-4">
          <div className="h-3 w-24 animate-pulse rounded bg-white/30" />
          <div className="h-10 w-28 animate-pulse rounded bg-white/30" />
          <div className="h-3 w-32 animate-pulse rounded bg-white/30" />
        </div>
      ) : (
        <>
          <p className="text-[0.72rem] font-bold uppercase tracking-[0.18em] opacity-80">
            {label}
          </p>
          <p className="mt-4 text-[2rem] font-semibold tracking-[-0.04em]">{value}</p>
          <p className="mt-2 text-sm opacity-80">{supportingText}</p>
        </>
      )}
    </article>
  )
}
