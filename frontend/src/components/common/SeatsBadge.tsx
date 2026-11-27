import type { EventResponse } from '@/types'
import { useCurrentTime } from '@/hooks/useCurrentTime'

export function SeatsBadge({ event }: { event: EventResponse }) {
  const now = useCurrentTime()
  const deadlinePassed = now !== null
    && new Date(`${event.registrationDeadline}Z`).getTime() < now
  if (event.status !== 'PUBLISHED' || deadlinePassed) {
    return <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-700">Registration closed</span>
  }
  if (event.availableSeats <= 0) {
    return <span className="rounded-full bg-rose-100 px-2.5 py-1 text-xs font-semibold text-rose-800">Full</span>
  }
  return (
    <span className="rounded-full bg-emerald-100 px-2.5 py-1 text-xs font-semibold text-emerald-800">
      {event.availableSeats} {event.availableSeats === 1 ? 'seat' : 'seats'} left
    </span>
  )
}
