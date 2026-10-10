/** Parses an ISO time as UTC when it carries no zone (Open-Meteo sends "2026-10-09T19:00"). */
export function parseUtc(value) {
  if (!value) return null
  const hasZone = /(Z|[+-]\d{2}:?\d{2})$/.test(value)
  const date = new Date(hasZone ? value : `${value}Z`)
  return Number.isNaN(date.getTime()) ? null : date
}

export function formatLocalTime(date) {
  return date.toLocaleString(undefined, {
    weekday: 'short',
    hour: 'numeric',
    minute: '2-digit',
  })
}

/** "just now", "5m ago", "2h ago", "3d ago", then a date. */
export function relativeTime(date, now) {
  const seconds = Math.round((now - date.getTime()) / 1000)
  if (seconds < 60) return 'just now'
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}d ago`
  return date.toLocaleDateString(undefined, { day: 'numeric', month: 'short' })
}
