import axios from 'axios'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

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
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  (error) => {
    const status = error.response?.status
    const backendMessage = error.response?.data?.message
    const errorMessage = backendMessage || error.message || '请求失败'

    if (status === 401) {
      showToast('登录已过期，请重新登录')
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/mobile/login'
    } else if (status >= 500) {
      showToast('服务器异常')
    } else {
      showToast(errorMessage)
    }
    return Promise.reject(error)
  },
)

export default request