import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/Button'

export function NotFoundPage() {
  return (
    <section className="page-shell py-20 text-center">
      <p className="text-sm font-semibold uppercase tracking-wider text-brand-700">404 · Page not found</p>
      <h1 className="mt-4 text-3xl font-bold text-slate-950">We can’t find that page</h1>
      <p className="mt-3 text-slate-600">The link may be outdated, or the page may have moved.</p>
      <Link to="/" className="mt-7 inline-flex"><Button>Go to home</Button></Link>
    </section>
  )
}
