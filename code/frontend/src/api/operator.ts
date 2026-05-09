import api from './index'
import type { ApiResponse, PageResponse, Shop, Category, Product, Order, User, Message } from '@/types'

export const operatorApi = {
  // 店铺
  getMyShop(): Promise<ApiResponse<Shop>> {
    return api.get('/operator/shop')
  },

  createShop(data: { name: string; description?: string }): Promise<ApiResponse<Shop>> {
    return api.post('/operator/shop', data)
  },

  updateShopStatus(status: number): Promise<ApiResponse<void>> {
    return api.put('/operator/shop/status', { status })
  },

  // 分类
  getCategories(): Promise<ApiResponse<Category[]>> {
    return api.get('/operator/categories')
  },

  createCategory(data: { name: string }): Promise<ApiResponse<Category>> {
    return api.post('/operator/categories', data)
  },

  updateCategory(id: string, data: { name: string }): Promise<ApiResponse<void>> {
    return api.put(`/operator/categories/${id}`, data)
  },

  deleteCategory(id: string): Promise<ApiResponse<void>> {
    return api.delete(`/operator/categories/${id}`)
  },

  // 商品
  getProducts(params?: { page?: number; pageSize?: number; categoryId?: string; status?: number }): Promise<ApiResponse<PageResponse<Product>>> {
    return api.get('/operator/products', { params })
  },

  createProduct(data: any): Promise<ApiResponse<Product>> {
    return api.post('/operator/products', data)
  },

  updateProduct(id: string, data: any): Promise<ApiResponse<void>> {
    return api.put(`/operator/products/${id}`, data)
  },

  deleteProduct(id: string): Promise<ApiResponse<void>> {
    return api.delete(`/operator/products/${id}`)
  },

  updateProductStatus(id: string, status: number): Promise<ApiResponse<void>> {
    return api.put(`/operator/products/${id}/status`, { status })
  },

  // 订单
  getOrders(params?: { page?: number; pageSize?: number; status?: number }): Promise<ApiResponse<PageResponse<Order>>> {
    return api.get('/operator/orders', { params })
  },

  confirmOrder(id: string): Promise<ApiResponse<void>> {
    return api.put(`/operator/orders/${id}/confirm`)
  },

  shipOrder(id: string): Promise<ApiResponse<void>> {
    return api.put(`/operator/orders/${id}/ship`)
  },

  closeOrder(id: string): Promise<ApiResponse<void>> {
    return api.put(`/operator/orders/${id}/close`)
  },

  completeOrder(id: string): Promise<ApiResponse<void>> {
    return api.put(`/operator/orders/${id}/complete`)
  },

  // 客户
  getCustomers(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<PageResponse<User>>> {
    return api.get('/operator/customers', { params })
  },

  adjustPoints(userId: string, points: number, description: string): Promise<ApiResponse<void>> {
    return api.post('/operator/customers/adjust', { userId, points, description })
  },

  // 消息
  getMessages(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<PageResponse<Message>>> {
    return api.get('/operator/messages', { params })
  },

  getUnreadCount(): Promise<ApiResponse<number>> {
    return api.get('/operator/messages/unread')
  },
}