import axios from 'axios'
import toast from 'react-hot-toast'
import { AUTH_TOKEN_KEY, clearStoredAuth } from '@/utils/authStorage'

const baseURL = import.meta.env.VITE_API_BASE_URL

const axiosClient = axios.create({
  baseURL,
  timeout: 10_000,
  headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
})

axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(AUTH_TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

axiosClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      const requestUrl = error.config?.url ?? ''
      const isAuthSubmission = /\/auth\/(login|register)\/?$/.test(requestUrl)
      if (!isAuthSubmission) {
        clearStoredAuth()
        if (window.location.pathname !== '/login') {
          toast.error('Your session has expired. Please log in again.')
          window.location.replace('/login')
        }
      }
    }
    return Promise.reject(error)
  },
)

export default axiosClient
