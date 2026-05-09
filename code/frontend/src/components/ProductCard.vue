<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { Product } from '@/types'

defineProps<{
  product: Product
}>()

const router = useRouter()

function goToDetail(id: string) {
  router.push(`/user/products/${id}`)
}
</script>

<template>
  <div
    @click="goToDetail(product.id)"
    class="bg-white rounded-xl shadow-sm overflow-hidden hover:shadow-lg transition-all duration-300 cursor-pointer group"
  >
    <div class="aspect-square bg-gray-100 relative overflow-hidden">
      <div v-if="!product.image" class="absolute inset-0 flex items-center justify-center text-5xl">
        📦
      </div>
      <img
        v-else
        :src="product.image"
        :alt="product.name"
        class="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
      />
      <div
        v-if="product.stock <= 5 && product.stock > 0"
        class="absolute top-2 left-2 px-2 py-1 text-xs font-medium bg-red-500 text-white rounded"
      >
        仅剩{{ product.stock }}
      </div>
      <div
        v-if="product.stock === 0"
        class="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center"
      >
        <span class="text-white font-medium">暂时缺货</span>
      </div>
    </div>

    <div class="p-4">
      <h3 class="font-medium text-gray-800 truncate">{{ product.name }}</h3>
      <p class="text-sm text-gray-500 mt-1 line-clamp-2">{{ product.description || '暂无描述' }}</p>

      <div class="mt-4 flex items-center justify-between">
        <div class="flex items-center gap-1">
          <span class="text-xl font-bold text-purple-600">{{ product.points }}</span>
          <span class="text-sm text-gray-500">积分</span>
        </div>
        <span class="text-xs text-gray-400">库存: {{ product.stock }}</span>
      </div>

      <button
        :disabled="product.stock === 0"
        class="mt-3 w-full py-2 bg-purple-600 text-white text-sm font-medium rounded-lg hover:bg-purple-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
      >
        {{ product.stock === 0 ? '暂时缺货' : '立即兑换' }}
      </button>
    </div>
  </div>
</template>