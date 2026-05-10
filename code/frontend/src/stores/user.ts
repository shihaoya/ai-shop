import { create } from 'zustand'

interface UserState {
  // 状态
  loading: boolean
  // 操作
  setLoading: (loading: boolean) => void
}

export const useUserStore = create<UserState>()((set) => ({
  loading: false,
  setLoading: (loading) => set({ loading }),
}))