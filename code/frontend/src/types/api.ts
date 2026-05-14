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

// 分页请求
export interface PageRequest {
  page?: number
  size?: number
}

// 用户信息
export interface UserInfo {
  id: string
  username: string
  nickname: string
  role: number       // 1=管理员, 2=店铺用户, 3=普通用户
  status: number     // 1=待审核, 2=正常, 3=冻结
  points?: number
  parentId?: string
  createdAt?: string
}

// 登录参数
export interface LoginParams {
  username: string
  password: string
}

// 注册参数
export interface RegisterParams {
  username: string
  nickname: string
  password: string
  confirmPassword: string
  inviteCode: string
}

// ============ 店铺相关 ============
export interface Shop {
  id: string
  name: string
  description?: string
  ownerId?: string
  ownerName?: string
  operatorId?: string
  operatorName?: string
  status: number     // 1=待审核, 2=通过, 3=拒绝, 4=禁用
  isActive: number   // 0=歇业, 1=营业
  rejectReason?: string
  createdAt?: string
  updatedAt?: string
}

// ============ 商品相关 ============
export interface Product {
  id: string
  name: string
  categoryId: string
  categoryName?: string
  type: string | number       // 1=虚拟, 2=实物
  price: number      // 积分价格
  pointsPrice?: number
  stock: number
  image?: string
  description?: string
  status: number     // 0=下架, 1=上架
  createdAt?: string
  limitPerUser?: number
  mainImage?: string
}

export interface Category {
  id: string
  name: string
  sort: number
  shopId?: string
  productCount?: number
}

// ============ 订单相关 ============
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
  status: number      // 1=已下单, 2=已确认, 3=已发货, 4=已完成, 5=已关闭
  addressId?: string
  addressInfo?: Address
  trackingNo?: string
  carrier?: string
  reason?: string
  createdAt?: string
  updatedAt?: string
}

// ============ 收货地址 ============
export interface Address {
  id: string
  userId: string
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: number   // 0=否, 1=是
}

// ============ 积分记录 ============
export interface PointsLog {
  id: string
  userId: string
  type: number        // 1=增加, 2=扣除
  amount: number
  balance: number
  remark?: string
  operatorId?: string
  operatorName?: string
  createdAt?: string
}

// ============ 邀请码 ============
export interface InviteCode {
  id: string
  code: string
  type: number         // 1=店铺邀请码, 2=普通用户邀请码
  status: number      // 0=未使用, 1=已使用
  userId: string
  usedBy?: string
  usedAt?: string
  expiresAt?: string
  createdAt?: string
}

// ============ 消息 ============
export interface Message {
  id: string
  userId: string
  title: string
  content: string
  isRead: number      // 0=未读, 1=已读
  createdAt?: string
}