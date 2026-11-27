import type { ReactNode } from 'react'

export function EmptyState({ title, description, action }: {
  title: string
  description?: string
  action?: ReactNode
}) {
  return (
    <section className="rounded-xl border border-dashed border-slate-300 bg-white px-6 py-10 text-center">
      <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
      {description && <p className="mx-auto mt-2 max-w-md text-slate-600">{description}</p>}
      {action && <div className="mt-5">{action}</div>}
    </section>
  )
}
