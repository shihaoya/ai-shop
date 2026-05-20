<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { showToast } from 'vant'
import { getProducts, createOrder, getAddresses, getPoints } from '@/api/user'
import type { Product, Address } from '@/types'

function getTypeText(type: number) {
  return type === 1 ? '虚拟' : '实物'
}

const products = ref<Product[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const size = ref(20)
const keyword = ref('')

const userPoints = ref(0)
const addresses = ref<Address[]>([])

const showDetail = ref(false)
const showExchange = ref(false)
const selectedProduct = ref<Product | null>(null)

const exchangeQty = ref(1)
const exchangeAddress = ref({ receiver: '', phone: '', province: '', city: '', district: '', detail: '' })
const exchangeLoading = ref(false)

const defaultAddress = computed(() => addresses.value.find(a => a.isDefault))

const remainingPoints = computed(() => {
  if (!selectedProduct.value) return 0
  const price = Number(selectedProduct.value.price) || 0
  return userPoints.value - price * exchangeQty.value
})

const isPhysical = computed(() => {
  return selectedProduct.value && selectedProduct.value.type === 2
})

async function fetchProducts() {
  loading.value = true
  try {
    const res = await getProducts({ page: page.value, size: size.value, keyword: keyword.value || undefined })
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

function onSearch() {
  products.value = []
  page.value = 1
  finished.value = false
  fetchProducts()
}

function onLoad() {
  if (!loading.value && !finished.value) fetchProducts()
}

async function loadPoints() {
  try {
    const res = await getPoints()
    userPoints.value = res.points
  } catch { /* ignore */ }
}

async function loadAddresses() {
  try {
    addresses.value = await getAddresses()
  } catch { /* ignore */ }
}

function openDetail(p: Product) {
  selectedProduct.value = p
  showDetail.value = true
}

function openExchange(p: Product) {
  selectedProduct.value = p
  exchangeQty.value = 1
  if (isPhysical.value && defaultAddress.value) {
    const addr = defaultAddress.value
    exchangeAddress.value = {
      receiver: addr.receiver,
      phone: addr.phone,
      province: addr.province,
      city: addr.city,
      district: addr.district,
      detail: addr.detail,
    }
  } else {
    exchangeAddress.value = { receiver: '', phone: '', province: '', city: '', district: '', detail: '' }
  }
  showDetail.value = false
  showExchange.value = true
}

async function handleExchange() {
  if (!selectedProduct.value) return
  if (remainingPoints.value < 0) {
    showToast('积分不足')
    return
  }
  if (isPhysical.value) {
    const addr = exchangeAddress.value
    if (!addr.receiver || !addr.phone || !addr.province || !addr.city || !addr.district || !addr.detail) {
      showToast('请填写完整收货地址')
      return
    }
  }
  exchangeLoading.value = true
  try {
    await createOrder(
      selectedProduct.value.id,
      exchangeQty.value,
      isPhysical.value ? exchangeAddress.value : undefined
    )
    showToast('兑换成功')
    showExchange.value = false
    loadPoints()
    loadAddresses()
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message || '兑换失败'
    showToast(msg)
  } finally {
    exchangeLoading.value = false
  }
}

onMounted(() => {
  fetchProducts()
  loadPoints()
  loadAddresses()
})
</script>

<template>
  <div class="product-list-page">
    <van-nav-bar title="商品列表" />
    <div class="search-bar">
      <van-search v-model="keyword" placeholder="搜索商品名称" @search="onSearch" />
    </div>
    <div class="content">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div class="product-grid">
          <div v-for="p in products" :key="p.id" class="product-card" @click="openDetail(p)">
            <div class="product-image">
              <van-image :src="p.mainImageUrl || p.mainImage" width="100%" height="140px" fit="cover" radius="8" />
              <div v-if="p.stock === 0" class="stock-badge">已售罄</div>
              <div v-else-if="p.stock < 5" class="stock-badge">仅剩 {{ p.stock }}</div>
            </div>
            <div class="product-info">
              <div class="product-name">{{ p.name }}</div>
              <div class="product-meta">
                <span class="type-tag">{{ getTypeText(p.type) }}</span>
              </div>
              <div class="product-bottom">
                <span class="product-price">{{ p.price }}积分</span>
                <span class="product-stock">库存 {{ p.stock }}</span>
              </div>
            </div>
          </div>
        </div>
        <van-empty v-if="!loading && products.length === 0" description="暂无商品" />
      </van-list>
    </div>

    <!-- 商品详情弹窗 -->
    <van-popup v-model:show="showDetail" position="bottom" round style="height: 85%">
      <div v-if="selectedProduct" class="detail-panel">
        <div class="detail-image">
          <van-image :src="selectedProduct.mainImageUrl || selectedProduct.mainImage" width="100%" height="240px" fit="cover" radius="12" />
        </div>
        <div class="detail-body">
          <div class="detail-name">{{ selectedProduct.name }}</div>
          <div class="detail-tags">
            <span class="type-tag">{{ getTypeText(selectedProduct.type) }}</span>
            <span v-if="selectedProduct.categoryName" class="cat-tag">{{ selectedProduct.categoryName }}</span>
          </div>
          <div class="detail-price">{{ selectedProduct.price }}<span class="unit">积分</span></div>
          <div class="detail-stock">库存 {{ selectedProduct.stock }}</div>
          <div v-if="selectedProduct.description" class="detail-desc">{{ selectedProduct.description }}</div>
          <div class="detail-actions">
            <van-button type="primary" block round @click="openExchange(selectedProduct)">立即兑换</van-button>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 兑换确认弹窗 -->
    <van-popup v-model:show="showExchange" position="bottom" round closeable style="height: auto; max-height: 85%;">
      <div v-if="selectedProduct" class="exchange-panel">
        <div class="panel-title">确认兑换</div>

        <div class="product-summary">
          <van-image :src="selectedProduct.mainImageUrl || selectedProduct.mainImage" width="56" height="56" fit="cover" radius="8" />
          <div class="summary-info">
            <div class="summary-name">{{ selectedProduct.name }}</div>
            <div class="summary-price">{{ selectedProduct.price }}积分/件</div>
          </div>
        </div>

        <div class="points-info">
          <div class="points-row">
            <span>当前积分</span>
            <span>{{ userPoints }}</span>
          </div>
          <div class="points-row">
            <span>兑换数量</span>
            <van-stepper v-model="exchangeQty" min="1" :max="selectedProduct.stock" />
          </div>
          <div class="points-row">
            <span>扣除积分</span>
            <span class="text-danger">-{{ selectedProduct.price * exchangeQty }}</span>
          </div>
          <div class="points-row">
            <span>剩余积分</span>
            <span :class="{ 'text-danger': remainingPoints < 0 }">{{ remainingPoints }}</span>
          </div>
        </div>

        <!-- 实物地址 -->
        <div v-if="isPhysical" class="address-form">
          <div class="form-title">收货地址</div>
          <van-field v-model="exchangeAddress.receiver" label="收货人" placeholder="请输入" />
          <van-field v-model="exchangeAddress.phone" label="电话" type="tel" placeholder="请输入" />
          <van-field v-model="exchangeAddress.province" label="省份" placeholder="请输入" />
          <van-field v-model="exchangeAddress.city" label="城市" placeholder="请输入" />
          <van-field v-model="exchangeAddress.district" label="区县" placeholder="请输入" />
          <van-field v-model="exchangeAddress.detail" label="详细地址" placeholder="请输入" rows="2" type="textarea" />
        </div>

        <van-button type="primary" block round :loading="exchangeLoading" @click="handleExchange">确认兑换</van-button>
      </div>
    </van-popup>
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

.search-bar {
  background: var(--bg-card);
  padding: 0 12px;
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
  cursor: pointer;
}

.product-image {
  position: relative;
}

.stock-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  background: rgba(0,0,0,0.6);
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
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 32px;
}

.product-meta {
  margin-bottom: 6px;
}

.type-tag {
  font-size: 10px;
  padding: 2px 6px;
  background: var(--bg-primary);
  color: var(--text-secondary);
  border-radius: 4px;
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

.product-stock {
  font-size: 11px;
  color: var(--text-muted);
}

/* 详情弹窗 */
.detail-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.detail-image {
  flex-shrink: 0;
}

.detail-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.detail-name {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
}

.detail-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.cat-tag {
  font-size: 10px;
  padding: 2px 6px;
  background: var(--bg-primary);
  color: var(--text-secondary);
  border-radius: 4px;
}

.detail-price {
  font-size: 24px;
  font-weight: 700;
  color: var(--accent);
  margin-bottom: 8px;
}

.detail-price .unit {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-muted);
}

.detail-stock {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 12px;
}

.detail-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  padding: 12px;
  background: var(--bg-primary);
  border-radius: 8px;
  margin-bottom: 16px;
}

.detail-actions {
  padding-top: 8px;
}

/* 兑换弹窗 */
.exchange-panel {
  padding: 20px 16px;
  max-height: 70vh;
  overflow-y: auto;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
}

.product-summary {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: var(--bg-primary);
  border-radius: 10px;
  margin-bottom: 16px;
}

.summary-info { flex: 1; }

.summary-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.summary-price {
  font-size: 13px;
  color: var(--accent);
  font-weight: 600;
}

.points-info {
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  margin-bottom: 16px;
}

.points-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  font-size: 14px;
}

.points-row:not(:last-child) {
  border-bottom: 1px solid var(--border-subtle);
}

.text-danger { color: #ef4444; }

.address-form {
  margin-bottom: 16px;
}

.form-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
</style>