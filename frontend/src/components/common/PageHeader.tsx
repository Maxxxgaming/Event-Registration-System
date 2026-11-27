import type { ReactNode } from 'react'

export function PageHeader({ title, description, action }: {
  title: string
  description?: string
  action?: ReactNode
}) {
  return (
    <header className="mb-7 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-slate-950">{title}</h1>
        {description && <p className="mt-2 text-slate-600">{description}</p>}
      </div>
      {action}
    </header>
  )
}
