interface WorkboardHeaderProps {
  date: string;
}

function formatDateLabel(date: string) {
  return new Intl.DateTimeFormat("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
  }).format(new Date(`${date}T00:00:00`));
}

export function WorkboardHeader({ date }: WorkboardHeaderProps) {
  return (
    <header className="rounded-[1.9rem] bg-white px-5 py-5 shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
      <p className="text-[0.68rem] font-bold uppercase tracking-[0.22em] text-[#7aa4e6]">
        Staff Workboard
      </p>
      <h1 className="mt-2 text-[2rem] font-semibold tracking-[-0.05em] text-slate-900">
        My Tasks
      </h1>
      <p className="mt-2 text-sm text-slate-500">
        Assigned work for {formatDateLabel(date)}.
      </p>
    </header>
  );
}

