import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { LoginPage } from './LoginPage'
import { AuthContext } from '../contexts/authContextDefinition'
import { authService } from '../services/authService'
import { ApiRequestError } from '../services/api'

jest.mock('../services/authService')
const mockedAuthService = authService as jest.Mocked<typeof authService>

function renderLoginPage(login = jest.fn()) {
  render(
    <AuthContext.Provider value={{ token: null, login, logout: jest.fn() }}>
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<div>Map page</div>} />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>,
  )
}

describe('LoginPage', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('logs in and navigates to the map on success', async () => {
    const login = jest.fn()
    mockedAuthService.login.mockResolvedValue({ token: 'a-jwt-token' })
    renderLoginPage(login)

    await userEvent.type(screen.getByLabelText('Email'), 'user@example.com')
    await userEvent.type(screen.getByLabelText('Password'), 'password123')
    await userEvent.click(screen.getByText('Log in'))

    expect(mockedAuthService.login).toHaveBeenCalledWith({
      email: 'user@example.com',
      password: 'password123',
    })
    expect(login).toHaveBeenCalledWith('a-jwt-token')
    expect(await screen.findByText('Map page')).toBeInTheDocument()
  })

  it('shows the backend error message on failure', async () => {
    mockedAuthService.login.mockRejectedValue(new ApiRequestError(403, 'Please verify your email address before logging in'))
    renderLoginPage()

    await userEvent.type(screen.getByLabelText('Email'), 'user@example.com')
    await userEvent.type(screen.getByLabelText('Password'), 'password123')
    await userEvent.click(screen.getByText('Log in'))

    expect(await screen.findByText('Please verify your email address before logging in')).toBeInTheDocument()
  })

  it('shows a generic error message for unexpected failures', async () => {
    mockedAuthService.login.mockRejectedValue(new Error('network down'))
    renderLoginPage()

    await userEvent.type(screen.getByLabelText('Email'), 'user@example.com')
    await userEvent.type(screen.getByLabelText('Password'), 'password123')
    await userEvent.click(screen.getByText('Log in'))

    expect(await screen.findByText('Could not log in. Please try again.')).toBeInTheDocument()
  })

  it('links to the register and forgot-password pages', () => {
    renderLoginPage()

    expect(screen.getByText('Sign up').closest('a')).toHaveAttribute('href', '/register')
    expect(screen.getByText('Forgot password?').closest('a')).toHaveAttribute('href', '/forgot-password')
  })
})
