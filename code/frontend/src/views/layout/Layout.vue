<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const app = useAppStore()

interface NavItem {
  path: string
  name: string
  icon: string
}

const adminNavs: NavItem[] = [
  { path: '/admin/shops', name: '店铺管理', icon: '🏪' },
  { path: '/admin/users', name: '用户管理', icon: '👥' },
]

const operatorNavs: NavItem[] = [
  { path: '/operator/shop', name: '我的店铺', icon: '🏪' },
  { path: '/operator/categories', name: '分类管理', icon: '📁' },
  { path: '/operator/products', name: '商品管理', icon: '📦' },
  { path: '/operator/orders', name: '订单管理', icon: '📋' },
  { path: '/operator/customers', name: '客户管理', icon: '👥' },
  { path: '/operator/messages', name: '消息中心', icon: '💬' },
]

const userNavs: NavItem[] = [
  { path: '/user/products', name: '商品列表', icon: '🛍️' },
  { path: '/user/orders', name: '我的订单', icon: '📋' },
  { path: '/user/addresses', name: '地址簿', icon: '📍' },
  { path: '/user/messages', name: '我的消息', icon: '💬' },
]

const currentNavs = computed(() => {
  switch (auth.userRole) {
    case 'ADMIN':
      return adminNavs
    case 'OPERATOR':
      return operatorNavs
    case 'USER':
      return userNavs
    default:
      return []
  }
})

const roleName = computed(() => {
  switch (auth.userRole) {
    case 'ADMIN':
      return '管理员'
    case 'OPERATOR':
      return '店铺用户'
    case 'USER':
      return '普通用户'
    default:
      return ''
  }
})

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="flex h-screen bg-gray-100">
    <!-- 侧边栏 -->
    <aside
      :class="[
        'bg-white border-r border-gray-200 transition-all duration-300 flex flex-col',
        app.sidebarCollapsed ? 'w-16' : 'w-64'
      ]"
    >
      <!-- Logo -->
      <div class="h-16 flex items-center justify-center border-b border-gray-200">
        <h1 v-if="!app.sidebarCollapsed" class="text-xl font-bold text-purple-600">积分商城</h1>
        <span v-else class="text-2xl">🎁</span>
      </div>

      <!-- 导航 -->
      <nav class="flex-1 py-4 px-2 space-y-1">
        <router-link
          v-for="nav in currentNavs"
          :key="nav.path"
          :to="nav.path"
          :class="[
            'flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors',
            route.path === nav.path
              ? 'bg-purple-50 text-purple-600'
              : 'text-gray-600 hover:bg-gray-50'
          ]"
        >
          <span class="text-xl">{{ nav.icon }}</span>
          <span v-if="!app.sidebarCollapsed" class="font-medium">{{ nav.name }}</span>
        </router-link>
      </nav>

      <!-- 用户信息 -->
      <div class="p-4 border-t border-gray-200">
        <div v-if="!app.sidebarCollapsed" class="mb-3">
          <p class="font-medium text-gray-800">{{ auth.user?.nickname || auth.user?.username }}</p>
          <p class="text-sm text-gray-500">{{ roleName }}</p>
        </div>
        <button
          @click="handleLogout"
          class="w-full px-3 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition-colors"
        >
          {{ app.sidebarCollapsed ? '🚪' : '退出登录' }}
        </button>
      </div>
    </aside>

    <!-- 主内容 -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- 顶部栏 -->
      <header class="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6">
        <button
          @click="app.toggleSidebar"
          class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
        >
          <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <div class="flex items-center gap-4">
          <span class="text-sm text-gray-600">{{ auth.user?.nickname || auth.user?.username }}</span>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="flex-1 overflow-auto p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>