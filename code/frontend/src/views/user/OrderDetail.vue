<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { getOrder, closeOrder, completeOrder } from '@/api/user'
import { message } from 'ant-design-vue'
import type { Order } from '@/types/api'

const themeStore = useThemeStore()
const router = useRouter()
const route = useRoute()

onMounted(() => {
  themeStore.init()
  loadOrderDetail()
})

const loading = ref(false)
const order = ref<Order | null>(null)

// 订单ID（雪花ID转String）
const orderId = computed(() => String(route.params.id))

// 状态信息映射
const statusInfo = computed(() => {
  const map: Record<number, { text: string; tagClass: string; icon: string }> = {
    1: { text: '已下单', tagClass: 'blue', icon: 'fa-clock' },
    2: { text: '已确认', tagClass: 'cyan', icon: 'fa-check' },
    3: { text: '已发货', tagClass: 'orange', icon: 'fa-truck' },
    4: { text: '已完成', tagClass: 'green', icon: 'fa-check-double' },
    5: { text: '已关闭', tagClass: 'gray', icon: 'fa-times-circle' },
  }
  return map[order.value?.status || 0] || { text: '未知', tagClass: 'gray', icon: 'fa-question' }
})

// 是否可取消订单（状态1可取消）
const canCancel = computed(() => order.value?.status === 1)

// 是否可确认收货（状态3可确认）
const canComplete = computed(() => order.value?.status === 3)

async function loadOrderDetail() {
  loading.value = true
  try {
    const res = await getOrder(orderId.value)
    order.value = {
      ...res,
      id: String(res.id)
    }
  } catch (e) {
    throw e
  }
}

// 取消订单
async function handleCancel() {
  if (!order.value) return
  try {
    await closeOrder(orderId.value)
    message.success('订单已取消')
    loadOrderDetail()
  } catch (e) {
    throw e
  }
}

// 确认收货
async function handleComplete() {
  if (!order.value) return
  try {
    await completeOrder(orderId.value)
    message.success('已确认收货')
    loadOrderDetail()
  } catch (e) {
    throw e
  }
}

// 返回订单列表
function goBack() {
  router.push({ name: 'UserOrderList' })
}

// 格式化日期
function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 格式化地址
function formatAddress(addr?: Order['addressInfo']) {
  if (!addr) return '-'
  return `${addr.province}${addr.city}${addr.district}${addr.detail} ${addr.receiver} ${addr.phone}`
}
</script>

