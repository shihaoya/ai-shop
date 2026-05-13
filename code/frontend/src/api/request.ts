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
        // 避免在登录页触发循环 logout
        if (router.currentRoute.value.name !== 'Login') {
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          message.error('登录已过期，请重新登录')
        }
      } else {
        message.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message))
    }
    // 返回内部真实数据（解包装）
    return res.data
  },
  (error) => {
    if (error.response?.status === 401) {
      // 避免在登录页触发循环 logout
      if (router.currentRoute.value.name !== 'Login') {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
        message.error('登录已过期，请重新登录')
      }
    } else {
      message.error(error.response?.data?.message || error.message || '网络异常')
    }
    return Promise.reject(error)
  },
)

export default request