export function LeaveFab() {
  return (
    <button
      type="button"
      aria-label="Add leave request"
      className="fixed bottom-24 right-5 z-40 flex h-14 w-14 items-center justify-center rounded-2xl bg-[#2849c7] text-white shadow-[0_18px_36px_rgba(40,73,199,0.32)]"
    >
      <svg viewBox="0 0 24 24" fill="none" className="h-6 w-6">
        <path
          d="M12 5v14M5 12h14"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
        />
      </svg>
    </button>
  )
}
