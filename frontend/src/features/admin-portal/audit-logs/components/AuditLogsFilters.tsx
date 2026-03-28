import type { AuditLogCategoryFilter } from '../types'

const tabs: Array<{ label: string; value: AuditLogCategoryFilter }> = [
  { label: 'All Actions', value: 'ALL' },
  { label: 'Operations', value: 'SYSTEM' },
]

export function AuditLogsFilters({
  value,
  loading,
  onChange,
}: {
  value: AuditLogCategoryFilter
  loading: boolean
  onChange: (value: AuditLogCategoryFilter) => void
}) {
  return (
    <div className="mb-6 flex flex-wrap gap-3">
      {tabs.map((tab) => {
        const active = value === tab.value

        return (
          <button
            key={tab.value}
            type="button"
            disabled={loading && !active}
            onClick={() => onChange(tab.value)}
            className={[
              'rounded-full px-5 py-3 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-70',
              active
                ? 'bg-[#0d7b74] text-white shadow-[0_14px_30px_rgba(13,123,116,0.28)]'
                : 'bg-[#f2f4f6] text-[#5f6f80] hover:bg-[#e9eef2]',
            ].join(' ')}
          >
            {tab.label}
          </button>
        )
      })}
    </div>
  )
}
