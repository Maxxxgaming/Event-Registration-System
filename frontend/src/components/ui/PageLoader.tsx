import { Spinner } from './Spinner'

export function PageLoader({ label = 'Loading...' }: { label?: string }) {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="flex items-center gap-3 text-slate-700">
        <Spinner size="md" label={label} />
        <span>{label}</span>
      </div>
    </main>
  )
}
