import { NavLink, Outlet } from 'react-router-dom'

const linkStyle = ({ isActive }: { isActive: boolean }) =>
  `block rounded-lg px-4 py-2.5 text-sm font-semibold ${isActive ? 'bg-brand-50 text-brand-700' : 'text-slate-700 hover:bg-slate-100'}`

export function AdminLayout() {
  return (
    <div className="page-shell py-8 sm:py-10">
      <div className="grid gap-8 md:grid-cols-[13rem_minmax(0,1fr)]">
        <aside>
          <h1 className="mb-3 px-4 text-xs font-bold uppercase tracking-wider text-slate-500">Administration</h1>
          <nav aria-label="Admin navigation" className="flex gap-2 overflow-x-auto md:flex-col">
            <NavLink end to="/admin" className={linkStyle}>Dashboard</NavLink>
            <NavLink to="/admin/events" className={linkStyle}>Events</NavLink>
          </nav>
        </aside>
        <section className="min-w-0"><Outlet /></section>
      </div>
    </div>
  )
}
