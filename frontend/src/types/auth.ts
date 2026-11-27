export type Role = 'USER' | 'ADMIN'

export interface User {
  id: number
  fullName: string
  email: string
  role: Role
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  fullName: string
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  tokenType: string
  expiresIn: number
  user: User
}
