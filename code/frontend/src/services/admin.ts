import { get, post, put } from './request'
import type { PageRequest, PageResult } from '@/types'

// 店铺类型
export interface Shop {
  id: string
  name: string
  description: string
  status: number
  userId: string
  deleted: number
}

// 用户类型（管理员视角）
export interface AdminUser {
  id: string
  username: string
  nickname: string
  role: number
  parentId: string
  status: number
  points?: number
}

// 邀请码类型
export interface InviteCode {
  code: string
  role: number
  used: boolean
  userId?: string
}

// 所有店铺列表
export const getShops = (params?: PageRequest) =>
  get<PageResult<Shop>>('/api/admin/shops', { params })

// 审核店铺
export const auditShop = (id: string, status: number) =>
  put<void>(`/api/admin/shops/${id}/audit`, { status })

// 所有用户列表
export const getUsers = (params?: PageRequest) =>
  get<PageResult<AdminUser>>('/api/admin/users', { params })

// 冻结/解冻用户
export const updateUserStatus = (id: string, status: number) =>
  put<void>(`/api/admin/users/${id}/status`, { status })

// 审核通过店铺用户
export const approveUser = (id: string) =>
  put<void>(`/api/admin/users/${id}/approve`)

// 获取邀请码
export const getInviteCode = () =>
  get<InviteCode>('/api/admin/invite-code')

// 生成/重新生成邀请码
export const generateInviteCode = () =>
  post<InviteCode>('/api/admin/invite-code')