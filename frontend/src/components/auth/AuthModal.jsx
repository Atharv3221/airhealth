import { useEffect } from 'react'
import cloud from '../../assests/cloud-icon.png'
import LoginForm from './LoginForm'
import SignupForm from './SignupForm'
import './AuthModal.css'

export default function AuthModal({ mode, onModeChange, onClose }) {
    // Close on Escape, and stop the page behind from scrolling while open
    useEffect(() => {
        function onKey(e) {
            if (e.key === 'Escape') {
                onClose()
            }
        }

        document.addEventListener('keydown', onKey)
        document.body.style.overflow = 'hidden'
        return () => {
            document.removeEventListener('keydown', onKey)
            document.body.style.overflow = ''
        }
    }, [onClose])

    return (
        <div className="auth-overlay" onClick={onClose}>
            <div className="auth-scrim" aria-hidden="true" />

            <div
              className="auth-card"
              role="dialog"
              aria-modal="true"
              aria-labelledby="auth-title"
              onClick={(e) => e.stopPropagation()}
            >
              <button type="button" className="auth-close" onClick={onClose} aria-label="Close">
                x
              </button>

              <div className="auth-badge">
                <img src={cloud} alt="cloud icon"/>
              </div>
              {
                /* key={mode} remounts this block on switch, which replays the slide animation */
              }
              <div className="auth-body" key={mode}>
                {mode === 'login' ? (
                    <LoginForm onSwitch={() => onModeChange('signup')} />
                ) : (
                    <SignupForm onSwitch={() => onModeChange('login')} />
                )}
              </div>
            </div>

        </div>
    )
}
