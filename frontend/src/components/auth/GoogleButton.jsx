import googleIcon from '../../assests/google_icon.png'

export default function GoogleButton({ label }) {
    function handleClick() {
        console.log('Google sign-in')
    }

    return (
        <button type="button" className="auth-google" onClick={handleClick}>
            <img src={googleIcon} alt="" width="20" height="20" />
            {label}
        </button>
    )
}