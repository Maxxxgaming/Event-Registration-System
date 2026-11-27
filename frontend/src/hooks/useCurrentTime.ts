import { useEffect, useState } from 'react'

export function useCurrentTime(): number | null {
  const [currentTime, setCurrentTime] = useState<number | null>(null)

  useEffect(() => {
    let intervalId = 0
    const timeoutId = window.setTimeout(() => {
      setCurrentTime(Date.now())
      intervalId = window.setInterval(() => setCurrentTime(Date.now()), 60_000)
    }, 0)
    return () => {
      window.clearTimeout(timeoutId)
      window.clearInterval(intervalId)
    }
  }, [])

  return currentTime
}
