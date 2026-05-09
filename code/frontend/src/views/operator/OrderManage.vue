<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { operatorApi } from '@/api/operator'
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
    const res = await operatorApi.getOrders({ page: pagination.value.page, pageSize: pagination.value.pageSize })
    orders.value = res.data.list
    pagination.value.total = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleConfirm(id: string) {
  try {
    await operatorApi.confirmOrder(id)
    await fetchOrders()
  } catch (e) {
    console.error(e)
  }
}

async function handleShip(id: string) {
  try {
    await operatorApi.shipOrder(id)
    await fetchOrders()
  } catch (e) {
    console.error(e)
  }
}

async function handleClose(id: string) {
  if (!confirm('确定要关闭该订单吗？')) return
  try {
    await operatorApi.closeOrder(id)
    await fetchOrders()
  } catch (e) {
    console.error(e)
  }
}

async function handleComplete(id: string) {
  try {
    await operatorApi.completeOrder(id)
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
    <h2 class="text-2xl font-bold text-gray-800">订单管理</h2>

    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">订单号</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">商品</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">用户</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">积分</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">时间</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="order in orders" :key="order.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm font-mono text-gray-800">{{ order.orderNo }}</td>
            <td class="px-6 py-4">
              <div class="flex items-center gap-2">
                <span class="text-sm text-gray-600">{{ order.productName }}</span>
                <span class="text-xs text-gray-400">x{{ order.quantity }}</span>
              </div>
            </td>
            <td class="px-6 py-4 text-sm text-gray-600">{{ order.userName }}</td>
            <td class="px-6 py-4 text-sm text-gray-800 font-medium">{{ order.totalPoints }}</td>
            <td class="px-6 py-4">
              <span :class="['px-2 py-1 text-xs font-medium rounded-full', statusMap[order.status]?.class]">
                {{ statusMap[order.status]?.label }}
              </span>
            </td>
            <td class="px-6 py-4 text-sm text-gray-500">{{ order.createTime }}</td>
            <td class="px-6 py-4">
              <div class="flex gap-2 flex-wrap">
                <button
                  v-if="order.status === 0"
                  @click="handleConfirm(order.id)"
                  class="px-3 py-1 text-xs font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md transition-colors"
                >
                  确认
                </button>
                <button
                  v-if="order.status === 1"
                  @click="handleShip(order.id)"
                  class="px-3 py-1 text-xs font-medium text-white bg-purple-600 hover:bg-purple-700 rounded-md transition-colors"
                >
                  发货
                </button>
                <button
                  v-if="order.status === 3"
                  @click="handleComplete(order.id)"
                  class="px-3 py-1 text-xs font-medium text-white bg-green-600 hover:bg-green-700 rounded-md transition-colors"
                >
                  完成
                </button>
                <button
                  v-if="order.status === 0 || order.status === 1"
                  @click="handleClose(order.id)"
                  class="px-3 py-1 text-xs font-medium text-white bg-red-600 hover:bg-red-700 rounded-md transition-colors"
                >
                  关闭
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="orders.length === 0">
            <td colspan="7" class="px-6 py-12 text-center text-gray-500">暂无订单</td>
          </tr>
        </tbody>
      </table>

      <div class="px-6 py-4 border-t border-gray-200 flex justify-center">
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
  </div>
</template>