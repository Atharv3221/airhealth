import { useEffect, useState } from 'react'
import { api, errorMessage, isAbort } from '../api'
import AqiBadge from '../components/AqiBadge'
import { Link } from '../components/nav'
import { EmptyState, ErrorState, Skeleton } from '../components/States'
import { useAirQuality } from '../hooks/useAirQuality'
import { useNearbyFires } from '../hooks/useNearbyFires'
import PlaceTypeIcon from '../components/PlaceTypeIcon'
import PlaceTypePicker from '../components/PlaceTypePicker'
import { getPlaceType, personalMessage } from '../utils/placeTypes'
import './Subscriptions.css'

export default function Subscriptions() {
  const [attempt, setAttempt] = useState(0)
  const [result, setResult] = useState(null)

  useEffect(() => {
    const controller = new AbortController()
    api.subscriptions({ signal: controller.signal }).then(
      (items) => setResult({ attempt, items }),
      (error) => {
        if (!isAbort(error)) setResult({ attempt, error: errorMessage(error) })
      },
    )
    return () => controller.abort()
  }, [attempt])

  function replaceItem(updated) {
    setResult((prev) => ({ ...prev, items: prev.items.map((s) => (s.id === updated.id ? updated : s)) }))
  }

  function removeItem(id) {
    setResult((prev) => ({ ...prev, items: prev.items.filter((s) => s.id !== id) }))
  }

  const loading = !result || result.attempt !== attempt

  return (
    <div>
      <h1 className="page-title">My subscriptions</h1>
      <p className="page-sub">Areas you're watching. You'll get an alert when AQI reaches your limit.</p>

      {loading && (
        <div className="subs">
          {[0, 1, 2].map((i) => <Skeleton key={i} style={{ height: 96, borderRadius: 18 }} />)}
        </div>
      )}

      {!loading && result.error && (
        <ErrorState message={result.error} onRetry={() => setAttempt((n) => n + 1)} />
      )}

      {!loading && result.items?.length === 0 && (
        <EmptyState title="You aren't watching any areas yet">
          <Link to="/app" className="button button--primary">Find a place</Link>
        </EmptyState>
      )}

      {!loading && result.items?.length > 0 && (
        <ul className="subs">
          {result.items.map((s) => (
            <SubscriptionRow
              key={s.id}
              subscription={s}
              onUpdated={replaceItem}
              onRemoved={() => removeItem(s.id)}
            />
          ))}
        </ul>
      )}
    </div>
  )
}

function SubscriptionRow({ subscription: s, onUpdated, onRemoved }) {
  const air = useAirQuality(s.latitude, s.longitude)
  const fire = useNearbyFires(s.latitude, s.longitude)
  const [removing, setRemoving] = useState(false)
  const [error, setError] = useState('')

  async function unsubscribe() {
    setRemoving(true)
    setError('')
    try {
      await api.unsubscribe(s.id)
      onRemoved()
    } catch (err) {
      // already gone on the server: drop it here too
      if (err.status === 404) {
        onRemoved()
        return
      }
      setError(errorMessage(err))
      setRemoving(false)
    }
  }

  const aqi = air.status === 'ready' ? air.current?.us_aqi : null
  const overLimit = typeof aqi === 'number' && aqi >= s.aqiThreshold
  const placeType = getPlaceType(s.type)
  const message = personalMessage(placeType.value, aqi, fire)

  return (
    <li className="sub" style={{ '--type': placeType.color }}>
      <span className="sub__icon" title={placeType.label}>
        <PlaceTypeIcon type={placeType.value} size={22} />
      </span>
      <div className="sub__info">
        <SubscriptionName subscription={s} onUpdated={onUpdated} />
        <p className="sub__meta">
          <span className="sub__type">{placeType.label}</span> · Alert at AQI {s.aqiThreshold}
          {s.areaName && s.areaName !== s.name && !PLACEHOLDER_NAMES.has(s.areaName) && <> · near {s.areaName}</>}
        </p>
      </div>

      <div className="sub__air" aria-live="polite">
        {air.status === 'loading' && <Skeleton style={{ height: 32, width: 150, borderRadius: 999 }} />}
        {air.status === 'error' && (
          <button type="button" className="sub__retry" onClick={air.reload}>
            AQI unavailable · retry
          </button>
        )}
        {air.status === 'ready' && (
          <>
            <AqiBadge aqi={aqi} />
            {overLimit && <span className="sub__over">Above your limit</span>}
          </>
        )}
        {/* hidden while loading or on error, like the dashboard card */}
        {fire && (
          <span className={`sub__heat${fire.count > 0 ? ' sub__heat--active' : ''}`}>
            {fire.count === 0
              ? `No heat spots within ${fire.radiusKm} km`
              : `${fire.count} heat ${fire.count === 1 ? 'spot' : 'spots'} within ${fire.radiusKm} km`}
          </span>
        )}
      </div>

      <div className="sub__actions">
        <button type="button" className="button button--danger" onClick={unsubscribe} disabled={removing}>
          {removing ? 'Removing…' : 'Unsubscribe'}
        </button>
        {error && <p className="sub__error" role="alert">{error}</p>}
      </div>

      {message && <p className="sub__message">{message}</p>}
    </li>
  )
}

