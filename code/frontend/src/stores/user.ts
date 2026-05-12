import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { UserInfo } from '@/types/api'

interface LoginParams {
  username: string
  password: string
}

export const useUserStore = defineStore('user', () => {
  const router = useRouter()
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

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
      // 模拟登录（实际应调用 API）
      // const res = await authApi.login(params)
      // token.value = res.data.token
      // userInfo.value = res.data.userInfo

      // 模拟返回
      if (params.username === 'admin' && params.password === 'admin123') {
        token.value = 'mock-token-admin'
        userInfo.value = {
          id: '1',
          username: 'admin',
          nickname: '管理员',
          role: 1,
          status: 2,
          points: 0,
        }
        localStorage.setItem('token', token.value)
        message.success('登录成功')
        router.push(homePath.value)
      } else if (params.username && params.password) {
        token.value = 'mock-token-user'
        userInfo.value = {
          id: '2',
          username: params.username,
          nickname: params.username,
          role: 3,
          status: 2,
          points: 1000,
        }
        localStorage.setItem('token', token.value)
        message.success('登录成功')
        router.push(homePath.value)
      } else {
        message.error('用户名或密码错误')
      }
    } catch (error: any) {
      message.error(error.message || '登录失败')
    }
  }

  async function fetchUserInfo() {
    try {
      // 模拟获取用户信息
      // const res = await authApi.getUserInfo()
      // userInfo.value = res.data
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
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