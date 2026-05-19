<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { confirmOrder, shipOrder, completeOrder, closeOrder } from '@/api/modules/operator'
import { message, Modal } from 'ant-design-vue'
import type { Order } from '@/types/api'
import { OrderStatusText, OrderStatusClass } from '@/types/enums'
import CyberPagination from '@/components/CyberPagination.vue'
import OrderDetailModal from '@/components/operator/OrderDetailModal.vue'
import OrderShipModal from '@/components/operator/OrderShipModal.vue'
import { useOrderSearch } from '@/composables/useOrderSearch'

const themeStore = useThemeStore()
const {
  orders,
  loading,
  searchParams,
  statusOptions,
  pagination,
  page,
  pageSize,
  total,
  loadOrders,
  handleSearch,
  handleReset,
  handleStatusChange,
  onPageChange,
} = useOrderSearch()

onMounted(() => {
  themeStore.init()
  loadOrders()
})

const searchQuery = ref('')

const detailVisible = ref(false)
const currentOrder = ref<Order | null>(null)

const shipVisible = ref(false)
const shipOrderData = ref<Order | null>(null)

function handleSearchWrapper() {
  searchParams.value.keyword = searchQuery.value
  handleSearch()
}

function handleResetWrapper() {
  searchQuery.value = ''
  handleReset()
}

function handleStatusChangeWrapper(status: number | undefined) {
  handleStatusChange(status)
}

function handlePageChangeWrapper(p: number) {
  onPageChange(p)
  loadOrders()
}

function viewDetail(order: Order) {
  currentOrder.value = order
  detailVisible.value = true
}

function closeDetail() {
  detailVisible.value = false
  currentOrder.value = null
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
function handleConfirm(order: Order) {
  Modal.confirm({
    title: '确认订单',
    content: `确定要确认订单 "${order.orderNo}" 吗？`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      await confirmOrder(order.id)
      message.success('订单已确认')
      loadOrders()
    },
  })
}

// 打开发货弹框
function openShipModal(order: Order) {
  shipOrderData.value = order
  shipVisible.value = true
}

function closeShipModal() {
  shipVisible.value = false
  shipOrderData.value = null
}

// 发货
async function handleShipOrder(orderId: string, trackingNo: string, carrier: string) {
  await shipOrder(orderId, trackingNo, carrier)
  message.success('已发货')
  closeShipModal()
  loadOrders()
}

// 完成订单
function handleComplete(order: Order) {
  Modal.confirm({
    title: '完成订单',
    content: `确定将订单 "${order.orderNo}" 标记为已完成吗？`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      await completeOrder(order.id)
      message.success('订单已完成')
      loadOrders()
    },
  })
}

// 关闭订单
function handleClose(order: Order) {
  Modal.confirm({
    title: '关闭订单',
    content: `确定要关闭订单 "${order.orderNo}" 吗？关闭后积分将退回用户。`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      await closeOrder(order.id, '店铺关闭订单')
      message.success('订单已关闭，积分已退回用户')
      closeDetail()
      loadOrders()
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
            @keyup.enter="handleSearchWrapper"
          />
          <select v-model="searchParams.status" class="cyber-input" style="max-width:130px;cursor:pointer;" @change="handleStatusChangeWrapper(searchParams.status)">
            <option v-for="opt in statusOptions" :key="String(opt.value)" :value="opt.value">{{ opt.label }}</option>
          </select>
          <button class="cyber-btn-primary" style="padding:9px 16px;" @click="handleSearchWrapper">
            <i class="fas fa-search" style="margin-right:5px;"></i>搜索
          </button>
          <button class="cyber-btn" style="padding:9px 16px;" @click="handleResetWrapper">
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
                      <button class="action-btn red" title="关闭" @click="handleClose(order)">
                        <i class="fas fa-times"></i>
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
      <div class="pagination-wrap" v-if="total > 0">
        <CyberPagination
          v-model:current="page"
          v-model:pageSize="pageSize"
          :total="total"
          @change="loadOrders"
        />
      </div>
    </div>

    <!-- 订单详情弹框 -->
    <OrderDetailModal
      v-model:visible="detailVisible"
      :order="currentOrder"
      @confirm="handleConfirm"
      @close="handleClose"
      @ship="openShipModal"
      @complete="handleComplete"
    />

    <!-- 发货弹框 -->
    <OrderShipModal
      v-model:visible="shipVisible"
      :order="shipOrderData"
      @ship="handleShipOrder"
    />
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