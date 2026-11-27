import { useCallback } from 'react'
import { Link } from 'react-router-dom'
import { eventApi } from '@/api/eventApi'
import type { EventResponse } from '@/types'
import { Button } from '@/components/ui/Button'
import { EventCard } from '@/components/events/EventCard'
import { EventGridSkeleton } from '@/components/events/EventGridSkeleton'
import { ErrorState } from '@/components/ui/ErrorState'
import { getErrorMessage } from '@/utils/errorHandler'
import { useAsync } from '@/hooks/useAsync'

async function loadUpcomingEvents(signal: AbortSignal): Promise<EventResponse[]> {
  const now = Date.now()
  const upcoming: EventResponse[] = []
  let page = 0
  let result = await eventApi.list({ page, size: 100 }, signal)
  while (true) {
    upcoming.push(...result.content.filter((event) => new Date(`${event.startDateTime}Z`).getTime() >= now))
    if (upcoming.length >= 3 || result.last) return upcoming.slice(0, 3)
    page += 1
    result = await eventApi.list({ page, size: 100 }, signal)
  }
}

export function HomePage() {
  const fetchEvents = useCallback(
    (signal: AbortSignal) => loadUpcomingEvents(signal),
    [],
  )
  const { data, loading, error, refetch } = useAsync(fetchEvents)
  const upcoming = data ?? []

  return (
    <div>
      <div className="page-shell py-16 sm:py-24">
        <section className="overflow-hidden rounded-3xl bg-gradient-to-br from-brand-50 via-white to-indigo-100 px-6 py-16 text-center sm:px-12">
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-700">Find your next moment</p>
          <h1 className="mx-auto mt-5 max-w-3xl text-4xl font-bold tracking-tight text-slate-950 sm:text-6xl">
            Good things happen when we gather.
          </h1>
          <p className="mx-auto mt-6 max-w-xl text-lg leading-8 text-slate-600">
            Discover welcoming events, meet your community, and save a place for what matters.
          </p>
          <Link to="/events" className="mt-8 inline-flex"><Button>Explore events</Button></Link>
        </section>
      </div>
      <section className="page-shell pb-16">
        <div className="mb-6 flex items-end justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold text-slate-950">Coming up</h2>
            <p className="mt-1 text-slate-600">A few events worth saving a seat for.</p>
          </div>
          <Link to="/events" className="text-sm font-semibold text-brand-700 hover:underline">View all events</Link>
        </div>
        {loading && <EventGridSkeleton count={3} />}
        {!loading && error !== null && <ErrorState message={getErrorMessage(error)} onRetry={refetch} />}
        {!loading && error === null && data === null && (
          <ErrorState message="Upcoming events could not be displayed. Please try again." onRetry={refetch} />
        )}
        {!loading && !error && data && upcoming.length === 0 && (
          <p className="rounded-xl border border-slate-200 bg-white p-6 text-slate-600">No upcoming events are listed right now. Check back soon.</p>
        )}
        {!loading && !error && upcoming.length > 0 && (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {upcoming.map((event) => <EventCard key={event.id} event={event} />)}
          </div>
        )}
      </section>
    </div>
  )
}
