import { useState } from "react"
import GoogleButton from "./GoogleButton"

export default function SignupForm({ onSwitch }) {
    const [name, setName] = useState('')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')

    function handleSubmit(e) {
        e.preventDefault();
        console.log('Sign up:', name, email)
    }

    return (
        <>
          <h2 id="auth-title">Join Airhealth</h2>
          <p className="auth-sub">
            Save areas and get alerts when the air changes.
          </p>

          <form className="auth-form" onSubmit={handleSubmit}>
            <div>
              <label htmlFor="signup-name">Name</label>
              <input
                id="signup-name"
                type="text"
                autoComplete="name"
                placeholder="Your name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                autoFocus
              />
            </div>

            <div>
              <label htmlFor="signup-email">Email</label>
              <input
                id="signup-email"
                type="email"
                autoComplete="email"
                placeholder="xyz@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div>
                <label htmlFor="signup-password">Password</label>
                <input
                  id="signup-password"
                  type="password"
                  autoComplete="new-password"
                  placeholder="At least 8 characters"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  minLength={8}
                  required
                />
            </div>

            <button type="submit" className="auth-submit">
              Create account
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
