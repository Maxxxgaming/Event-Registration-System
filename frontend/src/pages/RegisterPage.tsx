import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { useAuth } from '@/hooks/useAuth'
import { getErrorMessage, getFieldErrors } from '@/utils/errorHandler'
import { notifyError, notifySuccess } from '@/utils/notify'
import type { RegisterRequest } from '@/types'

export function RegisterPage() {
  const { register, isAuthenticated } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [values, setValues] = useState<RegisterRequest>({ fullName: '', email: '', password: '' })
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)

  if (isAuthenticated) return <Navigate to="/" replace />

  const validate = () => {
    const next: Record<string, string> = {}
    if (!values.fullName.trim()) next.fullName = 'Full name is required.'
    else if (values.fullName.length > 100) next.fullName = 'Full name must be 100 characters or fewer.'
    if (!values.email.trim()) next.email = 'Email is required.'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) next.email = 'Enter a valid email address.'
    if (!values.password) next.password = 'Password is required.'
    else if (values.password.length < 8 || !/[A-Za-z]/.test(values.password) || !/\d/.test(values.password)) {
      next.password = 'Password must be at least 8 characters and include at least one letter and one number.'
    }
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!validate()) return
    setSubmitting(true)
    try {
      await register(values)
      notifySuccess('Account created successfully!')
      const state = location.state as { from?: { pathname?: string; search?: string } } | null
      const destination = state?.from
        ? `${state.from.pathname ?? '/'}${state.from.search ?? ''}`
        : '/'
      navigate(destination, { replace: true })
    } catch (error: unknown) {
      setErrors(getFieldErrors(error))
      notifyError(getErrorMessage(error))
    } finally {
      setSubmitting(false)
    }
  }

  const update = (field: keyof RegisterRequest, value: string) => {
    setValues((current) => ({ ...current, [field]: value }))
    setErrors((current) => ({ ...current, [field]: '' }))
  }

  return (
    <div className="page-shell py-12 sm:py-16">
      <section className="card mx-auto max-w-md">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-slate-950">Create your account</h1>
          <p className="mt-2 text-sm text-slate-600">Join the community and find your next event.</p>
        </div>
        <form className="mt-8 space-y-5" onSubmit={submit} noValidate>
          <Input
            label="Full name"
            name="fullName"
            autoComplete="name"
            maxLength={100}
            value={values.fullName}
            onChange={(event) => update('fullName', event.target.value)}
            error={errors.fullName}
          />
          <Input
            label="Email"
            name="email"
            type="email"
            autoComplete="email"
            value={values.email}
            onChange={(event) => update('email', event.target.value)}
            error={errors.email}
          />
          <Input
            label="Password"
            name="password"
            type="password"
            autoComplete="new-password"
            minLength={8}
            value={values.password}
            onChange={(event) => update('password', event.target.value)}
            error={errors.password}
            showPasswordToggle
          />
          <p className="-mt-3 text-xs text-slate-500">At least 8 characters, including a letter and a number.</p>
          <Button type="submit" className="w-full" loading={submitting}>Create account</Button>
        </form>
        <p className="mt-6 text-center text-sm text-slate-600">
          Already have an account? <Link to="/login" state={location.state} className="font-semibold text-brand-700 hover:underline">Log in</Link>
        </p>
      </section>
    </div>
  )
}