const MAX_NAME_LENGTH = 120
// names the dashboard gives spots it couldn't look up; not worth showing as "near …"
const PLACEHOLDER_NAMES = new Set(['Pinned location', 'Current location'])

function SubscriptionName({ subscription: s, onUpdated }) {
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState('')
  const [draftType, setDraftType] = useState('OTHER')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  function startEditing() {
    setDraft(s.name ?? '')
    setDraftType(getPlaceType(s.type).value)
    setError('')
    setEditing(true)
  }

  async function save(e) {
    e.preventDefault()
    const name = draft.trim()
    if (!name) {
      setError('Please enter a name.')
      return
    }
    const nameChanged = name !== s.name
    const typeChanged = draftType !== getPlaceType(s.type).value
    if (!nameChanged && !typeChanged) {
      setEditing(false)
      return
    }
    setSaving(true)
    setError('')
    try {
      onUpdated(await api.updateSubscription(s.id, {
        name: nameChanged ? name : undefined,
        type: typeChanged ? draftType : undefined,
      }))
      setEditing(false)
    } catch (err) {
      setError(errorMessage(err, {
        400: `Names can be up to ${MAX_NAME_LENGTH} characters.`,
        404: 'This subscription no longer exists. Refresh the page.',
      }))
    } finally {
      setSaving(false)
    }
  }

  if (!editing) {
    return (
      <div className="sub__title">
        <h2 className="sub__name">{s.name || 'Unnamed area'}</h2>
        <button type="button" className="sub__rename" onClick={startEditing} aria-label={`Edit ${s.name || 'subscription'}`}>
          Edit
        </button>
      </div>
    )
  }

  return (
    <form className="sub__rename-form" onSubmit={save}>
      <label className="sr-only" htmlFor={`sub-name-${s.id}`}>Subscription name</label>
      <input
        id={`sub-name-${s.id}`}
        value={draft}
        onChange={(e) => setDraft(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === 'Escape') setEditing(false)
        }}
        maxLength={MAX_NAME_LENGTH}
        disabled={saving}
        autoFocus
      />
      <PlaceTypePicker
        className="sub__type-picker"
        legend="This place is"
        name={`sub-type-${s.id}`}
        value={draftType}
        onChange={setDraftType}
        disabled={saving}
      />
      <button type="submit" className="button button--primary" disabled={saving}>
        {saving ? 'Saving…' : 'Save'}
      </button>
      <button type="button" className="button button--ghost" onClick={() => setEditing(false)} disabled={saving}>
        Cancel
      </button>
      {error && <p className="sub__error sub__error--inline" role="alert">{error}</p>}
    </form>
  )
}
