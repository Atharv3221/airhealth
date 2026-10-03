import { useState } from 'react'
import Navbar from '../components/Navbar'
import CityCard from '../components/CityCard'
import AuthModal from '../components/auth/AuthModal'
import './Home.css'

const CITIES = [
  { name: 'Pune', aqi: 82 },
  { name: 'Mumbai', aqi: 118 },
  { name: 'Delhi', aqi: 246 },
  { name: 'Bengaluru', aqi: 58 },
]

export default function Home() {
  const [place, setPlace] = useState('')
  const [authMode, setAuthMode] = useState(null) // null | 'login' | 'signup'

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
    console.log('Search:', query)
  }

  function handleLocate() {
    if (!navigator.geolocation) return
    navigator.geolocation.getCurrentPosition(
      (pos) => console.log('Location:', pos.coords.latitude, pos.coords.longitude),
      () => console.log('Location permission denied')
    )
  }

  return (
    <>
    <main className="hero">
      <Navbar
        onLogin={() => setAuthMode('login')}
        onSignUp={() => setAuthMode('signup')}
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

      {/* Outside <main> so the .hero > * rule doesn't affect the overlay */}
      {authMode && (
        <AuthModal
          mode={authMode}
          onModeChange={setAuthMode}
          onClose={() => setAuthMode(null)}
        />
      )}
    </>
  )
}
