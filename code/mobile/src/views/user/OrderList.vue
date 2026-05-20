<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { getOrders, getOrder } from '@/api/user'
import type { Order } from '@/types'
import { OrderStatusText, OrderStatus } from '@/types/enums'

const orders = ref<Order[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const size = ref(10)
const keyword = ref('')

const statusTabs = [
  { text: '全部', value: undefined },
  { text: '已下单', value: OrderStatus.PENDING },
  { text: '已确认', value: OrderStatus.CONFIRMED },
  { text: '已发货', value: OrderStatus.SHIPPED },
  { text: '已完成', value: OrderStatus.COMPLETED },
  { text: '已关闭', value: OrderStatus.CLOSED },
]

const activeStatus = ref<number | undefined>(undefined)

const showOrderDetail = ref(false)
const selectedOrder = ref<any>(null)
const detailLoading = ref(false)

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getOrders({ page: page.value, size: size.value, status: activeStatus.value, keyword: keyword.value || undefined })
    if (page.value === 1) orders.value = res.list
    else orders.value.push(...res.list)
    if (res.list.length < size.value) finished.value = true
    page.value++
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  orders.value = []
  page.value = 1
  finished.value = false
  fetchOrders()
}

function onLoad() {
  if (!loading.value && !finished.value) fetchOrders()
}

function onStatusChange(status: number | undefined) {
  activeStatus.value = status
  orders.value = []
  page.value = 1
  finished.value = false
  fetchOrders()
}

async function openOrderDetail(o: Order) {
  detailLoading.value = true
  showOrderDetail.value = true
  try {
    selectedOrder.value = await getOrder(o.id)
  } catch {
    showToast('加载失败')
  } finally {
    detailLoading.value = false
  }
}

function getStatusClass(status: number) {
  switch (status) {
    case OrderStatus.PENDING: return 'status-pending'
    case OrderStatus.CONFIRMED: return 'status-confirmed'
    case OrderStatus.SHIPPED: return 'status-shipped'
    case OrderStatus.COMPLETED: return 'status-completed'
    case OrderStatus.CLOSED: return 'status-closed'
    default: return ''
  }
}

