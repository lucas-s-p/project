import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { ResetPasswordPage } from './ResetPasswordPage'
import { authService } from '../services/authService'
import { ApiRequestError } from '../services/api'

jest.mock('../services/authService')
const mockedAuthService = authService as jest.Mocked<typeof authService>

function renderPage(initialEntry = '/reset-password?token=abc-123') {
  render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <ResetPasswordPage />
    </MemoryRouter>,
  )
}

async function fill(password: string, confirmPassword: string) {
  await userEvent.type(screen.getByLabelText('New password'), password)
  await userEvent.type(screen.getByLabelText('Confirm new password'), confirmPassword)
}

describe('ResetPasswordPage', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('shows an error and does not call the API when the token is missing', async () => {
    renderPage('/reset-password')

    await fill('newpassword123', 'newpassword123')
    await userEvent.click(screen.getByText('Reset password'))

    expect(screen.getByText('Missing reset token.')).toBeInTheDocument()
    expect(mockedAuthService.resetPassword).not.toHaveBeenCalled()
  })

  it('rejects a password shorter than 8 characters', async () => {
    renderPage()

    await fill('short', 'short')
    await userEvent.click(screen.getByText('Reset password'))

    expect(screen.getByText('Password must be at least 8 characters long.')).toBeInTheDocument()
    expect(mockedAuthService.resetPassword).not.toHaveBeenCalled()
  })

  it('rejects mismatched passwords', async () => {
    renderPage()

    await fill('newpassword123', 'different456')
    await userEvent.click(screen.getByText('Reset password'))

    expect(screen.getByText('Passwords do not match.')).toBeInTheDocument()
    expect(mockedAuthService.resetPassword).not.toHaveBeenCalled()
  })

  it('resets the password and shows the confirmation on success', async () => {
    mockedAuthService.resetPassword.mockResolvedValue({
      message: 'Password updated. You can now log in with your new password.',
    })
    renderPage()

    await fill('newpassword123', 'newpassword123')
    await userEvent.click(screen.getByText('Reset password'))

    expect(mockedAuthService.resetPassword).toHaveBeenCalledWith({
      token: 'abc-123',
      newPassword: 'newpassword123',
    })
    expect(
      await screen.findByText('Password updated. You can now log in with your new password.'),
    ).toBeInTheDocument()
  })

  it('shows the backend error message when the token is invalid or expired', async () => {
    mockedAuthService.resetPassword.mockRejectedValue(
      new ApiRequestError(400, 'This password reset link is invalid or has expired'),
    )
    renderPage()

    await fill('newpassword123', 'newpassword123')
    await userEvent.click(screen.getByText('Reset password'))

    expect(await screen.findByText('This password reset link is invalid or has expired')).toBeInTheDocument()
  })
})
