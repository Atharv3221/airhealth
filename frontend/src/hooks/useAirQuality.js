import { useCallback, useEffect, useState } from 'react'
import { api, errorMessage, isAbort } from '../api'

/**
 * Current air quality for a point.
 * @returns {{ status: 'loading' | 'error' | 'ready', current?: object, error?: string, reload: () => void }}
 */
export function useAirQuality(lat, lon) {
  const [attempt, setAttempt] = useState(0)
  const [result, setResult] = useState(null)
  const key = `${lat},${lon},${attempt}`

  useEffect(() => {
    const controller = new AbortController()
    api.air(lat, lon, { signal: controller.signal }).then(
      (data) => setResult({ key, current: data?.current ?? null }),
      (error) => {
        if (!isAbort(error)) setResult({ key, error: errorMessage(error) })
      },
    )
    return () => controller.abort()
  }, [key, lat, lon])

  const reload = useCallback(() => setAttempt((n) => n + 1), [])

  if (!result || result.key !== key) return { status: 'loading', reload }
  if (result.error) return { status: 'error', error: result.error, reload }
  return { status: 'ready', current: result.current, reload }
}
