import api from './index'
import type { ApiResponse, PageResponse, Shop, User } from '@/types'

export const adminApi = {
  // 店铺管理
  getShops(params?: { page?: number; pageSize?: number; status?: number }): Promise<ApiResponse<PageResponse<Shop>>> {
    return api.get('/admin/shops', { params })
  },

  approveShop(id: string): Promise<ApiResponse<void>> {
    return api.put(`/admin/shops/${id}/approve`)
  },

  rejectShop(id: string): Promise<ApiResponse<void>> {
    return api.put(`/admin/shops/${id}/reject`)
  },

  // 用户管理
  getUsers(params?: { page?: number; pageSize?: number; role?: string; status?: number }): Promise<ApiResponse<PageResponse<User>>> {
    return api.get('/admin/users', { params })
  },

  approveUser(id: string): Promise<ApiResponse<void>> {
    return api.put(`/admin/users/${id}/approve`)
  },

  freezeUser(id: string): Promise<ApiResponse<void>> {
    return api.put(`/admin/users/${id}/freeze`)
  },

  unfreezeUser(id: string): Promise<ApiResponse<void>> {
    return api.put(`/admin/users/${id}/unfreeze`)
  },
}