import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { VerifyEmailPage } from './VerifyEmailPage'
import { authService } from '../services/authService'
import { ApiRequestError } from '../services/api'

jest.mock('../services/authService')
const mockedAuthService = authService as jest.Mocked<typeof authService>

function renderPage(initialEntry: string) {
  render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <VerifyEmailPage />
    </MemoryRouter>,
  )
}

describe('VerifyEmailPage', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('shows an error immediately when no token is present, without calling the API', () => {
    renderPage('/verify')

    expect(screen.getByText('Missing verification token.')).toBeInTheDocument()
    expect(mockedAuthService.verifyEmail).not.toHaveBeenCalled()
  })

  it('verifies the token and shows the success message', async () => {
    mockedAuthService.verifyEmail.mockResolvedValue({ message: 'Email verified. You can now log in.' })

    renderPage('/verify?token=abc-123')

    expect(mockedAuthService.verifyEmail).toHaveBeenCalledWith('abc-123')
    expect(await screen.findByText('Email verified. You can now log in.')).toBeInTheDocument()
    expect(screen.getByText('Go to login')).toBeInTheDocument()
  })

  it('shows the backend error message when verification fails', async () => {
    mockedAuthService.verifyEmail.mockRejectedValue(
      new ApiRequestError(400, 'This verification link is invalid or has expired'),
    )

    renderPage('/verify?token=bad-token')

    expect(await screen.findByText('This verification link is invalid or has expired')).toBeInTheDocument()
  })
})
