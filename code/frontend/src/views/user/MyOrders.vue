<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'
import type { Order } from '@/types'

const loading = ref(false)
const orders = ref<Order[]>([])
const pagination = ref({ page: 1, pageSize: 10, total: 0 })

const statusMap: Record<number, { label: string; class: string }> = {
  0: { label: '已下单', class: 'bg-blue-100 text-blue-800' },
  1: { label: '已确认', class: 'bg-yellow-100 text-yellow-800' },
  2: { label: '已发货', class: 'bg-purple-100 text-purple-800' },
  3: { label: '已完成', class: 'bg-green-100 text-green-800' },
  4: { label: '已关闭', class: 'bg-gray-100 text-gray-800' },
}

async function fetchOrders() {
  loading.value = true
  try {
    const res = await userApi.getMyOrders({ page: pagination.value.page, pageSize: pagination.value.pageSize })
    orders.value = res.data.list
    pagination.value.total = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleClose(id: string) {
  if (!confirm('确定要关闭该订单吗？')) return
  try {
    await userApi.closeOrder(id)
    await fetchOrders()
  } catch (e) {
    console.error(e)
  }
}

async function handleConfirm(id: string) {
  try {
    await userApi.confirmOrder(id)
    await fetchOrders()
  } catch (e) {
    console.error(e)
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  fetchOrders()
}

onMounted(fetchOrders)
</script>

<template>
  <div class="space-y-6">
    <h2 class="text-2xl font-bold text-gray-800">我的订单</h2>

    <div v-if="loading" class="text-center py-12 text-gray-500">加载中...</div>

    <div v-else class="space-y-4">
      <div
        v-for="order in orders"
        :key="order.id"
        class="bg-white rounded-xl shadow-sm p-4"
      >
        <div class="flex items-start justify-between border-b border-gray-100 pb-4">
          <div>
            <p class="text-sm text-gray-500">订单号: {{ order.orderNo }}</p>
            <p class="text-sm text-gray-500 mt-1">{{ order.createTime }}</p>
          </div>
          <span :class="['px-3 py-1 text-sm font-medium rounded-full', statusMap[order.status]?.class]">
            {{ statusMap[order.status]?.label }}
          </span>
        </div>

        <div class="flex items-center gap-4 py-4">
          <div class="w-16 h-16 bg-gray-100 rounded-lg flex items-center justify-center text-2xl">
            📦
          </div>
          <div class="flex-1">
            <p class="font-medium text-gray-800">{{ order.productName }}</p>
            <p class="text-sm text-gray-500">x{{ order.quantity }}</p>
          </div>
          <div class="text-right">
            <p class="text-sm text-gray-500">消耗积分</p>
            <p class="font-bold text-purple-600">{{ order.totalPoints }}</p>
          </div>
        </div>

        <div class="flex justify-end gap-2 pt-4 border-t border-gray-100">
          <button
            v-if="order.status === 2"
            @click="handleConfirm(order.id)"
            class="px-4 py-2 text-sm font-medium text-white bg-green-600 hover:bg-green-700 rounded-lg transition-colors"
          >
            确认收货
          </button>
          <button
            v-if="order.status === 0 || order.status === 1"
            @click="handleClose(order.id)"
            class="px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 rounded-lg transition-colors"
          >
            关闭订单
          </button>
        </div>
      </div>

      <div v-if="orders.length === 0" class="text-center py-12 text-gray-500 bg-white rounded-xl">
        暂无订单
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="pagination.total > 0" class="flex justify-center">
      <div class="flex gap-2">
        <button
          v-for="page in Math.ceil(pagination.total / pagination.pageSize)"
          :key="page"
          @click="handlePageChange(page)"
          :class="[
            'px-3 py-1 text-sm rounded-md',
            page === pagination.page ? 'bg-purple-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          ]"
        >
          {{ page }}
        </button>
      </div>
    </div>
  </div>
</template>