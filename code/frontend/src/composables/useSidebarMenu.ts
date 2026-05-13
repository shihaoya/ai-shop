import { useRouter, useRoute } from 'vue-router'
import { computed, type Ref } from 'vue'
import { ShopStatus } from '@/types/enums'

export interface SidebarItem {
  label: string
  icon: string
  path: string
}

const MINIMAL_MENU_PATHS = ['/operator/shop', '/operator/messages']

export function useSidebarMenu(basePath: string, hasShop: Ref<boolean> | boolean = true, shopStatus: Ref<number | null> | number | null = null) {
  const router = useRouter()
  const route = useRoute()

  const prefix = basePath.endsWith('/') ? basePath : basePath + '/'
  const hasShopRef = computed(() => typeof hasShop === 'boolean' ? hasShop : hasShop.value)
  const statusRef = computed(() => typeof shopStatus === 'number' || shopStatus === null ? shopStatus : (shopStatus as Ref<number | null>)?.value ?? null)

  const items = computed<SidebarItem[]>(() => {
    // 从所有已注册路由中筛选出以 basePath/ 开头且标记了 sidebar 的
    // 不依赖父路由的 children 属性（Vue Router 4 getRoutes 返回扁平列表）
    let routes = router
      .getRoutes()
      .filter(r => r.path.startsWith(prefix) && r.meta?.sidebar)

    const status = statusRef.value
    // 待审核/被拒/禁用状态也只显示核心页面
    if (!hasShopRef.value || status === ShopStatus.PENDING || status === ShopStatus.REJECTED || status === ShopStatus.DISABLED) {
      routes = routes.filter(r => MINIMAL_MENU_PATHS.includes(r.path))
    }

    return routes.map(r => ({
      label: (r.meta?.title as string) || '',
      icon: (r.meta?.icon as string) || 'fa-circle',
      path: r.path,
    }))
  })

  const currentLabel = computed(() =>
    items.value.find(n => route.path.startsWith(n.path))?.label || ''
  )

  function isActive(path: string): boolean {
    return route.path.startsWith(path)
  }

  return { items, currentLabel, isActive }
}