function getTypeText(type: number) {
  return type === 1 ? '虚拟' : '实物'
}

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="order-list-page">
    <van-nav-bar title="我的订单" />
    <div class="search-bar">
      <van-search v-model="keyword" placeholder="搜索订单号/商品名称" @search="onSearch" />
    </div>
    <div class="content">
      <van-tabs v-model:active="activeStatus" @change="onStatusChange" shrink sticky>
        <van-tab v-for="tab in statusTabs" :key="tab.value ?? 'all'" :title="tab.text" :name="tab.value">
          <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
            <div v-for="o in orders" :key="o.id" class="order-card" @click="openOrderDetail(o)">
              <div class="order-top">
                <div class="order-header">
                  <span class="order-no">订单号：{{ o.orderNo }}</span>
                  <span class="status-tag" :class="getStatusClass(o.status)">{{ OrderStatusText[o.status] }}</span>
                </div>
                <div class="order-product">
                  <span class="product-name">{{ o.productName }}</span>
                  <span class="product-qty">× {{ o.quantity }}</span>
                </div>
                <div class="order-bottom">
                  <span class="order-time">{{ o.createdAt?.slice(0, 16).replace('T', ' ') }}</span>
                  <span class="order-points">{{ o.totalPoints }}积分</span>
                </div>
              </div>
            </div>
            <van-empty v-if="!loading && orders.length === 0" description="暂无订单" />
          </van-list>
        </van-tab>
      </van-tabs>
    </div>

    <!-- 订单详情弹窗 -->
    <van-popup v-model:show="showOrderDetail" position="bottom" round style="height: 70%">
      <div v-if="detailLoading" class="detail-loading">
        <van-loading>加载中...</van-loading>
      </div>
      <div v-else-if="selectedOrder" class="detail-panel">
        <div class="panel-title">订单详情</div>
        <div class="detail-content">
          <div class="detail-section">
            <div class="section-title">订单信息</div>
            <div class="info-row">
              <span class="label">订单号</span>
              <span class="value">{{ selectedOrder.orderNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">状态</span>
              <span class="status-tag" :class="getStatusClass(selectedOrder.status)">{{ OrderStatusText[selectedOrder.status] }}</span>
            </div>
            <div class="info-row">
              <span class="label">下单时间</span>
              <span class="value">{{ selectedOrder.createdAt?.slice(0, 16).replace('T', ' ') }}</span>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">商品信息</div>
            <div class="info-row">
              <span class="label">商品名称</span>
              <span class="value">{{ selectedOrder.productName }}</span>
            </div>
            <div class="info-row">
              <span class="label">商品类型</span>
              <span class="value">{{ getTypeText(selectedOrder.productType) }}</span>
            </div>
            <div class="info-row">
              <span class="label">兑换数量</span>
              <span class="value">× {{ selectedOrder.quantity }}</span>
            </div>
            <div class="info-row">
              <span class="label">商品积分</span>
              <span class="value accent">{{ selectedOrder.price }}积分/件</span>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">积分信息</div>
            <div class="info-row">
              <span class="label">总积分</span>
              <span class="value accent big">{{ selectedOrder.totalPoints }}积分</span>
            </div>
          </div>

          <div v-if="selectedOrder.address" class="detail-section">
            <div class="section-title">收货地址</div>
            <div class="info-row">
              <span class="label">收货人</span>
              <span class="value">{{ selectedOrder.address.receiver }}</span>
            </div>
            <div class="info-row">
              <span class="label">电话</span>
              <span class="value">{{ selectedOrder.address.phone }}</span>
            </div>
            <div class="info-row">
              <span class="label">地址</span>
              <span class="value">{{ selectedOrder.address.province }} {{ selectedOrder.address.city }} {{ selectedOrder.address.district }} {{ selectedOrder.address.detail }}</span>
            </div>
          </div>

          <div v-if="selectedOrder.remark" class="detail-section">
            <div class="section-title">备注</div>
            <div class="info-row">
              <span class="value">{{ selectedOrder.remark }}</span>
            </div>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.order-list-page {
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
}

.content {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.search-bar {
  background: var(--bg-card);
  padding: 0 12px;
}

.order-card {
  margin: 10px 12px;
  background: var(--bg-card);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
}

.order-top { padding: 14px; }

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.order-no {
  font-size: 12px;
  color: var(--text-muted);
}

.status-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.status-pending { background: #fef3c7; color: #d97706; }
.status-confirmed { background: #dbeafe; color: #2563eb; }
.status-shipped { background: #f3e8ff; color: #9333ea; }
.status-completed { background: #dcfce7; color: #16a34a; }
.status-closed { background: #f5f5f5; color: #8b8ba7; }

.order-product {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-qty {
  font-size: 13px;
  color: var(--text-muted);
  margin-left: 8px;
}

.order-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-time {
  font-size: 12px;
  color: var(--text-muted);
}

.order-points {
  font-size: 14px;
  font-weight: 700;
  color: var(--accent);
}

/* 详情弹窗 */
.detail-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px 16px;
}

.detail-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
}

.detail-content {
  flex: 1;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 16px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
  margin-bottom: 8px;
  padding-bottom: 4px;
  border-bottom: 1px solid var(--border-subtle);
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 8px 0;
  font-size: 14px;
}

.info-row .label {
  color: var(--text-muted);
  flex-shrink: 0;
}

.info-row .value {
  color: var(--text-primary);
  text-align: right;
}

.info-row .value.accent {
  color: var(--accent);
  font-weight: 600;
}

.info-row .value.big {
  font-size: 18px;
  font-weight: 700;
}
</style>