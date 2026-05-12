<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getPoints, getPointsLog } from '@/api/user'
import { message } from 'ant-design-vue'
import type { PointsLog } from '@/types/api'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
  loadPoints()
  loadPointsLog()
})

const loading = ref(false)
const pointsLoading = ref(false)
const currentPoints = ref(0)
const pointsLog = ref<PointsLog[]>([])
const pagination = ref({ page: 1, size: 10, total: 0 })

async function loadPoints() {
  pointsLoading.value = true
  try {
    const res = await getPoints()
    currentPoints.value = res.points
  } catch (e: any) {
    message.error(e.message || '获取积分失败')
  } finally {
    pointsLoading.value = false
  }
}

async function loadPointsLog() {
  loading.value = true
  try {
    const res = await getPointsLog({
      page: pagination.value.page,
      size: pagination.value.size
    })
    pointsLog.value = res.records.map(log => ({
      ...log,
      id: String(log.id)
    }))
    pagination.value.total = res.total
  } catch (e: any) {
    message.error(e.message || '获取积分记录失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadPointsLog()
}

function handlePageSizeChange(size: number) {
  pagination.value.size = size
  pagination.value.page = 1
  loadPointsLog()
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

function getTypeInfo(type: number) {
  if (type === 1) {
    return { text: '增加', class: 'increase', icon: 'fa-arrow-up' }
  } else if (type === 2) {
    return { text: '扣除', class: 'decrease', icon: 'fa-arrow-down' }
  }
  return { text: '未知', class: 'unknown', icon: 'fa-question' }
}
</script>

<template>
  <div id="page-points-info">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <div class="page-head">
        <h2><span class="accent-line"></span>积分中心</h2>
      </div>

      <!-- 积分余额卡片 -->
      <div class="cyber-card points-balance-card">
        <a-spin :spinning="pointsLoading">
          <div class="balance-content">
            <div class="balance-icon">
              <i class="fas fa-gem"></i>
            </div>
            <div class="balance-info">
              <span class="balance-label">当前积分</span>
              <span class="balance-value">{{ currentPoints.toLocaleString() }}</span>
            </div>
          </div>
        </a-spin>
      </div>

      <!-- 积分记录 -->
      <div class="cyber-card">
        <h3 class="section-title">积分记录</h3>

        <a-spin :spinning="loading">
          <div v-if="pointsLog.length === 0" class="empty-state">
            <i class="fas fa-receipt"></i>
            <p>暂无积分记录</p>
          </div>

          <div v-else class="points-log-list">
            <div v-for="log in pointsLog" :key="log.id" class="log-item">
              <div class="log-main">
                <div class="log-type">
                  <span :class="['type-badge', getTypeInfo(log.type).class]">
                    <i :class="['fas', getTypeInfo(log.type).icon]"></i>
                    {{ getTypeInfo(log.type).text }}
                  </span>
                </div>
                <div class="log-amount" :class="getTypeInfo(log.type).class">
                  <span class="amount-prefix">{{ log.type === 1 ? '+' : '-' }}</span>
                  <span class="amount-value">{{ Math.abs(log.amount) }}</span>
                </div>
              </div>
              <div class="log-details">
                <div class="log-row">
                  <span class="log-label">余额</span>
                  <span class="log-value">{{ log.balance.toLocaleString() }}</span>
                </div>
                <div class="log-row" v-if="log.remark">
                  <span class="log-label">备注</span>
                  <span class="log-value remark">{{ log.remark }}</span>
                </div>
                <div class="log-row">
                  <span class="log-label">时间</span>
                  <span class="log-value time">{{ formatDate(log.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </a-spin>

        <!-- 分页 -->
        <div v-if="pointsLog.length > 0" class="pagination-wrapper">
          <a-pagination
            :current="pagination.page"
            :page-size="pagination.size"
            :total="pagination.total"
            :show-size-changer="true"
            :page-size-options="['5', '10', '20', '50']"
            show-quick-jumper
            @change="handlePageChange"
            @showSizeChange="handlePageSizeChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 积分余额卡片 */
.points-balance-card {
  margin-bottom: 24px;
}

.balance-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.balance-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), rgba(99, 102, 241, 0.6));
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--accent-glow);
}

.balance-icon i {
  font-size: 32px;
  color: #fff;
}

.balance-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.balance-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.balance-value {
  font-size: 36px;
  font-weight: 700;
  color: var(--accent);
  font-family: 'Orbitron', 'Roboto Mono', monospace;
  text-shadow: var(--accent-glow-text);
}

/* 列表样式 */
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.points-log-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-item {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s;
}

.log-item:hover {
  border-color: var(--accent);
  box-shadow: var(--accent-glow);
}

.log-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.type-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.type-badge.increase {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
}

.type-badge.decrease {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.type-badge.unknown {
  background: rgba(156, 163, 175, 0.15);
  color: #9ca3af;
}

.log-amount {
  display: flex;
  align-items: baseline;
  font-family: 'Orbitron', 'Roboto Mono', monospace;
  font-weight: 600;
}

.log-amount.increase {
  color: #22c55e;
}

.log-amount.decrease {
  color: #ef4444;
}

.amount-prefix {
  font-size: 14px;
  margin-right: 2px;
}

.amount-value {
  font-size: 20px;
}

.log-details {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--border);
}

.log-row {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.log-label {
  font-size: 11px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.log-value {
  font-size: 14px;
  color: var(--text-primary);
}

.log-value.remark {
  color: var(--text-secondary);
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.log-value.time {
  color: var(--text-secondary);
  font-size: 12px;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
