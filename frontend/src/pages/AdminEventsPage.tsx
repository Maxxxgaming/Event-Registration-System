import { useCallback, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { eventApi } from '@/api/eventApi'
import type { EventStatus } from '@/types'
import { PageHeader } from '@/components/common/PageHeader'
import { StatusBadge } from '@/components/common/StatusBadge'
import { Button } from '@/components/ui/Button'
import { ConfirmDialog } from '@/components/ui/ConfirmDialog'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { Pagination } from '@/components/ui/Pagination'
import { PageLoader } from '@/components/ui/PageLoader'
import { useAsync } from '@/hooks/useAsync'
import { formatDateTime } from '@/utils/formatters'
import { getErrorMessage } from '@/utils/errorHandler'
import { notifyError, notifySuccess } from '@/utils/notify'

const statuses: Array<EventStatus | 'ALL'> = ['ALL', 'DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED']

export function AdminEventsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const page = Math.max(0, Number.parseInt(searchParams.get('page') ?? '0', 10) || 0)
  const rawStatus = searchParams.get('status') ?? 'ALL'
  const status = statuses.includes(rawStatus as EventStatus | 'ALL') ? rawStatus as EventStatus | 'ALL' : 'ALL'
  const [pendingId, setPendingId] = useState<number | null>(null)
  const [cancelTarget, setCancelTarget] = useState<number | null>(null)

  const fetchEvents = useCallback(
    (signal: AbortSignal) => eventApi.adminList({
      page,
      size: 10,
      ...(status === 'ALL' ? {} : { status }),
    }, signal),
    [page, status],
  )
  const { data, loading, error, refetch } = useAsync(fetchEvents)

  const changeFilter = (nextStatus: EventStatus | 'ALL') => {
    const next = new URLSearchParams(searchParams)
    if (nextStatus === 'ALL') next.delete('status')
    else next.set('status', nextStatus)
    next.delete('page')
    setSearchParams(next)
  }

  const changePage = (nextPage: number) => {
    const next = new URLSearchParams(searchParams)
    if (nextPage > 0) next.set('page', String(nextPage))
    else next.delete('page')
    setSearchParams(next)
  }

  const updateStatus = async (id: number, nextStatus: EventStatus) => {
    setPendingId(id)
    try {
      await eventApi.updateStatus(id, { status: nextStatus })
      notifySuccess(nextStatus === 'PUBLISHED' ? 'Event published.' : 'Event unpublished.')
      refetch()
    } catch (requestError: unknown) {
      notifyError(getErrorMessage(requestError))
    } finally {
      setPendingId(null)
    }
  }

  const cancelEvent = async () => {
    if (cancelTarget === null) return
    const id = cancelTarget
    setPendingId(id)
    try {
      await eventApi.cancel(id)
      notifySuccess('Event cancelled.')
      setCancelTarget(null)
      refetch()
    } catch (requestError: unknown) {
      notifyError(getErrorMessage(requestError))
    } finally {
      setPendingId(null)
    }
  }

  return (
    <div>
      <PageHeader
        title="Events"
        description="Create and manage events, status, and attendee lists."
        action={<Link to="/admin/events/new"><Button>Create event</Button></Link>}
      />
      <div className="mb-5 max-w-xs">
        <label htmlFor="event-status-filter" className="mb-1.5 block text-sm font-medium text-slate-800">Filter by status</label>
        <select
          id="event-status-filter"
          className="field-control"
          value={status}
          onChange={(event) => changeFilter(event.target.value as EventStatus | 'ALL')}
        >
          {statuses.map((item) => <option key={item} value={item}>{item === 'ALL' ? 'All statuses' : item.charAt(0) + item.slice(1).toLowerCase()}</option>)}
        </select>
      </div>
      {loading && <PageLoader label="Loading events..." />}
      {!loading && error !== null && <ErrorState message={getErrorMessage(error)} onRetry={refetch} />}
      {!loading && error === null && data === null && (
        <ErrorState message="Events could not be displayed. Please try again." onRetry={refetch} />
      )}
      {!loading && !error && data && data.content.length === 0 && (
        <EmptyState title="No events to show" description="Create an event or choose a different status filter." action={<Link to="/admin/events/new"><Button>Create event</Button></Link>} />
      )}
      {!loading && !error && data && data.content.length > 0 && (
        <>
          <div className="hidden overflow-x-auto rounded-xl border border-slate-200 bg-white md:block">
            <table className="w-full min-w-[780px] text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-600">
                <tr><th className="px-4 py-3">Title</th><th className="px-4 py-3">Date</th><th className="px-4 py-3">Status</th><th className="px-4 py-3">Registered</th><th className="px-4 py-3">Actions</th></tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.content.map((event) => (
                  <tr key={event.id}>
                    <td className="px-4 py-4 font-semibold text-slate-900">{event.title}</td>
                    <td className="px-4 py-4 text-slate-600">{formatDateTime(event.startDateTime)}</td>
                    <td className="px-4 py-4"><StatusBadge status={event.status} /></td>
                    <td className="px-4 py-4 text-slate-600">{event.registeredCount} / {event.capacity}</td>
                    <td className="px-4 py-4"><EventActions eventId={event.id} status={event.status} pending={pendingId !== null} onStatus={updateStatus} onCancel={() => setCancelTarget(event.id)} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="space-y-4 md:hidden">
            {data.content.map((event) => (
              <article key={event.id} className="rounded-xl border border-slate-200 bg-white p-4">
                <div className="flex items-start justify-between gap-3"><h2 className="font-semibold text-slate-950">{event.title}</h2><StatusBadge status={event.status} /></div>
                <p className="mt-2 text-sm text-slate-600">{formatDateTime(event.startDateTime)}</p>
                <p className="mt-1 text-sm text-slate-600">{event.registeredCount} / {event.capacity} registered</p>
                <div className="mt-4"><EventActions eventId={event.id} status={event.status} pending={pendingId !== null} onStatus={updateStatus} onCancel={() => setCancelTarget(event.id)} /></div>
              </article>
            ))}
          </div>
          <div className="mt-6"><Pagination page={data.page} totalPages={data.totalPages} onPageChange={changePage} /></div>
        </>
      )}
      <ConfirmDialog
        open={cancelTarget !== null}
        onClose={() => setCancelTarget(null)}
        onConfirm={() => void cancelEvent()}
        title="Cancel this event?"
        message="Attendees will no longer be able to register for this event."
        confirmLabel="Cancel event"
        loading={pendingId === cancelTarget}
      />
    </div>
  )
}

function EventActions({
  eventId,
  status,
  pending,
  onStatus,
  onCancel,
}: {
  eventId: number
  status: EventStatus
  pending: boolean
  onStatus: (id: number, status: EventStatus) => void
  onCancel: () => void
}) {
  const isPublished = status === 'PUBLISHED'
  const canChangeStatus = status === 'DRAFT' || status === 'PUBLISHED'
  return (
    <div className="flex flex-wrap items-center gap-2">
      <Link to={`/admin/events/${eventId}/edit`} className="inline-flex min-h-9 items-center rounded-md px-2 text-xs font-semibold text-brand-700 hover:bg-brand-50">Edit</Link>
      {canChangeStatus && (
        <Button variant="secondary" className="min-h-9 px-2 text-xs" disabled={pending} onClick={() => onStatus(eventId, isPublished ? 'DRAFT' : 'PUBLISHED')}>
          {isPublished ? 'Unpublish' : 'Publish'}
        </Button>
      )}
      {status !== 'CANCELLED' && status !== 'COMPLETED' && (
        <Button variant="danger" className="min-h-9 px-2 text-xs" disabled={pending} onClick={onCancel}>Cancel</Button>
      )}
      <Link to={`/admin/events/${eventId}/registrations`} className="inline-flex min-h-9 items-center rounded-md px-2 text-xs font-semibold text-slate-700 hover:bg-slate-100">Registrations</Link>
    </div>
  )
}
