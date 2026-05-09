// 用户角色
export type UserRole = 'ADMIN' | 'OPERATOR' | 'USER'

// 用户状态
export type UserStatus = 0 | 1 | 2 // 0=待审核, 1=正常, 2=冻结

// 店铺状态
export type ShopStatus = 0 | 1 | 2 // 0=待审核, 1=营业, 2=歇业

// 订单状态
export type OrderStatus = 0 | 1 | 2 | 3 | 4 // 0=已下单, 1=已确认, 2=已发货, 3=已完成, 4=已关闭

// 商品状态
export type ProductStatus = 0 | 1 // 0=下架, 1=上架

// 通用响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页响应
export interface PageResponse<T = any> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

// 用户信息
export interface User {
  id: string
  username: string
  nickname: string
  role: UserRole
  status: UserStatus
  points?: number
  parentId?: string
  deleted?: boolean
  createTime?: string
  updateTime?: string
}

// 店铺
export interface Shop {
  id: string
  name: string
  description?: string
  status: ShopStatus
  operatorId: string
  operatorName?: string
  deleted?: boolean
  createTime?: string
  updateTime?: string
}

// 分类
export interface Category {
  id: string
  name: string
  shopId: string
  deleted?: boolean
  createTime?: string
  updateTime?: string
}

// 商品
export interface Product {
  id: string
  name: string
  description?: string
  image?: string
  points: number
  stock: number
  status: ProductStatus
  categoryId: string
  categoryName?: string
  shopId: string
  deleted?: boolean
  createTime?: string
  updateTime?: string
}

// 订单
export interface Order {
  id: string
  orderNo: string
  userId: string
  userName?: string
  shopId: string
  shopName?: string
  productId: string
  productName?: string
  productImage?: string
  points: number
  quantity: number
  totalPoints: number
  status: OrderStatus
  addressId?: string
  address?: Address
  deleted?: boolean
  createTime?: string
  updateTime?: string
}

// 地址
export interface Address {
  id: string
  userId: string
  name: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: boolean
  deleted?: boolean
  createTime?: string
  updateTime?: string
}

// 消息
export interface Message {
  id: string
  userId: string
  title: string
  content: string
  type: 'POINTS' | 'ORDER' | 'SYSTEM'
  isRead: boolean
  deleted?: boolean
  createTime?: string
  updateTime?: string
}

// 积分记录
export interface PointsRecord {
  id: string
  userId: string
  points: number
  type: 'EARN' | 'SPEND' | 'ADJUST'
  description: string
  orderId?: string
  operatorId?: string
  operatorName?: string
  deleted?: boolean
  createTime?: string
  updateTime?: string
}

// 登录请求
export interface LoginRequest {
  username: string
  password: string
}

// 注册请求
export interface RegisterRequest {
  username: string
  nickname: string
  password: string
  inviteCode?: string
}

// 登录响应
export interface LoginResponse {
  token: string
  user: User
}

// 店铺创建请求
export interface ShopCreateRequest {
  name: string
  description?: string
}

// 分类创建请求
export interface CategoryCreateRequest {
  name: string
}

// 商品创建请求
export interface ProductCreateRequest {
  name: string
  description?: string
  image?: string
  points: number
  stock: number
  categoryId: string
}

// 地址创建请求
export interface AddressCreateRequest {
  name: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault?: boolean
}

// 订单创建请求
export interface OrderCreateRequest {
  productId: string
  quantity: number
  addressId: string
}