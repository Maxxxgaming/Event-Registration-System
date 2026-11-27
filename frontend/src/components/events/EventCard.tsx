import { Link } from 'react-router-dom'
import type { EventResponse } from '@/types'
import { formatDateTime } from '@/utils/formatters'
import { SeatsBadge } from '@/components/common/SeatsBadge'

export function EventCard({ event }: { event: EventResponse }) {
  return (
    <article className="flex h-full flex-col rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
      <div className="flex items-start justify-between gap-3">
        <h2 className="text-lg font-semibold leading-snug text-slate-950">{event.title}</h2>
        <SeatsBadge event={event} />
      </div>
      <p className="mt-4 text-sm font-medium text-slate-700">{formatDateTime(event.startDateTime)}</p>
      <p className="mt-1 text-sm text-slate-600">{event.location}</p>
      {event.description && <p className="mt-3 line-clamp-2 text-sm leading-6 text-slate-600">{event.description}</p>}
      <Link
        to={`/events/${event.id}`}
        className="mt-auto inline-flex min-h-11 items-center pt-4 text-sm font-semibold text-brand-700 hover:text-brand-800 focus-visible:rounded"
        aria-label={`View details for ${event.title}`}
      >
        View details <span aria-hidden="true" className="ml-1">→</span>
      </Link>
    </article>
  )
}
