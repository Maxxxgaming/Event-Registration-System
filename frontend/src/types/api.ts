export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export interface ErrorResponse {
  success: false
  message: string
  errorCode: string
  status: number
  path: string
  timestamp: string
  fieldErrors?: FieldError[]
}

export interface FieldError {
  field: string
  message: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}
