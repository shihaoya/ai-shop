<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getOrders, getOrder, closeOrder, completeOrder } from '@/api/user'
import { message } from 'ant-design-vue'
import type { Order } from '@/types/api'
import CyberPagination from '@/components/CyberPagination.vue'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
  loadOrders()
})

const loading = ref(false)
const orders = ref<Order[]>([])
const pagination = ref({ page: 1, size: 10, total: 0 })
const selectedStatus = ref<number | undefined>(undefined)

// 详情弹框
const detailVisible = ref(false)
const detailLoading = ref(false)
const selectedOrder = ref<Order | null>(null)

// 状态筛选选项
const statusOptions = [
  { value: undefined, label: '全部' },
  { value: 1, label: '已下单' },
  { value: 2, label: '已确认' },
  { value: 3, label: '已发货' },
  { value: 4, label: '已完成' },
  { value: 5, label: '已关闭' },
]

// 详情状态信息
const statusInfo = computed(() => {
  const map: Record<number, { text: string; tagClass: string; icon: string }> = {
    1: { text: '已下单', tagClass: 'blue', icon: 'fa-clock' },
    2: { text: '已确认', tagClass: 'cyan', icon: 'fa-check' },
    3: { text: '已发货', tagClass: 'orange', icon: 'fa-truck' },
    4: { text: '已完成', tagClass: 'green', icon: 'fa-check-double' },
    5: { text: '已关闭', tagClass: 'gray', icon: 'fa-times-circle' },
  }
  return map[selectedOrder.value?.status || 0] || { text: '未知', tagClass: 'gray', icon: 'fa-question' }
})

const canCancel = computed(() => selectedOrder.value?.status === 1)
const canComplete = computed(() => selectedOrder.value?.status === 3)

