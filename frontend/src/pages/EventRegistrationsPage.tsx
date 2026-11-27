import { useCallback } from 'react'
import { Link, useParams } from 'react-router-dom'
import { eventApi } from '@/api/eventApi'
import { registrationApi } from '@/api/registrationApi'
import type { EventResponse, RegistrationResponse } from '@/types'
import { PageHeader } from '@/components/common/PageHeader'
import { StatusBadge } from '@/components/common/StatusBadge'
import { Button } from '@/components/ui/Button'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { PageLoader } from '@/components/ui/PageLoader'
import { useAsync } from '@/hooks/useAsync'
import { formatDateTime } from '@/utils/formatters'
import { getErrorMessage } from '@/utils/errorHandler'

interface EventAttendeeData {
  event: EventResponse
  registrations: RegistrationResponse[]
}

export function EventRegistrationsPage() {
  const { id } = useParams()
  const eventId = Number(id)
  const fetchData = useCallback(async (signal: AbortSignal): Promise<EventAttendeeData> => {
    if (!Number.isSafeInteger(eventId) || eventId < 1) throw new Error('Invalid event ID')
    const [event, registrations] = await Promise.all([
      eventApi.getById(eventId, signal),
      registrationApi.getEventRegistrations(eventId, signal),
    ])
    return { event, registrations }
  }, [eventId])
  const { data, loading, error, refetch } = useAsync(fetchData)

  const downloadCsv = () => {
    if (!data) return
    const rows = [
      ['Name', 'Status', 'Registered at'],
      ...data.registrations.map((registration) => [
        registration.userFullName,
        registration.status,
        registration.registeredAt,
      ]),
    ]
    const csv = rows.map((row) => row.map((field) => `"${field.replaceAll('"', '""')}"`).join(',')).join('\r\n')
    const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = `event-${data.event.id}-registrations.csv`
    anchor.click()
    URL.revokeObjectURL(url)
  }

  if (loading) return <PageLoader label="Loading registrations..." />
  if (error) return <ErrorState message={getErrorMessage(error)} onRetry={refetch} />
  if (!data) return <ErrorState message="Registration data could not be displayed. Please try again." onRetry={refetch} />

  return (
    <div>
      <PageHeader
        title="Event registrations"
        description={`${data.event.title} · ${formatDateTime(data.event.startDateTime)} · ${data.event.location}`}
        action={<Button variant="secondary" disabled={data.registrations.length === 0} onClick={downloadCsv}>Download CSV</Button>}
      />
      <div className="mb-5 flex flex-wrap items-center gap-3">
        <div className="rounded-lg border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700">
          {formatDateTime(data.event.startDateTime)} at {data.event.location}
        </div>
        <div className="rounded-lg border border-slate-200 bg-white px-4 py-3 text-sm">
          <span className="text-slate-600">Registered: </span>
          <span className="font-semibold text-slate-900">{data.event.registeredCount} / {data.event.capacity}</span>
        </div>
        <StatusBadge status={data.event.status} />
      </div>
      <p className="mb-5 text-sm text-slate-600">
        The registrations endpoint does not include attendee email addresses, so the attendee list and CSV contain only fields provided by the API.
      </p>
      {data.registrations.length === 0 ? (
        <EmptyState title="No registrations yet" description="Attendees will appear here after registering for this event." action={<Link to="/admin/events" className="font-semibold text-brand-700">Back to events</Link>} />
      ) : (
        <>
          <div className="hidden overflow-x-auto rounded-xl border border-slate-200 bg-white sm:block">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-600">
                <tr><th className="px-4 py-3">Attendee</th><th className="px-4 py-3">Status</th><th className="px-4 py-3">Registered at</th></tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.registrations.map((registration) => <AttendeeRow key={registration.id} registration={registration} />)}
              </tbody>
            </table>
          </div>
          <div className="space-y-3 sm:hidden">
            {data.registrations.map((registration) => (
              <article key={registration.id} className="rounded-xl border border-slate-200 bg-white p-4">
                <div className="flex items-start justify-between gap-3"><h2 className="font-semibold text-slate-950">{registration.userFullName}</h2><StatusBadge status={registration.status} /></div>
                <p className="mt-2 text-sm text-slate-600">Registered {formatDateTime(registration.registeredAt)}</p>
              </article>
            ))}
          </div>
        </>
      )}
    </div>
  )
}

function AttendeeRow({ registration }: { registration: RegistrationResponse }) {
  return (
    <tr>
      <td className="px-4 py-4 font-medium text-slate-900">{registration.userFullName}</td>
      <td className="px-4 py-4"><StatusBadge status={registration.status} /></td>
      <td className="px-4 py-4 text-slate-600">{formatDateTime(registration.registeredAt)}</td>
    </tr>
  )
}
