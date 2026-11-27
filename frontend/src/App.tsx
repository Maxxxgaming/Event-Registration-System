import { ErrorBoundary, type FallbackProps } from 'react-error-boundary'
import { AuthProvider } from '@/context/AuthContext'
import { AppRoutes } from '@/routes/AppRoutes'
import { Button } from '@/components/ui/Button'
import { Toaster } from 'react-hot-toast'

function AppFallback({ resetErrorBoundary }: FallbackProps) {
  return (
    <main className="page-shell flex min-h-screen items-center justify-center">
      <section className="card w-full max-w-lg text-center">
        <h1 className="text-2xl font-semibold text-slate-900">We hit an unexpected error</h1>
        <p className="mt-3 text-slate-600">Please try again. Your account and event data are safe.</p>
        <Button className="mt-6" onClick={resetErrorBoundary}>Try again</Button>
      </section>
    </main>
  )
}

function App() {
  return (
    <ErrorBoundary FallbackComponent={AppFallback}>
      <AuthProvider>
        <Toaster position="top-right" />
        <AppRoutes />
      </AuthProvider>
    </ErrorBoundary>
  )
}

export default App
