<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal, Upload } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons-vue'
import { getProducts, createProduct, updateProduct, deleteProduct, getCategories } from '@/api/operator'
import ImageUploader from '@/components/upload/ImageUploader.vue'
import AnimatedSelect from '@/components/AnimatedSelect.vue'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { useOperatorShop } from '@/composables/useOperatorShop'
import { ProductStatus, ProductStatusText, ProductStatusClass } from '@/types/enums'
import type { Product, Category } from '@/types/api'

const themeStore = useThemeStore()
const userStore = useUserStore()
const router = useRouter()
const { isApproved } = useOperatorShop()

onMounted(() => {
  themeStore.init()
  loadProducts()
})

const loading = ref(false)
const products = ref<Product[]>([])
const categories = ref<Category[]>([])
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
    message.error(e?.message || '加载商品失败')
  } finally {
    loading.value = false
  }
}

const categoryOptions = computed(() =>
  categories.value.map(c => ({ label: c.name, value: c.id }))
)

async function loadCategories() {
  try {
    const res = await getCategories()
    categories.value = res.map((c: Category) => ({
      ...c,
      id: String(c.id)
    }))
  } catch (e: any) {
    message.error(e?.message || '加载分类失败')
  }
}

async function ensureCategories() {
  if (categories.value.length === 0) {
    await loadCategories()
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadProducts()
}

// 新增商品弹框
const modalVisible = ref(false)
const modalTitle = ref('新增商品')
const editingProduct = ref<Product | null>(null)
const formState = ref({
  name: '',
  categoryId: '',
  type: 1,
  price: 0,
  stock: 0,
  limitPerUser: 0,
  mainImage: [] as string[],
  detailImages: [] as string[],
  description: ''
})


/** 处理主图变更 */
function onMainImageChange(val: string[]) {
  formState.value.mainImage = val
}

const submitLoading = ref(false)
const previewVisible = ref(false)
const previewImageUrl = ref('')

// 视图切换
const viewMode = ref<'table' | 'card'>('table')

async function openAddModal() {
  await ensureCategories()
  modalTitle.value = '新增商品'
  editingProduct.value = null
  formState.value = {
    name: '',
    categoryId: categories.value[0]?.id || '',
    type: 1,
    price: 0,
    stock: 0,
    limitPerUser: 0,
    mainImage: [],
    detailImages: [],
    description: ''
  }
  modalVisible.value = true
}

async function openEditModal(product: Product) {
  await ensureCategories()
  modalTitle.value = '编辑商品'
  editingProduct.value = product
  const rawMainImage = (product as any).mainImage
  let detailIds: string[] = []
  const rawDetail = (product as any).detailImages
  if (rawDetail && rawDetail.trim()) {
    detailIds = rawDetail.split(',').map((s: string) => s.trim()).filter((s: string) => !!s)
  } else {
    detailIds = []
  }
  formState.value = {
    name: product.name,
    categoryId: String(product.categoryId),
    type: Number(product.type),
    price: product.price,
    stock: product.stock,
    limitPerUser: (product as any).limitPerUser || 0,
    mainImage: rawMainImage ? [rawMainImage] : [],
    detailImages: detailIds,
    description: product.description || ''
  }
  modalVisible.value = true
}

async function handleSubmit() {
  if (!formState.value.name.trim()) {
    message.warning('请输入商品名称')
    return
  }
  if (!formState.value.categoryId) {
    message.warning('请选择分类')
    return
  }
  if (formState.value.price <= 0) {
    message.warning('请输入有效的积分价格')
    return
  }
  if (formState.value.stock < 0) {
    message.warning('库存不能为负数')
    return
  }
  submitLoading.value = true
  try {
    const isEdit = !!editingProduct.value
    const data: Record<string, any> = {
      name: formState.value.name.trim(),
      categoryId: formState.value.categoryId,
      type: Number(formState.value.type),
      price: Number(formState.value.price),
      stock: Number(formState.value.stock),
      limitPerUser: Number(formState.value.limitPerUser) || 0,
      description: formState.value.description.trim() || undefined
    }
    // 主图：ImageUploader 返回值是数组，但后端字段是单字符串，取第一个
    const mainId = formState.value.mainImage?.[0] || null
    if (isEdit) {
      data.mainImage = mainId // 编辑时传 null 可清空
    } else if (mainId) {
      data.mainImage = mainId
    }
    // 详情图
    const detailStr = formState.value.detailImages.length > 0
      ? formState.value.detailImages.join(',')
      : null
    if (isEdit) {
      data.detailImages = detailStr
    } else if (detailStr) {
      data.detailImages = detailStr
    }
    if (editingProduct.value) {
      await updateProduct(editingProduct.value.id, data as any)
      message.success('商品更新成功')
    } else {
      await createProduct(data as any)
      message.success('商品创建成功')
    }
    modalVisible.value = false
    loadProducts()
  } catch (e: any) {
    message.error(e?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleToggleStatus(product: Product) {
  const newStatus = product.status === ProductStatus.ON ? ProductStatus.OFF : ProductStatus.ON
  const actionText = newStatus === ProductStatus.ON ? '上架' : '下架'

  Modal.confirm({
    title: `确认${actionText}`,
    content: `确定要将商品"${product.name}"设置为${actionText}状态吗？`,
    okText: '确认',
    cancelText: '取消',
    maskClosable: false,
    onOk: async () => {
      try {
        await updateProduct(product.id, { status: newStatus })
        message.success(`${actionText}成功`)
        loadProducts()
      } catch (e: any) {
        message.error(e?.message || `${actionText}失败`)
      }
    }
  })
}

function handleDelete(product: Product) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除商品「${product.name}」吗？删除后不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deleteProduct(product.id)
        message.success('删除成功')
        loadProducts()
      } catch (e: any) {
        message.error(e?.message || '删除失败')
      }
    }
  })
}

