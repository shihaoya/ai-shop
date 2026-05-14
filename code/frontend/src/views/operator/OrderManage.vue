<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getOrders, confirmOrder, shipOrder, completeOrder } from '@/api/operator'
import { message, Modal } from 'ant-design-vue'
import type { Order } from '@/types/api'
import { OrderStatus, OrderStatusText, OrderStatusClass } from '@/types/enums'
import { useOperatorShop } from '@/composables/useOperatorShop'

const themeStore = useThemeStore()
const { isApproved } = useOperatorShop()

onMounted(() => {
  loadOrders()
})

const loading = ref(false)
const orders = ref<Order[]>([])
const pagination = ref({ page: 1, size: 10, total: 0 })
const selectedStatus = ref<number | undefined>(undefined)

// 状态筛选选项
const statusOptions = [
  { value: undefined, label: '全部' },
  { value: OrderStatus.PENDING, label: OrderStatusText[OrderStatus.PENDING] },
  { value: OrderStatus.CONFIRMED, label: OrderStatusText[OrderStatus.CONFIRMED] },
  { value: OrderStatus.SHIPPED, label: OrderStatusText[OrderStatus.SHIPPED] },
  { value: OrderStatus.COMPLETED, label: OrderStatusText[OrderStatus.COMPLETED] },
  { value: OrderStatus.CLOSED, label: OrderStatusText[OrderStatus.CLOSED] },
]

// 订单详情弹框
const detailVisible = ref(false)
const currentOrder = ref<Order | null>(null)

// 发货弹框
const shipVisible = ref(false)
const shipForm = ref({ carrier: '', trackingNo: '' })
const shipLoading = ref(false)

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
  } catch (e) {
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
  return {
    text: OrderStatusText[status] || '未知',
    class: OrderStatusClass[status] || 'gray',
  }
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 订单操作
function openShipModal(order: Order) {
  currentOrder.value = order
  shipForm.value = { carrier: '', trackingNo: '' }
  shipVisible.value = true
}

async function handleConfirm(order: Order) {
  Modal.confirm({
    title: '确认订单',
    content: `确定要确认订单 "${order.orderNo}" 吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await confirmOrder(order.id)
        message.success('订单已确认')
        loadOrders()
      } catch (e: any) {
        message.error(e?.message || '操作失败')
        throw e
      }
    },
  })
}

async function handleShip() {
  if (!shipForm.value.carrier.trim()) {
    message.warning('请输入物流公司')
    return
  }
  if (!shipForm.value.trackingNo.trim()) {
    message.warning('请输入物流单号')
    return
  }
  if (!currentOrder.value) return

  shipLoading.value = true
  try {
    await shipOrder(currentOrder.value.id, shipForm.value.trackingNo.trim(), shipForm.value.carrier.trim())
    message.success('已发货')
    shipVisible.value = false
    loadOrders()
  } catch (e: any) {
    message.error(e?.message || '操作失败')
    throw e
  } finally {
    shipLoading.value = false
  }
}

async function handleComplete(order: Order) {
  Modal.confirm({
    title: '完成订单',
    content: `确定将订单 "${order.orderNo}" 标记为已完成吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await completeOrder(order.id)
        message.success('订单已完成')
        loadOrders()
      } catch (e: any) {
        message.error(e?.message || '操作失败')
        throw e
      }
    },
  })
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

      <!-- 警告 Banner -->
      <div v-if="!isApproved()" class="warning-banner">
        <i class="fas fa-exclamation-triangle"></i>
        <span>您的店铺尚未通过审核，暂时无法管理订单</span>
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
                <th style="width:140px;">操作</th>
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
                  <div class="action-btns">
                    <button class="action-btn cyan" title="查看详情" @click="viewDetail(order)">
                      <i class="fas fa-eye"></i>
                    </button>
                    <button
                      v-if="order.status === OrderStatus.PENDING"
                      class="action-btn green"
                      title="确认订单"
                      @click="handleConfirm(order)"
                    >
                      <i class="fas fa-check"></i>
                    </button>
                    <button
                      v-if="order.status === OrderStatus.CONFIRMED"
                      class="action-btn purple"
                      title="发货"
                      @click="openShipModal(order)"
                    >
                      <i class="fas fa-truck"></i>
                    </button>
                    <button
                      v-if="order.status === OrderStatus.SHIPPED"
                      class="action-btn green"
                      title="完成订单"
                      @click="handleComplete(order)"
                    >
                      <i class="fas fa-flag-checkered"></i>
                    </button>
                  </div>
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

    <!-- Ship Modal -->
    <div class="modal-overlay" v-if="shipVisible" @click.self="shipVisible = false">
      <div class="modal-card">
        <div class="modal-header">
          <h3><span class="accent-line"></span>发货</h3>
          <button class="modal-close" @click="shipVisible = false">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-item">
            <label>物流公司</label>
            <input
              v-model="shipForm.carrier"
              type="text"
              placeholder="请输入物流公司名称"
              class="cyber-input"
            />
          </div>
          <div class="form-item">
            <label>物流单号</label>
            <input
              v-model="shipForm.trackingNo"
              type="text"
              placeholder="请输入物流单号"
              class="cyber-input"
            />
          </div>
          <div class="modal-actions">
            <button class="cyber-btn" @click="shipVisible = false">取消</button>
            <button class="cyber-btn-primary" :disabled="shipLoading" @click="handleShip">
              <i v-if="shipLoading" class="fas fa-spinner fa-spin"></i>
              <span v-else>确认发货</span>
            </button>
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
  color: var(--text-primary);
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
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.filter-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.status-filter {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-btn {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.filter-btn.active {
  background: var(--accent);
  border-color: var(--accent);
  color: #fff;
}

.table-card {
  background: rgba(13, 15, 36, 0.5);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius);
  overflow: hidden;
  margin-bottom: 20px;
  position: relative;
}

.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

thead {
  background: rgba(var(--accent-rgb), 0.03);
}

th {
  padding: 12px 14px;
  text-align: left;
  font-weight: 600;
  color: var(--text-muted);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  border-bottom: 1px solid var(--border-subtle);
}

td {
  padding: 12px 14px;
  border-bottom: 1px solid rgba(var(--accent-rgb), 0.05);
  color: var(--text-primary);
  font-size: 13px;
}

tbody tr {
  transition: all 0.2s ease;
}

tbody tr:hover {
  background: rgba(var(--accent-rgb), 0.04);
}

.id-cell {
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  color: var(--accent);
}

.time-cell {
  font-size: 12px;
  color: var(--text-secondary);
}

.points-cell {
  color: var(--accent);
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
  border: 1px solid var(--border-color);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  box-shadow: var(--accent-glow);
}

.action-btn.green:hover { border-color: var(--green); color: var(--green); box-shadow: 0 0 15px rgba(34, 197, 94, 0.25); }
.action-btn.red:hover { border-color: var(--red); color: var(--red); box-shadow: 0 0 15px rgba(239, 68, 68, 0.25); }
.action-btn.cyan:hover { border-color: var(--cyan); color: var(--cyan); box-shadow: 0 0 15px rgba(34, 211, 238, 0.25); }
.action-btn.purple:hover { border-color: var(--purple); color: var(--purple); box-shadow: 0 0 15px rgba(168, 85, 247, 0.25); }

.action-btns {
  display: flex;
  gap: 6px;
  justify-content: center;
}

.warning-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  background: rgba(245, 158, 11, 0.08);
  border: 1px solid rgba(245, 158, 11, 0.3);
  border-radius: 8px;
  color: #f59e0b;
  margin-bottom: 16px;
  font-size: 14px;
}

