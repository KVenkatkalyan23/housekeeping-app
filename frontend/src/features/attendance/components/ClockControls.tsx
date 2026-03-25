interface ClockControlsProps {
  isOnDuty: boolean
  isClockingIn: boolean
  isClockingOut: boolean
  onClockIn: () => void
  onClockOut: () => void
}

function ActionIcon({ type }: { type: 'in' | 'out' }) {
  const isClockOut = type === 'out'

  return (
    <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5">
      <path
        d="M12 5v14"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        opacity={isClockOut ? 0 : 1}
      />
      <path
        d={isClockOut ? 'M10 8l4 4-4 4' : 'M14 8l-4 4 4 4'}
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d={isClockOut ? 'M6 12h8' : 'M10 12h8'}
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
    </svg>
  )
}

export function ClockControls({
  isOnDuty,
  isClockingIn,
  isClockingOut,
  onClockIn,
  onClockOut,
}: ClockControlsProps) {
  return (
    <section className="rounded-[1.75rem] bg-white p-4 shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
      <div className="grid grid-cols-2 gap-3">
        <button
          type="button"
          onClick={onClockIn}
          disabled={isOnDuty || isClockingIn || isClockingOut}
          className="flex h-20 flex-col items-center justify-center gap-2 rounded-[1.2rem] bg-slate-100 text-slate-400 transition enabled:hover:bg-slate-200 disabled:cursor-not-allowed disabled:opacity-75"
        >
          <ActionIcon type="in" />
          <span className="text-sm font-semibold">
            {isClockingIn ? 'Clocking In...' : 'Clock In'}
          </span>
        </button>

        <button
          type="button"
          onClick={onClockOut}
          disabled={!isOnDuty || isClockingOut || isClockingIn}
          className="flex h-20 flex-col items-center justify-center gap-2 rounded-[1.2rem] bg-[#fbe6e3] text-[#7a3532] transition enabled:hover:bg-[#f8d8d3] disabled:cursor-not-allowed disabled:opacity-75"
        >
          <ActionIcon type="out" />
          <span className="text-sm font-semibold">
            {isClockingOut ? 'Clocking Out...' : 'Clock Out'}
          </span>
        </button>
      </div>
    </section>
  )
}
