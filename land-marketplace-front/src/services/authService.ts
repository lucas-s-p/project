import { api } from './api'
import type {
  AuthTokenResponse,
  ForgotPasswordInput,
  LoginInput,
  MessageResponse,
  RegisterInput,
  ResetPasswordInput,
} from '../types/auth'

export const authService = {
  register: (input: RegisterInput): Promise<MessageResponse> =>
    api.post<MessageResponse>('/auth/register', input),

  login: (input: LoginInput): Promise<AuthTokenResponse> =>
    api.post<AuthTokenResponse>('/auth/login', input),

  verifyEmail: (token: string): Promise<MessageResponse> =>
    api.get<MessageResponse>(`/auth/verify?token=${encodeURIComponent(token)}`),

  forgotPassword: (input: ForgotPasswordInput): Promise<MessageResponse> =>
    api.post<MessageResponse>('/auth/forgot-password', input),

  resetPassword: (input: ResetPasswordInput): Promise<MessageResponse> =>
    api.post<MessageResponse>('/auth/reset-password', input),
}
