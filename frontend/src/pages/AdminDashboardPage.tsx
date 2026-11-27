import { useCallback } from 'react'
import { eventApi } from '@/api/eventApi'
import { ErrorState } from '@/components/ui/ErrorState'
import { PageLoader } from '@/components/ui/PageLoader'
import { useAsync } from '@/hooks/useAsync'
import { getErrorMessage } from '@/utils/errorHandler'

interface DashboardStats {
  totalEvents: number
  publishedEvents: number
  upcomingPublishedEvents: number
}

async function loadDashboardStats(signal: AbortSignal): Promise<DashboardStats> {
  const [allEvents, firstPublishedPage] = await Promise.all([
    eventApi.adminList({ page: 0, size: 1 }, signal),
    eventApi.adminList({ page: 0, size: 100, status: 'PUBLISHED' }, signal),
  ])
  let upcomingPublishedEvents = 0
  let publishedPage = firstPublishedPage
  for (let page = 0; page < firstPublishedPage.totalPages; page += 1) {
    if (page > 0) publishedPage = await eventApi.adminList({ page, size: 100, status: 'PUBLISHED' }, signal)
    upcomingPublishedEvents += publishedPage.content.filter(
      (event) => new Date(`${event.startDateTime}Z`).getTime() >= Date.now(),
    ).length
  }
  return {
    totalEvents: allEvents.totalElements,
    publishedEvents: firstPublishedPage.totalElements,
    upcomingPublishedEvents,
  }
}

export function AdminDashboardPage() {
  const fetchStats = useCallback((signal: AbortSignal) => loadDashboardStats(signal), [])
  const { data, loading, error, refetch } = useAsync(fetchStats)
  if (loading) return <PageLoader label="Loading dashboard..." />
  if (error) return <ErrorState message={getErrorMessage(error)} onRetry={refetch} />
  if (!data) return <ErrorState message="Dashboard data could not be displayed. Please try again." onRetry={refetch} />

  const cards = [
    { title: 'Total events', value: data.totalEvents },
    { title: 'Published events', value: data.publishedEvents },
    { title: 'Upcoming published events', value: data.upcomingPublishedEvents },
  ]
  return (
    <div>
      <h2 className="text-2xl font-bold text-slate-950">Dashboard</h2>
      <p className="mt-2 text-slate-600">A quick overview of your event catalogue.</p>
      <div className="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {cards.map((card) => (
          <article key={card.title} className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
            <p className="text-sm font-medium text-slate-600">{card.title}</p>
            <p className="mt-3 text-3xl font-bold text-slate-950">{card.value}</p>
          </article>
        ))}
      </div>
      <p className="mt-5 text-sm text-slate-500">Registration totals aren’t available from the current summary APIs.</p>
    </div>
  )
}
