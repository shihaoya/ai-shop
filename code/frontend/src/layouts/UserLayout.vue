<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import ThemeToggleBtn from '@/components/layout/ThemeToggleBtn.vue'

const themeStore = useThemeStore()
const route = useRoute()

onMounted(() => {
  themeStore.init()
})

interface NavItem {
  label: string
  icon: string
  path: string
}

const navItems: NavItem[] = [
  { label: '商品列表', icon: 'fa-store', path: '/user/products' },
  { label: '我的订单', icon: 'fa-receipt', path: '/user/orders' },
  { label: '积分中心', icon: 'fa-star', path: '/user/points' },
  { label: '收货地址', icon: 'fa-map-marker-alt', path: '/user/addresses' },
  { label: '我的消息', icon: 'fa-envelope', path: '/user/messages' },
]

function isActive(path: string): boolean {
  return route.path.startsWith(path)
}
</script>

<template>
  <div class="user-layout">
    <!-- Header -->
    <header class="cyber-header">
      <div class="left">
        <div class="brand">P</div>
        <div class="breadcrumb">
          {{ navItems.find(n => isActive(n.path))?.label || '积分商城' }}
        </div>
      </div>
      <div class="right">
        <button class="icon-btn">
          <i class="far fa-bell"></i>
          <span class="dot"></span>
        </button>
        <div class="user-tag">
          <div class="avatar">U</div>
          <span class="name">用户</span>
          <i class="fas fa-chevron-down" style="font-size:10px;color:var(--text-muted);"></i>
        </div>
      </div>
    </header>

    <!-- Sidebar (simplified for user) -->
    <aside class="cyber-sidebar">
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
.user-layout {
  min-height: 100vh;
  position: relative;
}

.main-content {
  margin-left: calc(var(--sidebar-width, 200px) + 16px);
  margin-top: 68px;
  padding: 20px;
  transition: margin-left 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

:global(.cyber-sidebar),
:global(.cyber-sidebar .cyber-nav) {
  overflow: visible !important;
}
</style>
