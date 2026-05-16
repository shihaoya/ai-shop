import request from './request'
import type { PageRequest, Product, Order, Address, PointsLog, Message } from '@/types/api'

type ListResponse<T> = { list: T[]; total: number; page: number; size: number }

// ============ 商品 ============
export function getProducts(params: PageRequest & { keyword?: string }): Promise<ListResponse<Product>> {
  return request.get<ListResponse<Product>>('/user/products', { params }) as any
}

export function getProduct(id: string | number): Promise<Product> {
  return request.get<Product>(`/user/products/${id}`) as any
}

// ============ 订单 ============
export function createOrder(productId: string | number, quantity: number, addressInfo?: any): Promise<Order> {
  const data: any = { productId, quantity }
  if (addressInfo) {
    data.addressInfo = addressInfo
  }
  return request.post<Order>('/user/orders', data) as any
}

export function getOrders(params: PageRequest & { status?: number }): Promise<ListResponse<Order>> {
  return request.get<ListResponse<Order>>('/user/orders', { params }) as any
}

export function getOrder(id: string | number): Promise<Order> {
  return request.get<Order>(`/user/orders/${id}`) as any
}

export function closeOrder(id: string | number): Promise<null> {
  return request.put<null>(`/user/orders/${id}/close`) as any
}

export function completeOrder(id: string | number): Promise<null> {
  return request.put<null>(`/user/orders/${id}/complete`) as any
}

// ============ 积分 ============
export function getPoints(): Promise<{ points: number }> {
  return request.get<{ points: number }>('/user/points') as any
}

export function getPointsLog(params: PageRequest): Promise<ListResponse<PointsLog>> {
  return request.get<ListResponse<PointsLog>>('/user/points/log', { params }) as any
}

// ============ 地址 ============
export function getAddresses(): Promise<Address[]> {
  return request.get<Address[]>('/user/addresses') as any
}

export function createAddress(data: {
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
}): Promise<Address> {
  return request.post<Address>('/user/addresses', data) as any
}

export function updateAddress(id: string | number, data: Partial<Address>): Promise<null> {
  return request.put<null>(`/user/addresses/${id}`, data) as any
}

export function deleteAddress(id: string | number): Promise<null> {
  return request.delete<null>(`/user/addresses/${id}`) as any
}

export function setDefaultAddress(id: string | number): Promise<null> {
  return request.put<null>(`/user/addresses/${id}/default`) as any
}

// ============ 消息 ============
export function getMessages(params: PageRequest): Promise<ListResponse<Message>> {
  return request.get<ListResponse<Message>>('/user/messages', { params }) as any
}

export function markMessageRead(id: string | number): Promise<null> {
  return request.put<null>(`/user/messages/${id}/read`) as any
}
