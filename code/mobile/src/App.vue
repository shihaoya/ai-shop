<script setup lang="ts">
import { onMounted } from 'vue'
import { ConfigProvider } from 'vant'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
})
</script>

<template>
  <ConfigProvider :theme="themeStore.effectiveMode === 'dark' ? 'dark' : 'light'">
    <RouterView />
  </ConfigProvider>
</template>