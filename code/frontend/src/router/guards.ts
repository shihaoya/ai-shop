import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/user'

export function setupRouterGuard(router: Router) {
  router.beforeEach((to, _from, next) => {
    const userStore = useUserStore()
    const token = userStore.token

    // 已登录访问登录页 -> 跳转到对应首页
    if (token && to.meta.guest) {
      return next(userStore.homePath)
    }

    // 未登录且不是公开页 -> 跳登录
    if (!token && !to.meta.guest) {
      return next('/login')
    }

    // 已登录但角色不匹配 -> 跳对应首页
    if (token && to.meta.role && to.meta.role !== userStore.role) {
      return next(userStore.homePath)
    }

    // 已登录但状态异常（待审核/冻结）-> 登出并提示
    if (token && userStore.status !== 2 && !to.meta.guest) {
      if (userStore.status === 1) {
        // 待审核
      } else if (userStore.status === 3) {
        // 已冻结
      }
      userStore.logout()
      return next('/login')
    }

    next()
  })
}