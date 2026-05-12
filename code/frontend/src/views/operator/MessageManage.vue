<script setup lang="ts">
import { onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
})
</script>

<template>
  <div id="page-message-manage">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <main class="main-content">
      <div class="page-head">
        <h2><span class="accent-line"></span>消息中心</h2>
      </div>

      <div class="cyber-card">
        <div style="text-align:center;padding:60px 20px;color:var(--text-secondary);">
          <i class="fas fa-envelope" style="font-size:48px;margin-bottom:16px;opacity:0.5;"></i>
          <p style="font-size:16px;">消息中心 - 功能开发中</p>
        </div>
      </div>
    </main>
  </div>
</template>
