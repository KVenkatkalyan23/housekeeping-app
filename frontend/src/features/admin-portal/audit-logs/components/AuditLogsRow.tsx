import { AuditSeverityBadge } from './AuditSeverityBadge'
import type { AdminAuditLogItem } from '../types'

function formatDateParts(value: string) {
  const date = new Date(value)

  return {
    date: new Intl.DateTimeFormat('en-CA', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    }).format(date),
    time: new Intl.DateTimeFormat('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
    }).format(date),
  }
}

export function AuditLogsRow({ item }: { item: AdminAuditLogItem }) {
  const timestamp = formatDateParts(item.createdAt)

  return (
    <tr className="border-t border-[#edf1f5] text-sm text-[#415160]">
      <td className="whitespace-nowrap px-6 py-5 align-top">
        <p className="font-semibold text-[#293848]">{timestamp.date}</p>
        <p className="mt-1 text-xs text-[#7a8794]">{timestamp.time}</p>
      </td>
      <td className="px-6 py-5 align-top">
        <div className="flex items-start gap-3">
          <span className="mt-1 h-2.5 w-2.5 rounded-full bg-[#0d7b74]" />
          <div>
            <p className="font-semibold text-[#293848]">{item.eventTitle}</p>
            <p className="mt-1 text-[0.68rem] font-semibold uppercase tracking-[0.08em] text-[#7b8997]">
              {item.eventCode}
            </p>
          </div>
        </div>
      </td>
      <td className="px-6 py-5 align-top">
        <span className="inline-flex rounded-full bg-[#f1f3f5] px-3 py-1 text-[0.68rem] font-bold uppercase tracking-[0.08em] text-[#697786]">
          {item.eventCategory}
        </span>
      </td>
      <td className="px-6 py-5 align-top">
        <p className="font-semibold text-[#293848]">{item.actorName}</p>
        <p className="mt-1 text-xs text-[#7a8794]">
          {item.actorSubtitle ?? ' '}
        </p>
      </td>
      <td className="px-6 py-5 align-top">
        <p className="font-semibold uppercase text-[#293848]">
          {item.targetLabel}
        </p>
        <p className="mt-1 text-xs uppercase tracking-[0.08em] text-[#7a8794]">
          {item.targetSubLabel ?? ' '}
        </p>
      </td>
      <td className="px-6 py-5 align-top">
        <AuditSeverityBadge severity={item.severity} />
      </td>
    </tr>
  )
}
