import { ref } from 'vue'
import { ShopStatus } from '@/types/enums'

// 模块级状态，跨组件共享
const hasShop = ref(false)
const shopStatus = ref<number | null>(null)

export function useOperatorShop() {
  return {
    hasShop,
    shopStatus,
    setHasShop(has: boolean, status: number | null = null) {
      hasShop.value = has
      shopStatus.value = status
    },
    isApproved: () => hasShop.value && shopStatus.value === ShopStatus.APPROVED,
  }
}