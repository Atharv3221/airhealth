import googleIcon from '../../assests/google_icon.png'

// Google sign-in isn't supported by the backend yet. The wrapper carries the tooltip
// because disabled buttons don't receive hover events in every browser.
export default function GoogleButton({ label }) {
  return (
    <span className="auth-google-wrap" title="Coming soon">
      <button type="button" className="auth-google" disabled aria-describedby="google-soon">
        <img src={googleIcon} alt="" width="20" height="20" />
        {label}
        <span id="google-soon" className="soon-tag">Coming soon</span>
      </button>
    </span>
  )
}
