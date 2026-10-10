import { useState } from 'react'
import { api, errorMessage } from '../../api'
import { setToken } from '../../auth'
import GoogleButton from './GoogleButton'

export default function LoginForm({ onSwitch, notice }) {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')

    async function handleSubmit(e) {
        e.preventDefault()
        setError('')
        setSubmitting(true)
        try {
            const { token } = await api.login(email.trim(), password)
            // App sees the token and leaves this page
            setToken(token)
        } catch (err) {
            setError(errorMessage(err, { 401: 'Incorrect email or password.' }))
            setSubmitting(false)
        }
    }

    return (
        <>
          <h2 id="auth-title">Welcome back</h2>
          <p className="auth-sub">Log in to see your saved areas.</p>

          <form className="auth-form" onSubmit={handleSubmit}>
            {notice && !error && <p className="auth-alert auth-alert--info">{notice}</p>}
            {error && <p className="auth-alert auth-alert--error" role="alert">{error}</p>}

            <label className="field">
                Email
                <input
                  type="email"
                  autoComplete="email"
                  placeholder="xyz@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  autoFocus
                />
            </label>

            <label className="field">
                Password
                <input
                  type="password"
                  autoComplete="current-password"
                  placeholder="Your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
            </label>

            <button type="submit" className="button button--primary auth-submit" disabled={submitting}>
                {submitting ? 'Logging in…' : 'Log in'}
            </button>
          </form>

          <div className="auth-divider">or</div>
          <GoogleButton label="Sign in with Google"/>
          <p className="auth-switch">
            New to Airhealth?{' '}
            <button type="button" className="auth-link" onClick={onSwitch}>
              Sign up
            </button>
          </p>
        </>
    )
}
