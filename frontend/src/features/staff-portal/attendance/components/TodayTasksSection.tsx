const tasks = [
  {
    room: '402',
    badge: 'OCCUPIED',
    title: 'Daily Refresh Service',
    meta: 'Due 10:30 AM',
    secondary: 'Refil Minibar',
    done: false,
  },
  {
    room: '512',
    badge: 'CHECKOUT',
    title: 'Full Deep Clean',
    meta: 'Due 11:00 AM',
    secondary: '',
    done: false,
  },
  {
    room: '305',
    badge: 'DONE',
    title: 'Daily Refresh Service',
    meta: '',
    secondary: '',
    done: true,
  },
]

export function TodayTasksSection() {
  return (
    <section className="mt-7">
      <div className="flex items-center justify-between">
        <h2 className="text-[1.7rem] font-semibold tracking-[-0.04em] text-slate-900">
          Today&apos;s Tasks
        </h2>
        <span className="text-sm font-semibold text-[#4c78c8]">2 Left</span>
      </div>

      <div className="mt-4 space-y-3.5">
        {tasks.map((task) => (
          <article
            key={task.room}
            className={`rounded-[1.5rem] bg-white px-4 py-4 shadow-[0_14px_40px_rgba(15,23,42,0.08)] ${
              task.done ? 'opacity-45' : ''
            }`}
          >
            <div className="flex items-start justify-between gap-4">
              <div className="flex gap-3">
                <span className="mt-0.5 h-[4.5rem] w-1 rounded-full bg-[#1664c0]" />
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="text-[1.8rem] font-semibold leading-none text-slate-800">
                      {task.room}
                    </h3>
                    <span className="rounded-full bg-[#dfeeff] px-2 py-0.5 text-[0.58rem] font-bold tracking-[0.16em] text-[#5a86ce]">
                      {task.badge}
                    </span>
                  </div>
                  <p className="mt-1.5 text-sm font-semibold text-slate-600">
                    {task.title}
                  </p>
                  <div className="mt-3 flex flex-wrap items-center gap-3 text-[0.72rem] font-medium text-slate-400">
                    {task.meta ? <span>{task.meta}</span> : null}
                    {task.secondary ? <span>{task.secondary}</span> : null}
                  </div>
                </div>
              </div>

              <div
                className={`flex h-8 w-8 items-center justify-center rounded-full border ${
                  task.done
                    ? 'border-[#6ea4dc] bg-[#6ea4dc] text-white'
                    : 'border-[#cfe1fb] bg-white text-[#6ea4dc]'
                }`}
              >
                <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4">
                  <path
                    d="M6.5 12.5 10 16l7.5-8"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
              </div>
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}
