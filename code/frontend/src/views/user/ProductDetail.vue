<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '@/api/user'
import type { Product, Address } from '@/types'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const product = ref<Product | null>(null)
const addresses = ref<Address[]>([])
const selectedAddressId = ref('')
const quantity = ref(1)
const showAddressSelect = ref(false)
const error = ref('')

const totalPoints = computed(() => (product.value?.points || 0) * quantity.value)

async function fetchProduct() {
  loading.value = true
  try {
    const res = await userApi.getProduct(route.params.id as string)
    product.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function fetchAddresses() {
  try {
    const res = await userApi.getAddresses()
    addresses.value = res.data || []
    const defaultAddr = addresses.value.find((a: Address) => a.isDefault)
    if (defaultAddr) {
      selectedAddressId.value = defaultAddr.id
    } else if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].id
    }
  } catch (e) {
    console.error(e)
  }
}

async function handleExchange() {
  if (!selectedAddressId.value) {
    error.value = '请选择收货地址'
    return
  }
  if (!product.value || product.value.stock < quantity.value) {
    error.value = '库存不足'
    return
  }

  try {
    await userApi.createOrder({
      productId: product.value.id,
      quantity: quantity.value,
      addressId: selectedAddressId.value,
    })
    alert('兑换成功！')
    router.push('/user/orders')
  } catch (e: any) {
    error.value = e.message
  }
}

onMounted(() => {
  fetchProduct()
  fetchAddresses()
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center gap-4">
      <button @click="router.back()" class="p-2 hover:bg-gray-100 rounded-lg">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
      </button>
      <h2 class="text-2xl font-bold text-gray-800">商品详情</h2>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-500">加载中...</div>

    <div v-else-if="product" class="grid grid-cols-1 md:grid-cols-2 gap-8">
      <!-- 商品图片 -->
      <div class="bg-white rounded-xl shadow-sm p-6">
        <div class="aspect-square bg-gray-100 rounded-xl flex items-center justify-center">
          <span v-if="!product.image" class="text-6xl">📦</span>
          <img v-else :src="product.image" class="w-full h-full object-cover rounded-xl" />
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="bg-white rounded-xl shadow-sm p-6 space-y-6">
        <div>
          <h3 class="text-2xl font-bold text-gray-800">{{ product.name }}</h3>
          <p class="text-gray-500 mt-2">{{ product.description || '暂无描述' }}</p>
        </div>

        <div class="flex items-center gap-4">
          <span class="text-3xl font-bold text-purple-600">{{ product.points }}</span>
          <span class="text-gray-500">积分</span>
        </div>

        <div class="text-sm text-gray-500">
          <p>库存: {{ product.stock }}</p>
          <p>分类: {{ product.categoryName }}</p>
        </div>

        <!-- 数量选择 -->
        <div class="flex items-center gap-4">
          <span class="text-gray-700">数量:</span>
          <div class="flex items-center border border-gray-300 rounded-lg">
            <button
              @click="quantity = Math.max(1, quantity - 1)"
              class="px-3 py-2 hover:bg-gray-100"
            >
              -
            </button>
            <input
              v-model.number="quantity"
              type="number"
              min="1"
              :max="product.stock"
              class="w-16 text-center border-0 focus:ring-0"
            />
            <button
              @click="quantity = Math.min(product.stock, quantity + 1)"
              class="px-3 py-2 hover:bg-gray-100"
            >
              +
            </button>
          </div>
        </div>

        <!-- 地址选择 -->
        <div>
          <button
            @click="showAddressSelect = !showAddressSelect"
            class="text-purple-600 hover:underline text-sm"
          >
            {{ showAddressSelect ? '收起地址列表' : '选择收货地址' }}
          </button>

          <div v-if="showAddressSelect" class="mt-3 space-y-2">
            <div
              v-for="addr in addresses"
              :key="addr.id"
              @click="selectedAddressId = addr.id"
              :class="[
                'p-3 border rounded-lg cursor-pointer transition-colors',
                selectedAddressId === addr.id ? 'border-purple-500 bg-purple-50' : 'border-gray-200 hover:border-purple-300'
              ]"
            >
              <p class="font-medium text-gray-800">{{ addr.name }} {{ addr.phone }}</p>
              <p class="text-sm text-gray-500">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}</p>
              <span v-if="addr.isDefault" class="text-xs text-purple-600">默认</span>
            </div>
            <router-link to="/user/addresses" class="block text-center text-sm text-purple-600 hover:underline py-2">
              管理收货地址
            </router-link>
          </div>
        </div>

        <!-- 错误信息 -->
        <div v-if="error" class="text-red-500 text-sm">{{ error }}</div>

        <!-- 兑换按钮 -->
        <div class="pt-4 border-t">
          <div class="flex items-center justify-between mb-4">
            <span class="text-gray-600">合计:</span>
            <span class="text-2xl font-bold text-purple-600">{{ totalPoints }} 积分</span>
          </div>
          <button
            @click="handleExchange"
            :disabled="!selectedAddressId || product.stock < quantity"
            class="w-full py-3 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            立即兑换
          </button>
        </div>
      </div>
    </div>
  </div>
</template>