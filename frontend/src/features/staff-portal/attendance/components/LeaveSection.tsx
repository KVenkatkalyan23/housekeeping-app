import { useState } from 'react'
import { useSelector } from 'react-redux'
import { toast } from 'react-toastify'

import type { RootState } from '../../../../app/store'
import { useApplyLeaveMutation } from '../../leave/api'
import type { LeaveFormErrors, LeaveType } from '../../leave/types'

const leaveTypeOptions: Array<{ label: string; value: LeaveType }> = [
  { label: 'Planned Leave', value: 'PLANNED' },
  { label: 'Sick Leave', value: 'SICK' },
]

function parseApiError(error: unknown) {
  if (!error || typeof error !== 'object') {
    return 'Unable to submit leave request.'
  }

  const candidate = error as {
    data?: { message?: string; error?: { message?: string; details?: string[] } }
    error?: string
  }

  return (
    candidate.data?.error?.details?.[0] ??
    candidate.data?.message ??
    candidate.data?.error?.message ??
    candidate.error ??
    'Unable to submit leave request.'
  )
}

function calculateDurationDays(fromDate: string, toDate: string) {
  const start = new Date(`${fromDate}T00:00:00`)
  const end = new Date(`${toDate}T00:00:00`)
  const millisecondsPerDay = 1000 * 60 * 60 * 24
  return Math.floor((end.getTime() - start.getTime()) / millisecondsPerDay) + 1
}

function addDays(date: Date, days: number) {
  const nextDate = new Date(date)
  nextDate.setDate(nextDate.getDate() + days)
  return nextDate
}

function formatDateInput(date: Date) {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

function validateLeaveForm(
  fromDate: string,
  toDate: string,
  leaveType: LeaveType,
  reason: string,
): LeaveFormErrors {
  const errors: LeaveFormErrors = {}
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  if (!fromDate) {
    errors.fromDate = 'From date is required.'
  }

  if (!toDate) {
    errors.toDate = 'To date is required.'
  }

  if (reason.length > 500) {
    errors.reason = 'Reason must be 500 characters or fewer.'
  }

  if (errors.fromDate || errors.toDate) {
    return errors
  }

  const start = new Date(`${fromDate}T00:00:00`)
  const end = new Date(`${toDate}T00:00:00`)

  if (start < today) {
    errors.fromDate = 'From date must be today or a future date.'
  }

  if (end < today) {
    errors.toDate = 'To date must be today or a future date.'
  }

  if (end < start) {
    errors.toDate = 'To date must be on or after from date.'
    return errors
  }

  const durationDays = calculateDurationDays(fromDate, toDate)
  if (durationDays > 7) {
    errors.toDate = `${leaveType} leave cannot exceed 7 days.`
  }

  if (leaveType === 'PLANNED') {
    const minimumPlannedDate = addDays(today, 2)
    if (start < minimumPlannedDate) {
      errors.fromDate = 'Planned leave must be applied at least 2 days in advance.'
    }
  }

  return errors
}

function FieldError({ message }: { message?: string }) {
  if (!message) {
    return null
  }

  return <p className="mt-2 text-xs font-medium text-[#c55a62]">{message}</p>
}

export function LeaveSection() {
  const userId = useSelector((state: RootState) => state.auth.userId)
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [leaveType, setLeaveType] = useState<LeaveType>('PLANNED')
  const [reason, setReason] = useState('')
  const [errors, setErrors] = useState<LeaveFormErrors>({})
  const [applyLeave, { isLoading }] = useApplyLeaveMutation()

  const minDate = formatDateInput(new Date())

  const handleSubmit = async () => {
    const validationErrors = validateLeaveForm(fromDate, toDate, leaveType, reason)

    if (!userId) {
      validationErrors.form = 'You must be logged in to apply for leave.'
    }

    setErrors(validationErrors)

    if (Object.keys(validationErrors).length > 0 || !userId) {
      return
    }

    try {
      await applyLeave({
        userId,
        fromDate,
        toDate,
        leaveType,
        reason: reason.trim() || undefined,
      }).unwrap()

      setFromDate('')
      setToDate('')
      setLeaveType('PLANNED')
      setReason('')
      setErrors({})
      toast.success('Leave request submitted and approved.')
    } catch (error) {
      const message = parseApiError(error)
      setErrors({ form: message })
      toast.error(message)
    }
  }

  return (
    <section className="mt-6 rounded-[1.75rem] bg-white p-5 shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h2 className="text-[1.55rem] font-semibold tracking-[-0.04em] text-slate-900">
            Apply for Leave
          </h2>
          <p className="mt-1 text-sm text-slate-500">
            Sick leave can start today. Planned leave needs a 2-day notice.
          </p>
        </div>
        <span className="rounded-full bg-[#edf4ff] px-3 py-1 text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-[#5a7cc2]">
          Auto-approved
        </span>
      </div>

      <div className="mt-4 grid grid-cols-2 gap-3">
        <label className="text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-[#7090c8]">
          From Date
          <input
            type="date"
            min={minDate}
            value={fromDate}
            onChange={(event) => setFromDate(event.target.value)}
            className="mt-2 h-12 w-full rounded-2xl border border-slate-100 bg-slate-50 px-4 text-sm font-medium text-slate-700 outline-none focus:border-[#7aa4e6]"
          />
          <FieldError message={errors.fromDate} />
        </label>
        <label className="text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-[#7090c8]">
          To Date
          <input
            type="date"
            min={fromDate || minDate}
            value={toDate}
            onChange={(event) => setToDate(event.target.value)}
            className="mt-2 h-12 w-full rounded-2xl border border-slate-100 bg-slate-50 px-4 text-sm font-medium text-slate-700 outline-none focus:border-[#7aa4e6]"
          />
          <FieldError message={errors.toDate} />
        </label>
      </div>

      <label className="mt-4 block text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-[#7090c8]">
        Leave Type
        <select
          value={leaveType}
          onChange={(event) => setLeaveType(event.target.value as LeaveType)}
          className="mt-2 h-12 w-full rounded-2xl border border-slate-100 bg-slate-50 px-4 text-sm font-medium text-slate-700 outline-none focus:border-[#7aa4e6]"
        >
          {leaveTypeOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </label>

      <label className="mt-4 block text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-[#7090c8]">
        Reason
        <textarea
          value={reason}
          onChange={(event) => setReason(event.target.value)}
          rows={3}
          placeholder="Optional note for your leave request"
          className="mt-2 w-full rounded-2xl border border-slate-100 bg-slate-50 px-4 py-3 text-sm font-medium text-slate-700 outline-none focus:border-[#7aa4e6]"
        />
        <FieldError message={errors.reason} />
      </label>

      {errors.form ? (
        <div className="mt-4 rounded-2xl bg-[#fff2f2] px-4 py-3 text-sm font-medium text-[#b34d58]">
          {errors.form}
        </div>
      ) : null}

      <button
        type="button"
        onClick={handleSubmit}
        disabled={isLoading}
        className="mt-5 flex h-12 w-full items-center justify-center rounded-full bg-[#1664c0] text-sm font-semibold text-white shadow-[0_14px_24px_rgba(22,100,192,0.28)] transition hover:brightness-110 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {isLoading ? 'Submitting...' : 'Submit Request'}
      </button>
    </section>
  )
}
