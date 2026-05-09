import api from './index'
import type { ApiResponse, PageResponse, Product, Order, Address, Message, PointsRecord } from '@/types'

export const userApi = {
  // 商品
  getProducts(params?: { page?: number; pageSize?: number; keyword?: string; shopId?: string }): Promise<ApiResponse<PageResponse<Product>>> {
    return api.get('/user/products', { params })
  },

  getProduct(id: string): Promise<ApiResponse<Product>> {
    return api.get(`/user/products/${id}`)
  },

  // 订单
  createOrder(data: { productId: string; quantity: number; addressId: string }): Promise<ApiResponse<any>> {
    return api.post('/user/orders', data)
  },

  getMyOrders(params?: { page?: number; pageSize?: number; status?: number }): Promise<ApiResponse<PageResponse<Order>>> {
    return api.get('/user/orders', { params })
  },

  closeOrder(id: string): Promise<ApiResponse<void>> {
    return api.put(`/user/orders/${id}/close`)
  },

  confirmOrder(id: string): Promise<ApiResponse<void>> {
    return api.put(`/user/orders/${id}/confirm`)
  },

  // 地址
  getAddresses(): Promise<ApiResponse<Address[]>> {
    return api.get('/user/addresses')
  },

  createAddress(data: any): Promise<ApiResponse<Address>> {
    return api.post('/user/addresses', data)
  },

  updateAddress(id: string, data: any): Promise<ApiResponse<void>> {
    return api.put(`/user/addresses/${id}`, data)
  },

  deleteAddress(id: string): Promise<ApiResponse<void>> {
    return api.delete(`/user/addresses/${id}`)
  },

  setDefaultAddress(id: string): Promise<ApiResponse<void>> {
    return api.put(`/user/addresses/${id}/default`)
  },

  // 消息
  getMessages(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<PageResponse<Message>>> {
    return api.get('/user/messages', { params })
  },

  getUnreadCount(): Promise<ApiResponse<number>> {
    return api.get('/user/messages/unread')
  },

  readMessage(id: string): Promise<ApiResponse<void>> {
    return api.put(`/user/messages/${id}/read`)
  },

  // 积分
  getPoints(): Promise<ApiResponse<number>> {
    return api.get('/user/points')
  },

  getPointsRecords(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<PageResponse<PointsRecord>>> {
    return api.get('/user/points/records', { params })
  },
}