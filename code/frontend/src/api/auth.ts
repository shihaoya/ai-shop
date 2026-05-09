import api from './index'
import type { ApiResponse, LoginRequest, RegisterRequest, LoginResponse } from '@/types'

export const authApi = {
  login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return api.post('/auth/login', data)
  },

  register(data: RegisterRequest): Promise<ApiResponse<LoginResponse>> {
    return api.post('/auth/register', data)
  },

  getUserInfo(): Promise<ApiResponse<any>> {
    return api.get('/auth/userinfo')
  },

  logout(): Promise<ApiResponse<void>> {
    return api.post('/auth/logout')
  },
}