.warning-banner i {
  font-size: 18px;
}

.empty-cell {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
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
  background: var(--bg-deep);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--accent);
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-secondary);
  font-size: 13px;
}

.page-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-btn {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
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
  border-color: var(--accent);
  color: var(--accent);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 13px;
  color: var(--text-primary);
  min-width: 60px;
  text-align: center;
}

.page-size-select {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.text-muted {
  color: var(--text-secondary);
  font-size: 12px;
}

.empty-cell {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--bg-deep);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--accent);
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-secondary);
  font-size: 13px;
}

.page-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-btn {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
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
  border-color: var(--accent);
  color: var(--accent);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 13px;
  color: var(--text-primary);
  min-width: 60px;
  text-align: center;
}

.page-size-select {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  box-shadow: var(--accent-glow);
}

.action-btn.green:hover { border-color: var(--green); color: var(--green); box-shadow: 0 0 15px rgba(34, 197, 94, 0.25); }
.action-btn.red:hover { border-color: var(--red); color: var(--red); box-shadow: 0 0 15px rgba(239, 68, 68, 0.25); }
.action-btn.cyan:hover { border-color: var(--cyan); color: var(--cyan); box-shadow: 0 0 15px rgba(34, 211, 238, 0.25); }

.empty-cell {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--bg-deep);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--accent);
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-secondary);
  font-size: 13px;
}

.page-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-btn {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
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
  border-color: var(--accent);
  color: var(--accent);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 13px;
  color: var(--text-primary);
  min-width: 60px;
  text-align: center;
}

.page-size-select {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
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
  color: var(--text-secondary);
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
  background: var(--card-bg);
  border: 1px solid var(--border-color);
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
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.modal-close {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:hover {
  border-color: var(--red);
  color: var(--red);
}

.modal-body {
  padding: 24px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
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
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-item > span {
  font-size: 14px;
  color: var(--text-primary);
}

.points-value {
  color: var(--accent);
  font-weight: 600;
  font-size: 16px;
}

.text-red {
  color: var(--red);
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
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 0 40px rgba(99, 102, 241, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}

.modal-close {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  font-size: 18px;
  cursor: pointer;
  transition: color 0.3s;
  padding: 4px;
}

.modal-close:hover {
  color: var(--accent);
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
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-item span {
  font-size: 14px;
  color: var(--text-primary);
}

.points-value {
  color: var(--accent);
  font-weight: 600;
  font-size: 16px;
}

.text-red {
  color: var(--red);
}
</style>
