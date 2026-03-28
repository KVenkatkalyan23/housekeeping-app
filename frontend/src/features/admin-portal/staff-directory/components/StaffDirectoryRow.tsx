import type { AdminStaffDirectoryItem } from '../types'
import { StaffStatusBadge } from './StaffStatusBadge'

function MailIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" aria-hidden="true">
      <path
        d="M5 6.5h14A1.5 1.5 0 0 1 20.5 8v8A1.5 1.5 0 0 1 19 17.5H5A1.5 1.5 0 0 1 3.5 16V8A1.5 1.5 0 0 1 5 6.5Zm0 1.5v.17l7 4.38 7-4.38V8H5Zm14 8V9.93l-6.6 4.12a.75.75 0 0 1-.8 0L5 9.93V16h14Z"
        fill="currentColor"
      />
    </svg>
  )
}

function PhoneIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" aria-hidden="true">
      <path
        d="M7.7 4.5h2.15c.35 0 .66.24.74.58l.62 2.62a.75.75 0 0 1-.22.72l-1.37 1.3a12.88 12.88 0 0 0 4.66 4.66l1.3-1.37a.75.75 0 0 1 .72-.22l2.62.62c.34.08.58.39.58.74v2.15a.75.75 0 0 1-.75.75h-.8C9.93 20.5 3.5 14.07 3.5 6V5.25c0-.41.34-.75.75-.75H7.7Z"
        fill="currentColor"
      />
    </svg>
  )
}

function MoreIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5" aria-hidden="true">
      <path
        d="M12 6.75a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3Zm0 6.75a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3Zm0 6.75a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3Z"
        fill="currentColor"
      />
    </svg>
  )
}

function getAvatarLabel(name: string) {
  const parts = name.trim().split(/\s+/)
  const first = parts[0]?.charAt(0) ?? 'S'
  const second = parts[1]?.charAt(0) ?? ''
  return `${first}${second}`.toUpperCase()
}

export function StaffDirectoryRow({ item }: { item: AdminStaffDirectoryItem }) {
  return (
    <div className="grid grid-cols-1 gap-4 border-t border-[#edf1f5] px-4 py-4 text-sm text-[#263749] md:grid-cols-[2fr_1.35fr_1.9fr_0.45fr] md:items-center md:px-6">
      <div className="flex items-center gap-4">
        <div className="relative flex h-12 w-12 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#1f7e96,#244868)] text-sm font-bold text-white shadow-[0_12px_24px_rgba(31,126,150,0.18)]">
          {getAvatarLabel(item.fullName)}
          <span
            className={[
              'absolute bottom-0 right-0 h-3.5 w-3.5 rounded-full border-2 border-white',
              item.status === 'ON_DUTY'
                ? 'bg-[#2dbe64]'
                : item.status === 'OFF_DUTY'
                  ? 'bg-[#a0aec0]'
                  : item.status === 'LEAVE'
                    ? 'bg-[#f2a114]'
                    : 'bg-[#e24e47]',
            ].join(' ')}
          />
        </div>

        <div>
          <p className="text-base font-semibold tracking-[-0.03em] text-[#223346]">
            {item.fullName}
          </p>
          <p className="mt-0.5 text-xs font-semibold uppercase tracking-[0.12em] text-[#8b97a7]">
            ID: {item.staffCode}
          </p>
        </div>
      </div>

      <div>
        <p className="mb-2 text-[0.68rem] font-bold uppercase tracking-[0.18em] text-[#8c99a8] md:hidden">
          Current Status
        </p>
        <StaffStatusBadge status={item.status} />
      </div>

      <div className="space-y-1.5 text-[#506173]">
        <p className="mb-2 text-[0.68rem] font-bold uppercase tracking-[0.18em] text-[#8c99a8] md:hidden">
          Contact Info
        </p>
        <p className="flex items-center gap-2">
          {MailIcon()}
          <span className="break-all">{item.email ?? 'No email available'}</span>
        </p>
        <p className="flex items-center gap-2">
          {PhoneIcon()}
          <span className="break-all">{item.phone ?? 'No phone available'}</span>
        </p>
      </div>

      <div className="flex justify-start md:justify-end">
        <p className="mr-3 self-center text-[0.68rem] font-bold uppercase tracking-[0.18em] text-[#8c99a8] md:hidden">
          Actions
        </p>
        <button
          type="button"
          className="inline-flex h-10 w-10 items-center justify-center rounded-full text-[#6f7f90] transition hover:bg-[#f4f7fa]"
          aria-label={`Actions for ${item.fullName}`}
        >
          {MoreIcon()}
        </button>
      </div>
    </div>
  )
}
