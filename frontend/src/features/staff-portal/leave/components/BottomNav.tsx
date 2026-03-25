import { useNavigate } from 'react-router-dom'

const items = [
  { label: 'Tasks', path: '/staff/attendance' },
  { label: 'Attendance', path: '/staff/attendance/history' },
  { label: 'Leave', path: '/staff/leave' },
  { label: 'Profile', path: '/staff/profile' },
]

function NavIcon({ active }: { active: boolean }) {
  return (
    <span
      className={`inline-flex h-5 w-5 items-center justify-center rounded-md border ${
        active ? 'border-[#2849c7] bg-[#2849c7] text-white' : 'border-slate-300 text-slate-400'
      }`}
    >
      <span className="h-2.5 w-2.5 rounded-[3px] bg-current" />
    </span>
  )
}

export function BottomNav() {
  const navigate = useNavigate()

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-30">
      <div className="mx-auto max-w-md rounded-t-[1.75rem] bg-white px-6 py-4 shadow-[0_-8px_30px_rgba(15,23,42,0.08)]">
        <ul className="grid grid-cols-4 gap-2">
          {items.map((item) => (
            <li key={item.label} className="flex justify-center">
              <button
                type="button"
                onClick={() => {
                  if (item.path !== '#') {
                    navigate(item.path)
                  }
                }}
                className={`flex flex-col items-center gap-1 text-[0.65rem] font-semibold uppercase tracking-[0.08em] ${
                  item.label === 'Leave' ? 'text-[#2849c7]' : 'text-slate-400'
                }`}
              >
                <NavIcon active={item.label === 'Leave'} />
                <span>{item.label}</span>
              </button>
            </li>
          ))}
        </ul>
      </div>
    </nav>
  )
}
