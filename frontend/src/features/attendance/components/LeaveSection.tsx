import { useState } from 'react'
import { toast } from 'react-toastify'

const leaveReasons = ['Sick Leave', 'Emergency Leave', 'Personal Leave']

export function LeaveSection() {
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [reason, setReason] = useState(leaveReasons[0])

  const handleSubmit = () => {
    toast.info('Leave request UI is ready. API integration is not part of this feature yet.')
  }

  return (
    <section className="mt-6 rounded-[1.75rem] bg-white p-5 shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
      <h2 className="text-[1.55rem] font-semibold tracking-[-0.04em] text-slate-900">
        Apply for Leave
      </h2>

      <div className="mt-4 grid grid-cols-2 gap-3">
        <label className="text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-[#7090c8]">
          From Date
          <input
            type="date"
            value={fromDate}
            onChange={(event) => setFromDate(event.target.value)}
            className="mt-2 h-12 w-full rounded-2xl border border-slate-100 bg-slate-50 px-4 text-sm font-medium text-slate-700 outline-none focus:border-[#7aa4e6]"
          />
        </label>
        <label className="text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-[#7090c8]">
          To Date
          <input
            type="date"
            value={toDate}
            onChange={(event) => setToDate(event.target.value)}
            className="mt-2 h-12 w-full rounded-2xl border border-slate-100 bg-slate-50 px-4 text-sm font-medium text-slate-700 outline-none focus:border-[#7aa4e6]"
          />
        </label>
      </div>

      <label className="mt-4 block text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-[#7090c8]">
        Reason
        <select
          value={reason}
          onChange={(event) => setReason(event.target.value)}
          className="mt-2 h-12 w-full rounded-2xl border border-slate-100 bg-slate-50 px-4 text-sm font-medium text-slate-700 outline-none focus:border-[#7aa4e6]"
        >
          {leaveReasons.map((leaveReason) => (
            <option key={leaveReason} value={leaveReason}>
              {leaveReason}
            </option>
          ))}
        </select>
      </label>

      <button
        type="button"
        onClick={handleSubmit}
        className="mt-5 flex h-12 w-full items-center justify-center rounded-full bg-[#1664c0] text-sm font-semibold text-white shadow-[0_14px_24px_rgba(22,100,192,0.28)] transition hover:brightness-110"
      >
        Submit Request
      </button>
    </section>
  )
}
