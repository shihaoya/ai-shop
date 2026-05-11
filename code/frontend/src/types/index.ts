// 用户类型
export interface User {
  id: string
  username: string
  nickname: string
  role: number // 1=管理员 2=店铺用户 3=普通用户
  parentId: string
  status: number // 1=待审核 2=正常 3=已冻结
  points?: number // 普通用户积分
}

// 登录响应
export interface LoginResponse {
  token: string
  user: User
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
  confirmPassword: string
  inviteCode: string
}

// 修改密码请求
export interface PasswordRequest {
  oldPassword: string
  newPassword: string
}

// 分页请求
export interface PageRequest {
  page?: number
  pageSize?: number
  keyword?: string
  role?: number   // 角色筛选：1=管理员，2=店铺用户，3=普通用户
  status?: number  // 状态筛选：1=待审核，2=正常，3=已冻结
}

// 分页响应
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

// API 响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}