import axios from 'axios'
import type { ErrorResponse } from '@/types'

function isErrorResponse(value: unknown): value is ErrorResponse {
  return typeof value === 'object' && value !== null
    && 'message' in value && typeof value.message === 'string'
    && 'errorCode' in value && 'status' in value
}

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (error.code === 'ECONNABORTED' || error.code === 'ETIMEDOUT') {
      return 'The request took too long. Please try again.'
    }
    if (!error.response) {
      return 'Unable to reach the server. Please check your connection and try again.'
    }
    if (isErrorResponse(error.response.data)) return error.response.data.message
  }
  return 'Something went wrong. Please try again.'
}

export function getFieldErrors(error: unknown): Record<string, string> {
  if (!axios.isAxiosError(error) || !isErrorResponse(error.response?.data)) return {}
  return Object.fromEntries(
    (error.response.data.fieldErrors ?? []).map(({ field, message }) => [field, message]),
  )
}
