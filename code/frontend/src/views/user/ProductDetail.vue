<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { getProduct, createOrder, getPoints } from '@/api/user'
import { useThemeStore } from '@/stores/theme'
import type { Product } from '@/types/api'

const router = useRouter()
const route = useRoute()
const themeStore = useThemeStore()

const product = ref<Product | null>(null)
const userPoints = ref(0)
const loading = ref(false)
const exchangeLoading = ref(false)

const productId = computed(() => String(route.params.id))

const canExchange = computed(() => {
  if (!product.value) return false
  return userPoints.value >= product.value.price && product.value.stock > 0
})

const pointsDeficient = computed(() => {
  if (!product.value) return false
  return userPoints.value < product.value.price
})

async function fetchProduct() {
  loading.value = true
  try {
    const res = await getProduct(productId.value)
    if (res) {
      product.value = res
    } else {
      message.error('获取商品详情失败')
    }
  } catch (e) {
    message.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

async function fetchUserPoints() {
  try {
    const res = await getPoints()
    userPoints.value = res.points
  } catch (e) {
    console.error('获取用户积分失败', e)
  }
}

async function handleExchange() {
  if (!product.value) return

  if (product.value.stock <= 0) {
    message.warning('库存不足')
    return
  }

  if (pointsDeficient.value) {
    message.warning('积分不足，无法兑换')
    return
  }

  exchangeLoading.value = true
  try {
    const res = await createOrder(productId.value, 1)
    if (res) {
      message.success('兑换成功！')
      router.push('/user/orders')
    } else {
      message.error('兑换失败')
    }
  } catch (e) {
    message.error('兑换失败')
  } finally {
    exchangeLoading.value = false
  }
}

onMounted(() => {
  themeStore.init()
  fetchProduct()
  fetchUserPoints()
})
</script>

<template>
  <div id="page-product-detail">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <div class="page-head">
        <h2><span class="accent-line"></span>商品详情</h2>
      </div>

      <div class="cyber-card" v-if="loading">
        <div style="text-align:center;padding:60px 20px;color:var(--text-secondary);">
          <i class="fas fa-spinner fa-spin" style="font-size:48px;margin-bottom:16px;"></i>
          <p style="font-size:16px;">加载中...</p>
        </div>
      </div>

      <template v-else-if="product">
        <div class="product-detail-layout">
          <div class="product-image-section">
            <div class="product-image-wrapper">
              <img v-if="product.image" :src="product.image" :alt="product.name" class="product-image" />
              <div v-else class="product-image-placeholder">
                <i class="fas fa-box-open"></i>
              </div>
            </div>
          </div>

          <div class="product-info-section">
            <div class="product-category">
              <i class="fas fa-tag"></i>
              {{ product.categoryName || '未分类' }}
            </div>

            <h1 class="product-name">{{ product.name }}</h1>

            <div class="product-price">
              <span class="price-label">兑换积分</span>
              <span class="price-value">{{ product.price }}</span>
              <span class="price-unit">积分</span>
            </div>

            <div class="product-meta">
              <div class="meta-item">
                <i class="fas fa-cubes"></i>
                <span>库存: {{ product.stock }}</span>
              </div>
              <div class="meta-item">
                <i class="fas fa-th-large"></i>
                <span>类型: {{ product.type === 'virtual' ? '虚拟商品' : '实物商品' }}</span>
              </div>
            </div>

            <div class="user-points-info">
              <i class="fas fa-coins"></i>
              <span>我的积分: <strong>{{ userPoints }}</strong></span>
            </div>

            <div v-if="pointsDeficient" class="points-warning">
              <i class="fas fa-exclamation-triangle"></i>
              积分不足，还需要 {{ product.price - userPoints }} 积分才能兑换
            </div>

            <div class="product-description" v-if="product.description">
              <h3><i class="fas fa-file-alt"></i>商品描述</h3>
              <p>{{ product.description }}</p>
            </div>

            <div class="product-actions">
              <button
                class="cyber-btn cyber-btn-primary btn-exchange"
                :disabled="!canExchange || exchangeLoading"
                @click="handleExchange"
              >
                <i v-if="exchangeLoading" class="fas fa-spinner fa-spin"></i>
                <template v-else>
                  <i class="fas fa-exchange-alt"></i>
                  立即兑换
                </template>
              </button>
              <button class="cyber-btn btn-back" @click="router.push('/user/products')">
                <i class="fas fa-arrow-left"></i>
                返回列表
              </button>
            </div>
          </div>
        </div>
      </template>

      <div class="cyber-card" v-else>
        <div style="text-align:center;padding:60px 20px;color:var(--text-secondary);">
          <i class="fas fa-exclamation-circle" style="font-size:48px;margin-bottom:16px;opacity:0.5;"></i>
          <p style="font-size:16px;">商品不存在或已下架</p>
          <button class="cyber-btn" style="margin-top:20px;" @click="router.push('/user/products')">
            返回列表
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-detail-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

@media (max-width: 900px) {
  .product-detail-layout {
    grid-template-columns: 1fr;
  }
}

.product-image-section {
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.product-image-wrapper {
  width: 100%;
  max-width: 400px;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-image-placeholder {
  color: var(--text-secondary);
  font-size: 64px;
  opacity: 0.4;
}

.product-info-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-category {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: var(--accent-glow);
  border: 1px solid var(--accent);
  border-radius: 20px;
  font-size: 12px;
  color: var(--accent);
  width: fit-content;
}

.product-name {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.3;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(var(--accent-rgb), 0.1), rgba(var(--accent-rgb), 0.05));
  border: 1px solid rgba(var(--accent-rgb), 0.2);
  border-radius: 10px;
}

.price-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.price-value {
  font-size: 36px;
  font-weight: 700;
  color: var(--accent);
  text-shadow: var(--accent-glow-text);
}

.price-unit {
  font-size: 14px;
  color: var(--accent);
}

.product-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
}

.meta-item i {
  color: var(--accent);
}

.user-points-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  font-size: 15px;
  color: var(--text-secondary);
}

.user-points-info i {
  color: #f59e0b;
  font-size: 18px;
}

.user-points-info strong {
  color: #f59e0b;
  font-size: 18px;
}

.points-warning {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 10px;
  color: #ef4444;
  font-size: 14px;
}

.points-warning i {
  flex-shrink: 0;
}

.product-description {
  padding: 16px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 10px;
}

.product-description h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px 0;
}

.product-description h3 i {
  color: var(--accent);
}

.product-description p {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.7;
  margin: 0;
  white-space: pre-wrap;
}

.product-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.btn-exchange {
  flex: 1;
  padding: 14px 24px;
  font-size: 16px;
}

.btn-exchange:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-back {
  padding: 14px 20px;
}

.cyber-btn i {
  margin-right: 6px;
}
</style>