import { useSelector } from 'react-redux'

import type { RootState } from '../../../../app/store'

export function ProfileHeader() {
  const username = useSelector((state: RootState) => state.auth.username)

  return (
    <header className="flex items-center justify-between gap-4 border-b border-slate-100 pb-4">
      <div className="flex items-center gap-3">
        <div className="flex h-11 w-11 items-center justify-center rounded-full bg-[#edf2ff] text-[#2649c7] shadow-[0_10px_24px_rgba(15,23,42,0.08)]">
          <span className="text-lg font-semibold">
            {(username ?? 'S').slice(0, 1).toUpperCase()}
          </span>
        </div>
        <p className="text-[1.1rem] font-semibold text-[#2649c7]">Housekeeping</p>
      </div>

      <button
        type="button"
        aria-label="Notifications"
        className="flex h-10 w-10 items-center justify-center rounded-full bg-white text-slate-500 shadow-[0_12px_24px_rgba(15,23,42,0.08)]"
      >
        <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5">
          <path
            d="M12 5a4 4 0 0 0-4 4v2.2c0 .7-.24 1.37-.67 1.92L6 14.8h12l-1.33-1.68a3.1 3.1 0 0 1-.67-1.92V9a4 4 0 0 0-4-4Z"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
          <path
            d="M10 18a2 2 0 0 0 4 0"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
          />
        </svg>
      </button>
    </header>
  )
}
