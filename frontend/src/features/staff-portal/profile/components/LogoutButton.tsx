interface LogoutButtonProps {
  onLogout: () => void
}

export function LogoutButton({ onLogout }: LogoutButtonProps) {
  return (
    <button
      type="button"
      onClick={onLogout}
      className="mt-8 flex h-14 w-full items-center justify-center gap-3 rounded-[1.4rem] bg-[#ffd7d3] text-lg font-semibold text-[#b73835] shadow-[0_16px_30px_rgba(255,215,211,0.55)]"
    >
      <svg viewBox="0 0 24 24" fill="none" className="h-5 w-5">
        <path
          d="M10 7H7.5A1.5 1.5 0 0 0 6 8.5v7A1.5 1.5 0 0 0 7.5 17H10M14 8l4 4-4 4M18 12H10"
          stroke="currentColor"
          strokeWidth="1.9"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
      Logout from Portal
    </button>
  )
}
