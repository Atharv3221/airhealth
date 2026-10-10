import { useState } from "react"
import { api, errorMessage } from "../../api"
import { setToken } from "../../auth"
import GoogleButton from "./GoogleButton"

export default function SignupForm({ onSwitch }) {
    const [name, setName] = useState('')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')

    async function handleSubmit(e) {
        e.preventDefault()
        setError('')
        setSubmitting(true)
        const cleanEmail = email.trim()
        try {
            await api.signup(name.trim(), cleanEmail, password)
        } catch (err) {
            setError(errorMessage(err, {
              409: 'An account with this email already exists. Try logging in instead.',
            }))
            setSubmitting(false)
            return
        }
        try {
            // log straight in so the user lands in the app
            const { token } = await api.login(cleanEmail, password)
            setToken(token)
        } catch {
            setError('Your account was created, but we could not log you in. Please log in.')
            setSubmitting(false)
        }
    }

    return (
        <>
          <h2 id="auth-title">Join Airhealth</h2>
          <p className="auth-sub">
            Save areas and get alerts when the air changes.
          </p>

          <form className="auth-form" onSubmit={handleSubmit}>
            {error && <p className="auth-alert auth-alert--error" role="alert">{error}</p>}

            <label className="field">
              Name
              <input
                type="text"
                autoComplete="name"
                placeholder="Your name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                autoFocus
              />
            </label>

            <label className="field">
              Email
              <input
                type="email"
                autoComplete="email"
                placeholder="xyz@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </label>

            <label className="field">
                Password
                <input
                  type="password"
                  autoComplete="new-password"
                  placeholder="At least 8 characters"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  minLength={8}
                  required
                />
            </label>

            <button type="submit" className="button button--primary auth-submit" disabled={submitting}>
              {submitting ? 'Creating account…' : 'Create account'}
            </button>
          </form>

          <div className="auth-divider">or</div>
          <GoogleButton label="Sign up with Google" />

          <p className="auth-switch">
            Already have an account?{' '}
            <button type="button" className="auth-link" onClick={onSwitch}>
              Log in
            </button>
          </p>
        </>
    )
}
