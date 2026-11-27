import { useCallback, useEffect, useState } from 'react'

interface AsyncState<T> {
  data: T | null
  loading: boolean
  error: unknown
}

export function useAsync<T>(fetcher: (signal: AbortSignal) => Promise<T>) {
  const [state, setState] = useState<AsyncState<T> & { fetcher: typeof fetcher; requestNumber: number } | null>(null)
  const [requestNumber, setRequestNumber] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    let active = true
    Promise.resolve()
      .then(() => fetcher(controller.signal))
      .then(
        (data) => {
          if (active) setState({ fetcher, requestNumber, data, loading: false, error: null })
        },
        (error: unknown) => {
          if (active && !controller.signal.aborted) {
            setState((current) => ({
              fetcher,
              requestNumber,
              data: current?.data ?? null,
              loading: false,
              error,
            }))
          }
        },
      )
    return () => {
      active = false
      controller.abort()
    }
  }, [fetcher, requestNumber])

  const refetch = useCallback(() => setRequestNumber((number) => number + 1), [])
  const isCurrentRequest = state?.fetcher === fetcher && state.requestNumber === requestNumber
  return {
    data: state?.data ?? null,
    loading: !isCurrentRequest || state.loading,
    error: isCurrentRequest ? state.error : null,
    refetch,
  }
}
