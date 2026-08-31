import type { ReactNode } from 'react'
import './AuthLayout.css'

export function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <div className="auth-layout">
      <div className="auth-layout__card">
        <h1 className="auth-layout__title">Plotline</h1>
        <p className="auth-layout__tagline">Buy and sell land, one plot at a time.</p>
        {children}
      </div>
    </div>
  )
}
