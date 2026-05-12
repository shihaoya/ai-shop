import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { authApi } from '@/api/auth'
import type { UserInfo, LoginParams } from '@/types/api'

export const useUserStore = defineStore('user', () => {
  const router = useRouter()
  const USER_INFO_KEY = 'user-info'
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(
    (() => {
      try {
        const saved = localStorage.getItem(USER_INFO_KEY)
        return saved ? JSON.parse(saved) : null
      } catch {
        return null
      }
    })()
  )

  const role = computed(() => userInfo.value?.role)
  const status = computed(() => userInfo.value?.status)

  const homePath = computed(() => {
    const map: Record<number, string> = {
      1: '/admin/shops',
      2: '/operator/shop',
      3: '/user/products',
    }
    return map[role.value || 0] || '/login'
  })

  async function login(params: LoginParams) {
    try {
      const res = await authApi.login(params)
      console.log('登录响应:', res)
      token.value = res.token
      userInfo.value = res.userinfo
      console.log('userInfo after login:', userInfo.value)
      console.log('homePath:', homePath.value)
      localStorage.setItem('token', token.value)
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo.value))
      message.success('登录成功')
      await router.push(homePath.value)
    } catch (error) {
      // 错误已在 request 拦截器中提示，此处不再重复提示
      // 仅阻止 loading 状态即可
    }
  }

  async function fetchUserInfo() {
    try {
      const res = await authApi.getUserInfo()
      userInfo.value = res
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo.value))
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
  }

  function logout() {
    authApi.logout().catch(console.error)
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem(USER_INFO_KEY)
    router.push('/login')
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
  }

  return {
    token,
    userInfo,
    role,
    status,
    homePath,
    login,
    fetchUserInfo,
    logout,
    setUserInfo,
  }
})