// API 统一返回格式
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface PageRequest {
  page?: number
  size?: number
}

export interface UserInfo {
  id: string
  username: string
  nickname: string
  role: number
  status: number
  points?: number
  parentId?: string
  createdAt?: string
}

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  nickname: string
  password: string
  confirmPassword: string
  inviteCode: string
}

export interface Shop {
  id: string
  name: string
  description?: string
  ownerId?: string
  ownerName?: string
  operatorId?: string
  operatorName?: string
  status: number
  isActive: number
  rejectReason?: string
  createdAt?: string
  updatedAt?: string
}

export interface Product {
  id: string
  name: string
  categoryId: string
  categoryName?: string
  type: string | number
  price: number
  pointsPrice?: number
  stock: number
  image?: string
  description?: string
  status: number
  createdAt?: string
  limitPerUser?: number
  mainImage?: string
  mainImageUrl?: string
  detailImages?: string
}

export interface Category {
  id: string
  name: string
  sort: number
  shopId?: string
  productCount?: number
}

export interface Order {
  id: string
  orderNo: string
  userId: string
  userNickname?: string
  shopId: string
  shopName?: string
  productId: string
  productName?: string
  quantity: number
  totalPoints: number
  status: number
  receiverName?: string
  receiverPhone?: string
  receiverProvince?: string
  receiverCity?: string
  receiverDistrict?: string
  receiverDetail?: string
  expressCompany?: string
  expressNo?: string
  deliveryContent?: string
  createdAt?: string
  updatedAt?: string
  completedAt?: string
  closedAt?: string
  closeReason?: string
}

export interface Address {
  id: string
  userId: string
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: number
}

export interface PointsLog {
  id: string
  userId: string
  amount: number
  balance: number
  type: number
  remark?: string
  operatorId?: string
  createdAt: string
}

export interface Message {
  id: string
  userId: string
  title: string
  content: string
  isRead: number
  createdAt?: string
}

export interface InviteCode {
  id: string
  code: string
  type: number
  status: number
  userId: string
  usedBy?: string
  usedAt?: string
  expiresAt?: string
  createdAt?: string
}