<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getProducts } from '@/api/user'
import type { Product } from '@/types'

const router = useRouter()

const products = ref<Product[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const size = ref(10)

async function fetchProducts() {
  loading.value = true
  try {
    const res = await getProducts({ page: page.value, size: size.value })
    if (page.value === 1) products.value = res.list
    else products.value.push(...res.list)
    if (res.list.length < size.value) finished.value = true
    page.value++
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

function onLoad() {
  if (!loading.value && !finished.value) fetchProducts()
}

function goProductDetail(p: Product) {
  router.push(`/mobile/user/product/${p.id}`)
}

onMounted(() => {
  fetchProducts()
})
</script>

<template>
  <div class="product-list-page">
    <van-nav-bar title="商品列表" />
    <div class="content">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div class="product-grid">
          <div v-for="p in products" :key="p.id" class="product-card" @click="goProductDetail(p)">
            <div class="product-image">
              <van-image :src="p.mainImageUrl || p.mainImage || p.image" width="100%" height="120px" fit="cover" radius="8" />
              <div v-if="p.stock < 5" class="stock-badge">仅剩 {{ p.stock }}</div>
            </div>
            <div class="product-info">
              <div class="product-name">{{ p.name }}</div>
              <div class="product-bottom">
                <span class="product-price">{{ p.price }}积分</span>
              </div>
            </div>
          </div>
        </div>
        <van-empty v-if="!loading && products.length === 0" description="暂无商品" />
      </van-list>
    </div>
  </div>
</template>

<style scoped>
.product-list-page {
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
}

.content {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 10px;
}

.product-card {
  background: var(--bg-card);
  border-radius: 10px;
  overflow: hidden;
}

.product-image {
  position: relative;
}

.stock-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  background: rgba(239, 68, 68, 0.9);
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
}

.product-info {
  padding: 10px;
}

.product-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 36px;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 14px;
  font-weight: 700;
  color: var(--accent);
}
</style>