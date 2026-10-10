import { useEffect } from 'react'
import { navigate } from '../router'

/** In-app link: plain <a> that navigates without a page reload (modifier-clicks still open new tabs). */
export function Link({ to, onClick, ...props }) {
  function handleClick(e) {
    onClick?.(e)
    if (e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) {
      return
    }
    e.preventDefault()
    navigate(to)
  }

  return <a href={to} onClick={handleClick} {...props} />
}

export function Redirect({ to }) {
  useEffect(() => {
    navigate(to, { replace: true })
  }, [to])
  return null
}
