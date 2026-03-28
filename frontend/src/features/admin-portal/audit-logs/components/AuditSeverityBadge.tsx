import type { AuditSeverity } from '../types'

const severityClasses: Record<AuditSeverity, string> = {
  SUCCESS: 'bg-[#0f7f77] text-white',
  INFO: 'bg-[#eef1f4] text-[#586878]',
  WARNING: 'bg-[#9b6a12] text-white',
  ERROR: 'bg-[#ca302f] text-white',
}

export function AuditSeverityBadge({ severity }: { severity: AuditSeverity }) {
  return (
    <span
      className={[
        'inline-flex rounded-full px-2.5 py-1 text-[0.62rem] font-bold uppercase tracking-[0.08em]',
        severityClasses[severity],
      ].join(' ')}
    >
      {severity}
    </span>
  )
}
