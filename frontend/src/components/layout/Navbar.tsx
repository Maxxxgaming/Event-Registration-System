import { useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'
import { Button } from '@/components/ui/Button'

const linkStyle = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-brand-50 text-brand-700' : 'text-slate-700 hover:bg-slate-100'}`

export function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth()
  const [menuOpen, setMenuOpen] = useState(false)

  const closeMenu = () => setMenuOpen(false)
  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="page-shell flex min-h-16 items-center justify-between gap-4">
        <Link to="/" className="text-lg font-bold tracking-tight text-brand-700" onClick={closeMenu}>
          Gather<span className="text-slate-900">ly</span>
        </Link>
        <button
          type="button"
          className="rounded-md p-2 text-slate-700 hover:bg-slate-100 sm:hidden"
          aria-label={menuOpen ? 'Close menu' : 'Open menu'}
          aria-expanded={menuOpen}
          onClick={() => setMenuOpen((open) => !open)}
        >
          <span aria-hidden="true">{menuOpen ? '✕' : '☰'}</span>
        </button>
        <nav className={`${menuOpen ? 'flex' : 'hidden'} absolute left-0 right-0 top-16 z-20 flex-col gap-1 border-b border-slate-200 bg-white p-4 sm:static sm:flex sm:flex-row sm:items-center sm:border-0 sm:bg-transparent sm:p-0`}>
          <NavLink to="/" className={linkStyle} onClick={closeMenu}>Home</NavLink>
          <NavLink to="/events" className={linkStyle} onClick={closeMenu}>Events</NavLink>
          {isAuthenticated ? (
            <>
              <NavLink to="/my-registrations" className={linkStyle} onClick={closeMenu}>My registrations</NavLink>
              <span className="px-3 py-2 text-sm text-slate-600">Hi, {user?.fullName}</span>
              {isAdmin && <NavLink to="/admin" className={linkStyle} onClick={closeMenu}>Admin</NavLink>}
              <Button variant="ghost" className="justify-start" onClick={() => { logout(); closeMenu() }}>Log out</Button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={linkStyle} onClick={closeMenu}>Log in</NavLink>
              <Link to="/register" onClick={closeMenu}>
                <Button className="w-full">Create account</Button>
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  )
}
