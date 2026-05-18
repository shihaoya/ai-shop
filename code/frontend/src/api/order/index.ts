import request from '../request'
import type { PageRequest, Order } from '@/types/api'

type ListResponse<T> = { list: T[]; total: number; page: number; pageSize: number }

export function getOrders(params: PageRequest & { status?: number }): Promise<ListResponse<Order>> {
  return request.get<ListResponse<Order>>('/operator/orders', { params }) as any
}

export function getOrder(id: string | number): Promise<Order> {
  return request.get<Order>(`/operator/orders/${id}`) as any
}

export function confirmOrder(id: string | number): Promise<null> {
  return request.put<null>(`/operator/orders/${id}/confirm`) as any
}

export function shipOrder(id: string | number, expressNo: string, expressCompany?: string): Promise<null> {
  return request.put<null>(`/operator/orders/${id}/ship`, { expressNo, expressCompany }) as any
}

export function closeOrder(id: string | number, reason?: string): Promise<null> {
  return request.put<null>(`/operator/orders/${id}/close`, null, { params: { reason } }) as any
}

export function completeOrder(id: string | number): Promise<null> {
  return request.put<null>(`/operator/orders/${id}/complete`) as any
}