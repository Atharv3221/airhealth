import cloud from '../assests/cloud-icon.png'
import logo from '../assests/logo.png'
import LoginForm from '../components/auth/LoginForm'
import SignupForm from '../components/auth/SignupForm'
import { Link } from '../components/nav'
import { navigate, useLocation } from '../router'
import './AuthPage.css'

export default function AuthPage({ mode }) {
  const { params } = useLocation()
  const next = params.get('next')
  const expired = params.get('expired') === '1'

  // carry ?next over when switching between login and signup
  const suffix = next ? `?next=${encodeURIComponent(next)}` : ''

  return (
    <main className="auth-page">
      <Link to="/" className="auth-page__brand">
        <img src={logo} alt="Airhealth home" />
      </Link>

      <div className="auth-card">
        <div className="auth-badge">
          <img src={cloud} alt="" />
        </div>
        {/* key={mode} remounts this block on switch, which replays the slide animation */}
        <div className="auth-body" key={mode}>
          {mode === 'login' ? (
            <LoginForm
              notice={expired ? 'Your session has expired. Please log in again.' : ''}
              onSwitch={() => navigate(`/signup${suffix}`)}
            />
          ) : (
            <SignupForm onSwitch={() => navigate(`/login${suffix}`)} />
          )}
        </div>
      </div>
    </main>
  )
}
