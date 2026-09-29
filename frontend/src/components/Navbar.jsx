import logo from '../assests/logo.png'
import './Navbar.css'

export default function Navbar() {
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
                <a href="/login" className="btn btn--outline">
                  Log in
                </a>
                <a href="/signup" className="btn btn--solid">
                  Sign up
                </a>
            </div>
        </header>
    )
}
