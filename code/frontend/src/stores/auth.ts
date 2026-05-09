import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserRole } from '@/types'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<User | null>(null)

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const userRole = computed(() => user.value?.role)
  const isAdmin = computed(() => userRole.value === 'ADMIN')
  const isOperator = computed(() => userRole.value === 'OPERATOR')
  const isUser = computed(() => userRole.value === 'USER')

  async function login(username: string, password: string) {
    const res = await authApi.login({ username, password })
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    return res.data
  }

  async function register(username: string, nickname: string, password: string, inviteCode?: string) {
    const res = await authApi.register({ username, nickname, password, inviteCode })
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    return res.data
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await authApi.getUserInfo()
      user.value = res.data
    } catch (error) {
      logout()
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  function getHomeRoute(): string {
    switch (userRole.value) {
      case 'ADMIN':
        return '/admin/shops'
      case 'OPERATOR':
        return '/operator/shop'
      case 'USER':
        return '/user/products'
      default:
        return '/login'
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    userRole,
    isAdmin,
    isOperator,
    isUser,
    login,
    register,
    fetchUserInfo,
    logout,
    getHomeRoute,
  }
})