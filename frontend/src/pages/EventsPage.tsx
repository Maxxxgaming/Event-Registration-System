import { useCallback, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { eventApi } from '@/api/eventApi'
import { EventCard } from '@/components/events/EventCard'
import { EventGridSkeleton } from '@/components/events/EventGridSkeleton'
import { PageHeader } from '@/components/common/PageHeader'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { Input } from '@/components/ui/Input'
import { Pagination } from '@/components/ui/Pagination'
import { useAsync } from '@/hooks/useAsync'
import { useDebounce } from '@/hooks/useDebounce'
import { getErrorMessage } from '@/utils/errorHandler'

export function EventsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const page = Math.max(0, Number.parseInt(searchParams.get('page') ?? '0', 10) || 0)
  const search = searchParams.get('search') ?? ''

  const fetchEvents = useCallback(
    (signal: AbortSignal) => eventApi.list({ page, size: 9, search: search || undefined }, signal),
    [page, search],
  )
  const { data, loading, error, refetch } = useAsync(fetchEvents)

  const changePage = (nextPage: number) => {
    const next = new URLSearchParams(searchParams)
    if (nextPage > 0) next.set('page', String(nextPage))
    else next.delete('page')
    setSearchParams(next)
  }

  return (
    <div className="page-shell py-10 sm:py-14">
      <PageHeader title="Discover events" description="Find something happening in your community." />
      <div className="mb-7 max-w-xl">
        <EventsSearch key={search} search={search} />
      </div>
      {loading && <EventGridSkeleton />}
      {!loading && error !== null && <ErrorState message={getErrorMessage(error)} onRetry={refetch} />}
      {!loading && error === null && data === null && (
        <ErrorState message="Events could not be displayed. Please try again." onRetry={refetch} />
      )}
      {!loading && !error && data && data.content.length === 0 && (
        <EmptyState title="No events found" description="Try another search or check back later for new events." />
      )}
      {!loading && !error && data && data.content.length > 0 && (
        <>
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {data.content.map((event) => <EventCard key={event.id} event={event} />)}
          </div>
          <div className="mt-8">
            <Pagination page={data.page} totalPages={data.totalPages} onPageChange={changePage} />
          </div>
        </>
      )}
    </div>
  )
}

function EventsSearch({ search }: { search: string }) {
  const [searchParams, setSearchParams] = useSearchParams()
  const [searchInput, setSearchInput] = useState(search)
  const debouncedSearch = useDebounce(searchInput.trim())

  useEffect(() => {
    if (debouncedSearch === search) return
    const next = new URLSearchParams(searchParams)
    if (debouncedSearch) next.set('search', debouncedSearch)
    else next.delete('search')
    next.delete('page')
    setSearchParams(next)
  }, [debouncedSearch, search, searchParams, setSearchParams])

  return (
    <Input
      label="Search events"
      name="searchEvents"
      type="search"
      placeholder="Search by title or location"
      value={searchInput}
      onChange={(event) => setSearchInput(event.target.value)}
    />
  )
}
