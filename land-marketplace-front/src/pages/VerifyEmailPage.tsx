import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { AuthLayout } from '../components/AuthLayout'
import '../components/AuthForm.css'
import { ApiRequestError } from '../services/api'
import { authService } from '../services/authService'

type VerificationStatus = 'loading' | 'success' | 'error'

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')

  const [status, setStatus] = useState<VerificationStatus>(token ? 'loading' : 'error')
  const [message, setMessage] = useState(token ? 'Verifying your email…' : 'Missing verification token.')

  useEffect(() => {
    if (!token) {
      return
    }

    authService
      .verifyEmail(token)
      .then((response) => {
        setStatus('success')
        setMessage(response.message)
      })
      .catch((error: unknown) => {
        setStatus('error')
        setMessage(error instanceof ApiRequestError ? error.message : 'Could not verify your email.')
      })
  }, [token])

  return (
    <AuthLayout>
      <p className={status === 'error' ? 'auth-form__error' : 'auth-layout__message'}>{message}</p>
      {status !== 'loading' && (
        <p className="auth-layout__switch">
          <Link to="/login">Go to login</Link>
        </p>
      )}
    </AuthLayout>
  )
}
