export const AUTH_TOKEN_KEY = 'event-registration-token'
export const AUTH_EXPIRED_EVENT = 'event-registration:auth-expired'

export function clearStoredAuth(): void {
  localStorage.removeItem(AUTH_TOKEN_KEY)
  window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT))
}
