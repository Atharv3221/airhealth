import './App.css'
import { useToken } from './auth'
import AppShell from './components/AppShell'
import { Redirect } from './components/nav'
import AuthPage from './pages/AuthPage'
import Dashboard from './pages/Dashboard'
import Home from './pages/Home'
import Subscriptions from './pages/Subscriptions'
import { safeNext, useLocation } from './router'

const APP_PAGES = {
  '/app': Dashboard,
  '/app/subscriptions': Subscriptions,
}

export default function App() {
  const { pathname, params, href } = useLocation()
  const token = useToken()

  if (pathname === '/') {
    return token ? <Redirect to="/app" /> : <Home />
  }

  if (pathname === '/login' || pathname === '/signup') {
    if (token) return <Redirect to={safeNext(params.get('next'))} />
    return <AuthPage mode={pathname === '/login' ? 'login' : 'signup'} />
  }

  if (pathname === '/app' || pathname.startsWith('/app/')) {
    if (!token) return <Redirect to={`/login?next=${encodeURIComponent(href)}`} />
    const Page = APP_PAGES[pathname]
    if (!Page) return <Redirect to="/app" />
    return (
      <AppShell>
        <Page />
      </AppShell>
    )
  }

  return <Redirect to="/" />
}
