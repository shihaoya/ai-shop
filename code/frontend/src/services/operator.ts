import { get, post, put, del } from './request'
import type { PageRequest, PageResult } from '@/types'

// 店铺类型
export interface Shop {
  id: string
  name: string
  description: string
  status: number
  userId: string
}

// 分类类型
export interface Category {
  id: string
  name: string
  shopId: string
  deleted: number
}

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
  deleted: number
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

// 普通用户类型
export interface OperatorUser {
  id: string
  username: string
  nickname: string
  role: number
  parentId: string
  status: number
  points?: number
}

// 获取我的店铺
export const getMyShop = () =>
  get<Shop>('/api/operator/shop')

// 申请店铺
export const applyShop = (data: { name: string; description: string }) =>
  post<Shop>('/api/operator/shop', data)

// 切换营业/歇业
export const updateShopStatus = (status: number) =>
  put<void>('/api/operator/shop/status', { status })

// 获取分类列表
export const getCategories = () =>
  get<Category[]>('/api/operator/categories')

// 创建分类
export const createCategory = (data: { name: string }) =>
  post<Category>('/api/operator/categories', data)

// 编辑分类
export const updateCategory = (id: string, data: { name: string }) =>
  put<void>(`/api/operator/categories/${id}`, data)

// 删除分类
export const deleteCategory = (id: string) =>
  del<void>(`/api/operator/categories/${id}`)

// 商品列表
export const getProducts = (params?: PageRequest) =>
  get<PageResult<Product>>('/api/operator/products', { params })

// 商品详情
export const getProduct = (id: string) =>
  get<Product>(`/api/operator/products/${id}`)

// 创建商品
export const createProduct = (data: FormData) =>
  post<Product>('/api/operator/products', data)

// 编辑商品
export const updateProduct = (id: string, data: FormData) =>
  put<Product>(`/api/operator/products/${id}`, data)

// 删除商品
export const deleteProduct = (id: string) =>
  del<void>(`/api/operator/products/${id}`)

// 订单列表
export const getOrders = (params?: PageRequest) =>
  get<PageResult<Order>>('/api/operator/orders', { params })

// 确认订单
export const confirmOrder = (id: string) =>
  put<void>(`/api/operator/orders/${id}/confirm`)

// 发货
export const shipOrder = (id: string) =>
  put<void>(`/api/operator/orders/${id}/ship`)

// 关闭订单
export const closeOrder = (id: string) =>
  put<void>(`/api/operator/orders/${id}/close`)

// 完成订单
export const completeOrder = (id: string) =>
  put<void>(`/api/operator/orders/${id}/complete`)

// 普通用户列表
export const getUsers = (params?: PageRequest) =>
  get<PageResult<OperatorUser>>('/api/operator/users', { params })

// 发放/扣除积分
export const updateUserPoints = (id: string, data: { points: number; description: string }) =>
  post<void>(`/api/operator/users/${id}/points`, data)

// 积分流水
export const getPointsLog = (id: string, params?: PageRequest) =>
  get<PageResult<PointsLog>>(`/api/operator/users/${id}/points/log`, { params })

// 审核通过普通用户
export const approveUser = (id: string) =>
  put<void>(`/api/operator/users/${id}/approve`)

// 获取邀请码
export const getInviteCode = () =>
  get<{ code: string; role: number; used: boolean }>('/api/operator/invite-code')

// 生成/重新生成邀请码
export const generateInviteCode = () =>
  post<{ code: string; role: number; used: boolean }>('/api/operator/invite-code')