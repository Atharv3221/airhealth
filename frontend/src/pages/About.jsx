import { useEffect } from 'react'
import { Link } from '../components/nav'
import { useToken } from '../auth'
import { scrollToHash } from '../router'
import { AQI_BANDS, POLLUTANT_UNIT, POLLUTANTS } from '../utils/aqi'
import './About.css'

const SECTIONS = [
  { id: 'how-it-works', label: 'How it works' },
  { id: 'aqi-scale', label: 'Understanding AQI' },
  { id: 'pollutants', label: 'Pollutants' },
  { id: 'data', label: 'Data & accuracy' },
  { id: 'privacy', label: 'Your data' },
  { id: 'credits', label: 'Credits' },
]

function bandRange(index) {
  const min = index === 0 ? 0 : AQI_BANDS[index - 1].max + 1
  const { max } = AQI_BANDS[index]
  return max === Infinity ? `${min}+` : `${min}–${max}`
}

export default function About() {
  const loggedIn = Boolean(useToken())

  // opened straight from a link like /about#data: the section exists only after render
  useEffect(() => {
    if (window.location.hash) scrollToHash(window.location.hash)
  }, [])

  return (
    <article className="about">
      <header className="about__intro">
        <h1 className="page-title">About AirHealth</h1>
        <p className="about__lead">
          AirHealth shows what you're breathing right now, anywhere, in one calm glance, and lets you
          know when the air in the places you care about gets worse.
        </p>
        <Link to={loggedIn ? '/app' : '/signup'} className="button button--primary">
          {loggedIn ? 'Check the air' : 'Create a free account'}
        </Link>
      </header>

      <nav className="about__toc" aria-label="On this page">
        {SECTIONS.map((s) => (
          <Link key={s.id} to={`/about#${s.id}`}>{s.label}</Link>
        ))}
      </nav>

      <section id="how-it-works" className="about__section">
        <h2>How it works</h2>
        <ol className="about__steps">
          <li>
            <strong>Find a place.</strong> Search for a city or area, use your current location, or click
            anywhere on the map.
          </li>
          <li>
            <strong>See the air.</strong> You get the US Air Quality Index (AQI) and the six main pollutants
            for that spot.
          </li>
          <li>
            <strong>Subscribe.</strong> Pick an AQI limit for the places you care about, like home, work or
            your parents' place, and give each one your own name.
          </li>
          <li>
            <strong>Get alerted.</strong> Whenever you open AirHealth (and every minute while it's open), we
            check your places. If the AQI is at or above your limit, an alert appears under the bell.
            You get at most one alert per place every 6 hours, so a bad day doesn't flood you.
          </li>
        </ol>
        <p className="about__note">
          Alerts are in-app only for now. There are no emails or push notifications yet.
        </p>
      </section>

      <section id="aqi-scale" className="about__section">
        <h2>Understanding AQI</h2>
        <p>
          The Air Quality Index turns pollutant levels into one number from 0 upwards. AirHealth uses the
          US EPA scale: the higher the number, the more polluted the air.
        </p>
        <ul className="aqi-scale">
          {AQI_BANDS.map((band, i) => (
            <li key={band.label} className="aqi-scale__row">
              <span className="aqi-scale__swatch" style={{ background: band.color, color: band.text }}>
                {bandRange(i)}
              </span>
              <div>
                <strong>{band.label}</strong>
                <p>{band.advice}</p>
              </div>
            </li>
          ))}
        </ul>
        <p className="about__note">
          Other countries use different scales. India's CPCB AQI, for example, has different bands and
          labels, so the same air can get a different number there.
        </p>
      </section>

      <section id="pollutants" className="about__section">
        <h2>Pollutants</h2>
        <p>All values are concentrations in {POLLUTANT_UNIT} (micrograms per cubic metre of air).</p>
        <dl className="pollutant-list">
          {POLLUTANTS.map((p) => (
            <div key={p.key} className="pollutant-list__item">
              <dt>
                <span className="pollutant-list__code">{p.label}</span> {p.name}
              </dt>
              <dd>{p.about}</dd>
            </div>
          ))}
        </dl>
      </section>

      <section id="data" className="about__section">
        <h2>Data &amp; accuracy</h2>
        <ul className="about__list">
          <li>
            Readings come from the <strong>Copernicus Atmosphere Monitoring Service (CAMS)</strong> through
            Open-Meteo. They are <strong>modeled estimates</strong> from an atmospheric model, not
            measurements from a sensor near you.
          </li>
          <li>
            The model works on a coarse grid (tens of kilometres outside Europe), so it shows the general
            air in an area. It won't capture differences between one street and the next, like a busy road
            versus a park.
          </li>
          <li>
            Values are updated about every hour. We keep each reading for up to 30 minutes, and nearby
            points (within about 1 km) share one reading.
          </li>
          <li>
            AirHealth is for general awareness. It is not medical advice. If you have a health condition,
            follow your doctor's guidance and official local alerts.
          </li>
        </ul>
      </section>

      <section id="privacy" className="about__section">
        <h2>Your data</h2>
        <ul className="about__list">
          <li>
            We store your name, email, a securely hashed password (never the password itself), your
            subscriptions and your alerts.
          </li>
          <li>
            Your location is only used when you press "use my current location". It is sent to our server
            to look up the area name and air quality, and is not stored unless you subscribe to it.
          </li>
          <li>
            Place searches and map lookups go through our server to Open-Meteo and OpenStreetMap. Your
            account details are never sent to them.
          </li>
        </ul>
      </section>

      <section id="credits" className="about__section">
        <h2>Credits</h2>
        <ul className="about__list about__credits">
          <li>
            Air quality: <a href="https://open-meteo.com/en/docs/air-quality-api" target="_blank" rel="noreferrer">Open-Meteo Air Quality API</a>
            {' '}(CC BY 4.0). Contains modified Copernicus Atmosphere Monitoring Service information.
          </li>
          <li>
            Place search: <a href="https://open-meteo.com/en/docs/geocoding-api" target="_blank" rel="noreferrer">Open-Meteo Geocoding API</a>,
            based on GeoNames data.
          </li>
          <li>
            Map and area names: © <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noreferrer">OpenStreetMap contributors</a>
            {' '}(ODbL), looked up with Nominatim.
          </li>
          <li>
            Map display: <a href="https://leafletjs.com" target="_blank" rel="noreferrer">Leaflet</a>.
          </li>
        </ul>
      </section>
    </article>
  )
}