function getStatusTag(status: number) {
  return {
    text: ProductStatusText[status] || '未知',
    class: ProductStatusClass[status] || 'gray'
  }
}

function getTypeTag(type: string | number) {
  const t = Number(type)
  const map: Record<number, { text: string; class: string }> = {
    1: { text: '虚拟', class: 'blue' },
    2: { text: '实物', class: 'orange' },
  }
  return map[t] || { text: String(type), class: 'gray' }
}
</script>

<template>
  <div id="page-product-manage">
    <!-- BG -->
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <!-- 店铺未审核通过提示 -->
      <div v-if="userStore.token && !isApproved()" class="shop-status-banner">
        <i class="fas fa-exclamation-triangle" style="margin-right:8px;"></i>
        您的店铺尚未审核通过，暂时无法管理商品
      </div>

      <!-- Page Head -->
      <div class="page-head">
        <h2><span class="accent-line"></span>商品管理</h2>
        <div class="actions">
          <button class="cyber-btn" :class="{ active: viewMode === 'table' }" @click="viewMode = 'table'" title="表格视图">
            <i class="fas fa-table"></i>
          </button>
          <button class="cyber-btn" :class="{ active: viewMode === 'card' }" @click="viewMode = 'card'" title="卡片视图">
            <i class="fas fa-th-large"></i>
          </button>
          <button class="cyber-btn" @click="loadProducts">
            <i class="fas fa-sync-alt"></i>
          </button>
          <button class="cyber-btn cyber-btn-primary" :disabled="!isApproved()" @click="openAddModal">
            <i class="fas fa-plus"></i>新增商品
          </button>
        </div>
      </div>

      <!-- 表格视图 -->
      <div class="table-card" v-show="viewMode === 'table'">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th style="width:60px;">图片</th>
                <th>商品名称</th>
                <th>分类</th>
                <th>类型</th>
                <th>价格</th>
                <th>库存</th>
                <th>状态</th>
                <th style="width:200px;">操作</th>
              </tr>
            </thead>
            <tbody v-if="loading">
              <tr>
                <td colspan="8" class="empty-cell">加载中...</td>
              </tr>
            </tbody>
            <tbody v-else-if="products.length === 0">
              <tr>
                <td colspan="8" class="empty-cell">
                  <i class="fas fa-box-open" style="font-size:32px;opacity:0.3;"></i>
                  <p>暂无商品数据</p>
                </td>
              </tr>
            </tbody>
            <tbody v-else>
              <tr v-for="product in products" :key="product.id">
                <td>
                  <img
                    v-if="(product as any).mainImageUrl"
                    :src="(product as any).mainImageUrl"
                    class="table-thumb"
                  />
                  <div v-else class="table-thumb-placeholder">
                    <i class="fas fa-box"></i>
                  </div>
                </td>
                <td><strong>{{ product.name }}</strong></td>
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
                  <button class="action-btn accent" :disabled="!isApproved()" @click="openEditModal(product)" title="编辑">
                    <i class="fas fa-edit"></i>
                  </button>
                  <button
                    class="action-btn"
                    :class="product.status === ProductStatus.ON ? 'warning' : 'success'"
                    style="margin-left:8px;"
                    :disabled="!isApproved()"
                    @click="handleToggleStatus(product)"
                    :title="product.status === ProductStatus.ON ? '下架' : '上架'"
                  >
                    <i :class="product.status === ProductStatus.ON ? 'fas fa-arrow-down' : 'fas fa-arrow-up'"></i>
                  </button>
                  <button class="action-btn red" style="margin-left:8px;" @click="handleDelete(product)" title="删除">
                    <i class="fas fa-trash"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="loading-mask">
          <i class="fas fa-spinner fa-spin"></i>
        </div>
      </div>

      <!-- 卡片视图 -->
      <div v-show="viewMode === 'card'" class="product-grid">
        <div v-if="loading" class="grid-loading">加载中...</div>
        <template v-else-if="products.length === 0">
          <div class="grid-empty">
            <i class="fas fa-box-open" style="font-size:32px;opacity:0.3;"></i>
            <p>暂无商品数据</p>
          </div>
        </template>
        <div v-else v-for="product in products" :key="product.id" class="product-card">
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
              <span :class="['tag', getTypeTag(product.type).class]">
                {{ getTypeTag(product.type).text }}
              </span>
              <span>{{ product.categoryName || '-' }}</span>
            </div>
            <div class="card-info">
              <span><strong>{{ product.price }}</strong> 积分</span>
              <span>库存: {{ product.stock }}</span>
            </div>
            <div class="card-actions">
              <button class="action-btn accent" :disabled="!isApproved()" @click="openEditModal(product)" title="编辑">
                <i class="fas fa-edit"></i>
              </button>
              <button
                class="action-btn"
                :class="product.status === ProductStatus.ON ? 'warning' : 'success'"
                :disabled="!isApproved()"
                @click="handleToggleStatus(product)"
                :title="product.status === ProductStatus.ON ? '下架' : '上架'"
              >
                <i :class="product.status === ProductStatus.ON ? 'fas fa-arrow-down' : 'fas fa-arrow-up'"></i>
              </button>
              <button class="action-btn red" @click="handleDelete(product)" title="删除">
                <i class="fas fa-trash"></i>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-wrap" v-if="pagination.total > 0">
        <div class="pagination-info">共 {{ pagination.total }} 条</div>
        <div class="pagination-controls">
          <button class="cyber-btn-sm" :disabled="pagination.page <= 1" @click="handlePageChange(pagination.page - 1)">
            <i class="fas fa-chevron-left"></i>
          </button>
          <span class="page-info">{{ pagination.page }} / {{ Math.ceil(pagination.total / pagination.size) }}</span>
          <button class="cyber-btn-sm" :disabled="pagination.page >= Math.ceil(pagination.total / pagination.size)" @click="handlePageChange(pagination.page + 1)">
            <i class="fas fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑商品弹框 -->
    <div class="modal-overlay" v-if="modalVisible">
      <div class="modal-card">
        <div class="modal-header">
          <h3>{{ modalTitle }}</h3>
          <button class="modal-close" @click="modalVisible = false">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div style="margin-bottom:16px;">
            <label style="display:block;margin-bottom:6px;color:var(--text-secondary);font-size:13px;">商品名称 <span style="color:#ef4444;">*</span></label>
            <input v-model="formState.name" class="cyber-input" type="text" placeholder="请输入商品名称" style="width:100%;" />
          </div>
          <div style="margin-bottom:16px;">
            <label style="display:block;margin-bottom:6px;color:var(--text-secondary);font-size:13px;">商品分类 <span style="color:#ef4444;">*</span></label>
            <AnimatedSelect
              v-model="formState.categoryId"
              :options="categoryOptions"
              placeholder="请选择分类"
              searchable
            />
          </div>
          <div style="margin-bottom:16px;">
            <label style="display:block;margin-bottom:6px;color:var(--text-secondary);font-size:13px;">发货类型</label>
            <div class="radio-group">
              <label class="radio-item" :class="{ active: formState.type === 1 }">
                <input type="radio" v-model.number="formState.type" :value="1" />
                <span class="radio-dot"></span>
                <span>虚拟</span>
              </label>
              <label class="radio-item" :class="{ active: formState.type === 2 }">
                <input type="radio" v-model.number="formState.type" :value="2" />
                <span class="radio-dot"></span>
                <span>实物</span>
              </label>
            </div>
          </div>
          <div style="display:flex;gap:12px;margin-bottom:16px;">
            <div style="flex:1;">
              <label style="display:block;margin-bottom:6px;color:var(--text-secondary);font-size:13px;">积分价格 <span style="color:#ef4444;">*</span></label>
              <input v-model.number="formState.price" class="cyber-input" type="number" min="0" placeholder="0" style="width:100%;" />
            </div>
            <div style="flex:1;">
              <label style="display:block;margin-bottom:6px;color:var(--text-secondary);font-size:13px;">库存</label>
              <input v-model.number="formState.stock" class="cyber-input" type="number" min="0" placeholder="0" style="width:100%;" />
            </div>
          </div>

          <!-- 主图上传 -->
          <div style="margin-bottom:16px;">
            <label style="display:block;margin-bottom:6px;color:var(--text-secondary);font-size:13px;">商品主图</label>
            <ImageUploader
              :modelValue="formState.mainImage"
              @update:modelValue="onMainImageChange"
              businessType="product"
              :maxCount="1"
            />
          </div>

          <!-- 详情图上传 -->
          <div style="margin-bottom:16px;">
            <label style="display:block;margin-bottom:6px;color:var(--text-secondary);font-size:13px;">详情图（最多10张）</label>
            <ImageUploader
              :modelValue="formState.detailImages"
              @update:modelValue="(val) => formState.detailImages = val"
              businessType="product"
              :maxCount="10"
            />
          </div>

          <div style="margin-bottom:16px;">
            <label style="display:block;margin-bottom:6px;color:var(--text-secondary);font-size:13px;">每人限购数量</label>
            <input v-model.number="formState.limitPerUser" class="cyber-input" type="number" min="0" placeholder="0表示不限购" style="width:100%;" />
          </div>
          <div style="margin-bottom:16px;">
            <label style="display:block;margin-bottom:6px;color:var(--text-secondary);font-size:13px;">商品描述</label>
            <textarea v-model="formState.description" class="cyber-textarea" placeholder="可选，商品描述" rows="3" style="width:100%;resize:none;"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cyber-btn" @click="modalVisible = false">取消</button>
          <button class="cyber-btn cyber-btn-primary" :disabled="submitLoading" @click="handleSubmit">
            <span v-if="submitLoading"><i class="fas fa-spinner fa-spin" style="margin-right:5px;"></i>提交中</span>
            <span v-else>确认</span>
          </button>
        </div>
      </div>
    </div>
    <!-- 详情图预览弹框 -->
    <div v-if="previewVisible" class="modal-overlay" @click.self="previewVisible = false">
      <div class="modal-card" style="max-width:800px;padding:0;background:transparent;box-shadow:none;">
        <div style="position:relative;">
          <button class="modal-close" @click="previewVisible = false" style="position:absolute;top:8px;right:8px;z-index:10;background:rgba(0,0,0,0.5);border-radius:50%;color:#fff;width:32px;height:32px;">
            <i class="fas fa-times"></i>
          </button>
          <img :src="previewImageUrl" style="max-width:100%;max-height:80vh;border-radius:12px;" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#page-product-manage {
  min-height: 100vh;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
  padding: 24px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-head h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}

