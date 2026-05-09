<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { operatorApi } from '@/api/operator'
import type { Message } from '@/types'

const loading = ref(false)
const messages = ref<Message[]>([])
const pagination = ref({ page: 1, pageSize: 10, total: 0 })

async function fetchMessages() {
  loading.value = true
  try {
    const res = await operatorApi.getMessages({ page: pagination.value.page, pageSize: pagination.value.pageSize })
    messages.value = res.data.list
    pagination.value.total = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  fetchMessages()
}

onMounted(fetchMessages)
</script>

<template>
  <div class="space-y-6">
    <h2 class="text-2xl font-bold text-gray-800">消息中心</h2>

    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <div class="divide-y divide-gray-200">
        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['p-4 hover:bg-gray-50', !msg.isRead ? 'bg-purple-50' : '']"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-2">
                <span
                  :class="['px-2 py-0.5 text-xs font-medium rounded-full', msg.type === 'ORDER' ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-800']"
                >
                  {{ msg.type === 'ORDER' ? '订单' : '系统' }}
                </span>
                <span v-if="!msg.isRead" class="w-2 h-2 bg-purple-600 rounded-full"></span>
              </div>
              <h4 class="font-medium text-gray-800 mt-1">{{ msg.title }}</h4>
              <p class="text-sm text-gray-500 mt-1">{{ msg.content }}</p>
              <p class="text-xs text-gray-400 mt-2">{{ msg.createTime }}</p>
            </div>
          </div>
        </div>
        <div v-if="messages.length === 0" class="p-12 text-center text-gray-500">
          暂无消息
        </div>
      </div>

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