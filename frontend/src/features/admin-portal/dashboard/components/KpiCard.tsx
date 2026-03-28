import type { ReactNode } from 'react'

interface KpiCardProps {
  title: string
  value: string
  subtitle: string
  accentClassName: string
  icon: ReactNode
  children?: ReactNode
}

export function KpiCard({
  title,
  value,
  subtitle,
  accentClassName,
  icon,
  children,
}: KpiCardProps) {
  return (
    <article
      className={[
        'rounded-[1.4rem] border bg-white p-5 shadow-[0_18px_45px_rgba(25,39,52,0.06)]',
        accentClassName,
      ].join(' ')}
    >
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-[0.72rem] font-bold uppercase tracking-[0.14em] text-[#7b8796]">
            {title}
          </p>
          <p className="mt-3 text-[2.15rem] font-semibold tracking-[-0.05em] text-[#243648]">
            {value}
          </p>
          <p className="mt-1 text-sm text-[#6d7988]">{subtitle}</p>
        </div>
        <div className="text-[#8ba0b1]">{icon}</div>
      </div>
      {children ? <div className="mt-5">{children}</div> : null}
    </article>
  )
}
