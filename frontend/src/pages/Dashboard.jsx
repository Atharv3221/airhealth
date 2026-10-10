import { useEffect, useRef, useState } from 'react'
import { errorMessage, isAbort, reversePlace, searchPlaces } from '../api'
import AirCard from '../components/AirCard'
import PlaceMap from '../components/PlaceMap'
import { EmptyState, ErrorState, Skeleton } from '../components/States'
import { useLocation } from '../router'
import './Dashboard.css'

const SEARCH_ERRORS = { 502: 'Place search is unavailable right now. Please try again shortly.' }

function placeKey(place) {
  return `${place.latitude},${place.longitude}`
}

function placeLabel(place) {
  return [place.admin1, place.country].filter(Boolean).join(', ')
}

function geolocate() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error("Your browser can't share its location."))
      return
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => resolve(pos.coords),
      () => reject(new Error('Location permission was denied. Search or pick on the map instead.')),
      { timeout: 10_000 },
    )
  })
}

export default function Dashboard() {
  const { params } = useLocation()
  // the landing page can hand over a search (?q=) or a location request (?locate=1)
  const [initialQuery] = useState(() => params.get('q')?.trim() ?? '')
  const [initialLocate] = useState(() => params.get('locate') === '1')

  const [query, setQuery] = useState(initialQuery)
  const [submitted, setSubmitted] = useState(initialQuery ? { term: initialQuery, n: 0 } : null)
  const [result, setResult] = useState(null)
  const [place, setPlace] = useState(null)
  // a point picked on the map / by location while its name is being looked up
  const [pending, setPending] = useState(null)
  const [locating, setLocating] = useState(initialLocate)
  const [note, setNote] = useState('')
  const pickSeq = useRef(0)

  const searchKey = submitted ? `${submitted.n}:${submitted.term}` : null

  useEffect(() => {
    if (!submitted) return undefined
    const controller = new AbortController()
    searchPlaces(submitted.term, { signal: controller.signal }).then(
      (places) => setResult({ key: searchKey, places }),
      (error) => {
        if (!isAbort(error)) setResult({ key: searchKey, error: errorMessage(error, SEARCH_ERRORS) })
      },
    )
    return () => controller.abort()
  }, [searchKey, submitted])

  /** Selects a point, naming it via the backend's reverse lookup. Later picks win. */
  async function pickPoint(latitude, longitude, fallbackName) {
    const seq = ++pickSeq.current
    setPending({ latitude, longitude })
    setNote('')
    let named = null
    try {
      named = await reversePlace(latitude, longitude)
    } catch {
      // still usable without a name; the user can type one when subscribing
    }
    if (seq !== pickSeq.current) return
    setPending(null)
    if (!named) {
      setNote("Couldn't look up this spot's name. You can name it when subscribing.")
    }
    setPlace({
      name: named?.name ?? fallbackName,
      latitude,
      longitude,
      admin1: named?.admin1 ?? null,
      country: named?.country ?? null,
    })
  }

  function locate() {
    setLocating(true)
    setNote('')
    geolocate()
      .then(
        (coords) => pickPoint(coords.latitude, coords.longitude, 'Current location'),
        (error) => setNote(error.message),
      )
      .finally(() => setLocating(false))
  }

  useEffect(() => {
    if (!initialLocate) return
    geolocate()
      .then(
        (coords) => pickPoint(coords.latitude, coords.longitude, 'Current location'),
        (error) => setNote(error.message),
      )
      .finally(() => setLocating(false))
  }, [initialLocate])

  function handleSubmit(e) {
    e.preventDefault()
    const term = query.trim()
    if (!term) return
    setSubmitted((prev) => ({ term, n: (prev?.n ?? 0) + 1 }))
    setNote('')
  }

  function choose(p) {
    pickSeq.current += 1 // cancels a map lookup still in flight
    setPending(null)
    setNote('')
    setPlace(p)
  }

  let search = 'idle'
  if (submitted) {
    if (!result || result.key !== searchKey) search = 'loading'
    else search = result.error ? 'error' : 'ready'
  }

  const selectedKey = place ? placeKey(place) : null

  return (
    <div className="dashboard">
      <div className="dashboard__grid">
        <div className="dashboard__search">
          {/* inside the left column so the map starts right under the header on laptops */}
          <div>
            <h1 className="page-title">How&apos;s the air?</h1>
            <p className="page-sub">Search for a city or area, or click anywhere on the map.</p>
          </div>
          <form className="place-search" onSubmit={handleSubmit} role="search">
            <label htmlFor="place-query" className="sr-only">City or area</label>
            <input
              id="place-query"
              type="search"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="e.g. Pune, Koramangala"
              autoComplete="off"
              maxLength={100}
              autoFocus={!initialQuery}
            />
            <button type="submit" className="button button--primary" disabled={!query.trim()}>
              Search
            </button>
          </form>

          <button type="button" className="dashboard__locate" onClick={locate} disabled={locating}>
            {locating ? 'Finding your location…' : 'Or use my current location'}
          </button>
          {note && <p className="dashboard__note" role="status">{note}</p>}

          <div className="results" aria-live="polite">
            {search === 'loading' && (
              <div className="results__list">
                {[0, 1, 2].map((i) => <Skeleton key={i} style={{ height: 58 }} />)}
              </div>
            )}
            {search === 'error' && (
              <ErrorState
                message={result.error}
                onRetry={() => setSubmitted((prev) => ({ ...prev, n: prev.n + 1 }))}
              />
            )}
            {search === 'ready' && result.places.length === 0 && (
              <EmptyState title={`No places found for "${submitted.term}"`}>
                Check the spelling, try a nearby city, or pick the spot on the map.
              </EmptyState>
            )}
            {search === 'ready' && result.places.length > 0 && (
              <ul className="results__list">
                {result.places.map((p) => {
                  const selected = selectedKey === placeKey(p)
                  return (
                    <li key={placeKey(p)}>
                      <button
                        type="button"
                        className={`result${selected ? ' is-selected' : ''}`}
                        onClick={() => choose(p)}
                        aria-pressed={selected}
                      >
                        <strong>{p.name}</strong>
                        <span>{placeLabel(p) || `${p.latitude.toFixed(2)}, ${p.longitude.toFixed(2)}`}</span>
                      </button>
                    </li>
                  )
                })}
              </ul>
            )}
          </div>
        </div>

        <div className="dashboard__map">
          <PlaceMap marker={pending ?? place} onPick={(lat, lon) => pickPoint(lat, lon, 'Pinned location')} />
          <p className="dashboard__map-hint" aria-live="polite">
            {pending ? 'Looking up this spot…' : 'Click the map to pick a spot'}
          </p>
        </div>

        <div className="dashboard__card">
          {place ? (
            <AirCard key={placeKey(place)} place={place} />
          ) : (
            !pending && (
              <EmptyState title="Pick a place to see its air quality">
                Search above or click the map.
              </EmptyState>
            )
          )}
        </div>
      </div>
    </div>
  )
}
