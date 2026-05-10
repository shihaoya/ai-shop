import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface UserInfo {
  id: string
  username: string
  nickname: string
  role: number
  status: number
  points?: number
}

interface AuthState {
  token: string | null
  userInfo: UserInfo | null
  setToken: (token: string) => void
  setUserInfo: (user: UserInfo) => void
  login: (token: string, user: UserInfo) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      userInfo: null,
      setToken: (token) => set({ token }),
      setUserInfo: (userInfo) => set({ userInfo }),
      login: (token, userInfo) => set({ token, userInfo }),
      logout: () => set({ token: null, userInfo: null }),
    }),
    { name: 'auth-storage' }
  )
)