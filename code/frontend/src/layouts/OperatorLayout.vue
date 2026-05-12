<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { useSidebarStore } from '@/stores/sidebar'
import ThemeToggleBtn from '@/components/layout/ThemeToggleBtn.vue'

const themeStore = useThemeStore()
const sidebarStore = useSidebarStore()
const route = useRoute()

onMounted(() => {
  themeStore.init()
})

interface NavItem {
  label: string
  icon: string
  badge?: string
  path: string
}

const navItems: NavItem[] = [
  { label: '我的店铺', icon: 'fa-store', path: '/operator/shop' },
  { label: '商品管理', icon: 'fa-box', path: '/operator/products' },
  { label: '订单管理', icon: 'fa-receipt', badge: '5', path: '/operator/orders' },
  { label: '用户管理', icon: 'fa-users', path: '/operator/users' },
  { label: '分类管理', icon: 'fa-tags', path: '/operator/categories' },
  { label: '消息中心', icon: 'fa-envelope', path: '/operator/messages' },
  { label: '邀请码', icon: 'fa-qrcode', path: '/operator/invite-code' },
]

function isActive(path: string): boolean {
  return route.path.startsWith(path)
}
</script>

<template>
  <div class="admin-layout">
    <!-- Header -->
    <header class="cyber-header">
      <div class="left">
        <div class="brand">P</div>
        <div class="breadcrumb">
          {{ navItems.find(n => isActive(n.path))?.label || '店铺运营' }}
        </div>
      </div>
      <div class="right">
        <button class="icon-btn">
          <i class="far fa-bell"></i>
          <span class="dot"></span>
        </button>
        <button class="icon-btn">
          <i class="fas fa-expand"></i>
        </button>
        <div class="user-tag">
          <div class="avatar">O</div>
          <span class="name">运营人员</span>
          <i class="fas fa-chevron-down" style="font-size:10px;color:var(--text-muted);"></i>
        </div>
      </div>
    </header>

    <!-- Sidebar -->
    <aside class="cyber-sidebar" :class="{ collapsed: sidebarStore.collapsed }">
      <div class="collapse-btn">
        <button @click="sidebarStore.toggle()">
          <i class="fas fa-bars"></i>
        </button>
      </div>
      <nav class="cyber-nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="cyber-nav-item"
          :class="{ active: isActive(item.path) }"
          :data-tip="item.label"
        >
          <div class="active-glow" :style="{ display: isActive(item.path) ? 'block' : 'none' }"></div>
          <i :class="['fas', item.icon, 'icon']"></i>
          <span class="label">{{ item.label }}</span>
          <span v-if="item.badge" class="badge">{{ item.badge }}</span>
        </RouterLink>
      </nav>
    </aside>

    <!-- Main Content -->
    <main class="main-content">
      <RouterView v-slot="{ Component, route: curRoute }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" :key="curRoute.path" />
        </transition>
      </RouterView>
    </main>

    <!-- Theme Toggle -->
    <ThemeToggleBtn />
  </div>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
  position: relative;
}

:global(.cyber-sidebar.collapsed),
:global(.cyber-sidebar.collapsed .cyber-nav) {
  overflow: visible !important;
}
</style>
