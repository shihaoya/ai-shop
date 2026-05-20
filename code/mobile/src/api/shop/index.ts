import request from '../request'
import type { Shop } from '@/types'

export function getMyShop(): Promise<any> {
  return request.get<any>('/operator/shop') as any
}

export function applyShop(name: string, description: string): Promise<Shop> {
  return request.post<Shop>('/operator/shop', { name, description }) as any
}

export function changeShopStatus(isActive: number): Promise<null> {
  return request.put<null>('/operator/shop/status', null, { params: { isActive } }) as any
}