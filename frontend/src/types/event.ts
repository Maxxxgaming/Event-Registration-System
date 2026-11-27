export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED' | 'COMPLETED'

export interface EventRequest {
  title: string
  description: string | null
  location: string
  startDateTime: string
  endDateTime: string
  registrationDeadline: string
  capacity: number
}

export interface EventResponse {
  id: number
  title: string
  description: string | null
  location: string
  startDateTime: string
  endDateTime: string
  registrationDeadline: string
  capacity: number
  status: EventStatus
  createdBy: number | null
  createdAt: string | null
  updatedAt: string | null
  registeredCount: number
  availableSeats: number
}

export interface EventStatusUpdateRequest {
  status: EventStatus
}
