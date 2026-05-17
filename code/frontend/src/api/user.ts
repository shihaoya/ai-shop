import request from './request'
import type { PageRequest, Product, Order, Address, PointsLog, Message } from '@/types/api'

type ListResponse<T> = { list: T[]; total: number; page: number; size: number }

// ============ 商品 ============
export function getProducts(params: PageRequest & { keyword?: string }): Promise<ListResponse<Product>> {
  return request.get<ListResponse<Product>>('/products', { params }) as any
}

export function getProduct(id: string | number): Promise<Product> {
  return request.get<Product>(`/products/${id}`) as any
}

// ============ 订单 ============
export function createOrder(productId: string | number, quantity: number, addressInfo?: any): Promise<Order> {
  const data: any = { productId, quantity }
  if (addressInfo) {
    data.addressInfo = addressInfo
  }
  return request.post<Order>('/orders', data) as any
}

export function getOrders(params: PageRequest & { status?: number }): Promise<ListResponse<Order>> {
  return request.get<ListResponse<Order>>('/orders', { params }) as any
}

export function getOrder(id: string | number): Promise<Order> {
  return request.get<Order>(`/orders/${id}`) as any
}

export function closeOrder(id: string | number): Promise<null> {
  return request.put<null>(`/orders/${id}/close`) as any
}

export function completeOrder(id: string | number): Promise<null> {
  return request.put<null>(`/orders/${id}/complete`) as any
}

// ============ 用户信息 ============
export function getUserInfo(): Promise<any> {
  return request.get<any>('/users/me') as any
}

export function updateUserInfo(data: { nickname?: string }): Promise<any> {
  return request.put<any>('/users/me', data) as any
}

export function changePassword(oldPassword: string, newPassword: string): Promise<null> {
  return request.put<null>('/users/me/password', { oldPassword, newPassword }) as any
}

// ============ 积分 ============
export function getPoints(): Promise<{ points: number }> {
  return request.get<{ points: number }>('/users/points') as any
}

export function getPointsLog(params: PageRequest): Promise<ListResponse<PointsLog>> {
  return request.get<ListResponse<PointsLog>>('/users/points/log', { params }) as any
}

// ============ 地址 ============
export function getAddresses(): Promise<Address[]> {
  return request.get<Address[]>('/users/addresses') as any
}

export function createAddress(data: {
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
}): Promise<Address> {
  return request.post<Address>('/users/addresses', data) as any
}

export function updateAddress(id: string | number, data: Partial<Address>): Promise<null> {
  return request.put<null>(`/users/addresses/${id}`, data) as any
}

export function deleteAddress(id: string | number): Promise<null> {
  return request.delete<null>(`/users/addresses/${id}`) as any
}

export function setDefaultAddress(id: string | number): Promise<null> {
  return request.put<null>(`/users/addresses/${id}/default`) as any
}

// ============ 消息 ============
export function getMessages(params: PageRequest): Promise<ListResponse<Message>> {
  return request.get<ListResponse<Message>>('/user/messages', { params }) as any
}

export function markMessageRead(id: string | number): Promise<null> {
  return request.put<null>(`/user/messages/${id}/read`) as any
}

export const userApi = {
  getUserInfo,
  updateUserInfo,
  changePassword,
  getPoints,
  getPointsLog,
  getAddresses,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
}
