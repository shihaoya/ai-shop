import axios from 'axios'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      showToast(res.message || '请求失败')
      if (res.code === 1005 || res.code === 1003) {
        if (router.currentRoute.value.name !== 'Login') {
          const userStore = useUserStore()
          userStore.logout()
          router.push('/mobile/login')
          showToast('登录已过期，请重新登录')
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
        router.push('/mobile/login')
        showToast('登录已过期，请重新登录')
      }
    } else if (error.response?.status >= 500) {
      showToast('服务器异常')
    } else if (error.message) {
      showToast(error.message)
    }
    return Promise.reject(error)
  },
)

export default request