<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { operatorApi } from '@/api/operator'
import type { Shop } from '@/types'

const loading = ref(false)
const shop = ref<Shop | null>(null)
const showForm = ref(false)
const form = ref({ name: '', description: '' })
const error = ref('')

async function fetchShop() {
  loading.value = true
  try {
    const res = await operatorApi.getMyShop()
    shop.value = res.data
  } catch (e: any) {
    if (e.message.includes('404')) {
      shop.value = null
    }
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!form.value.name) {
    error.value = '请输入店铺名称'
    return
  }
  try {
    await operatorApi.createShop(form.value)
    showForm.value = false
    form.value = { name: '', description: '' }
    await fetchShop()
  } catch (e: any) {
    error.value = e.message
  }
}

async function handleToggleStatus() {
  if (!shop.value) return
  const newStatus = shop.value.status === 1 ? 2 : 1
  try {
    await operatorApi.updateShopStatus(newStatus)
    await fetchShop()
  } catch (e) {
    console.error(e)
  }
}

onMounted(fetchShop)
</script>

<template>
  <div class="space-y-6">
    <h2 class="text-2xl font-bold text-gray-800">我的店铺</h2>

    <div v-if="loading" class="text-center py-12 text-gray-500">加载中...</div>

    <div v-else-if="!shop" class="bg-white rounded-xl shadow-sm p-8">
      <div class="text-center">
        <p class="text-gray-500 mb-4">您还没有创建店铺</p>
        <button
          @click="showForm = true"
          class="px-6 py-3 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700 transition-colors"
        >
          申请开店
        </button>
      </div>

      <!-- 创建表单 -->
      <div v-if="showForm" class="mt-6 max-w-md mx-auto">
        <form @submit.prevent="handleCreate" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">店铺名称</label>
            <input
              v-model="form.name"
              type="text"
              placeholder="请输入店铺名称"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">店铺描述</label>
            <textarea
              v-model="form.description"
              rows="3"
              placeholder="请输入店铺描述（可选）"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none"
            ></textarea>
          </div>
          <div v-if="error" class="text-red-500 text-sm">{{ error }}</div>
          <div class="flex gap-3">
            <button
              type="submit"
              class="flex-1 px-4 py-2 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700 transition-colors"
            >
              提交申请
            </button>
            <button
              type="button"
              @click="showForm = false; error = ''"
              class="px-4 py-2 bg-gray-200 text-gray-700 font-medium rounded-lg hover:bg-gray-300 transition-colors"
            >
              取消
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-else class="bg-white rounded-xl shadow-sm p-6">
      <div class="flex items-start justify-between">
        <div>
          <h3 class="text-xl font-bold text-gray-800">{{ shop.name }}</h3>
          <p class="text-gray-500 mt-1">{{ shop.description || '暂无描述' }}</p>
          <div class="mt-3">
            <span
              :class="[
                'px-3 py-1 text-sm font-medium rounded-full',
                shop.status === 1 ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
              ]"
            >
              {{ shop.status === 1 ? '营业中' : '歇业中' }}
            </span>
          </div>
        </div>
        <button
          @click="handleToggleStatus"
          :class="[
            'px-4 py-2 font-medium rounded-lg transition-colors',
            shop.status === 1
              ? 'bg-yellow-100 text-yellow-800 hover:bg-yellow-200'
              : 'bg-green-100 text-green-800 hover:bg-green-200'
          ]"
        >
          {{ shop.status === 1 ? '设为歇业' : '开始营业' }}
        </button>
      </div>
    </div>
  </div>
</template>