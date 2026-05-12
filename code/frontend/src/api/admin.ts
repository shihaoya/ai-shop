import request from './request'
import type { PageResult, PageRequest, Shop, UserInfo } from '@/types/api'

type AdminListResponse<T> = { records: T[]; total: number; page: number; size: number }
type InviteCodeItem = { id: string; code: string; role: number; creatorId: string; status: number; createdAt?: string }

// 店铺列表
export function getShops(params: PageRequest): Promise<AdminListResponse<Shop>> {
  return request.get<AdminListResponse<Shop>>('/admin/shops', { params }) as any
}

// 审核店铺
export function auditShop(shopId: string | number, status: number): Promise<null> {
  return request.put<null>(`/admin/shops/${shopId}/audit`, null, { params: { status } }) as any
}

// 用户列表
export function getUsers(params: PageRequest & { role?: number; status?: number }): Promise<AdminListResponse<UserInfo>> {
  return request.get<AdminListResponse<UserInfo>>('/admin/users', { params }) as any
}

// 修改用户状态
export function changeUserStatus(userId: string | number, status: number): Promise<null> {
  return request.put<null>(`/admin/users/${userId}/status`, null, { params: { status } }) as any
}

// 审批用户
export function approveUser(userId: string | number): Promise<null> {
  return request.put<null>(`/admin/users/${userId}/approve`) as any
}

// 拒绝用户
export function rejectUser(userId: string | number): Promise<null> {
  return request.put<null>(`/admin/users/${userId}/reject`) as any
}

// 获取邀请码
export function getInviteCode(): Promise<InviteCodeItem[]> {
  return request.get<InviteCodeItem[]>('/admin/invite-code') as any
}

// 生成邀请码
export function createInviteCode(): Promise<{ code: string }> {
  return request.post<{ code: string }>('/admin/invite-code') as any
}
