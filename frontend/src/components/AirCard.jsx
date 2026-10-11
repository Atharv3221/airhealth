import { useAirQuality } from '../hooks/useAirQuality'
import { useNearbyFires } from '../hooks/useNearbyFires'
import { getAqiBand, POLLUTANT_UNIT, POLLUTANTS } from '../utils/aqi'
import { formatLocalTime, parseUtc } from '../utils/time'
import { Link } from './nav'
import { EmptyState, ErrorState, Skeleton } from './States'
import SubscribeForm from './SubscribeForm'
import './AirCard.css'

function formatValue(value) {
  if (typeof value !== 'number') return '—'
  return value >= 100 ? Math.round(value).toLocaleString() : value.toFixed(1)
}

/**
 * Current air quality for a place picked on the dashboard.
 * @param {{ place: { name: string, latitude: number, longitude: number, admin1?: string, country?: string } }} props
 */
export default function AirCard({ place }) {
  const air = useAirQuality(place.latitude, place.longitude)
  const region = [place.admin1, place.country].filter(Boolean).join(', ')

  return (
    <section className="air-card" aria-labelledby="air-card-title" aria-busy={air.status === 'loading'}>
      <header className="air-card__head">
        <div>
          <h2 id="air-card-title">{place.name}</h2>
          <p className="air-card__region">
            {region || `${place.latitude.toFixed(2)}, ${place.longitude.toFixed(2)}`}
          </p>
        </div>
        <span className="air-card__model" title="Values come from an atmospheric model, not a ground sensor">
          Modeled estimate
        </span>
      </header>

      {air.status === 'loading' && <AirSkeleton />}
      {air.status === 'error' && <ErrorState message={air.error} onRetry={air.reload} />}
      {air.status === 'ready' && !air.current && (
        <EmptyState title="No air quality data for this location right now." />
      )}
      {air.status === 'ready' && air.current && <AirReading current={air.current} />}
      <FireRow lat={place.latitude} lon={place.longitude} />

      <SubscribeForm place={place} />
    </section>
  )
}

function AirReading({ current }) {
  const band = getAqiBand(current.us_aqi)
  const time = parseUtc(current.time)

  return (
    <>
      <div className="air-card__aqi" style={{ '--band': band.color, '--band-text': band.text }}>
        <div className="air-card__aqi-number">
          <span className="air-card__aqi-value">{current.us_aqi ?? '—'}</span>
          <span className="air-card__aqi-unit">US AQI</span>
        </div>
        <div>
          <p className="air-card__aqi-label">{band.label}</p>
          {time && (
            <p className="air-card__time">
              Updated <time dateTime={time.toISOString()}>{formatLocalTime(time)}</time>
            </p>
          )}
        </div>
      </div>

      <ul className="pollutants">
        {POLLUTANTS.map((p) => (
          <li key={p.key} className="pollutant">
            <span className="pollutant__label" title={p.name}>{p.label}</span>
            <span className="pollutant__value">{formatValue(current[p.key])}</span>
            <span className="pollutant__unit">{POLLUTANT_UNIT}</span>
          </li>
        ))}
      </ul>
    </>
  )
}

function formatIst(date) {
  return date.toLocaleString('en-IN', {
    timeZone: 'Asia/Kolkata',
    day: 'numeric',
    month: 'short',
    hour: 'numeric',
    minute: '2-digit',
  })
}

// satellite fire detections, worded as "heat spots" since many are routine burning, not wildfires;
// hidden while loading or on error, so a lookup problem never breaks the card
function FireRow({ lat, lon }) {
  const fire = useNearbyFires(lat, lon)
  if (!fire) return null
  const latest = fire.latest ? new Date(fire.latest) : null

  const nearest = fire.nearestKm < 1 ? 'under 1 km' : `${Math.round(fire.nearestKm)} km`

  return (
    <div className={`fire-row${fire.count > 0 ? ' fire-row--active' : ''}`}>
      <span className="fire-row__title">Nearby heat spots</span>
      <p className="fire-row__result">
        {fire.count === 0
          ? `No heat spots seen by satellite within ${fire.radiusKm} km in the last ${fire.days} days`
          : `${fire.count} heat ${fire.count === 1 ? 'spot' : 'spots'} seen by satellite within ${fire.radiusKm} km. Nearest is ${nearest} away`}
      </p>
      <small className="fire-row__note">
        {latest && (
          <>
            Latest: <time dateTime={latest.toISOString()}>{formatIst(latest)}</time> IST ·{' '}
          </>
        )}
        Data: <Link to="/about#heat-spots">NASA</Link>
      </small>
    </div>
  )
}

function AirSkeleton() {
  return (
    <div aria-label="Loading air quality">
      <Skeleton className="air-card__aqi-skeleton" />
      <ul className="pollutants">
        {POLLUTANTS.map((p) => (
          <li key={p.key} className="pollutant">
            <Skeleton style={{ height: 14, width: '40%' }} />
            <Skeleton style={{ height: 26, width: '60%' }} />
          </li>
        ))}
      </ul>
    </div>
  )
}
