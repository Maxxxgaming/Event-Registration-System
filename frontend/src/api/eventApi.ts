import axiosClient from './axiosClient'
import type {
  ApiResponse,
  EventRequest,
  EventResponse,
  EventStatusUpdateRequest,
  PageResponse,
} from '@/types'

export interface EventListParams {
  page?: number
  size?: number
  search?: string
}

export interface AdminEventListParams extends EventListParams {
  status?: EventStatusUpdateRequest['status']
}

export const eventApi = {
  async list(params: EventListParams = {}, signal?: AbortSignal): Promise<PageResponse<EventResponse>> {
    const response = await axiosClient.get<ApiResponse<PageResponse<EventResponse>>>('/events', { params, signal })
    return response.data.data
  },
  async getById(id: number, signal?: AbortSignal): Promise<EventResponse> {
    const response = await axiosClient.get<ApiResponse<EventResponse>>(`/events/${id}`, { signal })
    return response.data.data
  },
  async create(request: EventRequest): Promise<EventResponse> {
    const response = await axiosClient.post<ApiResponse<EventResponse>>('/events', request)
    return response.data.data
  },
  async update(id: number, request: EventRequest): Promise<EventResponse> {
    const response = await axiosClient.put<ApiResponse<EventResponse>>(`/events/${id}`, request)
    return response.data.data
  },
  async updateStatus(id: number, request: EventStatusUpdateRequest): Promise<EventResponse> {
    const response = await axiosClient.patch<ApiResponse<EventResponse>>(`/events/${id}/status`, request)
    return response.data.data
  },
  async cancel(id: number): Promise<EventResponse> {
    const response = await axiosClient.delete<ApiResponse<EventResponse>>(`/events/${id}`)
    return response.data.data
  },
  async adminList(params: AdminEventListParams = {}, signal?: AbortSignal): Promise<PageResponse<EventResponse>> {
    const response = await axiosClient.get<ApiResponse<PageResponse<EventResponse>>>('/admin/events', { params, signal })
    return response.data.data
  },
}
