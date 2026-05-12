import { useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'

export interface SidebarItem {
  label: string
  icon: string
  path: string
}

export function useSidebarMenu(basePath: string) {
  const router = useRouter()
  const route = useRoute()

  const prefix = basePath.endsWith('/') ? basePath : basePath + '/'

  const items = computed<SidebarItem[]>(() => {
    // 从所有已注册路由中筛选出以 basePath/ 开头且标记了 sidebar 的
    // 不依赖父路由的 children 属性（Vue Router 4 getRoutes 返回扁平列表）
    return router
      .getRoutes()
      .filter(r => r.path.startsWith(prefix) && r.meta?.sidebar)
      .map(r => ({
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
