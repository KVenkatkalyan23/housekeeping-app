import type { AdminStaffDirectoryItemStatus } from '../types'

const statusStyles: Record<
  AdminStaffDirectoryItemStatus,
  { label: string; className: string; dotClassName: string }
> = {
  ON_DUTY: {
    label: 'ON-DUTY',
    className: 'bg-[#e3f7e8] text-[#2d9d58]',
    dotClassName: 'bg-[#2dbe64]',
  },
  OFF_DUTY: {
    label: 'OFF-DUTY',
    className: 'bg-[#eef2f7] text-[#708094]',
    dotClassName: 'bg-[#9aa8ba]',
  },
  LEAVE: {
    label: 'LEAVE',
    className: 'bg-[#fff2d9] text-[#cb8b12]',
    dotClassName: 'bg-[#f2a114]',
  },
  SICK: {
    label: 'SICK',
    className: 'bg-[#ffe3e1] text-[#d44944]',
    dotClassName: 'bg-[#e24e47]',
  },
}

export function StaffStatusBadge({
  status,
}: {
  status: AdminStaffDirectoryItemStatus
}) {
  const config = statusStyles[status]

  return (
    <span
      className={[
        'inline-flex items-center gap-2 rounded-full px-3 py-1 text-[0.68rem] font-bold tracking-[0.08em]',
        config.className,
      ].join(' ')}
    >
      <span className={['h-1.5 w-1.5 rounded-full', config.dotClassName].join(' ')} />
      {config.label}
    </span>
  )
}
