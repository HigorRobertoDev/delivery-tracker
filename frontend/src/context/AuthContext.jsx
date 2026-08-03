import { createContext, useContext, useMemo, useState } from 'react'
import { api } from '../api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user')
    return stored ? JSON.parse(stored) : null
  })

  const value = useMemo(() => ({
    user,
    isAuthenticated: Boolean(user),
    async login(email, password) {
      const data = await api.login({ email, password })
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify({ name: data.name, email: data.email }))
      setUser({ name: data.name, email: data.email })
      return data
    },
    async register(name, email, password) {
      const data = await api.register({ name, email, password })
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify({ name: data.name, email: data.email }))
      setUser({ name: data.name, email: data.email })
      return data
    },
    logout() {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      setUser(null)
    },
  }), [user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider')
  }
  return context
}
