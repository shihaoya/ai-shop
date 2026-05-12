import request from './request'
import type { ApiResult, LoginParams, RegisterParams, UserInfo } from '@/types/api'

export const authApi = {
  login(data: LoginParams) {
    return request.post<ApiResult<{ token: string; userInfo: UserInfo }>>('/auth/login', data)
  },
  register(data: RegisterParams) {
    return request.post<ApiResult<UserInfo>>('/auth/register', data)
  },
  updatePassword(data: { oldPassword: string; newPassword: string }) {
    return request.put<ApiResult<null>>('/auth/password', data)
  },
  logout() {
    return request.post<ApiResult<null>>('/auth/logout')
  },
  getUserInfo() {
    return request.get<ApiResult<UserInfo & { points?: number }>>('/auth/info')
  },
}