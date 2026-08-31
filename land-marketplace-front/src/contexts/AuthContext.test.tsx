import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AuthProvider } from './AuthContext'
import { useAuth } from './authContextDefinition'
import { authStorage } from '../services/authStorage'

function Consumer() {
  const { token, login, logout } = useAuth()
  return (
    <div>
      <span>token: {token ?? 'none'}</span>
      <button onClick={() => login('a-jwt-token')}>login</button>
      <button onClick={logout}>logout</button>
    </div>
  )
}

describe('AuthProvider / useAuth', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('starts with no token when localStorage is empty', () => {
    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>,
    )

    expect(screen.getByText('token: none')).toBeInTheDocument()
  })

  it('starts with whatever token is already in localStorage', () => {
    authStorage.setToken('existing-token')

    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>,
    )

    expect(screen.getByText('token: existing-token')).toBeInTheDocument()
  })

  it('login stores the token and updates state', async () => {
    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>,
    )

    await userEvent.click(screen.getByText('login'))

    expect(screen.getByText('token: a-jwt-token')).toBeInTheDocument()
    expect(authStorage.getToken()).toBe('a-jwt-token')
  })

  it('logout clears the token from state and storage', async () => {
    authStorage.setToken('existing-token')
    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>,
    )

    await userEvent.click(screen.getByText('logout'))

    expect(screen.getByText('token: none')).toBeInTheDocument()
    expect(authStorage.getToken()).toBeNull()
  })
})