.accent-line {
  width: 4px;
  height: 20px;
  background: var(--accent);
  border-radius: 2px;
}

.actions {
  display: flex;
  gap: 12px;
}

.cyber-btn {
  padding: 8px 16px;
  border: 1px solid var(--border-subtle);
  background: var(--bg-card);
  color: var(--text-secondary);
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
}

.cyber-btn:hover {
  background: var(--bg-card-hover);
  color: var(--text-primary);
  border-color: var(--accent);
}

.cyber-btn-primary {
  background: linear-gradient(135deg, rgba(99,102,241,0.3), rgba(139,92,246,0.3));
  border-color: var(--accent);
  color: var(--accent);
}

.cyber-btn.active {
  background: rgba(var(--accent-rgb), 0.12);
  border-color: var(--accent);
  color: var(--accent);
}

.cyber-btn-primary:hover {
  background: linear-gradient(135deg, rgba(99,102,241,0.5), rgba(139,92,246,0.5));
  border-color: var(--accent);
}

.cyber-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  pointer-events: none;
}

.cyber-btn-sm {
  padding: 5px 10px;
  border: 1px solid var(--border-subtle);
  background: var(--bg-card);
  color: var(--text-secondary);
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.cyber-btn-sm:hover {
  background: var(--bg-card-hover);
  color: var(--text-primary);
}

.cyber-btn-sm:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.cyber-btn-danger {
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.3);
}

