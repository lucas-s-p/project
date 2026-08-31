const STORAGE_KEY = 'plotline_auth_token'

export const authStorage = {
  getToken: (): string | null => localStorage.getItem(STORAGE_KEY),
  setToken: (token: string): void => localStorage.setItem(STORAGE_KEY, token),
  clearToken: (): void => localStorage.removeItem(STORAGE_KEY),
}
