<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getProducts, updateProduct, deleteProduct } from '@/api/operator'
import type { Product } from '@/types'

const router = useRouter()

const products = ref<Product[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const size = ref(10)
const keyword = ref('')

async function fetchProducts() {
  loading.value = true
  try {
    const res = await getProducts({ page: page.value, size: size.value, keyword: keyword.value })
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

function goAddProduct() {
  router.push('/mobile/operator/products/add')
}

function goEditProduct(id: string) {
  router.push(`/mobile/operator/products/edit/${id}`)
}

async function handleToggleStatus(p: Product) {
  const newStatus = p.status === 1 ? 0 : 1
  try {
    await showConfirmDialog({
      title: '确认操作',
      message: `确定${newStatus === 1 ? '上架' : '下架'}此商品？`
    })
    await updateProduct(p.id, { status: newStatus })
    showToast(newStatus === 1 ? '已上架' : '已下架')
    p.status = newStatus
  } catch { /* cancelled */ }
}

async function handleDelete(p: Product) {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定删除商品"${p.name}"？此操作不可恢复`
    })
    await deleteProduct(p.id)
    showToast('已删除')
    products.value = products.value.filter(item => item.id !== p.id)
  } catch { /* cancelled */ }
}

onMounted(() => {
  fetchProducts()
})
</script>

<template>
  <div class="product-manage-page">
    <van-nav-bar title="商品管理">
      <template #right>
        <van-button size="small" type="primary" round @click="goAddProduct">新增</van-button>
      </template>
    </van-nav-bar>
    <div class="search-bar">
      <van-search v-model="keyword" placeholder="搜索商品名称" @search="onSearch" />
    </div>
    <div class="content">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div v-for="p in products" :key="p.id" class="product-card">
          <div class="product-top">
            <van-image :src="p.mainImageUrl || p.mainImage || p.image" width="60" height="60" fit="cover" radius="8" />
            <div class="product-body">
              <div class="product-name">{{ p.name }}</div>
              <div class="product-meta">
                <span class="category">{{ p.categoryName || '未分类' }}</span>
                <span class="dot">·</span>
                <span class="type">{{ p.type === 2 || p.type === '2' ? '虚拟' : '实体' }}</span>
                <span class="dot">·</span>
                <span class="price">{{ p.price }}积分</span>
              </div>
              <div class="product-meta">
                <span :class="p.stock < 5 ? 'stock-low' : ''">库存 {{ p.stock }}</span>
              </div>
              <div class="product-status" :class="p.status === 1 ? 'status-on' : 'status-off'">
                {{ p.status === 1 ? '上架中' : '已下架' }}
              </div>
            </div>
          </div>
          <div class="product-actions">
            <van-button size="small" class="action-btn" :class="p.status === 1 ? 'btn-warning' : 'btn-success'" @click="handleToggleStatus(p)">
              {{ p.status === 1 ? '下架' : '上架' }}
            </van-button>
            <van-button size="small" class="action-btn btn-default" @click="goEditProduct(p.id)">编辑</van-button>
            <van-button size="small" class="action-btn btn-danger" @click="handleDelete(p)">删除</van-button>
          </div>
        </div>
        <van-empty v-if="!loading && products.length === 0" description="暂无商品" />
      </van-list>
    </div>
  </div>
</template>

<style scoped>
.product-manage-page {
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

.product-card {
  margin: 10px 12px;
  background: var(--bg-card);
  border-radius: 12px;
  overflow: hidden;
}

.product-top {
  display: flex;
  gap: 12px;
  padding: 12px;
}

.product-body { flex: 1; }

.product-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 4px;
}

.dot { margin: 0 4px; }

.category, .type { color: var(--text-muted); }
.price { color: var(--accent); font-weight: 600; }
.stock-low { color: #ef4444; }

.product-status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  width: fit-content;
}

.status-on { background: #dcfce7; color: #16a34a; }
.status-off { background: #f5f5f5; color: #8b8ba7; }

.product-actions {
  display: flex;
  border-top: 1px solid var(--border-subtle);
}

.action-btn {
  flex: 1;
  border: none;
  border-radius: 0;
  font-size: 12px;
  height: 36px;
}

.action-btn:not(:last-child) {
  border-right: 1px solid var(--border-subtle);
}

.btn-primary { color: var(--accent); }
.btn-default { color: #666; }
.btn-warning { color: var(--accent); }
.btn-success { color: var(--accent); }
.btn-danger { color: #ef4444; background: #fff5f5; }

.add-btn {
  margin: 12px;
  border-radius: 8px;
}
</style>