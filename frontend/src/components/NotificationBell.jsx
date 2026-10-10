import { useCallback, useEffect, useRef, useState } from 'react'
import { api, errorMessage } from '../api'
import { parseUtc, relativeTime } from '../utils/time'
import { EmptyState, ErrorState, Skeleton } from './States'
import './NotificationBell.css'

const POLL_MS = 60_000

export default function NotificationBell() {
  const [count, setCount] = useState(0)
  const [open, setOpen] = useState(false)
  const [list, setList] = useState({ status: 'idle', items: [] })
  const [now, setNow] = useState(0)
  const [markError, setMarkError] = useState('')
  const rootRef = useRef(null)

  const refreshCount = useCallback(() => {
    // polling also makes the server check thresholds and create new alerts
    api.unreadCount().then(({ count }) => setCount(count), () => {})
  }, [])

  useEffect(() => {
    refreshCount()
    const id = setInterval(refreshCount, POLL_MS)
    return () => clearInterval(id)
  }, [refreshCount])

  // close on outside click or Escape
  useEffect(() => {
    if (!open) return undefined
    function onPointer(e) {
      if (!rootRef.current?.contains(e.target)) setOpen(false)
    }
    function onKey(e) {
      if (e.key === 'Escape') setOpen(false)
    }
    document.addEventListener('mousedown', onPointer)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onPointer)
      document.removeEventListener('keydown', onKey)
    }
  }, [open])

  async function loadList() {
    setList((prev) => ({ status: 'loading', items: prev.items }))
    setMarkError('')
    try {
      const items = await api.notifications()
      setNow(Date.now())
      setList({ status: 'ready', items })
      setCount(items.filter((n) => !n.read).length)
    } catch (error) {
      setList({ status: 'error', items: [], error: errorMessage(error) })
    }
  }

  function toggle() {
    const next = !open
    setOpen(next)
    if (next) loadList()
  }

  async function markRead(notification) {
    if (notification.read) return
    setMarkError('')
    try {
      const updated = await api.markRead(notification.id)
      setList((prev) => ({
        ...prev,
        items: prev.items.map((n) => (n.id === updated.id ? updated : n)),
      }))
      setCount((c) => Math.max(0, c - 1))
    } catch (error) {
      setMarkError(errorMessage(error, { 404: 'That notification no longer exists.' }))
    }
  }

  const badge = count > 99 ? '99+' : String(count)

  return (
    <div className="bell" ref={rootRef}>
      <button
        type="button"
        className="bell__button"
        onClick={toggle}
        aria-expanded={open}
        aria-haspopup="true"
        aria-label={count ? `Notifications, ${count} unread` : 'Notifications'}
      >
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path
            d="M6 9a6 6 0 1 1 12 0c0 4.5 1.5 6.5 2.5 7.5h-17C4.5 15.5 6 13.5 6 9Z"
            stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round"
          />
          <path d="M10 19.5a2 2 0 0 0 4 0" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
        </svg>
        {count > 0 && <span className="bell__badge">{badge}</span>}
      </button>

      {open && (
        <div className="bell__panel" role="dialog" aria-label="Notifications">
          <div className="bell__head">
            <strong>Notifications</strong>
            {count > 0 && <span className="bell__unread">{count} unread</span>}
          </div>

          {markError && <p className="bell__inline-error" role="alert">{markError}</p>}

          <div className="bell__body">
            {list.status === 'loading' && list.items.length === 0 && (
              <div className="bell__skeletons">
                {[0, 1, 2].map((i) => <Skeleton key={i} style={{ height: 52 }} />)}
              </div>
            )}

            {list.status === 'error' && <ErrorState message={list.error} onRetry={loadList} />}

            {list.status === 'ready' && list.items.length === 0 && (
              <EmptyState title="No alerts yet">
                You'll be notified here when AQI crosses a limit you set.
              </EmptyState>
            )}

            {list.items.length > 0 && list.status !== 'error' && (
              <ul className="bell__list">
                {list.items.map((n) => {
                  const created = parseUtc(n.createdAt)
                  return (
                    <li key={n.id}>
                      <button
                        type="button"
                        className={`bell__item${n.read ? '' : ' is-unread'}`}
                        onClick={() => markRead(n)}
                        aria-label={n.read ? n.message : `Unread: ${n.message}. Mark as read`}
                      >
                        {!n.read && <span className="bell__dot" aria-hidden="true" />}
                        <span className="bell__message">{n.message}</span>
                        {created && (
                          <time className="bell__time" dateTime={n.createdAt} title={created.toLocaleString()}>
                            {relativeTime(created, now)}
                          </time>
                        )}
                      </button>
                    </li>
                  )
                })}
              </ul>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
