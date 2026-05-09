<script setup lang="ts">
import type { Order } from '@/types'

defineProps<{
  order: Order
}>()

const statusMap: Record<number, { label: string; class: string }> = {
  0: { label: '已下单', class: 'bg-blue-100 text-blue-800' },
  1: { label: '已确认', class: 'bg-yellow-100 text-yellow-800' },
  2: { label: '已发货', class: 'bg-purple-100 text-purple-800' },
  3: { label: '已完成', class: 'bg-green-100 text-green-800' },
  4: { label: '已关闭', class: 'bg-gray-100 text-gray-800' },
}
</script>

<template>
  <div class="bg-white rounded-xl shadow-sm p-4 hover:shadow-md transition-shadow">
    <div class="flex items-start justify-between border-b border-gray-100 pb-3">
      <div>
        <p class="text-sm text-gray-500 font-mono">{{ order.orderNo }}</p>
        <p class="text-xs text-gray-400 mt-1">{{ order.createTime }}</p>
      </div>
      <span :class="['px-3 py-1 text-sm font-medium rounded-full', statusMap[order.status]?.class]">
        {{ statusMap[order.status]?.label }}
      </span>
    </div>

    <div class="flex items-center gap-4 py-4">
      <div class="w-16 h-16 bg-gray-100 rounded-lg flex items-center justify-center text-2xl">
        {{ order.productImage ? '📦' : '📦' }}
      </div>
      <div class="flex-1">
        <p class="font-medium text-gray-800">{{ order.productName }}</p>
        <p class="text-sm text-gray-500">x{{ order.quantity }}</p>
      </div>
      <div class="text-right">
        <p class="text-xs text-gray-500">消耗积分</p>
        <p class="font-bold text-purple-600">{{ order.totalPoints }}</p>
      </div>
    </div>

    <div v-if="order.address" class="text-sm text-gray-500 pb-3 border-b border-gray-100">
      <p>收货人: {{ order.address.name }} {{ order.address.phone }}</p>
      <p class="text-xs">{{ order.address.province }} {{ order.address.city }} {{ order.address.district }} {{ order.address.detail }}</p>
    </div>

    <div class="flex justify-end gap-2 pt-3">
      <slot name="actions" />
    </div>
  </div>
</template>