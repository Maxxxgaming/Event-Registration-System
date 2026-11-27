import { lazy, Suspense } from 'react'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import { AdminLayout } from '@/components/admin/AdminLayout'
import { MainLayout } from '@/components/layout/MainLayout'
import { PageLoader } from '@/components/ui/PageLoader'
import { AdminRoute } from './AdminRoute'
import { ProtectedRoute } from './ProtectedRoute'

const HomePage = lazy(() => import('@/pages/HomePage').then((module) => ({ default: module.HomePage })))
const LoginPage = lazy(() => import('@/pages/LoginPage').then((module) => ({ default: module.LoginPage })))
const RegisterPage = lazy(() => import('@/pages/RegisterPage').then((module) => ({ default: module.RegisterPage })))
const NotFoundPage = lazy(() => import('@/pages/NotFoundPage').then((module) => ({ default: module.NotFoundPage })))
const EventsPage = lazy(() => import('@/pages/EventsPage').then((module) => ({ default: module.EventsPage })))
const EventDetailsPage = lazy(() => import('@/pages/EventDetailsPage').then((module) => ({ default: module.EventDetailsPage })))
const MyRegistrationsPage = lazy(() => import('@/pages/MyRegistrationsPage').then((module) => ({ default: module.MyRegistrationsPage })))
const AdminDashboardPage = lazy(() => import('@/pages/AdminDashboardPage').then((module) => ({ default: module.AdminDashboardPage })))
const AdminEventsPage = lazy(() => import('@/pages/AdminEventsPage').then((module) => ({ default: module.AdminEventsPage })))
const EventFormPage = lazy(() => import('@/pages/EventFormPage').then((module) => ({ default: module.EventFormPage })))
const EventRegistrationsPage = lazy(() => import('@/pages/EventRegistrationsPage').then((module) => ({ default: module.EventRegistrationsPage })))

const router = createBrowserRouter([
  {
    element: <Suspense fallback={<PageLoader label="Loading page..." />}><MainLayout /></Suspense>,
    children: [
      { path: '/', element: <HomePage /> },
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
      { path: '/events', element: <EventsPage /> },
      { path: '/events/:id', element: <EventDetailsPage /> },
      {
        element: <ProtectedRoute />,
        children: [{ path: '/my-registrations', element: <MyRegistrationsPage /> }],
      },
      {
        element: <AdminRoute />,
        children: [{
          path: '/admin',
          element: <AdminLayout />,
          children: [
            { index: true, element: <AdminDashboardPage /> },
            { path: 'events', element: <AdminEventsPage /> },
            { path: 'events/new', element: <EventFormPage /> },
            { path: 'events/:id/edit', element: <EventFormPage /> },
            { path: 'events/:id/registrations', element: <EventRegistrationsPage /> },
          ],
        }],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])

export function AppRoutes() {
  return <RouterProvider router={router} />
}
