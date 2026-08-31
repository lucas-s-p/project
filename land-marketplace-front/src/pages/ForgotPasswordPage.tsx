import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { AuthLayout } from '../components/AuthLayout'
import '../components/AuthForm.css'
import { ApiRequestError } from '../services/api'
import { authService } from '../services/authService'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [confirmationMessage, setConfirmationMessage] = useState<string | null>(null)

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    setError(null)
    setSubmitting(true)

    authService
      .forgotPassword({ email })
      .then((response) => setConfirmationMessage(response.message))
      .catch((err: unknown) => {
        setError(err instanceof ApiRequestError ? err.message : 'Could not send the reset email. Please try again.')
      })
      .finally(() => setSubmitting(false))
  }

  if (confirmationMessage) {
    return (
      <AuthLayout>
        <p className="auth-layout__message">{confirmationMessage}</p>
        <p className="auth-layout__switch">
          <Link to="/login">Back to login</Link>
        </p>
      </AuthLayout>
    )
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

        {error && <p className="auth-form__error">{error}</p>}

        <button type="submit" className="auth-form__submit" disabled={submitting}>
          {submitting ? 'Sending…' : 'Send reset link'}
        </button>
      </form>

      <p className="auth-layout__switch">
        <Link to="/login">Back to login</Link>
      </p>
    </AuthLayout>
  )
}
