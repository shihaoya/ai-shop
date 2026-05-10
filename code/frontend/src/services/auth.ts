import request from './request'
import type { LoginRequest, RegisterRequest, PasswordRequest, User } from '@/types'

// 登录
export const login = (data: LoginRequest) =>
  request.post('/api/auth/login', data)

// 注册
export const register = (data: RegisterRequest) =>
  request.post<void>('/api/auth/register', data)

// 修改密码
export const changePassword = (data: PasswordRequest) =>
  request.put<void>('/api/auth/password', data)

// 退出登录
export const logout = () =>
  request.post<void>('/api/auth/logout')

// 获取当前用户信息
export const getUserInfo = () =>
  request.get<User>('/api/auth/info')