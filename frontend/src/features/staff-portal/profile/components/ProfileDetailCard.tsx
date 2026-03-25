interface ProfileDetailCardProps {
  label: string
  value: string
  icon: 'email' | 'phone' | 'location'
}

function DetailIcon({ icon }: { icon: ProfileDetailCardProps['icon'] }) {
  if (icon === 'phone') {
    return (
      <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5">
        <path
          d="M8.5 5.5h-2a1 1 0 0 0-1 1c0 6.35 5.15 11.5 11.5 11.5a1 1 0 0 0 1-1v-2l-3-1.5-1.35 1.35a9.2 9.2 0 0 1-4.5-4.5L10.5 9l-2-3.5Z"
          stroke="currentColor"
          strokeWidth="1.8"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    )
  }

  if (icon === 'location') {
    return (
      <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5">
        <path
          d="M12 20s6-4.7 6-10a6 6 0 1 0-12 0c0 5.3 6 10 6 10Z"
          stroke="currentColor"
          strokeWidth="1.8"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        <circle cx="12" cy="10" r="2" fill="currentColor" />
      </svg>
    )
  }

  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5">
      <path
        d="M4 7.5 12 13l8-5.5M5.5 6h13A1.5 1.5 0 0 1 20 7.5v9a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 16.5v-9A1.5 1.5 0 0 1 5.5 6Z"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

export function ProfileDetailCard({ label, value, icon }: ProfileDetailCardProps) {
  return (
    <article className="flex items-center gap-4 rounded-[1.55rem] bg-white px-4 py-4 shadow-[0_12px_30px_rgba(15,23,42,0.07)]">
      <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#eef3ff] text-[#2649c7]">
        <DetailIcon icon={icon} />
      </div>
      <div>
        <p className="text-sm font-medium text-slate-500">{label}</p>
        <p className="mt-1 text-[1.02rem] font-semibold text-[#26324d]">{value}</p>
      </div>
    </article>
  )
}
