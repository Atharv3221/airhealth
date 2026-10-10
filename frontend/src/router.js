import { useSyncExternalStore } from 'react'

// Minimal history-based router: no extra dependency for a handful of pages

const listeners = new Set()

function emit() {
  listeners.forEach((listener) => listener())
}

window.addEventListener('popstate', emit)

function subscribe(listener) {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

function snapshot() {
  return window.location.pathname + window.location.search
}

export function navigate(to, { replace = false } = {}) {
  if (to === snapshot()) {
    return
  }
  window.history[replace ? 'replaceState' : 'pushState'](null, '', to)
  const hash = new URL(to, window.location.origin).hash
  if (!replace && !hash) {
    window.scrollTo(0, 0)
  }
  emit()
  if (hash) {
    // wait for the new page to render, then jump to the section
    requestAnimationFrame(() => scrollToHash(hash))
  }
}

export function scrollToHash(hash) {
  const el = hash && document.getElementById(decodeURIComponent(hash.slice(1)))
  if (el) {
    el.scrollIntoView({ block: 'start' })
  } else {
    window.scrollTo(0, 0)
  }
}

/**
 * Current location. `params` is a fresh URLSearchParams each render,
 * so read the values you need instead of using it as an effect dependency.
 */
export function useLocation() {
  const href = useSyncExternalStore(subscribe, snapshot)
  const url = new URL(href, window.location.origin)
  return { pathname: url.pathname, params: url.searchParams, href }
}

/** Only allow in-app paths as a post-login redirect target. */
export function safeNext(value) {
  return value && value.startsWith('/') && !value.startsWith('//') ? value : '/app'
}
