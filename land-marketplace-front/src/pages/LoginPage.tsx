import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { AuthLayout } from '../components/AuthLayout'
import '../components/AuthForm.css'
import { useAuth } from '../contexts/authContextDefinition'
import { ApiRequestError } from '../services/api'
import { authService } from '../services/authService'

export function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)

    authService
      .login({ email, password })
      .then((response) => {
        login(response.token)
        navigate('/', { replace: true })
      })
      .catch((err: unknown) => {
        setError(err instanceof ApiRequestError ? err.message : 'Could not log in. Please try again.')
      })
      .finally(() => setSubmitting(false))
  }

  return (
    <AuthLayout>
      <form className="auth-form" onSubmit={handleSubmit} noValidate>
        <label className="auth-form__field">
          Email
          <input
            type="email"
            autoComplete="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
        </label>

        <label className="auth-form__field">
          Password
          <input
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </label>

        {error && <p className="auth-form__error">{error}</p>}

        <button type="submit" className="auth-form__submit" disabled={submitting}>
          {submitting ? 'Logging in…' : 'Log in'}
        </button>
      </form>

      <p className="auth-layout__switch">
        Don&apos;t have an account? <Link to="/register">Sign up</Link>
      </p>
      <p className="auth-layout__switch">
        <Link to="/forgot-password">Forgot password?</Link>
      </p>
    </AuthLayout>
  )
}
