import axios from 'axios'
import { useCallback, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { eventApi } from '@/api/eventApi'
import { registrationApi } from '@/api/registrationApi'
import { PageHeader } from '@/components/common/PageHeader'
import { SeatsBadge } from '@/components/common/SeatsBadge'
import { Button } from '@/components/ui/Button'
import { ConfirmDialog } from '@/components/ui/ConfirmDialog'
import { EmptyState } from '@/components/ui/EmptyState'
import { ErrorState } from '@/components/ui/ErrorState'
import { PageLoader } from '@/components/ui/PageLoader'
import { useAsync } from '@/hooks/useAsync'
import { useCurrentTime } from '@/hooks/useCurrentTime'
import { useAuth } from '@/hooks/useAuth'
import { formatDateTime } from '@/utils/formatters'
import { getErrorMessage } from '@/utils/errorHandler'
import { notifyError, notifySuccess } from '@/utils/notify'

export function EventDetailsPage() {
  const { id } = useParams()
  const eventId = Number(id)
  const validId = Number.isSafeInteger(eventId) && eventId > 0
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const currentTime = useCurrentTime()
  const [submitting, setSubmitting] = useState(false)
  const [showCancel, setShowCancel] = useState(false)
  const [cancelPending, setCancelPending] = useState(false)

  const fetchEvent = useCallback(
    (signal: AbortSignal) => validId ? eventApi.getById(eventId, signal) : Promise.reject(new Error('Invalid event ID')),
    [eventId, validId],
  )
  const { data: event, loading, error, refetch } = useAsync(fetchEvent)

  const fetchMyRegistrations = useCallback(
    (signal: AbortSignal) => isAuthenticated ? registrationApi.getMyRegistrations(signal) : Promise.resolve([]),
    [isAuthenticated],
  )
  const {
    data: registrations,
    loading: registrationsLoading,
    error: registrationsError,
    refetch: refetchRegistrations,
  } = useAsync(fetchMyRegistrations)

  const activeRegistration = useMemo(
    () => registrations?.find((registration) => registration.eventId === eventId && registration.status === 'REGISTERED'),
    [registrations, eventId],
  )

  if (!validId) {
    return (
      <div className="page-shell py-12">
        <EmptyState title="Event not found" description="This event link is not valid." action={<Link className="font-semibold text-brand-700" to="/events">Browse events</Link>} />
      </div>
    )
  }
  if (loading) return <PageLoader label="Loading event..." />
  if (error) {
    const status = axios.isAxiosError(error) ? error.response?.status : undefined
    if (status === 404) {
      return (
        <div className="page-shell py-12">
          <EmptyState title="Event not found" description="This event may have been removed or the link may be incorrect." action={<Link className="font-semibold text-brand-700" to="/events">Browse events</Link>} />
        </div>
      )
    }
    return <div className="page-shell py-12"><ErrorState message={getErrorMessage(error)} onRetry={refetch} /></div>
  }
  if (!event) {
    return <div className="page-shell py-12"><ErrorState message="Event details were not returned. Please try again." onRetry={refetch} /></div>
  }

  const deadlinePassed = currentTime !== null
    && new Date(`${event.registrationDeadline}Z`).getTime() < currentTime
  const eventStarted = currentTime !== null
    && new Date(`${event.startDateTime}Z`).getTime() <= currentTime
  const canRegister = currentTime !== null
    && event.status === 'PUBLISHED'
    && !deadlinePassed
    && event.availableSeats > 0

  const register = async () => {
    setSubmitting(true)
    try {
      await registrationApi.register(event.id)
      notifySuccess(`You're registered for ${event.title}!`)
      refetchRegistrations()
      refetch()
    } catch (requestError: unknown) {
      notifyError(getErrorMessage(requestError))
    } finally {
      setSubmitting(false)
    }
  }

  const cancel = async () => {
    if (!activeRegistration) return
    setCancelPending(true)
    try {
      await registrationApi.cancelRegistration(activeRegistration.id)
      notifySuccess('Your registration has been cancelled.')
      setShowCancel(false)
      refetchRegistrations()
      refetch()
    } catch (requestError: unknown) {
      notifyError(getErrorMessage(requestError))
    } finally {
      setCancelPending(false)
    }
  }

  const registrationAction = !isAuthenticated ? (
    <Button onClick={() => navigate('/login', { state: { from: location } })}>Log in to register</Button>
  ) : activeRegistration ? (
    <div className="flex flex-wrap items-center gap-3">
      <span className="font-semibold text-emerald-800" role="status">You are registered</span>
      <Button variant="danger" disabled={eventStarted || currentTime === null} onClick={() => setShowCancel(true)}>
        {eventStarted ? 'Cancellation closed' : 'Cancel registration'}
      </Button>
    </div>
  ) : (
    <Button disabled={!canRegister || registrationsLoading || Boolean(registrationsError)} loading={submitting} onClick={() => void register()}>
    {currentTime === null ? 'Checking registration…' : registrationsLoading ? 'Checking registration…' : registrationsError ? 'Registration check unavailable' : event.status === 'DRAFT' ? 'Not published' : event.status === 'CANCELLED' ? 'Event cancelled' : event.status === 'COMPLETED' ? 'Event completed' : deadlinePassed ? 'Registration closed' : event.availableSeats <= 0 ? 'Event full' : 'Register'}
    </Button>
  )

  return (
    <div className="page-shell py-10 sm:py-14">
      <PageHeader title={event.title} description={event.location} action={<SeatsBadge event={event} />} />
      <div className="grid gap-8 lg:grid-cols-[minmax(0,1fr)_20rem]">
        <article className="card">
          <h2 className="text-xl font-semibold text-slate-950">About this event</h2>
          <p className="mt-4 whitespace-pre-wrap leading-7 text-slate-700">{event.description || 'More event details will be shared soon.'}</p>
          <dl className="mt-8 grid gap-5 sm:grid-cols-2">
            <div><dt className="text-sm font-medium text-slate-500">Starts</dt><dd className="mt-1 font-semibold text-slate-900">{formatDateTime(event.startDateTime)}</dd></div>
            <div><dt className="text-sm font-medium text-slate-500">Ends</dt><dd className="mt-1 font-semibold text-slate-900">{formatDateTime(event.endDateTime)}</dd></div>
            <div><dt className="text-sm font-medium text-slate-500">Registration deadline</dt><dd className="mt-1 font-semibold text-slate-900">{formatDateTime(event.registrationDeadline)}</dd></div>
            <div><dt className="text-sm font-medium text-slate-500">Location</dt><dd className="mt-1 font-semibold text-slate-900">{event.location}</dd></div>
          </dl>
        </article>
        <aside className="card h-fit">
          <h2 className="text-lg font-semibold text-slate-950">Registration</h2>
          <dl className="mt-4 space-y-3 text-sm">
            <div className="flex justify-between gap-3"><dt className="text-slate-600">Registered</dt><dd className="font-semibold">{event.registeredCount} / {event.capacity}</dd></div>
            <div className="flex justify-between gap-3"><dt className="text-slate-600">Seats available</dt><dd className="font-semibold">{event.availableSeats}</dd></div>
          </dl>
          <div className="mt-6">{registrationAction}</div>
          {isAuthenticated && registrationsError !== null && (
            <div className="mt-4">
              <p role="alert" className="text-sm text-rose-700">{getErrorMessage(registrationsError)}</p>
              <Button variant="ghost" className="mt-2" onClick={refetchRegistrations}>Retry registration check</Button>
            </div>
          )}
        </aside>
      </div>
      <ConfirmDialog
        open={showCancel}
        onClose={() => setShowCancel(false)}
        onConfirm={() => void cancel()}
        title="Cancel your registration?"
        message="You can register again later if registration is still open and seats remain."
        confirmLabel="Cancel registration"
        loading={cancelPending}
      />
    </div>
  )
}
