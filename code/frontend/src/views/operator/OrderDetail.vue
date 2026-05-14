<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { useOperatorShop } from '@/composables/useOperatorShop'
import { getOrder, confirmOrder, shipOrder, completeOrder } from '@/api/operator'
import { OrderStatus, OrderStatusText, OrderStatusClass } from '@/types/enums'
import { Modal, message } from 'ant-design-vue'
import type { Order } from '@/types/api'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()
const { isApproved } = useOperatorShop()

const loading = ref(false)
const order = ref<Order | null>(null)
const error = ref<string | null>(null)

// 物流弹框
const shipVisible = ref(false)
const shipForm = ref({ trackingNo: '', carrier: '' })

onMounted(() => {
  themeStore.init()
  const orderId = route.params.orderId as string
  if (orderId) {
    loadOrderDetail(orderId)
  } else {
    error.value = '缺少订单ID参数'
  }
})

async function loadOrderDetail(orderId: string) {
  loading.value = true
  error.value = null
  try {
    const res = await getOrder(orderId)
    order.value = { ...res, id: String(res.id) }
  } catch (e: any) {
    error.value = e?.message || '加载订单详情失败'
  } finally {
    loading.value = false
  }
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

function getStatusInfo(status: number) {
  return {
    text: OrderStatusText[status] || '未知',
    class: OrderStatusClass[status] || 'gray',
  }
}

// 操作按钮显示逻辑
const canConfirm = computed(() => order.value?.status === OrderStatus.PENDING && isApproved())
const canShip = computed(() => order.value?.status === OrderStatus.CONFIRMED && isApproved())
const canComplete = computed(() => order.value?.status === OrderStatus.SHIPPED && isApproved())

function handleConfirm() {
  if (!order.value) return
  Modal.confirm({
    title: '确认订单',
    content: `确定要确认订单「${order.value.orderNo}」吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await confirmOrder(order.value!.id)
        message.success('订单已确认')
        loadOrderDetail(order.value!.id)
      } catch (e: any) {
        message.error(e?.message || '操作失败')
      }
    }
  })
}

function handleShowShip() {
  shipForm.value = { trackingNo: '', carrier: '' }
  shipVisible.value = true
}

function handleShip() {
  if (!order.value || !shipForm.value.trackingNo.trim()) {
    message.error('请填写物流单号')
    return
  }
  Modal.confirm({
    title: '填写物流',
    content: `确定要发货吗？物流单号：${shipForm.value.trackingNo}`,
    okText: '确认发货',
    cancelText: '取消',
    onOk: async () => {
      try {
        await shipOrder(
          order.value!.id,
          shipForm.value.trackingNo.trim(),
          shipForm.value.carrier.trim() || undefined
        )
        message.success('订单已发货')
        shipVisible.value = false
        loadOrderDetail(order.value!.id)
      } catch (e: any) {
        message.error(e?.message || '操作失败')
      }
    }
  })
}

function handleComplete() {
  if (!order.value) return
  Modal.confirm({
    title: '确认收货',
    content: `确定要确认收货订单「${order.value.orderNo}」吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await completeOrder(order.value!.id)
        message.success('订单已完成')
        loadOrderDetail(order.value!.id)
      } catch (e: any) {
        message.error(e?.message || '操作失败')
      }
    }
  })
}

function goBack() {
  router.push('/operator/order-manage')
}
</script>

