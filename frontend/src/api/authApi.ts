import axiosClient from './axiosClient'
import type { ApiResponse, AuthResponse, LoginRequest, RegisterRequest, User } from '@/types'

export const authApi = {
  async login(request: LoginRequest): Promise<AuthResponse> {
    const response = await axiosClient.post<ApiResponse<AuthResponse>>('/auth/login', request)
    return response.data.data
  },
  async register(request: RegisterRequest): Promise<AuthResponse> {
    const response = await axiosClient.post<ApiResponse<AuthResponse>>('/auth/register', request)
    return response.data.data
  },
  async getCurrentUser(): Promise<User> {
    const response = await axiosClient.get<ApiResponse<User>>('/auth/me')
    return response.data.data
  },
}
