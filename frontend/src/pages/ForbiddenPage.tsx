import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/Button'

export function ForbiddenPage() {
  return (
    <section className="page-shell py-20 text-center">
      <p className="text-sm font-semibold uppercase tracking-wider text-brand-700">403 · Access denied</p>
      <h1 className="mt-4 text-3xl font-bold text-slate-950">This area is for administrators</h1>
      <p className="mx-auto mt-3 max-w-lg text-slate-600">You don’t have permission to view this page. You can head back home and keep exploring.</p>
      <Link to="/" className="mt-7 inline-flex"><Button>Go to home</Button></Link>
    </section>
  )
}
