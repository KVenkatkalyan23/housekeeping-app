export function TaskPriorityBadge({ priority }: { priority: string }) {
  const tone =
    priority === 'HIGH'
      ? 'bg-[#ffe3e1] text-[#d04f4a]'
      : priority === 'MEDIUM'
        ? 'bg-[#edf0f2] text-[#7a848f]'
        : 'bg-[#eef5ef] text-[#6d7d72]'

  return (
    <span className={`inline-flex rounded-md px-2 py-1 text-[0.62rem] font-bold uppercase tracking-[0.08em] ${tone}`}>
      {priority}
    </span>
  )
}
