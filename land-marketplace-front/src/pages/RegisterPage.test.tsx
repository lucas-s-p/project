import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { RegisterPage } from './RegisterPage'
import { authService } from '../services/authService'
import { ApiRequestError } from '../services/api'

jest.mock('../services/authService')
const mockedAuthService = authService as jest.Mocked<typeof authService>

function renderRegisterPage() {
  render(
    <MemoryRouter initialEntries={['/register']}>
      <RegisterPage />
    </MemoryRouter>,
  )
}

async function fill(email: string, password: string, confirmPassword: string) {
  await userEvent.type(screen.getByLabelText('Email'), email)
  await userEvent.type(screen.getByLabelText('Password'), password)
  await userEvent.type(screen.getByLabelText('Confirm password'), confirmPassword)
}

describe('RegisterPage', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('rejects a password shorter than 8 characters without calling the API', async () => {
    renderRegisterPage()

    await fill('user@example.com', 'short', 'short')
    await userEvent.click(screen.getByText('Sign up'))

    expect(screen.getByText('Password must be at least 8 characters long.')).toBeInTheDocument()
    expect(mockedAuthService.register).not.toHaveBeenCalled()
  })

  it('rejects mismatched passwords without calling the API', async () => {
    renderRegisterPage()

    await fill('user@example.com', 'password123', 'password456')
    await userEvent.click(screen.getByText('Sign up'))

    expect(screen.getByText('Passwords do not match.')).toBeInTheDocument()
    expect(mockedAuthService.register).not.toHaveBeenCalled()
  })

  it('registers and shows the confirmation message on success', async () => {
    mockedAuthService.register.mockResolvedValue({
      message: 'Account created. Please check your email to verify it before logging in.',
    })
    renderRegisterPage()

    await fill('user@example.com', 'password123', 'password123')
    await userEvent.click(screen.getByText('Sign up'))

    expect(mockedAuthService.register).toHaveBeenCalledWith({
      email: 'user@example.com',
      password: 'password123',
    })
    expect(
      await screen.findByText('Account created. Please check your email to verify it before logging in.'),
    ).toBeInTheDocument()
  })

  it('shows the backend error message when registration fails', async () => {
    mockedAuthService.register.mockRejectedValue(new ApiRequestError(409, 'An account with email user@example.com already exists'))
    renderRegisterPage()

    await fill('user@example.com', 'password123', 'password123')
    await userEvent.click(screen.getByText('Sign up'))

    expect(await screen.findByText('An account with email user@example.com already exists')).toBeInTheDocument()
  })
})
