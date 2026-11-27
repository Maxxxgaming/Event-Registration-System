import { lazy, Suspense } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { PageLoader } from '@/components/ui/PageLoader'
import { useAuth } from '@/hooks/useAuth'

const ForbiddenPage = lazy(() => import('@/pages/ForbiddenPage').then((module) => ({ default: module.ForbiddenPage })))

export function AdminRoute() {
  const { isAuthenticated, isAdmin } = useAuth()
  const location = useLocation()
  if (!isAuthenticated) return <Navigate to="/login" replace state={{ from: location }} />
  return isAdmin ? <Outlet /> : (
    <Suspense fallback={<PageLoader label="Loading page..." />}>
      <ForbiddenPage />
    </Suspense>
  )
}
