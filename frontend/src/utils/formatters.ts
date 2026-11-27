const dateTimeFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short',
})

const dateFormatter = new Intl.DateTimeFormat(undefined, { dateStyle: 'long' })

function parseBackendDate(value: string): Date {
  return new Date(/(?:Z|[+-]\d{2}:\d{2})$/i.test(value) ? value : `${value}Z`)
}

export function formatDateTime(value: string): string {
  return dateTimeFormatter.format(parseBackendDate(value))
}

export function formatDate(value: string): string {
  return dateFormatter.format(parseBackendDate(value))
}

export function formatRelative(value: string): string {
  const date = parseBackendDate(value)
  const seconds = Math.round((date.getTime() - Date.now()) / 1000)
  const absoluteSeconds = Math.abs(seconds)
  let amount = seconds
  let unit: Intl.RelativeTimeFormatUnit = 'second'
  if (absoluteSeconds >= 86_400) {
    amount = Math.round(seconds / 86_400)
    unit = 'day'
  } else if (absoluteSeconds >= 3600) {
    amount = Math.round(seconds / 3600)
    unit = 'hour'
  } else if (absoluteSeconds >= 60) {
    amount = Math.round(seconds / 60)
    unit = 'minute'
  }

  return new Intl.RelativeTimeFormat(undefined, { numeric: 'auto' }).format(amount, unit)
}

export function toDateTimeLocal(value: string): string {
  return value.slice(0, 16)
}

export function fromDateTimeLocal(value: string): string {
  return value.length === 16 ? `${value}:00` : value
}
