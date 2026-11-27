import axiosClient from './axiosClient'
import type { ApiResponse, RegistrationResponse } from '@/types'

export const registrationApi = {
  async register(eventId: number): Promise<RegistrationResponse> {
    const response = await axiosClient.post<ApiResponse<RegistrationResponse>>(
      `/events/${eventId}/registrations`,
    )
    return response.data.data
  },
  async cancelRegistration(id: number): Promise<RegistrationResponse> {
    const response = await axiosClient.delete<ApiResponse<RegistrationResponse>>(`/registrations/${id}`)
    return response.data.data
  },
  async getMyRegistrations(signal?: AbortSignal): Promise<RegistrationResponse[]> {
    const response = await axiosClient.get<ApiResponse<RegistrationResponse[]>>('/registrations/me', { signal })
    return response.data.data
  },
  async getEventRegistrations(eventId: number, signal?: AbortSignal): Promise<RegistrationResponse[]> {
    const response = await axiosClient.get<ApiResponse<RegistrationResponse[]>>(
      `/events/${eventId}/registrations`,
      { signal },
    )
    return response.data.data
  },
}
