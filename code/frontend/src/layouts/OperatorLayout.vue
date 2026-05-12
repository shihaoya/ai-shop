<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { useSidebarStore } from '@/stores/sidebar'
import { useSidebarMenu } from '@/composables/useSidebarMenu'
import ThemeToggleBtn from '@/components/layout/ThemeToggleBtn.vue'

const themeStore = useThemeStore()
const sidebarStore = useSidebarStore()
const { items: navItems, currentLabel, isActive } = useSidebarMenu('/operator')

onMounted(() => {
  themeStore.init()
})
</script>

<template>
  <div class="operator-layout">
    <!-- Header -->
    <header class="cyber-header">
      <div class="left">
        <div class="brand">P</div>
        <div class="breadcrumb">
          {{ currentLabel || '店铺运营' }}
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
.operator-layout {
  min-height: 100vh;
  position: relative;
}

.main-content {
  margin-left: calc(var(--sidebar-width, 200px) + 16px);
  margin-top: 68px;
  padding: 20px;
  transition: margin-left 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

:global(.cyber-sidebar.collapsed),
:global(.cyber-sidebar.collapsed .cyber-nav) {
  overflow: visible !important;
}
</style>
