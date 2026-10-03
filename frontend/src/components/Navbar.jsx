import logo from '../assests/logo.png'
import './Navbar.css'

export default function Navbar({ onLogin, onSignUp }) {
    return (
        <header className="navbar">
            <a href="/" className="navbar__brand">
              <img src={logo} alt="Airhealth" className="navbar__logo" />
            </a>

            <nav className="navbar__links" aria-label="Main">
                <a href="/" className="active">Home</a>
                <a href="/map">Live map</a>
                <a href="/routes">Clean routes</a>
                <a href="/health"></a>
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
