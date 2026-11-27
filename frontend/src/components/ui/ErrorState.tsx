import { Button } from './Button'

export function ErrorState({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <section role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-6 text-center">
      <h2 className="font-semibold text-rose-900">We couldn’t load this</h2>
      <p className="mt-2 text-sm text-rose-800">{message}</p>
      <Button variant="secondary" className="mt-4" onClick={onRetry}>Try again</Button>
    </section>
  )
}
