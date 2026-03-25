interface LogoutConfirmModalProps {
  open: boolean
  title?: string
  message?: string
  confirmLabel?: string
  onCancel: () => void
  onConfirm: () => void
}

export function LogoutConfirmModal({
  open,
  title = 'Log out while on duty?',
  message = 'You are currently on duty. Are you sure you want to log out?',
  confirmLabel = 'Continue',
  onCancel,
  onConfirm,
}: LogoutConfirmModalProps) {
  if (!open) {
    return null
  }

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-slate-950/30 px-4 pb-6 pt-10 sm:items-center">
      <div className="w-full max-w-sm rounded-[1.75rem] bg-white p-5 shadow-[0_24px_60px_rgba(15,23,42,0.2)]">
        <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
        <p className="mt-2 text-sm leading-6 text-slate-500">{message}</p>
        <div className="mt-5 grid grid-cols-2 gap-3">
          <button
            type="button"
            onClick={onCancel}
            className="h-11 rounded-full border border-slate-200 text-sm font-semibold text-slate-600"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className="h-11 rounded-full bg-[#1664c0] text-sm font-semibold text-white"
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
