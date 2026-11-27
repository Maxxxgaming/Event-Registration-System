import { useCallback } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { eventApi } from '@/api/eventApi'
import { ErrorState } from '@/components/ui/ErrorState'
import { PageLoader } from '@/components/ui/PageLoader'
import { useAsync } from '@/hooks/useAsync'
import { getErrorMessage } from '@/utils/errorHandler'
import { EventFormEditor } from '@/pages/EventFormEditor'

export function EventFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const eventId = id ? Number(id) : null
  const validId = eventId === null || (Number.isSafeInteger(eventId) && eventId > 0)
  const fetchEvent = useCallback(
    (signal: AbortSignal) => eventId === null
      ? Promise.resolve(null)
      : validId ? eventApi.getById(eventId, signal) : Promise.reject(new Error('Invalid event ID')),
    [eventId, validId],
  )
  const { data: event, loading, error, refetch } = useAsync(fetchEvent)

  if (!validId) return <div className="py-8"><ErrorState message="The event ID is invalid." onRetry={() => navigate('/admin/events')} /></div>
  if (loading) return <PageLoader label={eventId === null ? 'Preparing form...' : 'Loading event...'} />
  if (error) return <div className="py-8"><ErrorState message={getErrorMessage(error)} onRetry={refetch} /></div>
  if (eventId !== null && event === null) {
    return <div className="py-8"><ErrorState message="The event could not be found." onRetry={() => navigate('/admin/events')} /></div>
  }
  return <EventFormEditor key={eventId ?? 'new'} eventId={eventId} event={event} />
}
