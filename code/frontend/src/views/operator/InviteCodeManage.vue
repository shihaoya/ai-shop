<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getInviteCode, createInviteCode } from '@/api/operator'
import { message } from 'ant-design-vue'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
  loadCodes()
})

const loading = ref(false)
const codes = ref<{ code: string; status: number; usedBy?: string; createdAt?: string }[]>([])

async function loadCodes() {
  loading.value = true
  try {
    const res = await getInviteCode()
    codes.value = res || []
  } catch (e: any) {
    console.error('加载邀请码列表失败:', e)
    message.error(e?.message || (e as Error)?.message || '加载失败')
    throw e
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  try {
    await createInviteCode()
    message.success('邀请码生成成功')
    loadCodes()
  } catch (e: any) {
    console.error('生成邀请码失败:', e)
    message.error(e?.message || (e as Error)?.message || '生成失败')
    throw e
  }
}

function copyCode(code: string) {
  navigator.clipboard.writeText(code).then(() => {
    message.success('已复制到剪贴板')
  }).catch(() => {
    message.error('复制失败')
  })
}

function getStatusTag(status: number) {
  const map: Record<number, { text: string; class: string }> = {
    0: { text: '未使用', class: 'green' },
    1: { text: '已使用', class: 'gray' },
  }
  return map[status] || { text: '未知', class: 'gray' }
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}
</script>

<template>
  <div id="page-invite-code">
    <!-- BG -->
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <!-- Page Head -->
      <div class="page-head">
        <h2><span class="accent-line"></span>邀请码管理</h2>
        <div class="actions">
          <button class="cyber-btn" @click="loadCodes" :disabled="loading">
            <i class="fas fa-sync-alt" style="margin-right:5px;"></i>刷新
          </button>
          <button class="cyber-btn-primary" @click="handleCreate">
            <i class="fas fa-plus" style="margin-right:5px;"></i>生成邀请码
          </button>
        </div>
      </div>

      <!-- Stats Row -->
      <div class="stats-row">
        <div class="stat-card cyber-card">
          <div class="stat-icon" style="background:rgba(99,102,241,0.15);color:#818cf8;">
            <i class="fas fa-ticket-alt"></i>
          </div>
          <div class="stat-info">
            <span class="stat-label">总邀请码</span>
            <span class="stat-value">{{ codes.length }}</span>
          </div>
        </div>
        <div class="stat-card cyber-card">
          <div class="stat-icon" style="background:rgba(16,185,129,0.15);color:#10b981;">
            <i class="fas fa-check-circle"></i>
          </div>
          <div class="stat-info">
            <span class="stat-label">已使用</span>
            <span class="stat-value">{{ codes.filter(c => c.status === 1).length }}</span>
          </div>
        </div>
        <div class="stat-card cyber-card">
          <div class="stat-icon" style="background:rgba(245,158,11,0.15);color:#f59e0b;">
            <i class="fas fa-clock"></i>
          </div>
          <div class="stat-info">
            <span class="stat-label">未使用</span>
            <span class="stat-value">{{ codes.filter(c => c.status === 0).length }}</span>
          </div>
        </div>
      </div>

      <!-- Invite Code List -->
      <div class="code-list-card cyber-card" v-loading="loading">
        <div class="list-header">
          <h3><i class="fas fa-list" style="margin-right:8px;"></i>邀请码列表</h3>
        </div>

        <div class="code-table" v-if="codes.length > 0">
          <div class="table-head">
            <span class="col-code">邀请码</span>
            <span class="col-status">状态</span>
            <span class="col-user">使用者</span>
            <span class="col-time">创建时间</span>
            <span class="col-action">操作</span>
          </div>
          <div class="table-body">
            <div class="table-row" v-for="item in codes" :key="item.code">
              <span class="col-code">
                <span class="code-value" @click="copyCode(item.code)">
                  <i class="fas fa-copy" style="margin-right:5px;"></i>{{ item.code }}
                </span>
              </span>
              <span class="col-status">
                <span class="status-tag" :class="getStatusTag(item.status).class">
                  <i :class="item.status === 0 ? 'fas fa-circle' : 'fas fa-check'" style="margin-right:5px;font-size:8px;"></i>
                  {{ getStatusTag(item.status).text }}
                </span>
              </span>
              <span class="col-user">
                {{ item.usedBy || '-' }}
              </span>
              <span class="col-time">
                {{ formatDate(item.createdAt) }}
              </span>
              <span class="col-action">
                <button class="action-btn" @click="copyCode(item.code)" title="复制">
                  <i class="fas fa-copy"></i>
                </button>
              </span>
            </div>
          </div>
        </div>

        <!-- Empty -->
        <div class="empty-state" v-else>
          <i class="fas fa-ticket-alt"></i>
          <p>暂无邀请码</p>
          <span>点击"生成邀请码"按钮创建一个</span>
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
    linear-gradient(rgba(99, 102, 241, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.03) 1px, transparent 1px);
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
  max-width: 1200px;
  margin: 0 auto;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-head h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
}

.accent-line {
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, var(--accent), var(--accent-dark));
  border-radius: 2px;
  margin-right: 12px;
}

.actions {
  display: flex;
  gap: 12px;
}

.cyber-btn,
.cyber-btn-primary {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  border: 1px solid var(--accent);
}

.cyber-btn {
  background: transparent;
  color: var(--accent);
}

.cyber-btn:hover:not(:disabled) {
  background: rgba(99, 102, 241, 0.1);
}

.cyber-btn-primary {
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  color: white;
  border-color: var(--accent);
}

.cyber-btn-primary:hover {
  box-shadow: var(--accent-glow-hover);
}

.cyber-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
}

.cyber-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
}

.code-list-card {
  padding: 0;
  overflow: hidden;
}

.list-header {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.list-header h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.code-table {
  width: 100%;
}

.table-head {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1.5fr 0.5fr;
  padding: 12px 20px;
  background: rgba(99, 102, 241, 0.08);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.table-body {
  max-height: 500px;
  overflow-y: auto;
}

.table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1.5fr 0.5fr;
  padding: 14px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  align-items: center;
  transition: background 0.2s ease;
}

.table-row:hover {
  background: rgba(99, 102, 241, 0.04);
}

.col-code {
  color: var(--text-primary);
}

.code-value {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 6px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.code-value:hover {
  background: rgba(99, 102, 241, 0.2);
  box-shadow: var(--accent-glow);
}

.col-status .status-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.green {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.status-tag.gray {
  background: rgba(107, 114, 128, 0.15);
  color: #9ca3af;
}

.col-user,
.col-time {
  color: var(--text-secondary);
  font-size: 13px;
}

.col-action {
  display: flex;
  justify-content: center;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  background: rgba(99, 102, 241, 0.15);
  color: var(--accent);
  border-color: var(--accent);
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.empty-state i {
  font-size: 48px;
  color: rgba(255, 255, 255, 0.1);
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.empty-state span {
  font-size: 13px;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .table-head,
  .table-row {
    grid-template-columns: 2fr 1fr 1fr;
  }

  .col-time,
  .col-user {
    display: none;
  }
}
</style>
