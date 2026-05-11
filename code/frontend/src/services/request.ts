import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { toast } from 'sonner'
import type { ApiResponse } from '@/types'

const request: AxiosInstance = axios.create({
  baseURL: 'http://localhost:18780',
  timeout: 10000,
})

// 请求拦截器：添加 Authorization
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：处理 401 和 Result 包装
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, data, message } = response.data
    if (code === 200) {
      return data
    }
    // 非 200 码视为业务错误，直接 reject，不在这里 toast
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/auth/login'
    }

    // 后端通过 GlobalExceptionHandler 返回的标准 Result 错误
    const backendMessage = error.response?.data?.message
    if (backendMessage) {
      toast.error(backendMessage)
      return Promise.reject(new Error(backendMessage))
    }

    // HTTP 400 等无 body 情况
    const httpStatus = error.response?.status
    if (httpStatus === 400) {
      toast.error('请求参数有误，请检查输入')
      return Promise.reject(new Error('请求参数有误，请检查输入'))
    }

    return Promise.reject(error)
  }
)

export default request

// 通用请求方法
export const get = <T>(url: string, config?: AxiosRequestConfig) =>
  request.get<ApiResponse<T>>(url, config).then(res => res as unknown as T)

export const post = <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
  request.post<ApiResponse<T>>(url, data, config).then(res => res as unknown as T)

export const put = <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
  request.put<ApiResponse<T>>(url, data, config).then(res => res as unknown as T)

export const del = <T>(url: string, config?: AxiosRequestConfig) =>
  request.delete<ApiResponse<T>>(url, config).then(res => res as unknown as T)