async function loadOrders() {
  loading.value = true
  try {
    const res = await getOrders({
      page: pagination.value.page,
      size: pagination.value.size,
      status: selectedStatus.value
    })
    orders.value = res.list.map(o => ({
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

// 打开详情弹框
async function viewDetail(order: Order) {
  selectedOrder.value = null
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getOrder(order.id)
    selectedOrder.value = { ...res, id: String(res.id) }
  } catch {
    message.error('获取订单详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

// 取消订单
async function handleCancel() {
  if (!selectedOrder.value) return
  try {
    await closeOrder(selectedOrder.value.id)
    message.success('订单已取消')
    detailVisible.value = false
    loadOrders()
  } catch {
    message.error('取消订单失败')
  }
}

// 确认收货
async function handleComplete() {
  if (!selectedOrder.value) return
  try {
    await completeOrder(selectedOrder.value.id)
    message.success('已确认收货')
    detailVisible.value = false
    loadOrders()
  } catch {
    message.error('确认收货失败')
  }
}

function getStatusTag(status: number) {
  const map: Record<number, { text: string; class: string; icon: string }> = {
    1: { text: '已下单', class: 'blue', icon: 'fa-clock' },
    2: { text: '已确认', class: 'cyan', icon: 'fa-check-circle' },
    3: { text: '已发货', class: 'orange', icon: 'fa-truck' },
    4: { text: '已完成', class: 'green', icon: 'fa-check-double' },
    5: { text: '已关闭', class: 'gray', icon: 'fa-times-circle' },
  }
  return map[status] || { text: '未知', class: 'gray', icon: 'fa-question-circle' }
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

function formatAddress(addr?: Order['addressInfo']) {
  if (!addr) return '-'
  return `${addr.province}${addr.city}${addr.district}${addr.detail} ${addr.receiver} ${addr.phone}`
}
</script>

<template>
  <div id="page-order-list">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>
    <div class="cyber-bg-orb" style="width:300px;height:300px;top:40%;right:-80px;background:rgba(6,182,212,0.05);"></div>

    <div class="page-content">
      <div class="page-head">
        <h2><span class="accent-line"></span>我的订单</h2>
        <span class="order-count" v-if="pagination.total > 0">共 {{ pagination.total }} 笔订单</span>
      </div>

      <div class="cyber-card">
        <!-- 状态筛选 -->
        <div class="filter-bar">
          <div class="filter-label"><i class="fas fa-filter"></i></div>
          <a-radio-group :value="selectedStatus" @update:value="handleStatusChange" option-type="button" :options="statusOptions" />
        </div>

        <!-- 订单列表 -->
        <a-spin :spinning="loading">
          <div v-if="orders.length === 0" class="empty-state">
            <div class="empty-icon">
              <i class="fas fa-receipt"></i>
            </div>
            <h3 class="empty-title">暂无订单</h3>
            <p class="empty-desc">快去兑换心仪的商品吧</p>
          </div>

          <div v-else class="order-list">
            <div
              v-for="order in orders"
              :key="order.id"
              class="order-item"
              @click="viewDetail(order)"
            >
              <!-- 左侧状态指示器 -->
              <div :class="['status-indicator', getStatusTag(order.status).class]"></div>

              <!-- 产品缩略图 -->
              <div class="order-thumb">
                <i class="fas fa-box"></i>
              </div>

              <!-- 订单信息 -->
              <div class="order-info">
                <div class="order-info-main">
                  <span class="order-name">{{ order.productName || '商品' }}</span>
                  <span class="order-qty">x{{ order.quantity }}</span>
                </div>
                <div class="order-info-sub">
                  <span class="order-no">
                    <i class="fas fa-hashtag"></i>
                    {{ order.orderNo }}
                  </span>
                  <span class="order-time">
                    <i class="fas fa-clock"></i>
                    {{ formatDate(order.createdAt) }}
                  </span>
                </div>
              </div>

              <!-- 积分信息 -->
              <div class="order-points">
                <span class="points-value">{{ order.totalPoints }}</span>
                <span class="points-label">积分</span>
              </div>

              <!-- 状态标签 -->
              <div :class="['status-tag', getStatusTag(order.status).class]">
                <i :class="['fas', getStatusTag(order.status).icon]"></i>
                <span>{{ getStatusTag(order.status).text }}</span>
              </div>

              <!-- 查看详情 -->
              <div class="order-action">
                <span class="action-text">查看详情</span>
                <i class="fas fa-chevron-right"></i>
              </div>
            </div>
          </div>
        </a-spin>

        <!-- Pagination -->
        <div v-if="pagination.total > 0" class="pagination-wrapper">
          <CyberPagination
            v-model:current="pagination.page"
            v-model:pageSize="pagination.size"
            :total="pagination.total"
            @change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <!-- 订单详情弹框 -->
    <a-modal
      v-model:open="detailVisible"
      :centered="true"
      :width="520"
      :footer="null"
      class="cyber-modal"
    >
      <div v-if="!detailVisible || detailLoading" class="modal-loading" style="text-align:center;padding:60px 20px;color:var(--text-secondary);">
        <i v-if="detailLoading" class="fas fa-spinner fa-spin" style="font-size:32px;color:var(--accent);"></i>
        <p v-if="detailLoading" style="margin-top:16px;font-size:14px;">加载中...</p>
      </div>

      <div v-else-if="selectedOrder" class="modal-detail-content">
        <!-- 状态头部 -->
        <div class="detail-status-header" :class="statusInfo.tagClass">
          <div class="detail-status-icon">
            <i :class="['fas', statusInfo.icon]"></i>
          </div>
          <div class="detail-status-info">
            <h3>{{ statusInfo.text }}</h3>
            <p>订单号：{{ selectedOrder.orderNo }}</p>
            <p>下单时间：{{ formatDate(selectedOrder.createdAt) }}</p>
          </div>
        </div>

        <div class="detail-body">
          <!-- 商品信息 -->
          <div class="detail-section">
            <div class="detail-section-title"><i class="fas fa-box"></i> 商品信息</div>
            <div class="detail-product">
              <div class="detail-product-icon"><i class="fas fa-box"></i></div>
              <div class="detail-product-info">
                <div class="detail-product-name">{{ selectedOrder.productName }}</div>
                <div class="detail-product-meta">
                  <span><i class="fas fa-coins"></i> {{ selectedOrder.totalPoints }} 积分</span>
                  <span>x {{ selectedOrder.quantity }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 收货地址 -->
          <div class="detail-section" v-if="selectedOrder.addressInfo">
            <div class="detail-section-title"><i class="fas fa-map-marker-alt"></i> 收货地址</div>
            <div class="detail-address">{{ formatAddress(selectedOrder.addressInfo) }}</div>
          </div>

          <!-- 物流信息 -->
          <div class="detail-section" v-if="selectedOrder.trackingNo">
            <div class="detail-section-title"><i class="fas fa-truck"></i> 物流信息</div>
            <div class="detail-logistics">
              <div class="detail-log-row">
                <span class="log-label">承运商：</span>
                <span class="log-value">{{ selectedOrder.carrier || '-' }}</span>
              </div>
              <div class="detail-log-row">
                <span class="log-label">运单号：</span>
                <span class="log-value">{{ selectedOrder.trackingNo }}</span>
              </div>
            </div>
          </div>

          <!-- 订单时间线 -->
          <div class="detail-section">
            <div class="detail-section-title"><i class="fas fa-clock"></i> 订单时间</div>
            <div class="detail-timeline">
              <div class="detail-timeline-item">
                <div class="timeline-dot"></div>
                <div class="timeline-content">
                  <span class="timeline-time">{{ formatDate(selectedOrder.createdAt) }}</span>
                  <span class="timeline-event">创建订单</span>
                </div>
              </div>
              <div class="detail-timeline-item" v-if="selectedOrder.updatedAt && selectedOrder.status !== 1">
                <div class="timeline-dot"></div>
                <div class="timeline-content">
                  <span class="timeline-time">{{ formatDate(selectedOrder.updatedAt) }}</span>
                  <span class="timeline-event">
                    {{ selectedOrder.status === 5 ? '订单关闭' : selectedOrder.status === 4 ? '交易完成' : '状态更新' }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 关闭原因 -->
          <div class="detail-section" v-if="selectedOrder.reason">
            <div class="detail-section-title"><i class="fas fa-info-circle"></i> {{ selectedOrder.status === 5 ? '关闭' : '备注' }}信息</div>
            <div class="detail-reason">{{ selectedOrder.reason }}</div>
          </div>

          <!-- 操作按钮 -->
          <div class="detail-actions" v-if="canCancel || canComplete">
            <a-button v-if="canCancel" type="primary" danger @click="handleCancel">
              <i class="fas fa-times"></i> 取消订单
            </a-button>
            <a-button v-if="canComplete" type="primary" @click="handleComplete" style="background:var(--green);border-color:var(--green);">
              <i class="fas fa-check"></i> 确认收货
            </a-button>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
#page-order-list {
  min-height: 100vh;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
  padding: 20px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-head h2 {
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
}

.accent-line {
  display: inline-block;
  width: 4px;
  height: 20px;
  background: var(--accent);
  border-radius: 2px;
}

.order-count {
  font-size: 13px;
  color: var(--text-muted);
}

/* 卡片容器 */
.cyber-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius);
  padding: 20px;
  position: relative;
  overflow: visible;
}

.cyber-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(var(--accent-rgb), 0.5), transparent);
  opacity: 0.4;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-subtle);
}

.filter-label {
  color: var(--text-muted);
  font-size: 14px;
}

.filter-bar :deep(.ant-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-bar :deep(.ant-radio-button-wrapper) {
  background: var(--bg-input);
  border-color: var(--border-subtle);
  color: var(--text-secondary);
  font-size: 13px;
  padding: 0 14px;
  height: 30px;
  line-height: 28px;
  border-radius: var(--radius-sm);
}

.filter-bar :deep(.ant-radio-button-wrapper:hover) {
  color: var(--accent);
  border-color: var(--accent);
}

.filter-bar :deep(.ant-radio-button-wrapper-checked) {
  background: rgba(var(--accent-rgb), 0.15);
  border-color: var(--accent);
  color: var(--accent);
}

.filter-bar :deep(.ant-radio-button-wrapper::before) {
  display: none;
}

/* 订单列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 16px 16px 20px;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.order-item::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(var(--accent-rgb), 0.03), transparent);
  opacity: 0;
  transition: opacity 0.25s;
}

.order-item:hover {
  border-color: var(--border-glow);
  box-shadow: 0 0 20px rgba(var(--accent-rgb), 0.08), 0 4px 16px rgba(0,0,0,0.15);
  transform: translateY(-2px);
}

.order-item:hover::after {
  opacity: 1;
}

/* 状态指示器 */
.status-indicator {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
}

.status-indicator.blue { background: linear-gradient(180deg, #3b82f6, #60a5fa); }
.status-indicator.cyan { background: linear-gradient(180deg, #06b6d4, #22d3ee); }
.status-indicator.orange { background: linear-gradient(180deg, #f97316, #fb923c); }
.status-indicator.green { background: linear-gradient(180deg, #22c55e, #4ade80); }
.status-indicator.gray { background: linear-gradient(180deg, #6b7280, #9ca3af); }

/* 产品缩略图 */
.order-thumb {
  width: 56px;
  height: 56px;
  min-width: 56px;
  border-radius: 10px;
  background: var(--bg-input);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 22px;
  border: 1px solid var(--border-subtle);
}

.order-item:hover .order-thumb {
  border-color: var(--accent);
  color: var(--accent);
}

/* 订单信息 */
.order-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.order-info-main {
  display: flex;
  align-items: center;
  gap: 10px;
}

.order-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.order-qty {
  font-size: 13px;
  color: var(--text-secondary);
  background: var(--bg-input);
  padding: 2px 8px;
  border-radius: 4px;
}

.order-info-sub {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--text-muted);
}

.order-no {
  display: flex;
  align-items: center;
  gap: 4px;
  font-family: monospace;
}

.order-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 积分信息 */
.order-points {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 70px;
  padding: 0 16px;
}

.points-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--accent-light);
  line-height: 1;
  text-shadow: 0 0 20px rgba(var(--accent-rgb), 0.3);
}

.points-label {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
}

/* 状态标签 */
.status-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  min-width: 90px;
  justify-content: center;
}

.status-tag i {
  font-size: 11px;
}

.status-tag.blue {
  background: rgba(59, 130, 246, 0.12);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.status-tag.cyan {
  background: rgba(6, 182, 212, 0.12);
  color: #22d3ee;
  border: 1px solid rgba(6, 182, 212, 0.2);
}

.status-tag.orange {
  background: rgba(249, 115, 22, 0.12);
  color: #fb923c;
  border: 1px solid rgba(249, 115, 22, 0.2);
}

.status-tag.green {
  background: rgba(34, 197, 94, 0.12);
  color: #4ade80;
  border: 1px solid rgba(34, 197, 94, 0.2);
}

.status-tag.gray {
  background: rgba(156, 163, 175, 0.12);
  color: #9ca3af;
  border: 1px solid rgba(156, 163, 175, 0.2);
}

/* 查看详情 */
.order-action {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  color: var(--text-muted);
  font-size: 12px;
  transition: all 0.2s;
  border-radius: 8px;
  border: 1px solid transparent;
}

.order-action i {
  font-size: 10px;
  transition: transform 0.2s;
}

.order-item:hover .order-action {
  color: var(--accent);
  background: rgba(var(--accent-rgb), 0.08);
  border-color: rgba(var(--accent-rgb), 0.15);
}

.order-item:hover .order-action i {
  transform: translateX(3px);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  width: 90px;
  height: 90px;
  margin: 0 auto 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(var(--accent-rgb), 0.1), rgba(var(--accent-rgb), 0.05));
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border-subtle);
}

.empty-icon i {
  font-size: 36px;
  color: var(--text-muted);
  opacity: 0.6;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.empty-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 20px 0 8px;
  margin-top: 16px;
  border-top: 1px solid var(--border-subtle);
}

/* 响应式 */
@media (max-width: 768px) {
  .order-item {
    flex-wrap: wrap;
    gap: 12px;
    padding: 14px;
    padding-left: 18px;
  }

  .order-info {
    flex: 1 1 calc(100% - 72px);
  }

  .order-points {
    flex-direction: row;
    gap: 6px;
    min-width: auto;
    padding: 0;
  }

  .status-tag {
    min-width: auto;
    padding: 5px 10px;
  }

  .order-action {
    margin-left: auto;
  }
}

@media (max-width: 480px) {
  .order-points {
    display: none;
  }

  .status-tag span {
    display: none;
  }

  .status-tag i {
    font-size: 14px;
  }

  .order-action .action-text {
    display: none;
  }
}

/* ============ 详情弹框样式 ============ */
.cyber-modal :deep(.ant-modal-content) {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius);
  box-shadow: 0 0 40px rgba(0,0,0,0.4);
}
.cyber-modal :deep(.ant-modal-close) { color: var(--text-secondary); }
.cyber-modal :deep(.ant-modal-close:hover) { color: var(--text-primary); }

/* 状态头部 */
.detail-status-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
  margin: -24px -24px 0;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-subtle);
}
.detail-status-header.blue { background: linear-gradient(135deg, rgba(59,130,246,0.12), transparent); }
.detail-status-header.cyan { background: linear-gradient(135deg, rgba(6,182,212,0.12), transparent); }
.detail-status-header.orange { background: linear-gradient(135deg, rgba(249,115,22,0.12), transparent); }
.detail-status-header.green { background: linear-gradient(135deg, rgba(34,197,94,0.12), transparent); }
.detail-status-header.gray { background: linear-gradient(135deg, rgba(156,163,175,0.12), transparent); }

.detail-status-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--bg-input);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  border: 1px solid var(--border-subtle);
}
.detail-status-header.blue .detail-status-icon { color: #60a5fa; border-color: rgba(59,130,246,0.2); }
.detail-status-header.cyan .detail-status-icon { color: #22d3ee; border-color: rgba(6,182,212,0.2); }
.detail-status-header.orange .detail-status-icon { color: #fb923c; border-color: rgba(249,115,22,0.2); }
.detail-status-header.green .detail-status-icon { color: #4ade80; border-color: rgba(34,197,94,0.2); }
.detail-status-header.gray .detail-status-icon { color: #9ca3af; border-color: rgba(156,163,175,0.2); }

.detail-status-info h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px;
}
.detail-status-info p {
  font-size: 12px;
  color: var(--text-muted);
  margin: 0;
  line-height: 1.6;
}

/* 弹框内容 */
.detail-body {
  padding: 20px 0 4px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 商品信息 */
.detail-product {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--bg-input);
  border-radius: var(--radius-xs);
  border: 1px solid var(--border-subtle);
}

.detail-product-icon {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  background: var(--bg-card);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: var(--text-muted);
  border: 1px solid var(--border-subtle);
}

.detail-product-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.detail-product-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--text-secondary);
}

.detail-product-meta i {
  margin-right: 3px;
}

/* 收货地址 */
.detail-address {
  font-size: 13px;
  color: var(--text-primary);
  line-height: 1.6;
  padding: 8px 12px;
  background: var(--bg-input);
  border-radius: var(--radius-xs);
  border: 1px solid var(--border-subtle);
}

/* 物流信息 */
.detail-logistics {
  padding: 8px 12px;
  background: var(--bg-input);
  border-radius: var(--radius-xs);
  border: 1px solid var(--border-subtle);
}
.detail-log-row {
  display: flex;
  font-size: 13px;
  padding: 4px 0;
}
.log-label { color: var(--text-secondary); min-width: 64px; }
.log-value { color: var(--text-primary); }

/* 时间线 */
.detail-timeline {
  padding: 4px 0;
}
.detail-timeline-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 8px 0;
  position: relative;
}
.detail-timeline-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 26px;
  bottom: -8px;
  width: 1px;
  background: var(--border-subtle);
}
.timeline-dot {
  width: 11px;
  height: 11px;
  min-width: 11px;
  border-radius: 50%;
  background: var(--accent);
  margin-top: 3px;
  box-shadow: 0 0 6px rgba(var(--accent-rgb), 0.4);
}
.detail-timeline-item:last-child .timeline-dot {
  background: var(--text-muted);
  box-shadow: none;
}
.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.timeline-time {
  font-size: 12px;
  color: var(--text-muted);
}
.timeline-event {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
}

/* 关闭原因 */
.detail-reason {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  padding: 10px 12px;
  background: var(--bg-input);
  border-radius: var(--radius-xs);
  border: 1px solid var(--border-subtle);
}

/* 操作按钮 */
.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-subtle);
}
</style>
