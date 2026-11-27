import { useCallback, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { registrationApi } from '@/api/registrationApi'
import { PageHeader } from '@/components/common/PageHeader'
import { StatusBadge } from '@/components/common/StatusBadge'
import { Button } from '@/components/ui/Button'
import { ConfirmDialog } from '@/components/ui/ConfirmDialog'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { PageLoader } from '@/components/ui/PageLoader'
import { useAsync } from '@/hooks/useAsync'
import { useCurrentTime } from '@/hooks/useCurrentTime'
import type { RegistrationResponse } from '@/types'
import { formatDateTime } from '@/utils/formatters'
import { getErrorMessage } from '@/utils/errorHandler'
import { notifyError, notifySuccess } from '@/utils/notify'

type RegistrationFilter = 'UPCOMING' | 'PAST' | 'CANCELLED'

const filters: RegistrationFilter[] = ['UPCOMING', 'PAST', 'CANCELLED']

function isUpcoming(registration: RegistrationResponse, currentTime: number): boolean {
  return registration.status === 'REGISTERED'
    && new Date(`${registration.eventStartDateTime}Z`).getTime() >= currentTime
}

export function MyRegistrationsPage() {
  const fetchRegistrations = useCallback(
    (signal: AbortSignal) => registrationApi.getMyRegistrations(signal),
    [],
  )
  const { data: registrations, loading, error, refetch } = useAsync(fetchRegistrations)
  const currentTime = useCurrentTime()
  const [filter, setFilter] = useState<RegistrationFilter>('UPCOMING')
  const [selected, setSelected] = useState<RegistrationResponse | null>(null)
  const [pending, setPending] = useState(false)

  const visibleRegistrations = useMemo(() => (registrations ?? [])
    .filter((registration) => {
      if (filter === 'CANCELLED') return registration.status === 'CANCELLED'
      if (filter === 'PAST') return registration.status === 'REGISTERED' && currentTime !== null && !isUpcoming(registration, currentTime)
      return currentTime !== null && isUpcoming(registration, currentTime)
    })
    .sort((left, right) => left.eventStartDateTime.localeCompare(right.eventStartDateTime)),
  [registrations, filter, currentTime])

  const cancel = async () => {
    if (!selected) return
    setPending(true)
    try {
      await registrationApi.cancelRegistration(selected.id)
      notifySuccess('Your registration has been cancelled.')
      setSelected(null)
      refetch()
    } catch (requestError: unknown) {
      notifyError(getErrorMessage(requestError))
    } finally {
      setPending(false)
    }
  }

  if (loading) return <PageLoader label="Loading your registrations..." />
  if (currentTime === null) return <PageLoader label="Preparing your registrations..." />
  if (error) return <div className="page-shell py-12"><ErrorState message={getErrorMessage(error)} onRetry={refetch} /></div>

  return (
    <div className="page-shell py-10 sm:py-14">
      <PageHeader title="My registrations" description="Keep track of the events you’ve signed up for." />
      <div className="mb-6 flex flex-wrap gap-2" role="tablist" aria-label="Filter registrations">
        {filters.map((item) => (
          <button
            key={item}
            id={`registration-tab-${item.toLowerCase()}`}
            type="button"
            role="tab"
            aria-selected={filter === item}
            className={`min-h-10 rounded-full px-4 text-sm font-semibold ${filter === item ? 'bg-brand-600 text-white' : 'bg-white text-slate-700 ring-1 ring-slate-200 hover:bg-slate-50'}`}
            onClick={() => setFilter(item)}
          >
            {item.charAt(0) + item.slice(1).toLowerCase()}
          </button>
        ))}
      </div>
      {visibleRegistrations.length === 0 ? (
        <EmptyState
          title={filter === 'UPCOMING' ? 'No upcoming registrations' : filter === 'PAST' ? 'No past events yet' : 'No cancelled registrations'}
          description="Browse events and find something you’d like to attend."
          action={<Link to="/events"><Button>Browse events</Button></Link>}
        />
      ) : (
        <div className="space-y-4">
          {visibleRegistrations.map((registration) => (
            <article key={registration.id} className="flex flex-col gap-4 rounded-xl border border-slate-200 bg-white p-5 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <div className="flex flex-wrap items-center gap-3">
                  <h2 className="text-lg font-semibold text-slate-950">{registration.eventTitle}</h2>
                  <StatusBadge status={registration.status} />
                </div>
                <p className="mt-2 text-sm text-slate-700">{formatDateTime(registration.eventStartDateTime)}</p>
                <p className="mt-1 text-sm text-slate-600">{registration.eventLocation}</p>
              </div>
              {filter === 'UPCOMING' && registration.status === 'REGISTERED' && (
                <Button variant="danger" onClick={() => setSelected(registration)}>Cancel registration</Button>
              )}
            </article>
          ))}
        </div>
      )}
      <ConfirmDialog
        open={Boolean(selected)}
        onClose={() => setSelected(null)}
        onConfirm={() => void cancel()}
        title="Cancel this registration?"
        message={selected ? `You’ll give up your place at ${selected.eventTitle}.` : ''}
        confirmLabel="Cancel registration"
        loading={pending}
      />
    </div>
  )
}
