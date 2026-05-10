import { get, post, put, del } from './request'
import type { PageRequest, PageResult } from '@/types'

// 商品类型
export interface Product {
  id: string
  name: string
  description: string
  points: number
  stock: number
  categoryId: string
  shopId: string
  image?: string
}

// 订单类型
export interface Order {
  id: string
  userId: string
  shopId: string
  productId: string
  points: number
  status: number
  addressId: string
  deleted: number
  createTime?: string
  updateTime?: string
  product?: Product
  address?: Address
}

// 积分流水类型
export interface PointsLog {
  id: string
  userId: string
  points: number
  type: number
  description: string
  createTime: string
}

// 地址类型
export interface Address {
  id: string
  userId: string
  name: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: number
}

// 消息类型
export interface Message {
  id: string
  userId: string
  title: string
  content: string
  isRead: number
  createTime: string
}

// 商品列表
export const getProducts = (params?: PageRequest) =>
  get<PageResult<Product>>('/api/user/products', { params })

// 商品详情
export const getProduct = (id: string) =>
  get<Product>(`/api/user/products/${id}`)

// 下单
export const createOrder = (data: { productId: string; addressId: string }) =>
  post<Order>('/api/user/orders', data)

// 订单列表
export const getOrders = (params?: PageRequest) =>
  get<PageResult<Order>>('/api/user/orders', { params })

// 订单详情
export const getOrder = (id: string) =>
  get<Order>(`/api/user/orders/${id}`)

// 关闭订单
export const closeOrder = (id: string) =>
  put<void>(`/api/user/orders/${id}/close`)

// 完成订单
export const completeOrder = (id: string) =>
  put<void>(`/api/user/orders/${id}/complete`)

// 获取当前积分余额
export const getPoints = () =>
  get<{ points: number }>('/api/user/points')

// 获取积分流水
export const getPointsLog = (params?: PageRequest) =>
  get<PageResult<PointsLog>>('/api/user/points/log', { params })

// 地址列表
export const getAddresses = () =>
  get<Address[]>('/api/user/addresses')

// 新增地址
export const createAddress = (data: Omit<Address, 'id' | 'userId'>) =>
  post<Address>('/api/user/addresses', data)

// 编辑地址
export const updateAddress = (id: string, data: Partial<Address>) =>
  put<void>(`/api/user/addresses/${id}`, data)

// 删除地址
export const deleteAddress = (id: string) =>
  del<void>(`/api/user/addresses/${id}`)

// 设置默认地址
export const setDefaultAddress = (id: string) =>
  put<void>(`/api/user/addresses/${id}/default`)

// 消息列表
export const getMessages = (params?: PageRequest) =>
  get<PageResult<Message>>('/api/user/messages', { params })

// 标记已读
export const readMessage = (id: string) =>
  put<void>(`/api/user/messages/${id}/read`)