import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { ForgotPasswordPage } from './ForgotPasswordPage'
import { authService } from '../services/authService'
import { ApiRequestError } from '../services/api'

jest.mock('../services/authService')
const mockedAuthService = authService as jest.Mocked<typeof authService>

function renderPage() {
  render(
    <MemoryRouter initialEntries={['/forgot-password']}>
      <ForgotPasswordPage />
    </MemoryRouter>,
  )
}

describe('ForgotPasswordPage', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it(
    'sends the email and shows the confirmation message on success',
    async () => {
      mockedAuthService.forgotPassword.mockResolvedValue({
        message: 'If an account exists for that email, a password reset link has been sent.',
      })
      renderPage()

      await userEvent.type(screen.getByLabelText('Email'), 'user@example.com')
      await userEvent.click(screen.getByText('Send reset link'))

      expect(mockedAuthService.forgotPassword).toHaveBeenCalledWith({ email: 'user@example.com' })
      expect(
        await screen.findByText('If an account exists for that email, a password reset link has been sent.'),
      ).toBeInTheDocument()
    },
    15000,
  )

  it('shows the backend error message on failure', async () => {
    mockedAuthService.forgotPassword.mockRejectedValue(new ApiRequestError(400, 'Email must be a valid email address'))
    renderPage()

    await userEvent.type(screen.getByLabelText('Email'), 'not-an-email')
    await userEvent.click(screen.getByText('Send reset link'))

    expect(await screen.findByText('Email must be a valid email address')).toBeInTheDocument()
  })
})
