<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { getOrders } from '@/api/user'
import { message } from 'ant-design-vue'
import type { Order } from '@/types/api'

const themeStore = useThemeStore()
const router = useRouter()

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

async function loadOrders() {
  loading.value = true
  try {
    const res = await getOrders({
      page: pagination.value.page,
      size: pagination.value.size,
      status: selectedStatus.value
    })
    orders.value = res.records.map(o => ({
      ...o,
      id: String(o.id)
    }))
    pagination.value.total = res.total
  } catch (e: any) {
    message.error(e.message || '加载失败')
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
  router.push({ name: 'UserOrderDetail', params: { id: order.id } })
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
  <div id="page-order-list">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <div class="page-head">
        <h2><span class="accent-line"></span>我的订单</h2>
      </div>

      <div class="cyber-card">
        <!-- 状态筛选 -->
        <div class="filter-bar">
          <a-radio-group :value="selectedStatus" @update:value="handleStatusChange" option-type="button" :options="statusOptions" />
        </div>

        <!-- 订单列表 -->
        <a-spin :spinning="loading">
          <div v-if="orders.length === 0" class="empty-state">
            <i class="fas fa-receipt"></i>
            <p>暂无订单</p>
          </div>

          <div v-else class="order-list">
            <div v-for="order in orders" :key="order.id" class="order-item" @click="viewDetail(order)">
              <div class="order-header">
                <span class="order-no">订单号: {{ order.orderNo }}</span>
                <span :class="['status-tag', getStatusTag(order.status).class]">
                  {{ getStatusTag(order.status).text }}
                </span>
              </div>
              <div class="order-body">
                <div class="product-info">
                  <span class="product-name">{{ order.productName || '商品' }}</span>
                  <span class="product-qty">x{{ order.quantity }}</span>
                </div>
                <div class="order-points">
                  <span class="points-value">{{ order.totalPoints }}</span>
                  <span class="points-label">积分</span>
                </div>
              </div>
              <div class="order-footer">
                <span class="order-time">{{ formatDate(order.createdAt) }}</span>
                <span class="view-detail">查看详情 <i class="fas fa-chevron-right"></i></span>
              </div>
            </div>
          </div>
        </a-spin>

        <!-- 分页 -->
        <div v-if="orders.length > 0" class="pagination-wrapper">
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
.filter-bar {
  margin-bottom: 20px;
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

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.order-item:hover {
  border-color: var(--accent);
  box-shadow: var(--accent-glow);
  transform: translateY(-2px);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.order-no {
  font-size: 13px;
  color: var(--text-secondary);
  font-family: monospace;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.blue {
  background: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
}

.status-tag.cyan {
  background: rgba(34, 211, 238, 0.15);
  color: #22d3ee;
}

.status-tag.orange {
  background: rgba(249, 115, 22, 0.15);
  color: #f97316;
}

.status-tag.green {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
}

.status-tag.gray {
  background: rgba(156, 163, 175, 0.15);
  color: #9ca3af;
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-name {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 500;
}

.product-qty {
  font-size: 13px;
  color: var(--text-secondary);
}

.order-points {
  text-align: right;
}

.points-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--accent);
}

.points-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: 4px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--border);
}

.order-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.view-detail {
  font-size: 12px;
  color: var(--accent);
  display: flex;
  align-items: center;
  gap: 4px;
}

.view-detail i {
  font-size: 10px;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>