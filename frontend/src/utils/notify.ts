import toast from 'react-hot-toast'

const duration = 4000

export const notifySuccess = (message: string) => toast.success(message, { duration })
export const notifyError = (message: string) => toast.error(message, { duration })
export const notifyInfo = (message: string) => toast(message, { duration, icon: 'ℹ️' })
