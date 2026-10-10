import { useEffect, useState } from 'react'
import logo from '../assests/logo.png'
import { api } from '../api'
import { clearToken } from '../auth'
import { navigate, useLocation } from '../router'
import NotificationBell from './NotificationBell'
import SiteFooter from './SiteFooter'
import { Link } from './nav'
import './AppShell.css'

const NAV = [
  { to: '/app', label: 'Dashboard' },
  { to: '/app/subscriptions', label: 'My subscriptions' },
  { to: '/about', label: 'About' },
]

export default function AppShell({ children }) {
  const { pathname } = useLocation()
  const [user, setUser] = useState(null)

  useEffect(() => {
    const controller = new AbortController()
    // a 401 here is handled centrally; any other failure just leaves the name out
    api.me({ signal: controller.signal }).then(setUser, () => {})
    return () => controller.abort()
  }, [])

  function logout() {
    clearToken()
    navigate('/login')
  }

  return (
    <div className="shell">
      <header className="shell__header">
        <div className="shell__bar">
          <Link to="/app" className="shell__brand">
            <img src={logo} alt="Airhealth" />
          </Link>

          <nav className="shell__nav" aria-label="Main">
            {NAV.map((item) => (
              <Link
                key={item.to}
                to={item.to}
                className={pathname === item.to ? 'is-active' : undefined}
                aria-current={pathname === item.to ? 'page' : undefined}
              >
                {item.label}
              </Link>
            ))}
            <span className="shell__nav-soon" title="Coming soon" aria-disabled="true">
              Clean routes <span className="soon-tag">Soon</span>
            </span>
          </nav>

          <div className="shell__actions">
            {user && <span className="shell__user" title={user.email}>{user.name}</span>}
            <NotificationBell />
            <button type="button" className="button button--ghost shell__logout" onClick={logout}>
              Log out
            </button>
          </div>
        </div>
      </header>

      <main className="shell__main">{children}</main>

      <SiteFooter />
    </div>
  )
}
