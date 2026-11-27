import type { TextareaHTMLAttributes } from 'react'

interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label: string
  error?: string
}

export function Textarea({ label, error, id, ...props }: TextareaProps) {
  const inputId = id ?? props.name
  const errorId = `${inputId}-error`
  return (
    <div className="space-y-1.5">
      <label htmlFor={inputId} className="block text-sm font-medium text-slate-800">{label}</label>
      <textarea
        {...props}
        id={inputId}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? errorId : undefined}
        className={`field-control min-h-32 resize-y ${error ? 'border-rose-500 focus:border-rose-500 focus:ring-rose-100' : ''}`}
      />
      {error && <p id={errorId} className="text-sm text-rose-700">{error}</p>}
    </div>
  )
}
