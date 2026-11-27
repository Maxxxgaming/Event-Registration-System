import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { authApi } from '@/api/authApi'
import type { AuthResponse, LoginRequest, RegisterRequest, User } from '@/types'
import { AuthContext, type AuthContextValue } from './auth-context'
import { AUTH_EXPIRED_EVENT, AUTH_TOKEN_KEY } from '@/utils/authStorage'
import { PageLoader } from '@/components/ui/PageLoader'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(AUTH_TOKEN_KEY))
  const [loading, setLoading] = useState(true)

  const logout = useCallback(() => {
    localStorage.removeItem(AUTH_TOKEN_KEY)
    setToken(null)
    setUser(null)
  }, [])

  useEffect(() => {
    const onExpired = () => {
      setToken(null)
      setUser(null)
    }
    window.addEventListener(AUTH_EXPIRED_EVENT, onExpired)
    return () => window.removeEventListener(AUTH_EXPIRED_EVENT, onExpired)
  }, [])

  useEffect(() => {
    let active = true
    const restore = async () => {
      if (!token) {
        setLoading(false)
        return
      }
      try {
        const currentUser = await authApi.getCurrentUser()
        if (active) setUser(currentUser)
      } catch {
        localStorage.removeItem(AUTH_TOKEN_KEY)
        if (active) {
          setToken(null)
          setUser(null)
        }
      } finally {
        if (active) setLoading(false)
      }
    }
    void restore()
    return () => { active = false }
  }, [token])

  const acceptAuth = useCallback((auth: AuthResponse) => {
    localStorage.setItem(AUTH_TOKEN_KEY, auth.token)
    setToken(auth.token)
    setUser(auth.user)
    return auth
  }, [])

  const login = useCallback(async (request: LoginRequest) => acceptAuth(await authApi.login(request)), [acceptAuth])
  const register = useCallback(async (request: RegisterRequest) => acceptAuth(await authApi.register(request)), [acceptAuth])

  const value = useMemo<AuthContextValue>(() => ({
    user,
    token,
    isAuthenticated: Boolean(user && token),
    isAdmin: user?.role === 'ADMIN',
    loading,
    login,
    register,
    logout,
  }), [user, token, loading, login, register, logout])

  if (loading) return <PageLoader label="Restoring your session..." />
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
