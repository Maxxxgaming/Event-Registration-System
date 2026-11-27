import type { EventStatus, RegistrationStatus } from '@/types'

type Status = EventStatus | RegistrationStatus

const labels: Record<Status, string> = {
  DRAFT: 'Draft',
  PUBLISHED: 'Published',
  CANCELLED: 'Cancelled',
  COMPLETED: 'Completed',
  REGISTERED: 'Registered',
}

const styles: Record<Status, string> = {
  DRAFT: 'bg-slate-100 text-slate-700',
  PUBLISHED: 'bg-emerald-100 text-emerald-800',
  CANCELLED: 'bg-rose-100 text-rose-800',
  COMPLETED: 'bg-indigo-100 text-indigo-800',
  REGISTERED: 'bg-emerald-100 text-emerald-800',
}

export function StatusBadge({ status }: { status: Status }) {
  return (
    <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${styles[status]}`}>
      {labels[status]}
    </span>
  )
}
