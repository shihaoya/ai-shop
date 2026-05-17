import request from './request'
import type { LoginParams, RegisterParams, UserInfo } from '@/types/api'

export const authApi = {
  login(data: LoginParams): Promise<{ token: string; userinfo: UserInfo }> {
    return request.post<{ token: string; userinfo: UserInfo }>('/auth/login', data) as any
  },
  register(data: RegisterParams): Promise<UserInfo> {
    return request.post<UserInfo>('/auth/register', data) as any
  },
  logout(): Promise<null> {
    return request.post<null>('/auth/logout') as any
  },
}
