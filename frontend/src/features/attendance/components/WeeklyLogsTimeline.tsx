import type { WeeklyAttendanceLogItemResponse } from '../types'
import { WeeklyLogCard } from './WeeklyLogCard'

interface WeeklyLogsTimelineProps {
  logs: WeeklyAttendanceLogItemResponse[]
}

export function WeeklyLogsTimeline({ logs }: WeeklyLogsTimelineProps) {
  return (
    <section className="mt-8">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold tracking-[-0.03em] text-[#23324d]">
          Weekly Logs
        </h2>
        <button
          type="button"
          className="text-[0.7rem] font-bold uppercase tracking-[0.16em] text-[#3c65c7]"
        >
          View Full History
        </button>
      </div>

      <div className="relative mt-4 pl-5">
        <div className="absolute bottom-0 left-[0.38rem] top-2 w-px bg-[#d6e2fb]" />
        <div className="space-y-3.5">
          {logs.map((log) => (
            <div key={log.date} className="relative">
              <span className="absolute -left-5 top-5 h-2.5 w-2.5 rounded-full bg-[#2d63cb]" />
              <WeeklyLogCard log={log} />
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}
