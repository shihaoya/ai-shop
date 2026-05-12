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
    products.value = res.records.map(p => ({
      ...p,
      id: String(p.id)
    }))
    pagination.value.total = res.total
  } catch (e: any) {
    message.error(e.message || '加载失败')
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

function getTypeText(type: string) {
  return type === 'virtual' ? '虚拟商品' : '实物商品'
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

      <div class="cyber-card">
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
              <div class="product-image">
                <img v-if="product.image" :src="product.image" :alt="product.name" />
                <div v-else class="product-image-placeholder">
                  <i class="fas fa-box-open"></i>
                </div>
                <span :class="['product-status', getStatusTag(product.status).class]">
                  {{ getStatusTag(product.status).text }}
                </span>
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ product.name }}</h3>
                <p class="product-category">{{ product.categoryName || '未分类' }} · {{ getTypeText(product.type) }}</p>
                <div class="product-price">
                  <span class="price-value">{{ product.price }}</span>
                  <span class="price-label">积分</span>
                </div>
                <div class="product-stock">
                  <i class="fas fa-boxes-stacked"></i>
                  <span>库存: {{ product.stock }}</span>
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
        <div class="detail-modal">
          <div class="detail-image">
            <img v-if="selectedProduct.image" :src="selectedProduct.image" :alt="selectedProduct.name" />
            <div v-else class="detail-image-placeholder">
              <i class="fas fa-box-open"></i>
            </div>
          </div>
          <div class="detail-content">
            <h2 class="detail-title">{{ selectedProduct.name }}</h2>
            <div class="detail-meta">
              <span :class="['status-tag', getStatusTag(selectedProduct.status).class]">
                {{ getStatusTag(selectedProduct.status).text }}
              </span>
              <span class="detail-type">{{ getTypeText(selectedProduct.type) }}</span>
              <span class="detail-category">{{ selectedProduct.categoryName || '未分类' }}</span>
            </div>
            <div class="detail-price">
              <span class="price-value">{{ selectedProduct.price }}</span>
              <span class="price-label">积分</span>
            </div>
            <div class="detail-stock">
              <i class="fas fa-boxes-stacked"></i>
              <span>库存: {{ selectedProduct.stock }}</span>
            </div>
            <p v-if="selectedProduct.description" class="detail-desc">
              {{ selectedProduct.description }}
            </p>
          </div>
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

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  padding: 4px;
}

.product-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.product-card:hover {
  border-color: var(--border-glow);
  transform: translateY(-4px);
  box-shadow: var(--accent-glow);
}

.product-image {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
  background: var(--bg-surface);
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 48px;
}

.product-status {
  position: absolute;
  top: 8px;
  right: 8px;
}

.product-info {
  padding: 14px;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-category {
  font-size: 11px;
  color: var(--text-muted);
  margin-bottom: 10px;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 8px;
}

.price-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--accent);
  text-shadow: var(--accent-glow-text);
}

.price-label {
  font-size: 12px;
  color: var(--text-muted);
}

.product-stock {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

.product-stock i {
  color: var(--text-muted);
}

/* 详情弹框样式 */
.detail-modal {
  padding: 8px;
}

.detail-image {
  width: 100%;
  height: 240px;
  border-radius: var(--radius);
  overflow: hidden;
  background: var(--bg-surface);
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

.detail-content {
  padding: 0 4px;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.detail-type,
.detail-category {
  font-size: 12px;
  color: var(--text-secondary);
}

.detail-price {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 10px;
}

.detail-price .price-value {
  font-size: 28px;
}

.detail-stock {
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

.empty-state p {
  font-size: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0 8px;
  margin-top: 8px;
  border-top: 1px solid var(--border-subtle);
}

.cyber-modal :deep(.ant-modal-content) {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius);
  box-shadow: 0 0 40px rgba(0, 0, 0, 0.4);
}

.cyber-modal :deep(.ant-modal-close) {
  color: var(--text-secondary);
}

.cyber-modal :deep(.ant-modal-close:hover) {
  color: var(--text-primary);
}
</style>

<style scoped>
/* Product Grid */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
  position: relative;
  min-height: 300px;
}

.product-card {
  cursor: pointer;
  overflow: hidden;
  padding: 0;
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  border-color: var(--border-glow);
  box-shadow: var(--accent-glow-hover);
}

.product-image {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
  background: var(--bg-input);
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 36px;
}

.image-placeholder.large {
  font-size: 64px;
}

.product-status {
  position: absolute;
  top: 10px;
  left: 10px;
}

.product-info {
  padding: 14px;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.type-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
}

.type-tag.blue {
  background: rgba(6, 182, 212, 0.1);
  color: var(--cyan);
}

.type-tag.orange {
  background: rgba(245, 158, 11, 0.1);
  color: var(--orange);
}

.stock-info {
  font-size: 11px;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

.stock-info.low-stock {
  color: var(--red);
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--accent-light);
  text-shadow: var(--accent-glow-text);
  font-family: var(--font-mono);
}

.price-unit {
  font-size: 12px;
  color: var(--text-muted);
}

/* Empty State */
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  color: var(--text-muted);
}

/* Loading */
.loading-overlay {
  position: absolute;
  inset: 0;
  background: rgba(7, 8, 22, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--accent);
  z-index: 10;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(7, 8, 22, 0.8);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  padding: 0;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
}

.modal-header h3 {
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.close-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-xs);
  border: 1px solid var(--border-subtle);
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-btn:hover {
  border-color: var(--border-glow);
  color: var(--text-primary);
}

.modal-body {
  padding: 20px;
}

.detail-image {
  width: 100%;
  height: 200px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--bg-input);
  margin-bottom: 16px;
}

.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.detail-row .label {
  flex-shrink: 0;
  width: 80px;
  font-size: 12px;
  color: var(--text-muted);
}

.detail-row .value {
  flex: 1;
  font-size: 13px;
  color: var(--text-primary);
}

.detail-row .price-highlight {
  font-size: 16px;
  font-weight: 600;
  color: var(--accent-light);
  text-shadow: var(--accent-glow-text);
}

.detail-row .id-value {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
}

.detail-row .description {
  color: var(--text-secondary);
  line-height: 1.5;
}

.low-stock {
  color: var(--red) !important;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid var(--border-subtle);
}

/* Page Content */
.page-content {
  position: relative;
  z-index: 1;
  padding: 20px;
}
</style>
