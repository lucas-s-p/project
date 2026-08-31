import { render } from '@testing-library/react'
import { useAuth } from './authContextDefinition'

function Consumer() {
  useAuth()
  return null
}

describe('useAuth', () => {
  it('throws when used outside an AuthProvider', () => {
    const consoleError = jest.spyOn(console, 'error').mockImplementation(() => {})

    expect(() => render(<Consumer />)).toThrow('useAuth must be used within an AuthProvider')

    consoleError.mockRestore()
  })
})
