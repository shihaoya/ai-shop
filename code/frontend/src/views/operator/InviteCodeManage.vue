<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getInviteCode, createInviteCode } from '@/api/operator'
import { message, Modal } from 'ant-design-vue'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
  loadCodes()
})

const loading = ref(false)
const currentCode = ref<string>('')
const createdAt = ref<string>('')

async function loadCodes() {
  loading.value = true
  try {
    const res: any = await getInviteCode()
    // 后端返回 { code, status, usedBy, createdAt }
    if (res && res.code) {
      currentCode.value = res.code
      createdAt.value = res.createdAt || ''
    } else {
      currentCode.value = ''
      createdAt.value = ''
    }
  } catch (e) {
    throw e
  } finally {
    loading.value = false
  }
}

async function doGenerate() {
  loading.value = true
  try {
    const res: any = await createInviteCode()
    currentCode.value = res.code || res
    createdAt.value = new Date().toISOString()
    message.success('邀请码已生成')
  } catch (e: any) {
    message.error(e?.message || '生成失败')
  } finally {
    loading.value = false
  }
}

function handleGenerate() {
  if (currentCode.value) {
    Modal.confirm({
      title: '确认生成新码',
      content: '生成新码后旧码将立即失效，是否继续？',
      okText: '确认',
      cancelText: '取消',
      async onOk() {
        await doGenerate()
      },
    })
  } else {
    doGenerate()
  }
}

function copyCode() {
  if (!currentCode.value) return
  navigator.clipboard.writeText(currentCode.value).then(() => {
    message.success('已复制到剪贴板')
  }).catch(() => {
    message.error('复制失败')
  })
}

loadCodes()
</script>

<template>
  <div id="page-invite-code">
    <!-- BG -->
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <div class="page-head">
        <h2><span class="accent-line"></span>邀请码管理</h2>
      </div>

      <!-- 邀请码卡片 -->
      <div class="invite-card" :class="{ 'has-code': !!currentCode }">
        <div class="invite-card-inner">
          <div class="invite-icon">
            <i class="fas fa-qrcode"></i>
          </div>

          <template v-if="currentCode">
            <div class="invite-code-display">{{ currentCode }}</div>
            <div v-if="createdAt" class="invite-meta">生成于 {{ new Date(createdAt).toLocaleString('zh-CN') }}</div>
            <div class="invite-actions">
              <button class="cyber-btn" @click="copyCode">
                <i class="fas fa-copy"></i>复制邀请码
              </button>
              <button class="cyber-btn-primary" @click="handleGenerate" :disabled="loading">
                <i class="fas fa-sync-alt" :class="{ 'fa-spin': loading }"></i>生成新码
              </button>
            </div>
          </template>

          <template v-else>
            <div class="invite-empty">暂无可用邀请码</div>
            <div class="invite-actions">
              <button class="cyber-btn-primary" @click="handleGenerate" :disabled="loading">
                <i class="fas fa-plus" :class="{ 'fa-spin': loading }"></i>生成邀请码
              </button>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#page-invite-code {
  min-height: 100vh;
  padding: 20px;
  position: relative;
}

.cyber-bg-grid {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(var(--accent-rgb), 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(var(--accent-rgb), 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
  z-index: 0;
}

.cyber-bg-orb {
  position: fixed;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
  z-index: 0;
}

.page-content {
  position: relative;
  z-index: 1;
  max-width: 600px;
  margin: 0 auto;
}
.page-head {
  margin-bottom: 32px;
}
.page-head h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}
.accent-line {
  display: inline-block;
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, var(--accent), var(--accent-light));
  border-radius: 2px;
}
.invite-card {
  position: relative;
  z-index: 1;
  overflow: hidden;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: 16px;
  padding: 48px 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  animation: fadeInUp 0.4s ease-out;
}
.invite-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--accent), transparent);
  animation: borderGlow 3s ease-in-out infinite;
}
.invite-card:hover {
  border-color: var(--border-glow);
  box-shadow: 0 0 24px rgba(var(--accent-rgb), 0.12);
}
.invite-card.has-code {
  border-color: var(--border-glow);
  box-shadow: 0 0 30px rgba(var(--accent-rgb), 0.10);
}
.invite-card-inner {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  width: 100%;
}
.invite-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(var(--accent-rgb), 0.10);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--accent);
  margin-bottom: 8px;
}
.invite-code-display {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 4px;
  color: var(--accent);
  font-family: 'Courier New', monospace;
  background: rgba(var(--accent-rgb), 0.06);
  border: 1px dashed rgba(var(--accent-rgb), 0.30);
  border-radius: 8px;
  padding: 16px 32px;
  user-select: all;
}
.invite-meta {
  font-size: 12px;
  color: var(--text-muted);
}
.invite-empty {
  font-size: 16px;
  color: var(--text-muted);
}
.invite-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}
.cyber-btn,
.cyber-btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  color: var(--text-primary);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}
.cyber-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}
.cyber-btn-primary {
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  border-color: transparent;
  color: #fff;
}
.cyber-btn-primary:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(var(--accent-rgb), 0.30);
}
.cyber-btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes borderGlow {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}
</style>
