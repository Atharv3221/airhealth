import { useState } from 'react'
import GoogleButton from './GoogleButton'

export default function LoginForm({ onSwitch }) {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')

    function handleSubmit(e) {
        e.preventDefault();
        console.log('Log in:', email)
    }

    return (
        <>
          <h2 id="auth-title">Welcome back</h2>
          <p className="auth-sub">Log in to see your saved areas.</p>

          <form className="auth-form" onSubmit={handleSubmit}>
            <div>
                <label htmlFor="login-email">Email</label>
                <input
                  id="login-email"
                  type="email"
                  autoComplete="email"
                  placeholder="xyz@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  autoFocus
                />
            </div>

            <div>
                <label htmlFor="login-password">Password</label>
                <input
                  id="login-password"
                  type="password"
                  autoComplete="current-password"
                  placeholder="Your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
            </div>

            <button type="submit" className="auth-submit">
                Log in
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
