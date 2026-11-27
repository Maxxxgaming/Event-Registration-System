export type RegistrationStatus = 'REGISTERED' | 'CANCELLED'

export interface RegistrationResponse {
  id: number
  eventId: number
  eventTitle: string
  eventStartDateTime: string
  eventLocation: string
  userId: number
  userFullName: string
  status: RegistrationStatus
  registeredAt: string
}
