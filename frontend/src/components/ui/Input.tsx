import { useState, type InputHTMLAttributes } from 'react'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  error?: string
  showPasswordToggle?: boolean
}

export function Input({ label, error, id, type = 'text', showPasswordToggle = false, ...props }: InputProps) {
  const [visible, setVisible] = useState(false)
  const inputId = id ?? props.name
  const errorId = `${inputId}-error`
  const inputType = type === 'password' && showPasswordToggle
    ? visible ? 'text' : 'password'
    : type

  return (
    <div className="space-y-1.5">
      <label htmlFor={inputId} className="block text-sm font-medium text-slate-800">{label}</label>
      <div className="relative">
        <input
          {...props}
          id={inputId}
          type={inputType}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? errorId : undefined}
          className={`field-control ${showPasswordToggle ? 'pr-20' : ''} ${error ? 'border-rose-500 focus:border-rose-500 focus:ring-rose-100' : ''}`}
        />
        {showPasswordToggle && (
          <button
            type="button"
            className="absolute inset-y-0 right-3 my-auto h-8 text-sm font-medium text-slate-600"
            onClick={() => setVisible((current) => !current)}
            aria-label={visible ? 'Hide password' : 'Show password'}
          >
            {visible ? 'Hide' : 'Show'}
          </button>
        )}
      </div>
      {error && <p id={errorId} className="text-sm text-rose-700">{error}</p>}
    </div>
  )
}
