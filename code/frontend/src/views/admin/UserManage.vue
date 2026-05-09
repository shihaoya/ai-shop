<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import type { User } from '@/types'

const loading = ref(false)
const users = ref<User[]>([])
const pagination = ref({ page: 1, pageSize: 10, total: 0 })

const statusMap: Record<number, { label: string; class: string }> = {
  0: { label: '待审核', class: 'bg-yellow-100 text-yellow-800' },
  1: { label: '正常', class: 'bg-green-100 text-green-800' },
  2: { label: '冻结', class: 'bg-red-100 text-red-800' },
}

const roleMap: Record<string, { label: string; class: string }> = {
  ADMIN: { label: '管理员', class: 'bg-purple-100 text-purple-800' },
  OPERATOR: { label: '店铺用户', class: 'bg-blue-100 text-blue-800' },
  USER: { label: '普通用户', class: 'bg-gray-100 text-gray-800' },
}

async function fetchUsers() {
  loading.value = true
  try {
    const res = await adminApi.getUsers({ page: pagination.value.page, pageSize: pagination.value.pageSize })
    users.value = res.data.list
    pagination.value.total = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleApprove(id: string) {
  try {
    await adminApi.approveUser(id)
    await fetchUsers()
  } catch (e) {
    console.error(e)
  }
}

async function handleFreeze(id: string) {
  try {
    await adminApi.freezeUser(id)
    await fetchUsers()
  } catch (e) {
    console.error(e)
  }
}

async function handleUnfreeze(id: string) {
  try {
    await adminApi.unfreezeUser(id)
    await fetchUsers()
  } catch (e) {
    console.error(e)
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  fetchUsers()
}

onMounted(fetchUsers)
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-bold text-gray-800">用户管理</h2>
    </div>

    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">用户名</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">昵称</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">角色</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">积分</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="user in users" :key="user.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm text-gray-500">{{ user.id }}</td>
            <td class="px-6 py-4 text-sm font-medium text-gray-800">{{ user.username }}</td>
            <td class="px-6 py-4 text-sm text-gray-600">{{ user.nickname }}</td>
            <td class="px-6 py-4">
              <span :class="['px-2 py-1 text-xs font-medium rounded-full', roleMap[user.role]?.class]">
                {{ roleMap[user.role]?.label }}
              </span>
            </td>
            <td class="px-6 py-4">
              <span :class="['px-2 py-1 text-xs font-medium rounded-full', statusMap[user.status]?.class]">
                {{ statusMap[user.status]?.label }}
              </span>
            </td>
            <td class="px-6 py-4 text-sm text-gray-600">{{ user.points ?? '-' }}</td>
            <td class="px-6 py-4">
              <div class="flex gap-2">
                <button
                  v-if="user.status === 0"
                  @click="handleApprove(user.id)"
                  class="px-3 py-1 text-xs font-medium text-white bg-green-600 hover:bg-green-700 rounded-md transition-colors"
                >
                  审核通过
                </button>
                <button
                  v-if="user.status === 1"
                  @click="handleFreeze(user.id)"
                  class="px-3 py-1 text-xs font-medium text-white bg-red-600 hover:bg-red-700 rounded-md transition-colors"
                >
                  冻结
                </button>
                <button
                  v-if="user.status === 2"
                  @click="handleUnfreeze(user.id)"
                  class="px-3 py-1 text-xs font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md transition-colors"
                >
                  解冻
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="users.length === 0">
            <td colspan="7" class="px-6 py-12 text-center text-gray-500">暂无数据</td>
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