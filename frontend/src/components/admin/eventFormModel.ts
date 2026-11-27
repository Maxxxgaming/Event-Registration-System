import type { EventRequest, EventResponse } from '@/types'
import { fromDateTimeLocal, toDateTimeLocal } from '@/utils/formatters'

export interface EventFormValues {
  title: string
  description: string
  location: string
  startDateTime: string
  endDateTime: string
  registrationDeadline: string
  capacity: string
}

export const emptyEventForm: EventFormValues = {
  title: '',
  description: '',
  location: '',
  startDateTime: '',
  endDateTime: '',
  registrationDeadline: '',
  capacity: '1',
}

export function eventToForm(event: EventResponse): EventFormValues {
  return {
    title: event.title,
    description: event.description ?? '',
    location: event.location,
    startDateTime: toDateTimeLocal(event.startDateTime),
    endDateTime: toDateTimeLocal(event.endDateTime),
    registrationDeadline: toDateTimeLocal(event.registrationDeadline),
    capacity: String(event.capacity),
  }
}

export function eventFormToRequest(values: EventFormValues): EventRequest {
  return {
    title: values.title.trim(),
    description: values.description.trim() || null,
    location: values.location.trim(),
    startDateTime: fromDateTimeLocal(values.startDateTime),
    endDateTime: fromDateTimeLocal(values.endDateTime),
    registrationDeadline: fromDateTimeLocal(values.registrationDeadline),
    capacity: Number(values.capacity),
  }
}

export function validateEventForm(values: EventFormValues, isCreate: boolean): Record<string, string> {
  const errors: Record<string, string> = {}
  if (!values.title.trim()) errors.title = 'Event title is required.'
  else if (values.title.length > 200) errors.title = 'Event title must be 200 characters or fewer.'
  if (values.description.length > 5000) errors.description = 'Description must be 5000 characters or fewer.'
  if (!values.location.trim()) errors.location = 'Event location is required.'
  else if (values.location.length > 255) errors.location = 'Location must be 255 characters or fewer.'
  if (!values.startDateTime) errors.startDateTime = 'Event start date and time are required.'
  if (!values.endDateTime) errors.endDateTime = 'Event end date and time are required.'
  else if (values.startDateTime && values.endDateTime <= values.startDateTime) errors.endDateTime = 'End date and time must be after the start.'
  if (!values.registrationDeadline) errors.registrationDeadline = 'Registration deadline is required.'
  else if (values.startDateTime && values.registrationDeadline > values.startDateTime) {
    errors.registrationDeadline = 'Registration deadline must be before or equal to the start.'
  }
  if (isCreate && values.startDateTime && new Date(values.startDateTime).getTime() <= Date.now()) {
    errors.startDateTime = 'Event start date and time must be in the future.'
  }
  const capacity = Number(values.capacity)
  if (!values.capacity || !Number.isInteger(capacity) || capacity < 1) errors.capacity = 'Event capacity must be at least 1.'
  return errors
}
