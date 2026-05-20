<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getOrders } from '@/api/user'
import type { Order } from '@/types'
import { OrderStatusText, OrderStatus } from '@/types/enums'

const router = useRouter()

const orders = ref<Order[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const size = ref(10)

const statusTabs = [
  { text: '全部', value: undefined },
  { text: '已下单', value: OrderStatus.PENDING },
  { text: '已确认', value: OrderStatus.CONFIRMED },
  { text: '已发货', value: OrderStatus.SHIPPED },
  { text: '已完成', value: OrderStatus.COMPLETED },
  { text: '已关闭', value: OrderStatus.CLOSED },
]

const activeStatus = ref<number | undefined>(undefined)

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getOrders({ page: page.value, size: size.value, status: activeStatus.value })
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

function goOrderDetail(o: Order) {
  router.push(`/mobile/user/order/${o.id}`)
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
  <div class="order-list-page">
    <van-nav-bar title="我的订单" />
    <div class="content">
      <van-tabs v-model:active="activeStatus" @change="onStatusChange" shrink sticky>
        <van-tab v-for="tab in statusTabs" :key="tab.value ?? 'all'" :title="tab.text" :name="tab.value">
          <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
            <div v-for="o in orders" :key="o.id" class="order-card" @click="goOrderDetail(o)">
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
</style>