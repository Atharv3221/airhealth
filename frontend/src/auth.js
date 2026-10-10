import { useSyncExternalStore } from 'react'

const TOKEN_KEY = 'airhealth.token'
const listeners = new Set()

function load() {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

let token = load()

function emit() {
  listeners.forEach((listener) => listener())
}

// keep tabs in sync when logging in or out elsewhere
window.addEventListener('storage', (e) => {
  if (e.key === TOKEN_KEY) {
    token = e.newValue
    emit()
  }
})

// JWT `exp` is in seconds; an unreadable token counts as expired
function isExpired(value) {
  try {
    const payload = value.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const { exp } = JSON.parse(atob(payload))
    return typeof exp === 'number' && exp * 1000 <= Date.now()
  } catch {
    return true
  }
}

export function getToken() {
  if (token && isExpired(token)) {
    clearToken()
  }
  return token
}

export function setToken(value) {
  token = value
  try {
    localStorage.setItem(TOKEN_KEY, value)
  } catch {
    // storage blocked: the session lasts until the tab closes
  }
  emit()
}

export function clearToken() {
  if (token === null) {
    return
  }
  token = null
  try {
    localStorage.removeItem(TOKEN_KEY)
  } catch {
    // nothing stored
  }
  emit()
}

function subscribe(listener) {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

export function useToken() {
  return useSyncExternalStore(subscribe, getToken)
}
