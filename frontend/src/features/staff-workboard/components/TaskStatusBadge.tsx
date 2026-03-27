interface TaskStatusBadgeProps {
  status: "ASSIGNED" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";
}

function getStatusTone(status: TaskStatusBadgeProps["status"]) {
  switch (status) {
    case "COMPLETED":
      return "bg-[#dcfce7] text-[#166534]";
    case "IN_PROGRESS":
      return "bg-[#dbeafe] text-[#1d4ed8]";
    case "ASSIGNED":
      return "bg-[#fef3c7] text-[#92400e]";
    default:
      return "bg-slate-100 text-slate-500";
  }
}

export function TaskStatusBadge({ status }: TaskStatusBadgeProps) {
  return (
    <span
      className={`rounded-full px-2.5 py-1 text-[0.65rem] font-bold uppercase tracking-[0.16em] ${getStatusTone(
        status
      )}`}
    >
      {status}
    </span>
  );
}

