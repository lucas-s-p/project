import { authStorage } from './authStorage'

describe('authStorage', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('returns null when no token has been stored', () => {
    expect(authStorage.getToken()).toBeNull()
  })

  it('persists and retrieves a token', () => {
    authStorage.setToken('a-jwt-token')

    expect(authStorage.getToken()).toBe('a-jwt-token')
  })

  it('clears the stored token', () => {
    authStorage.setToken('a-jwt-token')

    authStorage.clearToken()

    expect(authStorage.getToken()).toBeNull()
  })
})
