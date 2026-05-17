import request from './request'
import type { PageRequest, Shop, Product, Category, Order, UserInfo, PointsLog, InviteCode, Message } from '@/types/api'

type ListResponse<T> = { list: T[]; total: number; page: number; pageSize: number }

// ============ 店铺 ============
export function getMyShop(): Promise<any> {
  return request.get<any>('/operator/shop') as any
}

export function applyShop(name: string, description: string): Promise<Shop> {
  return request.post<Shop>('/operator/shop', { name, description }) as any
}

export function changeShopStatus(isActive: number): Promise<null> {
  return request.put<null>('/operator/shop/status', null, { params: { isActive } }) as any
}

// ============ 分类 ============
export function getCategories() {
  return request.get<Category[]>('/operator/categories') as any
}

export function createCategory(name: string, sort?: number): Promise<Category> {
  return request.post<Category>('/operator/categories', { name, sort }) as any
}

export function updateCategory(id: string | number, name?: string, sort?: number): Promise<null> {
  return request.put<null>(`/operator/categories/${id}`, { name, sort }) as any
}

export function deleteCategory(id: string | number): Promise<null> {
  return request.delete<null>(`/operator/categories/${id}`) as any
}

// ============ 商品 ============
export function getProducts(params: PageRequest & { keyword?: string }): Promise<ListResponse<Product>> {
  return request.get<ListResponse<Product>>('/operator/products', { params }) as any
}

export function createProduct(data: {
  name: string
  categoryId: string | number
  type: number
  price: number
  stock: number
  limitPerUser?: number
  mainImage?: string
  detailImages?: string
  description?: string
}): Promise<Product> {
  return request.post<Product>('/operator/products', data) as any
}

export function getProduct(id: string | number): Promise<Product> {
  return request.get<Product>(`/operator/products/${id}`) as any
}

export function updateProduct(id: string | number, data: {
  name?: string
  categoryId?: string | number
  type?: number
  price?: number
  stock?: number
  limitPerUser?: number
  mainImage?: string
  detailImages?: string
  description?: string
  status?: number
}): Promise<null> {
  return request.put<null>(`/operator/products/${id}`, data) as any
}

export function deleteProduct(id: string | number): Promise<null> {
  return request.delete<null>(`/operator/products/${id}`) as any
}

// ============ 订单 ============
export function getOrders(params: PageRequest & { status?: number }): Promise<ListResponse<Order>> {
  return request.get<ListResponse<Order>>('/operator/orders', { params }) as any
}

export function getOrder(id: string | number): Promise<Order> {
  return request.get<Order>(`/operator/orders/${id}`) as any
}

export function confirmOrder(id: string | number): Promise<null> {
  return request.put<null>(`/operator/orders/${id}/confirm`) as any
}

export function shipOrder(id: string | number, trackingNo: string, carrier?: string): Promise<null> {
  return request.put<null>(`/operator/orders/${id}/ship`, { trackingNo, carrier }) as any
}

export function closeOrder(id: string | number, reason?: string): Promise<null> {
  return request.put<null>(`/operator/orders/${id}/close`, null, { params: { reason } }) as any
}

export function completeOrder(id: string | number): Promise<null> {
  return request.put<null>(`/operator/orders/${id}/complete`) as any
}

// ============ 用户 ============
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

// ============ 消息 ============
export function getMessages(params: PageRequest): Promise<ListResponse<Message>> {
  return request.get<ListResponse<Message>>('/operator/messages', { params }) as any
}

export function markMessageRead(id: string | number): Promise<null> {
  return request.put<null>(`/operator/messages/${id}/read`) as any
}
