<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getOrders } from '@/api/operator'
import { message } from 'ant-design-vue'
import type { Order } from '@/types/api'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
  loadOrders()
})

const loading = ref(false)
const orders = ref<Order[]>([])
const pagination = ref({ page: 1, size: 10, total: 0 })
const selectedStatus = ref<number | undefined>(undefined)

// 状态筛选选项
const statusOptions = [
  { value: undefined, label: '全部' },
  { value: 1, label: '已下单' },
  { value: 2, label: '已确认' },
  { value: 3, label: '已发货' },
  { value: 4, label: '已完成' },
  { value: 5, label: '已关闭' },
]

// 订单详情弹框
const detailVisible = ref(false)
const currentOrder = ref<Order | null>(null)

async function loadOrders() {
  loading.value = true
  try {
    const res = await getOrders({
      page: pagination.value.page,
      size: pagination.value.size,
      status: selectedStatus.value
    })
    orders.value = res.list.map((o: Order) => ({
      ...o,
      id: String(o.id)
    }))
    pagination.value.total = res.total
  } catch (e: any) {
    console.error('加载订单列表失败:', e)
    message.error(e?.message || (e as Error)?.message || '加载失败')
    throw e
  } finally {
    loading.value = false
  }
}

function handleStatusChange(status: number | undefined) {
  selectedStatus.value = status
  pagination.value.page = 1
  loadOrders()
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadOrders()
}

function handlePageSizeChange(size: number) {
  pagination.value.size = size
  pagination.value.page = 1
  loadOrders()
}

function viewDetail(order: Order) {
  currentOrder.value = order
  detailVisible.value = true
}

function getStatusTag(status: number) {
  const map: Record<number, { text: string; class: string }> = {
    1: { text: '已下单', class: 'blue' },
    2: { text: '已确认', class: 'cyan' },
    3: { text: '已发货', class: 'orange' },
    4: { text: '已完成', class: 'green' },
    5: { text: '已关闭', class: 'gray' },
  }
  return map[status] || { text: '未知', class: 'gray' }
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}
</script>

