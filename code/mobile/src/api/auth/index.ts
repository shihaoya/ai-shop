import request from '../request'
import type { LoginParams, RegisterParams, UserInfo } from '@/types'

export const authApi = {
  login(data: LoginParams): Promise<{ token: string; userinfo: UserInfo }> {
    return request.post<{ token: string; userinfo: UserInfo }>('/auth/login', data) as any
  },
  register(data: RegisterParams): Promise<UserInfo> {
    return request.post<UserInfo>('/auth/register', data) as any
  },
  updatePassword(data: { oldPassword: string; newPassword: string }): Promise<null> {
    return request.put<null>('/auth/password', data) as any
  },
  logout(): Promise<null> {
    return request.post<null>('/auth/logout') as any
  },
  getUserInfo(): Promise<UserInfo & { points?: number }> {
    return request.get<UserInfo & { points?: number }>('/auth/info') as any
  },
  updateUserInfo(data: { nickname?: string }): Promise<UserInfo> {
    return request.put<UserInfo>('/auth/info', data) as any
  },
}

export type { LoginParams, RegisterParams, UserInfo }