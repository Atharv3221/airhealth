import { getAqiBand } from '../utils/aqi'

/** Coloured pill with the AQI number and its category. */
export default function AqiBadge({ aqi, size = 'md' }) {
  const band = getAqiBand(aqi)
  return (
    <span className={`aqi-badge aqi-badge--${size}`} style={{ background: band.color, color: band.text }}>
      <strong>{aqi ?? '—'}</strong>
      <span>{band.label}</span>
    </span>
  )
}
