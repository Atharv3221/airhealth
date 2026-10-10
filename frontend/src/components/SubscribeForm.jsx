import { useState } from 'react'
import { api, errorMessage } from '../api'
import { Link } from './nav'

/**
 * "Subscribe" button that expands into an area-name + threshold form.
 * Reset it for a new place by changing its parent's key.
 */
export default function SubscribeForm({ place }) {
  const [open, setOpen] = useState(false)
  const [areaName, setAreaName] = useState(place.name)
  const [threshold, setThreshold] = useState('100')
  const [state, setState] = useState({ status: 'idle' })

  async function handleSubmit(e) {
    e.preventDefault()
    setState({ status: 'submitting' })
    try {
      const subscription = await api.subscribeArea({
        areaName: areaName.trim() || place.name,
        latitude: place.latitude,
        longitude: place.longitude,
        aqiThreshold: Number(threshold),
      })
      setState({ status: 'done', subscription })
    } catch (error) {
      setState({
        status: 'error',
        message: errorMessage(error, { 409: "You're already subscribed to this area" }),
      })
    }
  }

  if (state.status === 'done') {
    const s = state.subscription
    return (
      <div className="subscribe subscribe--done" role="status">
        <p>
          Subscribed to <strong>{s.name}</strong> ({s.latitude.toFixed(2)}, {s.longitude.toFixed(2)}).
          You'll get an alert when AQI reaches <strong>{s.aqiThreshold}</strong>.
        </p>
        <Link to="/app/subscriptions" className="button button--ghost">View my subscriptions</Link>
      </div>
    )
  }

  if (!open) {
    return (
      <div className="subscribe">
        <p className="subscribe__hint">Get an in-app alert when the air here gets worse.</p>
        <button type="button" className="button button--primary" onClick={() => setOpen(true)}>
          Subscribe
        </button>
      </div>
    )
  }

  const submitting = state.status === 'submitting'

  return (
    <form className="subscribe subscribe--form" onSubmit={handleSubmit}>
      <label className="field subscribe__name">
        Area name
        <input
          type="text"
          value={areaName}
          onChange={(e) => setAreaName(e.target.value)}
          maxLength={120}
          required
        />
      </label>
      <label className="field subscribe__threshold">
        Alert me at AQI
        <input
          type="number"
          inputMode="numeric"
          min={1}
          max={500}
          step={1}
          value={threshold}
          onChange={(e) => setThreshold(e.target.value)}
          required
          autoFocus
        />
      </label>
      <div className="subscribe__actions">
        <button type="submit" className="button button--primary" disabled={submitting}>
          {submitting ? 'Subscribing…' : 'Confirm'}
        </button>
        <button type="button" className="button button--ghost" onClick={() => setOpen(false)} disabled={submitting}>
          Cancel
        </button>
      </div>
      {state.status === 'error' && (
        <p className="subscribe__error" role="alert">
          {state.message}
          {state.message.startsWith("You're already") && (
            <> · <Link to="/app/subscriptions">See subscriptions</Link></>
          )}
        </p>
      )}
    </form>
  )
}