<template>
  <div id="page-order-detail">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <!-- Page Head -->
      <div class="page-head">
        <h2><span class="accent-line"></span>订单详情</h2>
        <div class="actions">
          <button class="cyber-btn" @click="goBack">
            <i class="fas fa-arrow-left" style="margin-right:5px;"></i>返回
          </button>
          <button class="cyber-btn" @click="loadOrderDetail(order!.id)" v-if="order">
            <i class="fas fa-sync-alt" style="margin-right:5px;"></i>刷新
          </button>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="cyber-card" style="text-align:center;padding:60px;">
        <i class="fas fa-spinner fa-spin" style="font-size:32px;color:var(--accent);"></i>
        <p style="margin-top:16px;color:var(--text-secondary);">加载中...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="cyber-card" style="text-align:center;padding:60px;">
        <i class="fas fa-exclamation-triangle" style="font-size:48px;color:var(--red);opacity:0.5;"></i>
        <p style="margin-top:16px;color:var(--text-secondary);">{{ error }}</p>
        <button class="cyber-btn" style="margin-top:20px;" @click="goBack">返回列表</button>
      </div>

      <!-- Order Detail -->
      <div v-else-if="order" class="cyber-card">
        <div class="detail-grid">
          <div class="detail-item">
            <label>订单号</label>
            <span class="id-cell">{{ order.orderNo }}</span>
          </div>
          <div class="detail-item">
            <label>订单状态</label>
            <span class="status-tag" :class="getStatusInfo(order.status).class">
              <span class="dot"></span>{{ getStatusInfo(order.status).text }}
            </span>
          </div>
          <div class="detail-item">
            <label>用户</label>
            <span>{{ order.userNickname || order.userId }}</span>
          </div>
          <div class="detail-item">
            <label>商品</label>
            <span>{{ order.productName }}</span>
          </div>
          <div class="detail-item">
            <label>数量</label>
            <span>x{{ order.quantity }}</span>
          </div>
          <div class="detail-item">
            <label>总积分</label>
            <span class="points-value">{{ order.totalPoints }}</span>
          </div>
          <div class="detail-item" v-if="order.trackingNo">
            <label>物流单号</label>
            <span>{{ order.trackingNo }}</span>
          </div>
          <div class="detail-item" v-if="order.carrier">
            <label>物流公司</label>
            <span>{{ order.carrier }}</span>
          </div>
          <div class="detail-item" v-if="order.reason">
            <label>关闭原因</label>
            <span class="text-red">{{ order.reason }}</span>
          </div>
          <div class="detail-item full-width">
            <label>下单时间</label>
            <span>{{ formatDate(order.createdAt) }}</span>
          </div>
        </div>

        <!-- Actions -->
        <div class="detail-actions" v-if="canConfirm || canShip || canComplete">
          <button v-if="canConfirm" class="cyber-btn primary" @click="handleConfirm">
            <i class="fas fa-check" style="margin-right:5px;"></i>确认订单
          </button>
          <button v-if="canShip" class="cyber-btn primary" @click="handleShowShip">
            <i class="fas fa-truck" style="margin-right:5px;"></i>填写物流发货
          </button>
          <button v-if="canComplete" class="cyber-btn primary" @click="handleComplete">
            <i class="fas fa-check-double" style="margin-right:5px;"></i>确认收货
          </button>
        </div>
      </div>
    </div>

    <!-- Ship Modal -->
    <div class="modal-overlay" v-if="shipVisible" @click.self="shipVisible = false">
      <div class="modal-card">
        <div class="modal-header">
          <h3><span class="accent-line"></span>填写物流</h3>
          <button class="modal-close" @click="shipVisible = false">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-item">
            <label>物流单号 <span class="required">*</span></label>
            <input
              v-model="shipForm.trackingNo"
              type="text"
              class="cyber-input"
              placeholder="请输入物流单号"
            />
          </div>
          <div class="form-item">
            <label>物流公司</label>
            <input
              v-model="shipForm.carrier"
              type="text"
              class="cyber-input"
              placeholder="请输入物流公司（选填）"
            />
          </div>
          <div class="form-actions">
            <button class="cyber-btn" @click="shipVisible = false">取消</button>
            <button class="cyber-btn primary" @click="handleShip">确认发货</button>
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
  padding: 30px 40px;
  max-width: 900px;
  margin: 0 auto;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-head h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 12px;
}

.accent-line {
  display: inline-block;
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, var(--accent), #8b5cf6);
  border-radius: 2px;
}

.actions {
  display: flex;
  gap: 12px;
}

.cyber-btn {
  display: inline-flex;
  align-items: center;
  padding: 10px 20px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.cyber-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.cyber-btn.primary {
  background: var(--accent);
  border-color: var(--accent);
  color: white;
}

.cyber-btn.primary:hover {
  background: #7c3aed;
  border-color: #7c3aed;
  color: white;
}

.cyber-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 16px;
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

.id-cell {
  font-family: monospace;
  color: var(--text-secondary);
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.status-tag.orange { color: #f59e0b; background: rgba(245, 158, 11, 0.1); }
.status-tag.blue { color: #3b82f6; background: rgba(59, 130, 246, 0.1); }
.status-tag.purple { color: #8b5cf6; background: rgba(139, 92, 246, 0.1); }
.status-tag.cyan { color: #06b6d4; background: rgba(6, 182, 212, 0.1); }
.status-tag.green { color: #10b981; background: rgba(16, 185, 129, 0.1); }
.status-tag.gray { color: #9ca3af; background: rgba(156, 163, 175, 0.1); }

.detail-actions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}

.required {
  color: var(--red);
}

.cyber-input {
  width: 100%;
  padding: 10px 14px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.cyber-input:focus {
  outline: none;
  border-color: var(--accent);
}

.cyber-input::placeholder {
  color: var(--text-muted);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}
</style>