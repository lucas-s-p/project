import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { AuthLayout } from '../components/AuthLayout'
import '../components/AuthForm.css'
import { ApiRequestError } from '../services/api'
import { authService } from '../services/authService'

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')

  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [confirmationMessage, setConfirmationMessage] = useState<string | null>(null)

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    setError(null)

    if (!token) {
      setError('Missing reset token.')
      return
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters long.')
      return
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    setSubmitting(true)
    authService
      .resetPassword({ token, newPassword: password })
      .then((response) => setConfirmationMessage(response.message))
      .catch((err: unknown) => {
        setError(err instanceof ApiRequestError ? err.message : 'Could not reset your password. Please try again.')
      })
      .finally(() => setSubmitting(false))
  }

  if (confirmationMessage) {
    return (
      <AuthLayout>
        <p className="auth-layout__message">{confirmationMessage}</p>
        <p className="auth-layout__switch">
          <Link to="/login">Go to login</Link>
        </p>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout>
      <form className="auth-form" onSubmit={handleSubmit} noValidate>
        <label className="auth-form__field">
          New password
          <input
            type="password"
            autoComplete="new-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </label>

        <label className="auth-form__field">
          Confirm new password
          <input
            type="password"
            autoComplete="new-password"
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
          />
        </label>

        {error && <p className="auth-form__error">{error}</p>}

        <button type="submit" className="auth-form__submit" disabled={submitting}>
          {submitting ? 'Saving…' : 'Reset password'}
        </button>
      </form>

      <p className="auth-layout__switch">
        <Link to="/login">Back to login</Link>
      </p>
    </AuthLayout>
  )
}
