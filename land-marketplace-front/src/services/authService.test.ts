import { authService } from './authService'
import { api } from './api'

jest.mock('./api', () => ({
  api: {
    get: jest.fn(),
    post: jest.fn(),
  },
}))

const mockedApi = api as jest.Mocked<typeof api>

describe('authService', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('register posts the email and password', async () => {
    mockedApi.post.mockResolvedValue({ message: 'ok' })
    const input = { email: 'user@example.com', password: 'password123' }

    await authService.register(input)

    expect(mockedApi.post).toHaveBeenCalledWith('/auth/register', input)
  })

  it('login posts credentials and returns the token response', async () => {
    mockedApi.post.mockResolvedValue({ token: 'a-jwt-token' })

    const result = await authService.login({ email: 'user@example.com', password: 'password123' })

    expect(mockedApi.post).toHaveBeenCalledWith('/auth/login', {
      email: 'user@example.com',
      password: 'password123',
    })
    expect(result).toEqual({ token: 'a-jwt-token' })
  })

  it('verifyEmail sends the token as an encoded query parameter', async () => {
    mockedApi.get.mockResolvedValue({ message: 'verified' })

    await authService.verifyEmail('token with spaces')

    expect(mockedApi.get).toHaveBeenCalledWith('/auth/verify?token=token%20with%20spaces')
  })

  it('forgotPassword posts the email', async () => {
    mockedApi.post.mockResolvedValue({ message: 'ok' })

    await authService.forgotPassword({ email: 'user@example.com' })

    expect(mockedApi.post).toHaveBeenCalledWith('/auth/forgot-password', { email: 'user@example.com' })
  })

  it('resetPassword posts the token and new password', async () => {
    mockedApi.post.mockResolvedValue({ message: 'ok' })

    await authService.resetPassword({ token: 'abc', newPassword: 'newpassword123' })

    expect(mockedApi.post).toHaveBeenCalledWith('/auth/reset-password', {
      token: 'abc',
      newPassword: 'newpassword123',
    })
  })
})
