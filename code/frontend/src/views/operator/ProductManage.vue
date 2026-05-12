<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { getProducts, deleteProduct } from '@/api/operator'
import { message } from 'ant-design-vue'
import type { Product } from '@/types/api'

const themeStore = useThemeStore()
const router = useRouter()

onMounted(() => {
  themeStore.init()
  loadProducts()
})

const loading = ref(false)
const products = ref<Product[]>([])
const pagination = ref({ page: 1, size: 10, total: 0 })

async function loadProducts() {
  loading.value = true
  try {
    const res = await getProducts({ page: pagination.value.page, size: pagination.value.size })
    products.value = res.list.map((p: Product) => ({
      ...p,
      id: String(p.id)
    }))
    pagination.value.total = res.total
  } catch (e: any) {
    console.error('加载商品列表失败:', e)
    message.error(e?.message || (e as Error)?.message || '加载失败')
    throw e
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number, size: number) {
  pagination.value.page = page
  pagination.value.size = size
  loadProducts()
}

function handleAdd() {
  router.push('/operator/product-edit')
}

function handleEdit(productId: string) {
  router.push(`/operator/product-edit?id=${productId}`)
}

async function handleToggleStatus(product: Product) {
  const newStatus = product.status === 1 ? 0 : 1
  try {
    // 导入 updateProduct 来切换状态
    const { updateProduct } = await import('@/api/operator')
    await updateProduct(product.id, { status: newStatus })
    message.success(newStatus === 1 ? '上架成功' : '下架成功')
    loadProducts()
  } catch (e) {
    throw e
  }
}

async function handleDelete(productId: string) {
  try {
    await deleteProduct(productId)
    message.success('删除成功')
    loadProducts()
  } catch (e) {
    throw e
  }
}

function getStatusTag(status: number) {
  const map: Record<number, { text: string; class: string }> = {
    0: { text: '已下架', class: 'red' },
    1: { text: '已上架', class: 'green' },
  }
  return map[status] || { text: '未知', class: 'gray' }
}

function getTypeTag(type: string) {
  const map: Record<string, { text: string; class: string }> = {
    virtual: { text: '虚拟', class: 'blue' },
    physical: { text: '实物', class: 'orange' },
  }
  return map[type] || { text: type, class: 'gray' }
}
</script>

<template>
  <div id="page-product-manage">
    <!-- BG -->
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <!-- Page Head -->
      <div class="page-head">
        <h2><span class="accent-line"></span>商品管理</h2>
        <div class="actions">
          <button class="cyber-btn" @click="loadProducts">
            <i class="fas fa-sync-alt" style="margin-right:5px;"></i>刷新
          </button>
          <button class="cyber-btn cyber-btn-primary" @click="handleAdd">
            <i class="fas fa-plus" style="margin-right:5px;"></i>新增商品
          </button>
        </div>
      </div>

      <!-- Table Card -->
      <div class="table-card">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>商品名称</th>
                <th>分类</th>
                <th>类型</th>
                <th>价格</th>
                <th>库存</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="8" style="text-align:center;padding:40px;">
                  <i class="fas fa-spinner fa-spin" style="font-size:24px;color:var(--text-secondary);"></i>
                </td>
              </tr>
              <tr v-else-if="products.length === 0">
                <td colspan="8" style="text-align:center;padding:40px;color:var(--text-secondary);">
                  暂无商品数据
                </td>
              </tr>
              <tr v-for="product in products" :key="product.id">
                <td>{{ product.id }}</td>
                <td>{{ product.name }}</td>
                <td>{{ product.categoryName || '-' }}</td>
                <td>
                  <span :class="['tag', getTypeTag(product.type).class]">
                    {{ getTypeTag(product.type).text }}
                  </span>
                </td>
                <td>{{ product.price }} 积分</td>
                <td>{{ product.stock }}</td>
                <td>
                  <span :class="['tag', getStatusTag(product.status).class]">
                    {{ getStatusTag(product.status).text }}
                  </span>
                </td>
                <td>
                  <div class="action-btns">
                    <button class="cyber-btn-sm" @click="handleEdit(product.id)">
                      <i class="fas fa-edit"></i>
                    </button>
                    <button
                      class="cyber-btn-sm"
                      :class="product.status === 1 ? 'cyber-btn-warning' : 'cyber-btn-success'"
                      @click="handleToggleStatus(product)"
                    >
                      <i :class="product.status === 1 ? 'fas fa-arrow-down' : 'fas fa-arrow-up'"></i>
                    </button>
                    <button class="cyber-btn-sm cyber-btn-danger" @click="handleDelete(product.id)">
                      <i class="fas fa-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="pagination-wrap" v-if="pagination.total > 0">
          <div class="pagination-info">
            共 {{ pagination.total }} 条
          </div>
          <div class="pagination-controls">
            <button
              class="cyber-btn-sm"
              :disabled="pagination.page <= 1"
              @click="handlePageChange(pagination.page - 1, pagination.size)"
            >
              <i class="fas fa-chevron-left"></i>
            </button>
            <span class="page-num">{{ pagination.page }} / {{ Math.ceil(pagination.total / pagination.size) }}</span>
            <button
              class="cyber-btn-sm"
              :disabled="pagination.page >= Math.ceil(pagination.total / pagination.size)"
              @click="handlePageChange(pagination.page + 1, pagination.size)"
            >
              <i class="fas fa-chevron-right"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.action-btns {
  display: flex;
  gap: 6px;
  justify-content: center;
}

.cyber-btn-sm {
  padding: 4px 8px;
  font-size: 12px;
  border: 1px solid var(--border-color);
  background: rgba(255,255,255,0.03);
  color: var(--text-primary);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.cyber-btn-sm:hover {
  background: rgba(99,102,241,0.15);
  border-color: var(--primary-color);
}

.cyber-btn-sm.cyber-btn-danger:hover {
  background: rgba(239,68,68,0.15);
  border-color: #ef4444;
}

.cyber-btn-sm.cyber-btn-success:hover {
  background: rgba(34,197,94,0.15);
  border-color: #22c55e;
}

.cyber-btn-sm.cyber-btn-warning:hover {
  background: rgba(234,179,8,0.15);
  border-color: #eab308;
}

.cyber-btn-sm:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.cyber-btn-primary {
  background: linear-gradient(135deg, rgba(99,102,241,0.2), rgba(139,92,246,0.2));
  border-color: var(--primary-color);
}

.cyber-btn-primary:hover {
  background: linear-gradient(135deg, rgba(99,102,241,0.35), rgba(139,92,246,0.35));
}

.pagination-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid var(--border-color);
}

.pagination-info {
  color: var(--text-secondary);
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-num {
  color: var(--text-primary);
  font-size: 14px;
  min-width: 60px;
  text-align: center;
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.tag.green {
  background: rgba(34,197,94,0.15);
  color: #22c55e;
  border: 1px solid rgba(34,197,94,0.3);
}

.tag.red {
  background: rgba(239,68,68,0.15);
  color: #ef4444;
  border: 1px solid rgba(239,68,68,0.3);
}

.tag.orange {
  background: rgba(234,179,8,0.15);
  color: #eab308;
  border: 1px solid rgba(234,179,8,0.3);
}

.tag.blue {
  background: rgba(59,130,246,0.15);
  color: #3b82f6;
  border: 1px solid rgba(59,130,246,0.3);
}

.tag.gray {
  background: rgba(156,163,175,0.15);
  color: #9ca3af;
  border: 1px solid rgba(156,163,175,0.3);
}
</style>
