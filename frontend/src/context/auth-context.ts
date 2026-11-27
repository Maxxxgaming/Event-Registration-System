import { createContext } from 'react'
import type { AuthResponse, LoginRequest, RegisterRequest, User } from '@/types'

export interface AuthContextValue {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  isAdmin: boolean
  loading: boolean
  login: (request: LoginRequest) => Promise<AuthResponse>
  register: (request: RegisterRequest) => Promise<AuthResponse>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
