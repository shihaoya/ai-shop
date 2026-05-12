// API 统一返回格式
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 用户信息
export interface UserInfo {
  id: string
  username: string
  nickname: string
  role: number       // 1=管理员, 2=店铺用户, 3=普通用户
  status: number     // 1=待审核, 2=正常, 3=已冻结
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