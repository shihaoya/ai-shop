import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('mobile_token'))
  const userInfo = ref<UserInfo | null>(JSON.parse(localStorage.getItem('mobile_user_info') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('mobile_token', t)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem('mobile_user_info', JSON.stringify(info))
  }

  function logout() {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('mobile_token')
    localStorage.removeItem('mobile_user_info')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    role,
    setToken,
    setUserInfo,
    logout,
  }
})