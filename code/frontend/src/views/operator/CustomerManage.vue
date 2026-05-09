<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { operatorApi } from '@/api/operator'
import type { User } from '@/types'

const loading = ref(false)
const customers = ref<User[]>([])
const pagination = ref({ page: 1, pageSize: 10, total: 0 })
const showAdjustForm = ref(false)
const adjustingUser = ref<User | null>(null)
const adjustForm = ref({ points: 0, description: '' })
const error = ref('')

async function fetchCustomers() {
  loading.value = true
  try {
    const res = await operatorApi.getCustomers({ page: pagination.value.page, pageSize: pagination.value.pageSize })
    customers.value = res.data.list
    pagination.value.total = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openAdjustForm(user: User) {
  adjustingUser.value = user
  adjustForm.value = { points: 0, description: '' }
  error.value = ''
  showAdjustForm.value = true
}

async function handleAdjust() {
  if (!adjustingUser.value) return
  if (!adjustForm.value.description) {
    error.value = '请输入调整原因'
    return
  }
  try {
    await operatorApi.adjustPoints(adjustingUser.value.id, adjustForm.value.points, adjustForm.value.description)
    showAdjustForm.value = false
    await fetchCustomers()
  } catch (e: any) {
    error.value = e.message
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  fetchCustomers()
}

onMounted(fetchCustomers)
</script>

<template>
  <div class="space-y-6">
    <h2 class="text-2xl font-bold text-gray-800">客户管理</h2>

    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">用户</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">昵称</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">当前积分</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">注册时间</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="customer in customers" :key="customer.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm font-medium text-gray-800">{{ customer.username }}</td>
            <td class="px-6 py-4 text-sm text-gray-600">{{ customer.nickname }}</td>
            <td class="px-6 py-4 text-sm text-gray-800 font-medium">{{ customer.points ?? 0 }}</td>
            <td class="px-6 py-4 text-sm text-gray-500">{{ customer.createTime }}</td>
            <td class="px-6 py-4">
              <button
                @click="openAdjustForm(customer)"
                class="px-3 py-1 text-xs font-medium text-purple-600 hover:bg-purple-50 rounded-md transition-colors"
              >
                积分调整
              </button>
            </td>
          </tr>
          <tr v-if="customers.length === 0">
            <td colspan="5" class="px-6 py-12 text-center text-gray-500">暂无客户</td>
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

    <!-- 积分调整弹窗 -->
    <div v-if="showAdjustForm" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
        <h3 class="text-lg font-bold text-gray-800 mb-4">积分调整 - {{ adjustingUser?.nickname }}</h3>
        <form @submit.prevent="handleAdjust" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">调整积分（正数为增加，负数为减少）</label>
            <input
              v-model="adjustForm.points"
              type="number"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 outline-none"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">原因 *</label>
            <input
              v-model="adjustForm.description"
              type="text"
              placeholder="请输入调整原因"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 outline-none"
            />
          </div>
          <div v-if="error" class="text-red-500 text-sm">{{ error }}</div>
          <div class="flex gap-3">
            <button
              type="submit"
              class="flex-1 px-4 py-2 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700 transition-colors"
            >
              确定
            </button>
            <button
              type="button"
              @click="showAdjustForm = false"
              class="px-4 py-2 bg-gray-200 text-gray-700 font-medium rounded-lg hover:bg-gray-300 transition-colors"
            >
              取消
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>