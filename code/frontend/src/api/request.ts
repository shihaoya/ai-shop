import axios from 'axios'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器
request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      // 认证令牌无效（1005）或用户不存在（1003）视为未登录，跳转登录页
      if (res.code === 1005 || res.code === 1003) {
        if (router.currentRoute.value.name !== 'Login') {
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          message.error('登录已过期，请重新登录')
        }
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  (error) => {
    if (error.response?.status === 401) {
      if (router.currentRoute.value.name !== 'Login') {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
        message.error('登录已过期，请重新登录')
      }
    } else if (error.response?.status >= 500) {
      message.error('服务器异常')
    } else if (error.message) {
      message.error(error.message)
    }
    return Promise.reject(error)
  },
)

export default request