export function TaskAllocationSummaryCard({
  label,
  value,
  accent,
  detail,
  icon,
}: {
  label: string
  value: number
  accent: string
  detail: string
  icon: string
}) {
  return (
    <article className="rounded-[1.45rem] bg-white p-5 shadow-[0_18px_45px_rgba(25,39,52,0.06)]">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-[0.68rem] font-bold uppercase tracking-[0.2em] text-[#7b8896]">
            {label}
          </p>
          <p className="mt-3 text-[2rem] font-semibold leading-none tracking-[-0.05em] text-[#243648]">
            {String(value).padStart(2, '0')}
          </p>
        </div>
        <span className="text-sm font-semibold" style={{ color: accent }}>
          {icon}
        </span>
      </div>
      <p className="mt-4 text-xs font-semibold text-[#6e7c8d]">{detail}</p>
    </article>
  )
}
