import logo from '../assests/logo.png'
import { useToken } from '../auth'
import { Link } from './nav'
import './SiteFooter.css'

const APP_LINKS = [
  { to: '/app', label: 'Dashboard' },
  { to: '/app/subscriptions', label: 'My subscriptions' },
]

const GUEST_LINKS = [
  { to: '/', label: 'Home' },
  { to: '/login', label: 'Log in' },
  { to: '/signup', label: 'Sign up' },
]

export default function SiteFooter() {
  const loggedIn = Boolean(useToken())
  const year = new Date().getFullYear()

  return (
    <footer className="site-footer">
      <div className="site-footer__inner">
        <div className="site-footer__brand">
          <Link to={loggedIn ? '/app' : '/'}>
            <img src={logo} alt="Airhealth" />
          </Link>
          <p>Air quality, in plain sight. Check any place and get alerted when the air gets worse.</p>
        </div>

        <nav className="site-footer__col" aria-label="App">
          <h2>{loggedIn ? 'App' : 'Get started'}</h2>
          <ul>
            {(loggedIn ? APP_LINKS : GUEST_LINKS).map((l) => (
              <li key={l.to}><Link to={l.to}>{l.label}</Link></li>
            ))}
            <li>
              <span className="site-footer__soon" title="Coming soon">
                Clean routes <span className="soon-tag">Soon</span>
              </span>
            </li>
          </ul>
        </nav>

        <nav className="site-footer__col" aria-label="About">
          <h2>About</h2>
          <ul>
            <li><Link to="/about">About AirHealth</Link></li>
            <li><Link to="/about#aqi-scale">Understanding AQI</Link></li>
            <li><Link to="/about#data">Data &amp; accuracy</Link></li>
            <li><Link to="/about#credits">Credits</Link></li>
          </ul>
        </nav>
      </div>

      <div className="site-footer__bottom">
        <p>
          Values are <strong>modeled estimates</strong>, not sensor readings, and are not medical advice.
          Data: Open-Meteo / CAMS · Map © OpenStreetMap contributors
        </p>
        <p>© {year} AirHealth</p>
      </div>
    </footer>
  )
}
