<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getOrders, confirmOrder, shipOrder, completeOrder } from '@/api/operator'
import { message, Modal } from 'ant-design-vue'
import type { Order } from '@/types/api'
import { OrderStatus, OrderStatusText, OrderStatusClass } from '@/types/enums'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
  loadOrders()
})

const loading = ref(false)
const orders = ref<Order[]>([])
const pagination = ref({ page: 1, size: 10, total: 0 })

const searchQuery = ref('')
const statusFilter = ref<number | undefined>(undefined)

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
    const params: any = { page: pagination.value.page, size: pagination.value.size }
    if (searchQuery.value) params.keyword = searchQuery.value
    if (statusFilter.value !== undefined) params.status = statusFilter.value

    const res = await getOrders(params)
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

function handleSearch() {
  pagination.value.page = 1
  loadOrders()
}

function handleReset() {
  searchQuery.value = ''
  statusFilter.value = undefined
  pagination.value.page = 1
  loadOrders()
}

function handleStatusChange(status: number | undefined) {
  statusFilter.value = status
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

// 确认订单
async function handleConfirm(order: Order) {
  Modal.confirm({
    title: '确认订单',
    content: `确定要确认订单 "${order.orderNo}" 吗？`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
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

// 发货
function openShipModal(order: Order) {
  currentOrder.value = order
  shipForm.value = { carrier: '', trackingNo: '' }
  shipVisible.value = true
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
  } finally {
    shipLoading.value = false
  }
}

// 完成订单
async function handleComplete(order: Order) {
  Modal.confirm({
    title: '完成订单',
    content: `确定将订单 "${order.orderNo}" 标记为已完成吗？`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
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
      </div>

      <!-- Search Bar -->
      <div class="search-bar">
        <div class="inner">
          <input
            v-model="searchQuery"
            class="cyber-input"
            type="text"
            placeholder="搜索订单号..."
            style="flex:1;max-width:220px;"
            @keyup.enter="handleSearch"
          />
          <select v-model="statusFilter" class="cyber-input" style="max-width:130px;cursor:pointer;" @change="handleStatusChange(statusFilter)">
            <option :value="undefined">全部状态</option>
            <option :value="OrderStatus.PENDING">待确认</option>
            <option :value="OrderStatus.CONFIRMED">已确认</option>
            <option :value="OrderStatus.SHIPPED">已发货</option>
            <option :value="OrderStatus.COMPLETED">已完成</option>
            <option :value="OrderStatus.CLOSED">已关闭</option>
          </select>
          <button class="cyber-btn-primary" style="padding:9px 16px;" @click="handleSearch">
            <i class="fas fa-search" style="margin-right:5px;"></i>搜索
          </button>
          <button class="cyber-btn" style="padding:9px 16px;" @click="handleReset">
            <i class="fas fa-undo"></i>
          </button>
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
                <td><strong>{{ order.productName }}</strong></td>
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
                    <template v-if="order.status === OrderStatus.PENDING">
                      <button class="action-btn green" title="确认" @click="handleConfirm(order)">
                        <i class="fas fa-check"></i>
                      </button>
                    </template>
                    <template v-if="order.status === OrderStatus.CONFIRMED">
                      <button class="action-btn purple" title="发货" @click="openShipModal(order)">
                        <i class="fas fa-truck"></i>
                      </button>
                    </template>
                    <template v-if="order.status === OrderStatus.SHIPPED">
                      <button class="action-btn green" title="完成" @click="handleComplete(order)">
                        <i class="fas fa-flag-checkered"></i>
                      </button>
                    </template>
                  </div>
                </td>
              </tr>
              <tr v-if="orders.length === 0 && !loading">
                <td colspan="8" class="empty-cell">
                  <i class="fas fa-receipt" style="font-size:36px;opacity:0.25;"></i>
                  <p>暂无订单记录</p>
                  <span style="font-size:12px;opacity:0.5;margin-top:4px;">当前没有待处理的订单</span>
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
        <button class="page-btn" :disabled="pagination.page <= 1" @click="pagination.page--; loadOrders()">
          <i class="fas fa-chevron-left"></i>
        </button>
        <button class="page-btn active">{{ pagination.page }}</button>
        <button class="page-btn" :disabled="pagination.page * pagination.size >= pagination.total" @click="pagination.page++; loadOrders()">
          <i class="fas fa-chevron-right"></i>
        </button>
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
#page-order-manage {
  min-height: 100vh;
  position: relative;
}

.id-cell {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--accent);
}

.time-cell {
  font-size: 12px;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

.points-cell {
  color: var(--accent-light);
  font-weight: 600;
  text-shadow: var(--accent-glow-text);
}

.action-btn.green {
  color: var(--green, #22c55e);
}

.action-btn.green:hover {
  background: rgba(16, 185, 129, 0.1);
}

.action-btn.red:hover {
  background: rgba(239, 68, 68, 0.1);
}

.action-btn.purple {
  color: #a855f7;
}

.action-btn.purple:hover {
  border-color: #a855f7;
  color: #a855f7;
  box-shadow: 0 0 15px rgba(168, 85, 247, 0.25);
}

.empty-cell {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-muted);
}

.empty-cell i {
  display: block;
  margin-bottom: 10px;
}

.empty-cell p {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}
</style>