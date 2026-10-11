import { clearToken, getToken } from './auth'
import { navigate } from './router'

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

/** A failed request. status 0 means the server could not be reached. */
export class ApiError extends Error {
  constructor(status) {
    super(status === 0 ? 'Network error' : `Request failed with status ${status}`)
    this.name = 'ApiError'
    this.status = status
  }
}

const MESSAGES = {
  0: "Can't reach the server. Check your connection and try again.",
  400: 'Some of the details look wrong. Please check them and try again.',
  401: 'Your session has expired. Please log in again.',
  404: "We couldn't find that. It may have been removed.",
  409: 'That already exists.',
  500: 'Something went wrong on our side. Please try again in a moment.',
}

/**
 * Friendly text for an error, based only on the HTTP status
 * (the backend's error bodies carry no useful message).
 * @param {unknown} error
 * @param {Record<number, string>} [overrides] per-status text for this call site
 */
export function errorMessage(error, overrides = {}) {
  const status = error instanceof ApiError ? error.status : -1
  if (overrides[status]) return overrides[status]
  if (MESSAGES[status]) return MESSAGES[status]
  if (status >= 500) return MESSAGES[500]
  if (status >= 400) return MESSAGES[400]
  return 'Something went wrong. Please try again.'
}

export function isAbort(error) {
  return error?.name === 'AbortError'
}

function handleUnauthorized() {
  clearToken()
  const here = window.location.pathname + window.location.search
  if (!window.location.pathname.startsWith('/login')) {
    navigate(`/login?expired=1&next=${encodeURIComponent(here)}`, { replace: true })
  }
}

async function request(path, { method = 'GET', body, auth = true, signal } = {}) {
  const headers = { Accept: 'application/json' }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  const token = auth ? getToken() : null
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  let res
  try {
    res = await fetch(BASE_URL + path, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      signal,
    })
  } catch (error) {
    if (isAbort(error)) throw error
    throw new ApiError(0)
  }

  // on public endpoints (login) a 401 just means bad credentials
  if (res.status === 401 && auth) {
    handleUnauthorized()
  }
  if (!res.ok) {
    throw new ApiError(res.status)
  }

  const text = await res.text()
  return text ? JSON.parse(text) : null
}

export const api = {
  signup: (name, email, password) =>
    request('/user', { method: 'POST', body: { name, email, password }, auth: false }),
  login: (email, password) =>
    request('/api/auth/login', { method: 'POST', body: { email, password }, auth: false }),
  me: (opts) => request('/user/me', opts),

  air: (lat, lon, opts) =>
    request(`/api/air?lat=${encodeURIComponent(lat)}&lon=${encodeURIComponent(lon)}`, opts),

  /** Satellite fire detections near a point, summarised (count, nearest km, latest time). */
  nearbyFires: (lat, lon, opts) =>
    request(`/api/fires/nearby?lat=${encodeURIComponent(lat)}&lon=${encodeURIComponent(lon)}`, opts),

  subscriptions: (opts) => request('/api/subscriptions', opts),
  subscribeArea: ({ areaName, latitude, longitude, aqiThreshold, type }) =>
    request('/api/subscriptions/area', {
      method: 'POST',
      body: { areaName, latitude, longitude, aqiThreshold, type },
    }),
  /** Change a subscription's name and/or type; leave one undefined to keep it. */
  updateSubscription: (id, { name, type }) =>
    request(`/api/subscriptions/${id}`, { method: 'PATCH', body: { name, type } }),
  unsubscribe: (id) => request(`/api/subscriptions/${id}`, { method: 'DELETE' }),

  notifications: () => request('/api/notifications'),
  unreadCount: () => request('/api/notifications/unread-count'),
  markRead: (id) => request(`/api/notifications/${id}/read`, { method: 'PATCH' }),
}

/**
 * @typedef {{ name: string | null, latitude: number, longitude: number, admin1?: string | null, country?: string | null }} Place
 */

/** Places matching a name (backend proxies Open-Meteo geocoding). @returns {Promise<Place[]>} */
export function searchPlaces(query, opts) {
  return request(`/api/places/search?q=${encodeURIComponent(query)}`, opts)
}

/** The named area at a point (backend proxies OpenStreetMap). `name` is null when nothing is there. @returns {Promise<Place>} */
export function reversePlace(lat, lon, opts) {
  return request(`/api/places/reverse?lat=${encodeURIComponent(lat)}&lon=${encodeURIComponent(lon)}`, opts)
}