<template>
  <div id="page-order-detail">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <div class="page-head">
        <h2><span class="accent-line"></span>订单详情</h2>
        <a-button type="text" class="back-btn" @click="goBack">
          <i class="fas fa-arrow-left"></i> 返回列表
        </a-button>
      </div>

      <div v-if="loading" class="cyber-card">
        <div style="text-align:center;padding:60px 20px;">
          <i class="fas fa-spinner fa-spin" style="font-size:32px;color:var(--accent);"></i>
          <p style="margin-top:16px;color:var(--text-secondary);">加载中...</p>
        </div>
      </div>

      <template v-else-if="order">
        <!-- 订单状态卡片 -->
        <div class="cyber-card status-card">
          <div class="status-header">
            <div class="status-icon">
              <i :class="['fas', statusInfo.icon]"></i>
            </div>
            <div class="status-info">
              <h3>{{ statusInfo.text }}</h3>
              <p>订单号：{{ order.orderNo }}</p>
              <p>下单时间：{{ formatDate(order.createdAt) }}</p>
            </div>
          </div>
          <div class="status-actions">
            <a-button v-if="canCancel" type="primary" danger @click="handleCancel">
              <i class="fas fa-times"></i> 取消订单
            </a-button>
            <a-button v-if="canComplete" type="primary" success @click="handleComplete">
              <i class="fas fa-check"></i> 确认收货
            </a-button>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="cyber-card">
          <div class="card-title">
            <i class="fas fa-box"></i> 商品信息
          </div>
          <div class="product-info">
            <div class="product-name">{{ order.productName }}</div>
            <div class="product-meta">
              <span class="price">
                <i class="fas fa-coins"></i> {{ order.totalPoints }} 积分
              </span>
              <span class="quantity">x {{ order.quantity }}</span>
            </div>
          </div>
        </div>

        <!-- 收货地址 -->
        <div class="cyber-card">
          <div class="card-title">
            <i class="fas fa-map-marker-alt"></i> 收货地址
          </div>
          <div class="address-info">
            {{ formatAddress(order.addressInfo) }}
          </div>
        </div>

        <!-- 物流信息 -->
        <div class="cyber-card" v-if="order.trackingNo">
          <div class="card-title">
            <i class="fas fa-truck"></i> 物流信息
          </div>
          <div class="logistics-info">
            <div class="logistics-item">
              <span class="label">承运商：</span>
              <span class="value">{{ order.carrier || '-' }}</span>
            </div>
            <div class="logistics-item">
              <span class="label">运单号：</span>
              <span class="value">{{ order.trackingNo }}</span>
            </div>
          </div>
        </div>

        <!-- 订单时间线 -->
        <div class="cyber-card">
          <div class="card-title">
            <i class="fas fa-clock"></i> 订单时间
          </div>
          <div class="timeline">
            <div class="timeline-item">
              <span class="time">{{ formatDate(order.createdAt) }}</span>
              <span class="event">创建订单</span>
            </div>
            <div class="timeline-item" v-if="order.updatedAt && order.status !== 1">
              <span class="time">{{ formatDate(order.updatedAt) }}</span>
              <span class="event">
                {{ order.status === 5 ? '订单关闭' : order.status === 4 ? '交易完成' : '状态更新' }}
              </span>
            </div>
          </div>
        </div>

        <!-- 关闭原因 -->
        <div class="cyber-card" v-if="order.reason">
          <div class="card-title">
            <i class="fas fa-info-circle"></i> {{ order.status === 5 ? '关闭' : '备注' }}信息
          </div>
          <div class="reason-info">
            {{ order.reason }}
          </div>
        </div>
      </template>

      <div v-else class="cyber-card">
        <div style="text-align:center;padding:60px 20px;color:var(--text-secondary);">
          <i class="fas fa-exclamation-triangle" style="font-size:48px;margin-bottom:16px;opacity:0.5;"></i>
          <p style="font-size:16px;">订单不存在或已删除</p>
          <a-button type="link" @click="goBack">返回列表</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#page-order-detail {
  min-height: 100vh;
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-head h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  color: var(--text-secondary);
}

.back-btn:hover {
  color: var(--accent);
}

.cyber-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
  backdrop-filter: blur(10px);
}

.status-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, rgba(99,102,241,0.1) 0%, rgba(236,72,153,0.05) 100%);
}

.status-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.status-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  box-shadow: var(--accent-glow);
}

.status-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
}

.status-info p {
  margin: 4px 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.status-actions {
  display: flex;
  gap: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--accent);
}

.product-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
}

.product-meta {
  text-align: right;
}

.price {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: var(--accent);
}

.quantity {
  color: var(--text-secondary);
}

.address-info,
.logistics-info,
.reason-info {
  color: var(--text-primary);
  line-height: 1.6;
}

.logistics-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.logistics-item {
  display: flex;
  gap: 8px;
}

.logistics-item .label {
  color: var(--text-secondary);
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.timeline-item {
  display: flex;
  gap: 16px;
  padding-left: 20px;
  position: relative;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent);
}

.timeline-item::after {
  content: '';
  position: absolute;
  left: 3px;
  top: 20px;
  width: 2px;
  height: calc(100% - 8px);
  background: var(--border-color);
}

.timeline-item:last-child::after {
  display: none;
}

.timeline-item .time {
  color: var(--text-secondary);
  font-size: 13px;
  min-width: 160px;
}

.timeline-item .event {
  color: var(--text-primary);
}

/* 按钮样式 */
:deep(.ant-btn) {
  height: 36px;
  padding: 0 20px;
  border-radius: 8px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

:deep(.ant-btn-primary) {
  background: var(--accent);
  border: none;
}

:deep(.ant-btn-primary:hover) {
  background: var(--accent-dark);
  box-shadow: var(--accent-glow-hover);
}

:deep(.ant-btn-primary.danger) {
  background: #ef4444;
}

:deep(.ant-btn-primary.danger:hover) {
  background: #dc2626;
}

:deep(.ant-btn-primary[success]) {
  background: #22c55e;
}

:deep(.ant-btn-primary[success]:hover) {
  background: #16a34a;
}
</style>