.cyber-btn-danger:hover {
  background: rgba(239, 68, 68, 0.1);
  border-color: #ef4444;
}

.cyber-btn-warning {
  color: #f59e0b;
  border-color: rgba(245, 158, 11, 0.3);
}

.cyber-btn-warning:hover {
  background: rgba(245, 158, 11, 0.1);
  border-color: #f59e0b;
}

.cyber-btn-success {
  color: #10b981;
  border-color: rgba(16, 185, 129, 0.3);
}

.cyber-btn-success:hover {
  background: rgba(16, 185, 129, 0.1);
  border-color: #10b981;
}

.cyber-input {
  padding: 9px 12px;
  border: 1px solid var(--border-subtle);
  background: var(--bg-input);
  color: var(--text-primary);
  border-radius: var(--radius);
  font-size: 13px;
  outline: none;
  transition: all 0.2s;
}

.cyber-input:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(var(--accent-rgb), 0.10), inset 0 0 10px rgba(var(--accent-rgb), 0.03);
}

.cyber-textarea {
  padding: 9px 12px;
  border: 1px solid var(--border-subtle);
  background: var(--bg-input);
  color: var(--text-primary);
  border-radius: var(--radius);
  font-size: 13px;
  outline: none;
  transition: all 0.2s;
  font-family: inherit;
}

