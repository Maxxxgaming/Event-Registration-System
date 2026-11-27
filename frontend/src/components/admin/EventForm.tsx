import type { FormEvent } from 'react'
import type { EventFormValues } from './eventFormModel'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Textarea } from '@/components/ui/Textarea'

interface EventFormProps {
  values: EventFormValues
  errors: Record<string, string>
  submitting: boolean
  submitLabel: string
  onChange: (field: keyof EventFormValues, value: string) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

export function EventForm({ values, errors, submitting, submitLabel, onChange, onSubmit }: EventFormProps) {
  const update = (field: keyof EventFormValues, value: string) => {
    onChange(field, value)
  }
  return (
    <form
      className="space-y-5"
      noValidate
      onSubmit={(event) => {
        onSubmit(event)
      }}
    >
      <Input label="Event title" name="title" maxLength={200} required value={values.title} onChange={(event) => update('title', event.target.value)} error={errors.title} />
      <Textarea label="Description" name="description" maxLength={5000} value={values.description} onChange={(event) => update('description', event.target.value)} error={errors.description} />
      <Input label="Location" name="location" maxLength={255} required value={values.location} onChange={(event) => update('location', event.target.value)} error={errors.location} />
      <div className="grid gap-5 sm:grid-cols-2">
        <Input label="Starts" name="startDateTime" type="datetime-local" required value={values.startDateTime} onChange={(event) => update('startDateTime', event.target.value)} error={errors.startDateTime} />
        <Input label="Ends" name="endDateTime" type="datetime-local" required value={values.endDateTime} onChange={(event) => update('endDateTime', event.target.value)} error={errors.endDateTime} />
        <Input label="Registration deadline" name="registrationDeadline" type="datetime-local" required value={values.registrationDeadline} onChange={(event) => update('registrationDeadline', event.target.value)} error={errors.registrationDeadline} />
        <Input label="Capacity" name="capacity" type="number" min={1} step={1} required value={values.capacity} onChange={(event) => update('capacity', event.target.value)} error={errors.capacity} />
      </div>
      <div className="flex justify-end pt-2">
        <Button type="submit" loading={submitting}>{submitLabel}</Button>
      </div>
    </form>
  )
}
