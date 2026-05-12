// ============ 店铺状态枚举 ============
export const ShopStatus = {
  PENDING: 1,    // 待审核
  APPROVED: 2,   // 已通过
  REJECTED: 3,   // 已拒绝
  DISABLED: 4,   // 已禁用
} as const

export type ShopStatusValue = typeof ShopStatus[keyof typeof ShopStatus]

export const ShopStatusText: Record<number, string> = {
  [ShopStatus.PENDING]: '待审核',
  [ShopStatus.APPROVED]: '已通过',
  [ShopStatus.REJECTED]: '已拒绝',
  [ShopStatus.DISABLED]: '已禁用',
}

export const ShopStatusClass: Record<number, string> = {
  [ShopStatus.PENDING]: 'orange',
  [ShopStatus.APPROVED]: 'green',
  [ShopStatus.REJECTED]: 'red',
  [ShopStatus.DISABLED]: 'gray',
}

// ============ 用户状态枚举 ============
export const UserStatus = {
  PENDING: 1,    // 待审核
  NORMAL: 2,     // 正常
  FROZEN: 3,     // 冻结
} as const

export type UserStatusValue = typeof UserStatus[keyof typeof UserStatus]

export const UserStatusText: Record<number, string> = {
  [UserStatus.PENDING]: '待审核',
  [UserStatus.NORMAL]: '正常',
  [UserStatus.FROZEN]: '已冻结',
}

export const UserStatusClass: Record<number, string> = {
  [UserStatus.PENDING]: 'orange',
  [UserStatus.NORMAL]: 'green',
  [UserStatus.FROZEN]: 'red',
}

// ============ 订单状态枚举 ============
export const OrderStatus = {
  PENDING: 1,    // 已下单
  CONFIRMED: 2,   // 已确认
  SHIPPED: 3,     // 已发货
  COMPLETED: 4,   // 已完成
  CLOSED: 5,      // 已关闭
} as const

export type OrderStatusValue = typeof OrderStatus[keyof typeof OrderStatus]

export const OrderStatusText: Record<number, string> = {
  [OrderStatus.PENDING]: '已下单',
  [OrderStatus.CONFIRMED]: '已确认',
  [OrderStatus.SHIPPED]: '已发货',
  [OrderStatus.COMPLETED]: '已完成',
  [OrderStatus.CLOSED]: '已关闭',
}

export const OrderStatusClass: Record<number, string> = {
  [OrderStatus.PENDING]: 'orange',
  [OrderStatus.CONFIRMED]: 'blue',
  [OrderStatus.SHIPPED]: 'purple',
  [OrderStatus.COMPLETED]: 'green',
  [OrderStatus.CLOSED]: 'gray',
}

// ============ 商品状态枚举 ============
export const ProductStatus = {
  OFF: 0,   // 下架
  ON: 1,    // 上架
} as const

export type ProductStatusValue = typeof ProductStatus[keyof typeof ProductStatus]

export const ProductStatusText: Record<number, string> = {
  [ProductStatus.OFF]: '下架',
  [ProductStatus.ON]: '上架',
}

export const ProductStatusClass: Record<number, string> = {
  [ProductStatus.OFF]: 'gray',
  [ProductStatus.ON]: 'green',
}

// ============ 积分变动类型枚举 ============
export const PointsType = {
  INCREASE: 1,   // 增加
  DECREASE: 2,   // 扣除
} as const

export type PointsTypeValue = typeof PointsType[keyof typeof PointsType]

export const PointsTypeText: Record<number, string> = {
  [PointsType.INCREASE]: '增加',
  [PointsType.DECREASE]: '扣除',
}

export const PointsTypeClass: Record<number, string> = {
  [PointsType.INCREASE]: 'green',
  [PointsType.DECREASE]: 'red',
}

// ============ 邀请码状态枚举 ============
export const InviteCodeStatus = {
  UNUSED: 0,  // 未使用
  USED: 1,    // 已使用
} as const

export type InviteCodeStatusValue = typeof InviteCodeStatus[keyof typeof InviteCodeStatus]

export const InviteCodeStatusText: Record<number, string> = {
  [InviteCodeStatus.UNUSED]: '未使用',
  [InviteCodeStatus.USED]: '已使用',
}

export const InviteCodeStatusClass: Record<number, string> = {
  [InviteCodeStatus.UNUSED]: 'green',
  [InviteCodeStatus.USED]: 'gray',
}

// ============ 店铺营业状态枚举 ============
export const ShopActiveStatus = {
  CLOSED: 0,    // 歇业
  OPEN: 1,      // 营业
} as const

export type ShopActiveStatusValue = typeof ShopActiveStatus[keyof typeof ShopActiveStatus]

export const ShopActiveStatusText: Record<number, string> = {
  [ShopActiveStatus.CLOSED]: '歇业中',
  [ShopActiveStatus.OPEN]: '营业中',
}

export const ShopActiveStatusClass: Record<number, string> = {
  [ShopActiveStatus.CLOSED]: 'gray',
  [ShopActiveStatus.OPEN]: 'green',
}