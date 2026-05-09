<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/user'

const auth = useAuthStore()
const points = ref(0)

async function fetchPoints() {
  try {
    const res = await userApi.getPoints()
    points.value = res.data
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  if (auth.isUser) {
    fetchPoints()
  }
})
</script>

<template>
  <header class="bg-white shadow-sm">
    <div class="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
      <router-link to="/" class="flex items-center gap-2">
        <span class="text-2xl">🎁</span>
        <span class="text-xl font-bold text-purple-600">积分商城</span>
      </router-link>

      <div class="flex items-center gap-6">
        <router-link
          v-if="auth.isUser"
          to="/user/products"
          class="text-gray-600 hover:text-purple-600 transition-colors"
        >
          商品列表
        </router-link>
        <router-link
          v-if="auth.isUser"
          to="/user/orders"
          class="text-gray-600 hover:text-purple-600 transition-colors"
        >
          我的订单
        </router-link>

        <div v-if="auth.isLoggedIn" class="flex items-center gap-4">
          <div v-if="auth.isUser" class="flex items-center gap-2">
            <span class="text-gray-500">积分:</span>
            <span class="font-bold text-purple-600">{{ points }}</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-gray-600">{{ auth.user?.nickname || auth.user?.username }}</span>
            <span
              class="px-2 py-0.5 text-xs font-medium rounded-full bg-purple-100 text-purple-800"
            >
              {{ auth.userRole === 'ADMIN' ? '管理员' : auth.userRole === 'OPERATOR' ? '店铺' : '用户' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>