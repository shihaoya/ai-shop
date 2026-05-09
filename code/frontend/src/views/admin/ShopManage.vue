<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import type { Shop } from '@/types'

const loading = ref(false)
const shops = ref<Shop[]>([])
const pagination = ref({ page: 1, pageSize: 10, total: 0 })

const statusMap: Record<number, { label: string; class: string }> = {
  0: { label: '待审核', class: 'bg-yellow-100 text-yellow-800' },
  1: { label: '营业', class: 'bg-green-100 text-green-800' },
  2: { label: '歇业', class: 'bg-gray-100 text-gray-800' },
}

async function fetchShops() {
  loading.value = true
  try {
    const res = await adminApi.getShops({ page: pagination.value.page, pageSize: pagination.value.pageSize })
    shops.value = res.data.list
    pagination.value.total = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleApprove(id: string) {
  try {
    await adminApi.approveShop(id)
    await fetchShops()
  } catch (e) {
    console.error(e)
  }
}

async function handleReject(id: string) {
  try {
    await adminApi.rejectShop(id)
    await fetchShops()
  } catch (e) {
    console.error(e)
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  fetchShops()
}

onMounted(fetchShops)
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-bold text-gray-800">店铺管理</h2>
    </div>

    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">店铺名称</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">运营商</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">创建时间</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="shop in shops" :key="shop.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm text-gray-500">{{ shop.id }}</td>
            <td class="px-6 py-4 text-sm font-medium text-gray-800">{{ shop.name }}</td>
            <td class="px-6 py-4 text-sm text-gray-600">{{ shop.operatorName || '-' }}</td>
            <td class="px-6 py-4">
              <span :class="['px-2 py-1 text-xs font-medium rounded-full', statusMap[shop.status]?.class]">
                {{ statusMap[shop.status]?.label }}
              </span>
            </td>
            <td class="px-6 py-4 text-sm text-gray-500">{{ shop.createTime }}</td>
            <td class="px-6 py-4">
              <div class="flex gap-2">
                <button
                  v-if="shop.status === 0"
                  @click="handleApprove(shop.id)"
                  class="px-3 py-1 text-xs font-medium text-white bg-green-600 hover:bg-green-700 rounded-md transition-colors"
                >
                  通过
                </button>
                <button
                  v-if="shop.status === 0"
                  @click="handleReject(shop.id)"
                  class="px-3 py-1 text-xs font-medium text-white bg-red-600 hover:bg-red-700 rounded-md transition-colors"
                >
                  拒绝
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="shops.length === 0">
            <td colspan="6" class="px-6 py-12 text-center text-gray-500">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="px-6 py-4 border-t border-gray-200 flex justify-between items-center">
        <span class="text-sm text-gray-500">共 {{ pagination.total }} 条</span>
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