<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { getOrders, confirmOrder, shipOrder, closeOrder } from '@/api/operator'
import type { Order } from '@/types'
import { OrderStatusText, OrderStatus } from '@/types/enums'

const orders = ref<Order[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const size = ref(10)
const keyword = ref('')

const showShipDialog = ref(false)
const shipOrderId = ref('')
const expressNo = ref('')
const expressCompany = ref('')

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getOrders({ page: page.value, size: size.value, keyword: keyword.value })
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

async function handleConfirm(o: Order) {
  try {
    await showConfirmDialog({ title: '确认订单', message: `确认订单 ${o.orderNo}？` })
    await confirmOrder(o.id)
    showToast('已确认')
    refreshList()
  } catch { /* cancelled */ }
}

function openShipDialog(o: Order) {
  shipOrderId.value = o.id
  expressNo.value = ''
  expressCompany.value = ''
  showShipDialog.value = true
}

async function handleShip() {
  if (!expressNo.value.trim()) {
    showToast('请输入快递单号')
    return
  }
  try {
    await shipOrder(shipOrderId.value, { expressNo: expressNo.value, expressCompany: expressCompany.value })
    showToast('已发货')
    showShipDialog.value = false
    refreshList()
  } catch {
    showToast('发货失败')
  }
}

async function handleClose(o: Order) {
  try {
    await showConfirmDialog({ title: '关闭订单', message: `确定关闭订单 ${o.orderNo}？` })
    await closeOrder(o.id)
    showToast('已关闭')
    refreshList()
  } catch { /* cancelled */ }
}

function refreshList() {
  orders.value = []
  page.value = 1
  finished.value = false
  fetchOrders()
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

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="order-manage-page">
    <van-nav-bar title="订单管理" />
    <div class="search-bar">
      <van-search v-model="keyword" placeholder="搜索订单号/商品名称" @search="onSearch" />
    </div>
    <div class="content">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div v-for="o in orders" :key="o.id" class="order-card">
          <div class="order-top">
            <div class="order-header">
              <span class="order-no">{{ o.orderNo }}</span>
              <span class="status-tag" :class="getStatusClass(o.status)">{{ OrderStatusText[o.status] }}</span>
            </div>
            <div class="order-product">
              <span class="product-name">{{ o.productName }}</span>
              <span class="product-qty">× {{ o.quantity }}</span>
            </div>
            <div class="order-info">
              <span class="points">{{ o.totalPoints }}积分</span>
              <span v-if="o.expressNo" class="express">{{ o.expressCompany || '快递' }}：{{ o.expressNo }}</span>
            </div>
          </div>
          <div class="order-actions">
            <van-button v-if="o.status === OrderStatus.PENDING" size="small" class="action-btn btn-primary" @click="handleConfirm(o)">确认</van-button>
            <van-button v-if="o.status === OrderStatus.CONFIRMED" size="small" class="action-btn btn-primary" @click="openShipDialog(o)">发货</van-button>
            <van-button v-if="o.status !== OrderStatus.COMPLETED && o.status !== OrderStatus.CLOSED" size="small" class="action-btn btn-danger" @click="handleClose(o)">关闭</van-button>
          </div>
        </div>
        <van-empty v-if="!loading && orders.length === 0" description="暂无订单" />
      </van-list>
    </div>

    <van-popup v-model:show="showShipDialog" position="bottom" round>
      <div class="ship-dialog">
        <div class="dialog-title">填写物流信息</div>
        <van-field v-model="expressCompany" label="快递公司" placeholder="请输入快递公司" />
        <van-field v-model="expressNo" label="快递单号" placeholder="请输入快递单号" />
        <div class="dialog-actions">
          <van-button size="small" type="default" round @click="showShipDialog = false">取消</van-button>
          <van-button size="small" type="primary" round @click="handleShip">确认发货</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.order-manage-page {
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
  margin-bottom: 8px;
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

.order-info {
  display: flex;
  gap: 12px;
  font-size: 12px;
}

.points {
  color: var(--accent);
  font-weight: 600;
}

.express {
  color: var(--text-muted);
}

.order-actions {
  display: flex;
  border-top: 1px solid var(--border-subtle);
}

.action-btn {
  flex: 1;
  border: none;
  border-radius: 0;
  font-size: 12px;
  height: 36px;
}

.action-btn:not(:last-child) {
  border-right: 1px solid var(--border-subtle);
}

.btn-primary { color: var(--accent); }
.btn-danger { color: #ef4444; background: #fff5f5; }

.ship-dialog {
  padding: 20px 16px;
}

.dialog-title {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
}

.dialog-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}

.dialog-actions .van-button {
  flex: 1;
}
</style>