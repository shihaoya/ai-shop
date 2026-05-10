import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
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
    // 非 200 码视为错误
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/auth/login'
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