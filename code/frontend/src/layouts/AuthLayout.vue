<script setup lang="ts">
import ThemeToggleBtn from '@/components/layout/ThemeToggleBtn.vue'
</script>

<template>
  <div class="auth-layout">
    <router-view v-slot="{ Component, route }">
      <transition name="page-fade" mode="out-in">
        <component :is="Component" :key="route.path" />
      </transition>
    </router-view>
    <ThemeToggleBtn />
  </div>
</template>

<style scoped>
.auth-layout {
  min-height: 100vh;
  position: relative;
}
</style>