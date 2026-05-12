<script setup lang="ts">
import { ref } from 'vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const panelVisible = ref(false)

const colorPresets = [
  '#6366f1', '#ec4899', '#06b6d4', '#10b981',
  '#f59e0b', '#ef4444', '#8b5cf6', '#f472b6',
]

function togglePanel() {
  panelVisible.value = !panelVisible.value
}

function setMode(mode: 'light' | 'dark' | 'auto') {
  themeStore.setMode(mode)
}

function setAccent(color: string) {
  themeStore.setAccentColor(color)
}

function resetTheme() {
  themeStore.reset()
}

// 点击外部关闭
function handleClickOutside(e: MouseEvent) {
  const fab = document.getElementById('themeFab')
  const panel = document.getElementById('themePanel')
  if (fab && !fab.contains(e.target as Node) && panel && panel.classList.contains('cyber-panel')) {
    panelVisible.value = false
  }
}

document.addEventListener('click', handleClickOutside)
</script>

<template>
  <div class="cyber-fab" id="themeFab">
    <!-- 面板 -->
    <div class="cyber-panel" :class="{ hidden: !panelVisible }" id="themePanel">
      <h3>✦ 主题配置</h3>

      <!-- 模式切换 -->
      <div class="mode-row">
        <button
          class="mode-btn"
          :class="{ active: themeStore.mode === 'dark' }"
          @click="setMode('dark')"
        >
          <i class="fas fa-moon"></i>
          暗色
        </button>
        <button
          class="mode-btn"
          :class="{ active: themeStore.mode === 'light' }"
          @click="setMode('light')"
        >
          <i class="fas fa-sun"></i>
          亮色
        </button>
        <button
          class="mode-btn"
          :class="{ active: themeStore.mode === 'auto' }"
          @click="setMode('auto')"
        >
          <i class="fas fa-circle-half-stroke"></i>
          自动
        </button>
      </div>

      <div class="divider"></div>

      <div class="color-label">霓虹主题色</div>
      <div class="color-grid">
        <div
          v-for="color in colorPresets"
          :key="color"
          class="color-dot"
          :class="{ active: themeStore.accentColor === color }"
          :style="{ background: color }"
          :title="color"
          @click="setAccent(color)"
        ></div>
      </div>

      <button class="reset-btn" @click="resetTheme">
        <i class="fas fa-undo" style="margin-right:4px;"></i>
        恢复默认
      </button>
    </div>

    <!-- FAB 按钮 -->
    <button class="fab-btn" @click.stop="togglePanel">
      <i class="fas fa-bolt"></i>
    </button>
  </div>
</template>

<style scoped>
.cyber-fab {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
}

.fab-btn {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  border: 1px solid var(--border-subtle);
  background: rgba(13, 15, 36, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--accent-light);
  transition: all 0.3s ease;
  animation: neonPulse 3s ease-in-out infinite;
}

[data-theme='light'] .fab-btn {
  background: rgba(255, 255, 255, 0.90);
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.fab-btn:hover {
  animation: none;
  border-color: var(--border-active);
  transform: scale(1.1);
  box-shadow: var(--accent-glow-hover);
}

.cyber-panel {
  position: absolute;
  bottom: 56px;
  right: 0;
  width: 230px;
  padding: 18px;
  background: rgba(13, 15, 36, 0.95);
  backdrop-filter: blur(24px);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius);
  box-shadow: 0 0 40px rgba(0,0,0,0.5);
  animation: scaleIn 0.2s ease-out;
  transform-origin: bottom right;
}

[data-theme='light'] .cyber-panel {
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 8px 32px rgba(0,0,0,0.08);
}

.cyber-panel.hidden {
  display: none;
}

.cyber-panel h3 {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

.mode-row {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
}

.mode-btn {
  flex: 1;
  padding: 6px 0;
  border-radius: var(--radius-xs);
  border: 1px solid var(--border-subtle);
  background: var(--bg-input);
  cursor: pointer;
  font-size: 11px;
  color: var(--text-muted);
  transition: all 0.2s ease;
  text-align: center;
  font-family: inherit;
}

.mode-btn i {
  display: block;
  margin-bottom: 2px;
  font-size: 15px;
}

.mode-btn.active {
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  border-color: transparent;
  color: white;
  box-shadow: var(--accent-glow);
}

.divider {
  height: 1px;
  background: var(--border-subtle);
  margin: 12px 0;
}

.color-label {
  font-size: 11px;
  color: var(--text-muted);
  margin-bottom: 8px;
  letter-spacing: 0.3px;
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 6px;
}

.color-dot {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.color-dot:hover {
  transform: scale(1.15);
}

.color-dot.active {
  border-color: white;
  box-shadow: 0 0 12px currentColor;
}

.reset-btn {
  width: 100%;
  margin-top: 12px;
  padding: 6px;
  border-radius: var(--radius-xs);
  border: 1px solid var(--border-subtle);
  background: var(--bg-input);
  cursor: pointer;
  font-size: 11px;
  color: var(--text-muted);
  transition: all 0.2s ease;
  font-family: inherit;
}

.reset-btn:hover {
  border-color: var(--border-glow);
  color: var(--text-primary);
}
</style>