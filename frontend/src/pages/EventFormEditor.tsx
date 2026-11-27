import { useMemo, useState, type FormEvent } from 'react'
import { Link, useBlocker, useNavigate } from 'react-router-dom'
import type { EventResponse } from '@/types'
import { EventForm } from '@/components/admin/EventForm'
import { emptyEventForm, eventFormToRequest, eventToForm, validateEventForm, type EventFormValues } from '@/components/admin/eventFormModel'
import { PageHeader } from '@/components/common/PageHeader'
import { ConfirmDialog } from '@/components/ui/ConfirmDialog'
import { useBeforeUnload } from '@/hooks/useBeforeUnload'
import { getErrorMessage, getFieldErrors } from '@/utils/errorHandler'
import { notifyError, notifySuccess } from '@/utils/notify'
import { eventApi } from '@/api/eventApi'

export function EventFormEditor({ eventId, event }: { eventId: number | null; event: EventResponse | null }) {
  const isEdit = eventId !== null
  const initialForm = useMemo(() => event ? eventToForm(event) : emptyEventForm, [event])
  const [values, setValues] = useState<EventFormValues>(initialForm)
  const [initialValues, setInitialValues] = useState<EventFormValues>(initialForm)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)
  const navigate = useNavigate()
  const isDirty = useMemo(() => JSON.stringify(values) !== JSON.stringify(initialValues), [values, initialValues])
  const blocker = useBlocker(isDirty && !submitting)

  useBeforeUnload(isDirty && !submitting)

  const update = (field: keyof EventFormValues, value: string) => {
    setValues((current) => ({ ...current, [field]: value }))
    setErrors((current) => ({ ...current, [field]: '' }))
  }

  const submit = async (formEvent: FormEvent<HTMLFormElement>) => {
    formEvent.preventDefault()
    const clientErrors = validateEventForm(values, !isEdit)
    setErrors(clientErrors)
    if (Object.keys(clientErrors).length > 0) return
    setSubmitting(true)
    try {
      const request = eventFormToRequest(values)
      if (eventId === null) await eventApi.create(request)
      else await eventApi.update(eventId, request)
      setInitialValues(values)
      notifySuccess(isEdit ? 'Event updated successfully.' : 'Event created successfully.')
      navigate('/admin/events', { replace: true })
    } catch (requestError: unknown) {
      setErrors(getFieldErrors(requestError))
      notifyError(getErrorMessage(requestError))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div>
      <PageHeader
        title={isEdit ? 'Edit event' : 'Create event'}
        description={isEdit ? 'Update the details for this event.' : 'Add a new event to the catalogue.'}
        action={<Link to="/admin/events" className="text-sm font-semibold text-slate-700 hover:underline">Back to events</Link>}
      />
      <div className="card">
        <EventForm
          values={values}
          errors={errors}
          submitting={submitting}
          submitLabel={isEdit ? 'Save changes' : 'Create event'}
          onChange={update}
          onSubmit={(formEvent) => void submit(formEvent)}
        />
      </div>
      <ConfirmDialog
        open={blocker.state === 'blocked'}
        onClose={() => blocker.reset?.()}
        onConfirm={() => blocker.proceed?.()}
        title="Discard unsaved changes?"
        message="You have unsaved event changes. Leave this page and discard them?"
        confirmLabel="Leave page"
      />
    </div>
  )
}
