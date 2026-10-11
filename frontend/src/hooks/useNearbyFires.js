import { useEffect, useState } from 'react'
import { api, isAbort } from '../api'

/**
 * Fire detections near a point.
 * @returns {{ count: number, nearestKm: number | null, maxFrp: number | null, latest: string | null, radiusKm: number, days: number } | null}
 *   null while loading or if the request failed
 */
export function useNearbyFires(lat, lon) {
  const [result, setResult] = useState(null)
  const key = `${lat},${lon}`

  useEffect(() => {
    const controller = new AbortController()
    api.nearbyFires(lat, lon, { signal: controller.signal }).then(
      (data) => setResult({ key, data }),
      (error) => {
        if (!isAbort(error)) setResult({ key, data: null })
      },
    )
    return () => controller.abort()
  }, [key, lat, lon])

  return result?.key === key ? result.data : null
}
