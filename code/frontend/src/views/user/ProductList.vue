<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'
import type { Product } from '@/types'
import ProductCard from '@/components/ProductCard.vue'

const loading = ref(false)
const products = ref<Product[]>([])
const pagination = ref({ page: 1, pageSize: 12, total: 0 })
const searchKeyword = ref('')
const viewMode = ref<'card' | 'table'>('card')

async function fetchProducts() {
  loading.value = true
  try {
    const res = await userApi.getProducts({
      page: pagination.value.page,
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value || undefined,
    })
    products.value = res.data.list.filter((p: Product) => p.status === 1)
    pagination.value.total = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.page = 1
  fetchProducts()
}

function handlePageChange(page: number) {
  pagination.value.page = page
  fetchProducts()
}

onMounted(fetchProducts)
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-bold text-gray-800">商品列表</h2>
      <div class="flex gap-3">
        <input
          v-model="searchKeyword"
          @keyup.enter="handleSearch"
          type="text"
          placeholder="搜索商品..."
          class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none"
        />
        <button
          @click="handleSearch"
          class="px-4 py-2 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700 transition-colors"
        >
          搜索
        </button>
        <div class="flex border border-gray-300 rounded-lg overflow-hidden">
          <button
            @click="viewMode = 'card'"
            :class="['px-3 py-2', viewMode === 'card' ? 'bg-purple-600 text-white' : 'bg-white text-gray-600']"
          >
            卡片
          </button>
          <button
            @click="viewMode = 'table'"
            :class="['px-3 py-2', viewMode === 'table' ? 'bg-purple-600 text-white' : 'bg-white text-gray-600']"
          >
            表格
          </button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-500">加载中...</div>

    <!-- 卡片视图 -->
    <div v-else-if="viewMode === 'card'" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      <ProductCard v-for="product in products" :key="product.id" :product="product" />
    </div>

    <!-- 表格视图 -->
    <div v-else class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">商品</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">积分</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">库存</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="product in products" :key="product.id" class="hover:bg-gray-50">
            <td class="px-6 py-4">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center text-2xl">
                  📦
                </div>
                <div>
                  <p class="font-medium text-gray-800">{{ product.name }}</p>
                  <p class="text-sm text-gray-500">{{ product.description }}</p>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 text-sm font-medium text-purple-600">{{ product.points }}</td>
            <td class="px-6 py-4 text-sm text-gray-600">{{ product.stock }}</td>
            <td class="px-6 py-4">
              <router-link
                :to="`/user/products/${product.id}`"
                class="text-sm text-purple-600 hover:underline font-medium"
              >
                立即兑换
              </router-link>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="flex justify-center">
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