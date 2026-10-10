import logo from '../assests/logo.png'
import { Link } from './nav'
import SiteFooter from './SiteFooter'
import './AppShell.css'

/** Header + footer for pages logged-out visitors can open (e.g. About). */
export default function PublicShell({ children }) {
  return (
    <div className="shell">
      <header className="shell__header">
        <div className="shell__bar">
          <Link to="/" className="shell__brand">
            <img src={logo} alt="Airhealth home" />
          </Link>
          <nav className="shell__nav" aria-label="Main">
            <Link to="/">Home</Link>
            <Link to="/about" className="is-active" aria-current="page">About</Link>
          </nav>
          <div className="shell__actions">
            <Link to="/login" className="button button--ghost">Log in</Link>
            <Link to="/signup" className="button button--primary">Sign up</Link>
          </div>
        </div>
      </header>
      <main className="shell__main">{children}</main>
      <SiteFooter />
    </div>
  )
}
