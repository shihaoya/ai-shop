<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getInviteCode, createInviteCode } from '@/api/admin'
import { message } from 'ant-design-vue'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
  loadCodes()
})

const loading = ref(false)
const codes = ref<{ id: string; code: string; role: number; creatorId: string; status: number; createdAt?: string }[]>([])

async function loadCodes() {
  loading.value = true
  try {
    const res = await getInviteCode()
    codes.value = res || []
  } catch (e: any) {
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  try {
    const res = await createInviteCode()
    message.success('邀请码生成成功')
    codes.value.unshift({
      id: Date.now().toString(),
      code: res.code || '',
      role: 1,
      creatorId: '',
      status: 0,
      createdAt: new Date().toISOString()
    })
  } catch (e: any) {
    message.error(e.message || '生成失败')
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
          <button class="cyber-btn" @click="loadCodes">
            <i class="fas fa-sync-alt" style="margin-right:5px;"></i>刷新
          </button>
          <button class="cyber-btn-primary" @click="handleCreate">
            <i class="fas fa-plus" style="margin-right:5px;"></i>生成邀请码
          </button>
        </div>
      </div>

      <!-- Stats Row -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon"><i class="fas fa-ticket-alt"></i></div>
          <div class="stat-info">
            <span class="stat-num">{{ codes.length }}</span>
            <span class="stat-label">总邀请码</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon green"><i class="fas fa-check-circle"></i></div>
          <div class="stat-info">
            <span class="stat-num">{{ codes.filter(c => c.status === 0).length }}</span>
            <span class="stat-label">未使用</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon orange"><i class="fas fa-check"></i></div>
          <div class="stat-info">
            <span class="stat-num">{{ codes.filter(c => c.status === 1).length }}</span>
            <span class="stat-label">已使用</span>
          </div>
        </div>
      </div>

      <!-- Table Card -->
      <div class="table-card">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>邀请码</th>
                <th>状态</th>
                <th>使用者</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in codes" :key="item.id">
                <td>
                  <code class="code-cell" @click="copyCode(item.code)">
                    {{ item.code }}
                    <i class="fas fa-copy copy-icon"></i>
                  </code>
                </td>
                <td>
                  <span class="status-tag" :class="getStatusTag(item.status).class">
                    <span class="dot"></span>{{ getStatusTag(item.status).text }}
                  </span>
                </td>
                <td>-</td>
                <td class="time-cell">{{ formatDate(item.createdAt) }}</td>
                <td>
                  <button class="action-btn" title="复制" @click="copyCode(item.code)">
                    <i class="fas fa-copy"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="codes.length === 0 && !loading">
                <td colspan="5" class="empty-cell">
                  <i class="fas fa-ticket-alt" style="font-size:32px;opacity:0.3;"></i>
                  <p>暂无邀请码，点击上方按钮生成</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="loading-mask">
          <i class="fas fa-spinner fa-spin"></i>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#page-invite-code {
  min-height: 100vh;
  position: relative;
}

.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 18px;
}

.stat-card {
  background: linear-gradient(145deg, var(--bg-card), var(--bg-surface));
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius);
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
}

.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-sm);
  background: rgba(var(--accent-rgb), 0.10);
  color: var(--accent-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.stat-icon.green {
  background: rgba(16, 185, 129, 0.10);
  color: var(--green);
}

.stat-icon.orange {
  background: rgba(245, 158, 11, 0.10);
  color: var(--orange);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-num {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
}

.code-cell {
  font-family: var(--font-mono);
  font-size: 14px;
  color: var(--accent-light);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px;
  background: rgba(var(--accent-rgb), 0.08);
  border-radius: var(--radius-xs);
  border: 1px solid rgba(var(--accent-rgb), 0.15);
}

.code-cell:hover {
  background: rgba(var(--accent-rgb), 0.15);
}

.copy-icon {
  font-size: 12px;
  opacity: 0.6;
}

.time-cell {
  font-size: 12px;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

.empty-cell {
  text-align: center;
  padding: 40px !important;
  color: var(--text-muted);
}

.empty-cell i {
  display: block;
  margin-bottom: 8px;
}

.loading-mask {
  position: absolute;
  inset: 0;
  background: rgba(7, 8, 22, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--accent);
}
</style>