.cyber-textarea:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(var(--accent-rgb), 0.10), inset 0 0 10px rgba(var(--accent-rgb), 0.03);
}

/* 店铺状态警告 */
.shop-status-banner {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, rgba(249,115,22,0.15), rgba(234,88,12,0.10));
  border: 1px solid rgba(249,115,22,0.4);
  border-radius: var(--radius);
  color: #f97316;
  font-size: 13px;
  font-weight: 500;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

.pagination-info {
  font-size: 13px;
  color: var(--text-muted);
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-info {
  font-size: 13px;
  color: var(--text-secondary);
  padding: 0 8px;
}

/* 表格缩略图 */
.table-thumb {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  object-fit: cover;
  display: block;
}
.table-thumb-placeholder {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  background: var(--bg-input);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 18px;
}

/* 卡片网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin-top: 4px;
}
.grid-loading,
.grid-empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  color: var(--text-muted);
  font-size: 14px;
}
.product-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius);
  overflow: hidden;
  transition: all 0.25s ease;
  animation: fadeInUp 0.35s ease-out;
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
.card-status-tag.blue { background: rgba(59,130,246,0.75); color: #fff; }
.card-status-tag.orange { background: rgba(249,115,22,0.75); color: #fff; }
.card-status-tag.green { background: rgba(16,185,129,0.75); color: #fff; }
.card-status-tag.red { background: rgba(239,68,68,0.75); color: #fff; }
.card-status-tag.gray { background: rgba(156,163,175,0.6); color: #fff; }
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
.card-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.card-actions {
  display: flex;
  gap: 4px;
  padding-top: 8px;
  border-top: 1px solid var(--border-subtle);
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
}

.modal-card {
  background: var(--bg-card);
  border: 1px solid var(--border-glow);
  border-radius: var(--radius);
  width: 520px;
  max-width: 90vw;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  box-shadow: var(--accent-glow), 0 20px 50px rgba(0,0,0,0.4);
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
  color: var(--text-primary);
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  transition: color 0.3s;
}

.modal-close:hover {
  color: var(--accent);
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 12px 20px;
  border-top: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

/* Loading */
.loading-mask {
  position: absolute;
  inset: 0;
  background: rgba(7, 8, 22, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--accent);
}

/* 操作按钮 */
.action-btn {
  padding: 6px 10px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  border-radius: var(--radius-xs);
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
}

.action-btn:hover {
  background: var(--bg-card-hover);
  color: var(--accent);
}

.action-btn.accent:hover {
  background: rgba(99, 102, 241, 0.1);
  color: var(--accent);
}

.action-btn.red:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.action-btn.warning:hover {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.action-btn.success:hover {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

/* 标签 */
.tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.tag.blue { background: rgba(59, 130, 246, 0.15); color: #60a5fa; }
.tag.orange { background: rgba(249, 115, 22, 0.15); color: #f97316; }
.tag.green { background: rgba(16, 185, 129, 0.15); color: #10b981; }
.tag.red { background: rgba(239, 68, 68, 0.15); color: #ef4444; }
.tag.gray { background: rgba(156, 163, 175, 0.15); color: #9ca3af; }

/* Empty */
.empty-cell {
  text-align: center;
  padding: 40px !important;
  color: var(--text-muted);
}

.empty-cell i {
  display: block;
  margin-bottom: 8px;
}

/* Radio Group */
.radio-group {
  display: flex;
  gap: 12px;
}

.radio-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 16px;
  border: 1px solid var(--border-subtle);
  background: var(--bg-input);
  color: var(--text-secondary);
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
}

.radio-item:hover {
  border-color: var(--accent);
  color: var(--text-primary);
  background: rgba(var(--accent-rgb), 0.04);
}

.radio-item.active {
  border-color: var(--accent);
  color: var(--accent);
  background: rgba(var(--accent-rgb), 0.08);
  box-shadow: 0 0 0 3px rgba(var(--accent-rgb), 0.06), inset 0 0 10px rgba(var(--accent-rgb), 0.02);
}

.radio-item input[type="radio"] {
  display: none;
}

.radio-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 2px solid var(--border-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.radio-item.active .radio-dot {
  border-color: var(--accent);
  box-shadow: inset 0 0 0 3px var(--bg-input);
  background: var(--accent);
}

/* ========== 详情图上传 ========== */
.detail-images-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.detail-upload-item {
  width: 96px;
  height: 96px;
}
</style>