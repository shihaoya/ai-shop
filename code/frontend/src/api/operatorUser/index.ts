import request from '../request'
import type { PageRequest, UserInfo, PointsLog } from '@/types/api'

type ListResponse<T> = { list: T[]; total: number; page: number; pageSize: number }

export function getUsers(params: PageRequest & { status?: number }): Promise<ListResponse<UserInfo>> {
  return request.get<ListResponse<UserInfo>>('/operator/users', { params }) as any
}

export function adjustPoints(userId: string | number, amount: number, remark?: string): Promise<null> {
  return request.post<null>(`/operator/users/${userId}/points`, { amount, remark }) as any
}

export function getPointsLog(userId: string | number, params: PageRequest): Promise<ListResponse<PointsLog>> {
  return request.get<ListResponse<PointsLog>>(`/operator/users/${userId}/points/log`, { params }) as any
}

export function approveUser(userId: string | number): Promise<null> {
  return request.put<null>(`/operator/users/${userId}/approve`) as any
}

export function rejectUser(userId: string | number): Promise<null> {
  return request.put<null>(`/operator/users/${userId}/reject`) as any
}

export function resetPassword(userId: string | number): Promise<{ password: string }> {
  return request.put<{ password: string }>(`/operator/users/${userId}/password/reset`) as any
}

export function createUser(username: string, nickname: string, password: string): Promise<UserInfo> {
  return request.post<UserInfo>('/operator/users/create', { username, nickname, password }) as any
}

// ============ 导入用户 ============
/** 下载导入模板 */
export function downloadImportTemplate() {
  const token = localStorage.getItem('token')
  const baseUrl = import.meta.env.VITE_API_BASE || ''
  const url = `${baseUrl}/api/operator/users/import/template`

  const xhr = new XMLHttpRequest()
  xhr.open('GET', url, true)
  xhr.setRequestHeader('Authorization', `Bearer ${token}`)
  xhr.responseType = 'blob'
  xhr.onload = () => {
    if (xhr.status === 200) {
      const blob = xhr.response
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = '用户导入模板.xlsx'
      link.click()
      URL.revokeObjectURL(link.href)
    }
  }
  xhr.send()
}

/** 导入用户（上传 Excel） */
export function importUsers(file: File): Promise<ImportResult> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ImportResult>('/operator/users/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }) as any
}

export interface ImportError {
  row: number
  message: string
}

export interface ImportResult {
  hasErrors: boolean
  success?: boolean
  errors?: ImportError[]
  users?: { username: string; nickname: string; password: string }[]
}

// ============ 邀请码 ============
export function getInviteCode(): Promise<string | null> {
  return request.get<string | null>('/operator/invite-code') as any
}

export function createInviteCode(): Promise<string> {
  return request.post<string>('/operator/invite-code') as any
}