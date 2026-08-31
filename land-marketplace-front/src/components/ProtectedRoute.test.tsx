import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './ProtectedRoute'
import { AuthContext } from '../contexts/authContextDefinition'

function renderWithAuth(token: string | null) {
  return render(
    <AuthContext.Provider value={{ token, login: jest.fn(), logout: jest.fn() }}>
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route path="/login" element={<div>Login page</div>} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <div>Protected content</div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>,
  )
}

describe('ProtectedRoute', () => {
  it('renders its children when a token is present', () => {
    renderWithAuth('a-jwt-token')

    expect(screen.getByText('Protected content')).toBeInTheDocument()
  })

  it('redirects to /login when there is no token', () => {
    renderWithAuth(null)

    expect(screen.getByText('Login page')).toBeInTheDocument()
    expect(screen.queryByText('Protected content')).not.toBeInTheDocument()
  })
})
