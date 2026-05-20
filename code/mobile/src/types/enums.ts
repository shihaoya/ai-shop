export const ShopStatus = {
  PENDING: 1,
  APPROVED: 2,
  REJECTED: 3,
  DISABLED: 4,
} as const

export const ShopStatusText: Record<number, string> = {
  [ShopStatus.PENDING]: '待审核',
  [ShopStatus.APPROVED]: '已通过',
  [ShopStatus.REJECTED]: '已拒绝',
  [ShopStatus.DISABLED]: '已禁用',
}

export const UserStatus = {
  PENDING: 1,
  NORMAL: 2,
  FROZEN: 3,
} as const

export const UserStatusText: Record<number, string> = {
  [UserStatus.PENDING]: '待审核',
  [UserStatus.NORMAL]: '正常',
  [UserStatus.FROZEN]: '已冻结',
}

export const OrderStatus = {
  PENDING: 1,
  CONFIRMED: 2,
  SHIPPED: 3,
  COMPLETED: 4,
  CLOSED: 5,
} as const

export const OrderStatusText: Record<number, string> = {
  [OrderStatus.PENDING]: '已下单',
  [OrderStatus.CONFIRMED]: '已确认',
  [OrderStatus.SHIPPED]: '已发货',
  [OrderStatus.COMPLETED]: '已完成',
  [OrderStatus.CLOSED]: '已关闭',
}

export const ProductStatus = {
  OFF: 0,
  ON: 1,
} as const

export const PointsType = {
  INCREASE: 1,
  DECREASE: 2,
} as const

export const InviteCodeStatus = {
  UNUSED: 0,
  USED: 1,
} as const

export const ShopActiveStatus = {
  CLOSED: 0,
  OPEN: 1,
} as const