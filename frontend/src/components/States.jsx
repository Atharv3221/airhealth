import './States.css'

export function Skeleton({ className = '', style }) {
  return <span className={`skeleton ${className}`} style={style} aria-hidden="true" />
}

export function EmptyState({ title, children }) {
  return (
    <div className="state state--empty">
      <p className="state__title">{title}</p>
      {children && <div className="state__body">{children}</div>}
    </div>
  )
}

export function ErrorState({ message, onRetry }) {
  return (
    <div className="state state--error" role="alert">
      <p className="state__title">{message}</p>
      {onRetry && (
        <button type="button" className="button button--ghost" onClick={onRetry}>
          Try again
        </button>
      )}
    </div>
  )
}
