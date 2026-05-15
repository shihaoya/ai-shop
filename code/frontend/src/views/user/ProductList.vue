<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getProducts } from '@/api/user'
import { message } from 'ant-design-vue'
import type { Product } from '@/types/api'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
  loadProducts()
})

const loading = ref(false)
const products = ref<Product[]>([])
const pagination = ref({ page: 1, size: 12, total: 0 })
const detailVisible = ref(false)
const selectedProduct = ref<Product | null>(null)

async function loadProducts() {
  loading.value = true
  try {
    const res = await getProducts({
      page: pagination.value.page,
      size: pagination.value.size
    })
    products.value = res.list.map(p => ({
      ...p,
      id: String(p.id)
    }))
    pagination.value.total = res.total
    } catch (e: any) {
      message.error(e?.message || '加载商品失败')
    } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function handlePageSizeChange(size: number) {
  pagination.value.size = size
  pagination.value.page = 1
  loadProducts()
}

function showDetail(product: Product) {
  selectedProduct.value = product
  detailVisible.value = true
}

function getStatusTag(status: number) {
  const map: Record<number, { text: string; class: string }> = {
    0: { text: '已下架', class: 'gray' },
    1: { text: '上架中', class: 'green' },
  }
  return map[status] ?? { text: '未知', class: 'gray' }
}

function getTypeText(type: string | number) {
  const t = Number(type)
  if (t === 1) return '虚拟'
  if (t === 2) return '实物'
  return String(type)
}
</script>

<template>
  <div id="page-product-list">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <div class="page-head">
        <h2><span class="accent-line"></span>商品列表</h2>
      </div>

      <!-- 卡片列表 -->
      <a-spin :spinning="loading">
        <div v-if="products.length === 0" class="empty-state">
          <i class="fas fa-store"></i>
          <p>暂无商品</p>
        </div>

        <div v-else class="product-grid">
          <div
            v-for="product in products"
            :key="product.id"
            class="product-card"
            @click="showDetail(product)"
          >
            <div class="card-img-wrap">
              <img
                v-if="(product as any).mainImageUrl"
                :src="(product as any).mainImageUrl"
                class="card-img"
              />
              <div v-else class="card-img-placeholder">
                <i class="fas fa-box"></i>
              </div>
              <span :class="['card-status-tag', getStatusTag(product.status).class]">
                {{ getStatusTag(product.status).text }}
              </span>
            </div>
            <div class="card-body">
              <div class="card-name">{{ product.name }}</div>
              <div class="card-meta">
                <span class="type-tag" :class="getTypeText(product.type) === '虚拟' ? 'blue' : 'orange'">
                  {{ getTypeText(product.type) }}
                </span>
                <span>{{ product.categoryName || '未分类' }}</span>
              </div>
              <div class="card-info">
                <span class="price-value">{{ product.price }}</span>
                <span>库存 {{ product.stock }}</span>
              </div>
            </div>
          </div>
        </div>
      </a-spin>

      <!-- 分页 -->
      <div v-if="products.length > 0" class="pagination-wrapper">
        <a-pagination
          :current="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          :show-size-changer="true"
          :page-size-options="['12', '24', '36', '48']"
          show-quick-jumper
          @change="handlePageChange"
          @showSizeChange="handlePageSizeChange"
        />
      </div>
    </div>

    <!-- 商品详情弹框 -->
    <a-modal
      v-model:open="detailVisible"
      :footer="null"
      :centered="true"
      :width="500"
      class="cyber-modal"
    >
      <template v-if="selectedProduct">
        <div class="detail-body">
          <div class="detail-image">
            <img
              v-if="(selectedProduct as any).mainImageUrl"
              :src="(selectedProduct as any).mainImageUrl"
            />
            <div v-else class="detail-image-placeholder">
              <i class="fas fa-box-open"></i>
            </div>
          </div>
          <h2 class="detail-title">{{ selectedProduct.name }}</h2>
          <div class="detail-meta-row">
            <span :class="['status-tag', getStatusTag(selectedProduct.status).class]">
              {{ getStatusTag(selectedProduct.status).text }}
            </span>
            <span class="detail-label">{{ getTypeText(selectedProduct.type) }}</span>
            <span class="detail-label">{{ selectedProduct.categoryName || '未分类' }}</span>
          </div>
          <div class="detail-price-row">
            <span class="detail-price-value">{{ selectedProduct.price }}</span>
            <span class="detail-price-unit">积分</span>
          </div>
          <div class="detail-stock-row">
            <i class="fas fa-boxes-stacked"></i>
            <span>库存: {{ selectedProduct.stock }}</span>
          </div>
          <p v-if="selectedProduct.description" class="detail-desc">
            {{ selectedProduct.description }}
          </p>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<style scoped>
#page-product-list {
  min-height: 100vh;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
  padding: 20px;
}

.page-head {
  margin-bottom: 24px;
}
.page-head h2 {
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}
.accent-line {
  display: inline-block;
  width: 4px;
  height: 20px;
  background: var(--accent);
  border-radius: 2px;
}

/* 卡片网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  position: relative;
  min-height: 300px;
}

.product-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s ease;
}

.product-card:hover {
  border-color: var(--border-glow);
  box-shadow: 0 4px 20px rgba(var(--accent-rgb), 0.10);
  transform: translateY(-2px);
}

.card-img-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: var(--bg-input);
  overflow: hidden;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.card-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 36px;
  opacity: 0.4;
}

.card-status-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  backdrop-filter: blur(4px);
}
.card-status-tag.green { background: rgba(16,185,129,0.75); color: #fff; }
.card-status-tag.gray { background: rgba(156,163,175,0.6);  color: #fff; }

.card-body {
  padding: 10px 12px 12px;
}

.card-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--text-muted);
  margin-bottom: 6px;
}

.type-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
}
.type-tag.blue { background: rgba(6,182,212,0.1); color: var(--cyan); }
.type-tag.orange { background: rgba(245,158,11,0.1); color: var(--orange); }

.card-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-secondary);
}
.card-info .price-value {
  font-weight: 700;
  color: var(--accent-light);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-secondary);
}
.empty-state i {
  font-size: 56px;
  margin-bottom: 16px;
  opacity: 0.4;
}
.empty-state p { font-size: 16px; }

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0 8px;
  margin-top: 8px;
  border-top: 1px solid var(--border-subtle);
}

/* 弹框 */
.cyber-modal :deep(.ant-modal-content) {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius);
  box-shadow: 0 0 40px rgba(0,0,0,0.4);
}
.cyber-modal :deep(.ant-modal-close) { color: var(--text-secondary); }
.cyber-modal :deep(.ant-modal-close:hover) { color: var(--text-primary); }

.detail-body {
  padding: 4px;
}
.detail-image {
  width: 100%;
  height: 240px;
  border-radius: var(--radius);
  overflow: hidden;
  background: var(--bg-input);
  margin-bottom: 16px;
}
.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.detail-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 64px;
}
.detail-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 10px;
}
.detail-meta-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.status-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}
.status-tag.green { background: rgba(16,185,129,0.15); color: #10b981; }
.status-tag.gray { background: rgba(156,163,175,0.15); color: #9ca3af; }
.detail-label {
  font-size: 12px;
  color: var(--text-secondary);
}
.detail-price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 10px;
}
.detail-price-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--accent-light);
  text-shadow: var(--accent-glow-text);
}
.detail-price-unit { font-size: 12px; color: var(--text-muted); }
.detail-stock-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 14px;
}
.detail-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  padding-top: 12px;
  border-top: 1px solid var(--border-subtle);
}
</style>
