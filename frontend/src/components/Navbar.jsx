import logo from '../assests/logo.png'
import { Link } from './nav'
import './Navbar.css'

export default function Navbar({ onLogin, onSignUp }) {
    return (
        <header className="navbar">
            <a href="/" className="navbar__brand">
              <img src={logo} alt="Airhealth" className="navbar__logo" />
            </a>

            <nav className="navbar__links" aria-label="Main">
                <Link to="/" className="active">Home</Link>
                <Link to="/about">About</Link>
                <span className="navbar__soon" title="Coming soon" aria-disabled="true">
                    Clean routes <span className="soon-tag">Soon</span>
                </span>
            </nav>

            <div className="navbar__actions">
                <button type="button" className="btn btn--outline" onClick={onLogin}>
                    Log in
                </button>
                <button type="button" className="btn btn--solid" onClick={onSignUp}>
                    Sign up
                </button>
            </div>
        </header>
    )
}
