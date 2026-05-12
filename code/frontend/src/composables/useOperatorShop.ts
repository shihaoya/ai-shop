import { ref } from 'vue'

// 模块级状态，跨组件共享
const hasShop = ref(false)

export function useOperatorShop() {
  return {
    hasShop,
    setHasShop(value: boolean) {
      hasShop.value = value
    },
  }
}