import { useCallback, useState } from 'react'
import type { ReactNode } from 'react'
import { authStorage } from '../services/authStorage'
import { AuthContext } from './authContextDefinition'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => authStorage.getToken())

  const login = useCallback((newToken: string) => {
    authStorage.setToken(newToken)
    setToken(newToken)
  }, [])

  const logout = useCallback(() => {
    authStorage.clearToken()
    setToken(null)
  }, [])

  return <AuthContext.Provider value={{ token, login, logout }}>{children}</AuthContext.Provider>
}
