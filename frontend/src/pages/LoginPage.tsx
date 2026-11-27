import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { useAuth } from '@/hooks/useAuth'
import { getErrorMessage, getFieldErrors } from '@/utils/errorHandler'
import { notifyError, notifySuccess } from '@/utils/notify'
import type { LoginRequest } from '@/types'

interface LoginLocationState {
  from?: { pathname?: string; search?: string }
}

export function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [values, setValues] = useState<LoginRequest>({ email: '', password: '' })
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)

  if (isAuthenticated) return <Navigate to="/" replace />

  const validate = () => {
    const next: Record<string, string> = {}
    if (!values.email.trim()) next.email = 'Email is required.'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) next.email = 'Enter a valid email address.'
    if (!values.password) next.password = 'Password is required.'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!validate()) return
    setSubmitting(true)
    try {
      const response = await login(values)
      notifySuccess(`Welcome back, ${response.user.fullName}!`)
      const state = location.state as LoginLocationState | null
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

  const update = (field: keyof LoginRequest, value: string) => {
    setValues((current) => ({ ...current, [field]: value }))
    setErrors((current) => ({ ...current, [field]: '' }))
  }

  return (
    <div className="page-shell py-12 sm:py-16">
      <section className="card mx-auto max-w-md">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-slate-950">Welcome back</h1>
          <p className="mt-2 text-sm text-slate-600">Log in to continue to your events.</p>
        </div>
        <form className="mt-8 space-y-5" onSubmit={submit} noValidate>
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
            autoComplete="current-password"
            value={values.password}
            onChange={(event) => update('password', event.target.value)}
            error={errors.password}
            showPasswordToggle
          />
          <Button type="submit" className="w-full" loading={submitting}>Log in</Button>
        </form>
        <p className="mt-6 text-center text-sm text-slate-600">
          New to Gatherly? <Link to="/register" state={location.state} className="font-semibold text-brand-700 hover:underline">Create an account</Link>
        </p>
      </section>
    </div>
  )
}
