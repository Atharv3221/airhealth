import { useState } from 'react'
import Navbar from '../components/Navbar'
import SiteFooter from '../components/SiteFooter'
import CityCard from '../components/CityCard'
import { navigate } from '../router'
import './Home.css'

const CITIES = [
  { name: 'Pune', aqi: 82 },
  { name: 'Mumbai', aqi: 118 },
  { name: 'Delhi', aqi: 246 },
  { name: 'Bengaluru', aqi: 58 },
]

export default function Home() {
  const [place, setPlace] = useState('')

  const today = new Date().toLocaleDateString('en-IN', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })

  function handleSubmit(e) {
    e.preventDefault()
    const query = place.trim()
    if (!query) {
      return
    }
    // the dashboard is behind login; it runs this search once the user is in
    navigate(`/app?q=${encodeURIComponent(query)}`)
  }

  function handleLocate() {
    navigate('/app?locate=1')
  }

  return (
    <>
    <main className="hero">
      <Navbar
        onLogin={() => navigate('/login')}
        onSignUp={() => navigate('/signup')}
      />

      <section className="hero__content">
        <div className="hero__pills">
          <span className="pill pill--dark">{today}</span>
          <span className="pill pill--light">Air quality, in plain sight</span>
        </div>

        <h1>How&apos;s the air where you are?</h1>
        <p className="hero__sub">
          Type your area and see what you are breathing right now, in one calm
          glance.
        </p>

        <form className="search" onSubmit={handleSubmit}>
          <label htmlFor="place" className="sr-only">
            Area or city
          </label>
          <input
            id="place"
            type="text"
            value={place}
            onChange={(e) => setPlace(e.target.value)}
            placeholder="Enter your area or city"
          />
          <button type="submit">Check my air</button>
        </form>

        <button type="button" className="hero__locate" onClick={handleLocate}>
          Or use my current location
        </button>
      </section>

      <section className="cities" aria-label="Sample air quality readings">
        <p className="cities__note">Sample readings, not live</p>
        <div className="cities__row">
          {CITIES.map((city) => (
            <CityCard key={city.name} name={city.name} aqi={city.aqi} />
          ))}
        </div>
      </section>
    </main>
    <SiteFooter />
    </>
  )
}