<template>
  <div id="page-order-manage">
    <!-- BG -->
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <!-- Page Head -->
      <div class="page-head">
        <h2><span class="accent-line"></span>订单管理</h2>
        <div class="actions">
          <button class="cyber-btn" @click="loadOrders">
            <i class="fas fa-sync-alt" style="margin-right:5px;"></i>刷新
          </button>
        </div>
      </div>

      <!-- Filter Card -->
      <div class="filter-card">
        <div class="filter-row">
          <span class="filter-label">订单状态</span>
          <div class="status-filter">
            <button
              v-for="opt in statusOptions"
              :key="opt.value"
              class="filter-btn"
              :class="{ active: selectedStatus === opt.value }"
              @click="handleStatusChange(opt.value)"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- Table Card -->
      <div class="table-card">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>订单号</th>
                <th>用户</th>
                <th>商品</th>
                <th>数量</th>
                <th>积分</th>
                <th>状态</th>
                <th>时间</th>
                <th style="width:80px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in orders" :key="order.id">
                <td class="id-cell">{{ order.orderNo }}</td>
                <td>{{ order.userNickname || order.userId }}</td>
                <td>
                  <strong>{{ order.productName }}</strong>
                </td>
                <td>x{{ order.quantity }}</td>
                <td class="points-cell">{{ order.totalPoints }}</td>
                <td>
                  <span class="status-tag" :class="getStatusTag(order.status).class">
                    <span class="dot"></span>{{ getStatusTag(order.status).text }}
                  </span>
                </td>
                <td class="time-cell">{{ formatDate(order.createdAt) }}</td>
                <td>
                  <button class="action-btn cyan" title="查看详情" @click="viewDetail(order)">
                    <i class="fas fa-eye"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="orders.length === 0 && !loading">
                <td colspan="8" class="empty-cell">
                  <i class="fas fa-inbox" style="font-size:32px;opacity:0.3;"></i>
                  <p>暂无订单数据</p>
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

      <!-- Pagination -->
      <div class="pagination" v-if="pagination.total > 0">
        <span># TOTAL: {{ pagination.total }} RECORDS</span>
        <div class="page-controls">
          <button class="page-btn" :disabled="pagination.page <= 1" @click="handlePageChange(pagination.page - 1)">
            <i class="fas fa-chevron-left"></i>
          </button>
          <span class="page-info">{{ pagination.page }} / {{ Math.ceil(pagination.total / pagination.size) }}</span>
          <button class="page-btn" :disabled="pagination.page >= Math.ceil(pagination.total / pagination.size)" @click="handlePageChange(pagination.page + 1)">
            <i class="fas fa-chevron-right"></i>
          </button>
          <select class="page-size-select" :value="pagination.size" @change="handlePageSizeChange(Number(($event.target as HTMLSelectElement).value))">
            <option :value="10">10/页</option>
            <option :value="20">20/页</option>
            <option :value="50">50/页</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Order Detail Modal -->
    <div class="modal-overlay" v-if="detailVisible" @click.self="detailVisible = false">
      <div class="modal-card">
        <div class="modal-header">
          <h3><span class="accent-line"></span>订单详情</h3>
          <button class="modal-close" @click="detailVisible = false">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body" v-if="currentOrder">
          <div class="detail-grid">
            <div class="detail-item">
              <label>订单号</label>
              <span class="id-cell">{{ currentOrder.orderNo }}</span>
            </div>
            <div class="detail-item">
              <label>订单状态</label>
              <span class="status-tag" :class="getStatusTag(currentOrder.status).class">
                <span class="dot"></span>{{ getStatusTag(currentOrder.status).text }}
              </span>
            </div>
            <div class="detail-item">
              <label>用户</label>
              <span>{{ currentOrder.userNickname || currentOrder.userId }}</span>
            </div>
            <div class="detail-item">
              <label>商品</label>
              <span>{{ currentOrder.productName }}</span>
            </div>
            <div class="detail-item">
              <label>数量</label>
              <span>x{{ currentOrder.quantity }}</span>
            </div>
            <div class="detail-item">
              <label>总积分</label>
              <span class="points-value">{{ currentOrder.totalPoints }}</span>
            </div>
            <div class="detail-item" v-if="currentOrder.trackingNo">
              <label>物流单号</label>
              <span>{{ currentOrder.trackingNo }}</span>
            </div>
            <div class="detail-item" v-if="currentOrder.carrier">
              <label>物流公司</label>
              <span>{{ currentOrder.carrier }}</span>
            </div>
            <div class="detail-item" v-if="currentOrder.reason">
              <label>关闭原因</label>
              <span class="text-red">{{ currentOrder.reason }}</span>
            </div>
            <div class="detail-item full-width">
              <label>下单时间</label>
              <span>{{ formatDate(currentOrder.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cyber-bg-grid {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(99, 102, 241, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
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
  padding: 24px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-head h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary, #e0e0e0);
  display: flex;
  align-items: center;
  gap: 10px;
}

.accent-line {
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, var(--accent, #6366f1), var(--accent-light, #818cf8));
  border-radius: 2px;
}

.cyber-btn {
  background: linear-gradient(135deg, var(--accent, #6366f1), var(--accent-dark, #4f46e5));
  border: 1px solid var(--accent, #6366f1);
  color: #fff;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.cyber-btn:hover {
  box-shadow: var(--accent-glow-hover, 0 0 20px rgba(99, 102, 241, 0.4));
  transform: translateY(-1px);
}

.filter-card {
  background: var(--card-bg, rgba(30, 30, 46, 0.8));
  border: 1px solid var(--border-color, rgba(99, 102, 241, 0.2));
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.filter-label {
  font-size: 13px;
  color: var(--text-secondary, #9ca3af);
}

.status-filter {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-btn {
  background: var(--bg-secondary, rgba(20, 20, 35, 0.6));
  border: 1px solid var(--border-color, rgba(99, 102, 241, 0.2));
  color: var(--text-secondary, #9ca3af);
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-btn:hover {
  border-color: var(--accent, #6366f1);
  color: var(--accent, #6366f1);
}

.filter-btn.active {
  background: var(--accent, #6366f1);
  border-color: var(--accent, #6366f1);
  color: #fff;
}

.table-card {
  background: var(--card-bg, rgba(30, 30, 46, 0.8));
  border: 1px solid var(--border-color, rgba(99, 102, 241, 0.2));
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
  position: relative;
}

.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: var(--bg-secondary, rgba(20, 20, 35, 0.6));
}

th {
  padding: 14px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary, #9ca3af);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid var(--border-color, rgba(99, 102, 241, 0.2));
}

td {
  padding: 14px 16px;
  font-size: 13px;
  color: var(--text-primary, #e0e0e0);
  border-bottom: 1px solid var(--border-color, rgba(99, 102, 241, 0.1));
}

tbody tr:hover {
  background: var(--bg-hover, rgba(99, 102, 241, 0.05));
}

.id-cell {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  color: var(--accent, #6366f1);
}

.time-cell {
  font-size: 12px;
  color: var(--text-secondary, #9ca3af);
}

.points-cell {
  color: var(--accent, #6366f1);
  font-weight: 600;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.status-tag.blue { background: rgba(59, 130, 246, 0.15); color: #3b82f6; }
.status-tag.cyan { background: rgba(34, 211, 238, 0.15); color: #22d3ee; }
.status-tag.orange { background: rgba(249, 115, 22, 0.15); color: #f97316; }
.status-tag.green { background: rgba(34, 197, 94, 0.15); color: #22c55e; }
.status-tag.red { background: rgba(239, 68, 68, 0.15); color: #ef4444; }
.status-tag.gray { background: rgba(156, 163, 175, 0.15); color: #9ca3af; }

.action-btn {
  width: 30px;
  height: 30px;
  border-radius: 6px;
  border: 1px solid var(--border-color, rgba(99, 102, 241, 0.2));
  background: transparent;
  color: var(--text-secondary, #9ca3af);
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  border-color: var(--accent, #6366f1);
  color: var(--accent, #6366f1);
  box-shadow: var(--accent-glow, 0 0 15px rgba(99, 102, 241, 0.25));
}

.action-btn.green:hover { border-color: #22c55e; color: #22c55e; box-shadow: 0 0 15px rgba(34, 197, 94, 0.25); }
.action-btn.red:hover { border-color: #ef4444; color: #ef4444; box-shadow: 0 0 15px rgba(239, 68, 68, 0.25); }
.action-btn.cyan:hover { border-color: #22d3ee; color: #22d3ee; box-shadow: 0 0 15px rgba(34, 211, 238, 0.25); }

.empty-cell {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary, #9ca3af);
}

.empty-cell i {
  display: block;
  margin-bottom: 12px;
}

.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(10, 10, 20, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--accent, #6366f1);
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-secondary, #9ca3af);
  font-size: 13px;
}

.page-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-btn {
  background: var(--card-bg, rgba(30, 30, 46, 0.8));
  border: 1px solid var(--border-color, rgba(99, 102, 241, 0.2));
  color: var(--text-secondary, #9ca3af);
  width: 32px;
  height: 32px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-btn:hover:not(:disabled) {
  border-color: var(--accent, #6366f1);
  color: var(--accent, #6366f1);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 13px;
  color: var(--text-primary, #e0e0e0);
  min-width: 60px;
  text-align: center;
}

.page-size-select {
  background: var(--card-bg, rgba(30, 30, 46, 0.8));
  border: 1px solid var(--border-color, rgba(99, 102, 241, 0.2));
  color: var(--text-secondary, #9ca3af);
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}

.text-muted {
  color: var(--text-secondary, #9ca3af);
  font-size: 12px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-card {
  background: var(--card-bg, rgba(30, 30, 46, 0.95));
  border: 1px solid var(--border-color, rgba(99, 102, 241, 0.3));
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  backdrop-filter: blur(20px);
  box-shadow: 0 0 40px rgba(99, 102, 241, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color, rgba(99, 102, 241, 0.2));
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #e0e0e0);
  display: flex;
  align-items: center;
  gap: 10px;
}

.modal-close {
  background: transparent;
  border: none;
  color: var(--text-secondary, #9ca3af);
  font-size: 18px;
  cursor: pointer;
  transition: color 0.3s;
  padding: 4px;
}

.modal-close:hover {
  color: var(--accent, #6366f1);
}

.modal-body {
  padding: 24px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-item label {
  font-size: 12px;
  color: var(--text-secondary, #9ca3af);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-item span {
  font-size: 14px;
  color: var(--text-primary, #e0e0e0);
}

.points-value {
  color: var(--accent, #6366f1);
  font-weight: 600;
  font-size: 16px;
}

.text-red {
  color: #ef4444;
}
</style>
