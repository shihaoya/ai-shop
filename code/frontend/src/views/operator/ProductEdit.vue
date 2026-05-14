<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { getProduct, createProduct, updateProduct, getCategories } from '@/api/operator'
import { message } from 'ant-design-vue'
import type { Product, Category } from '@/types/api'
import { useOperatorShop } from '@/composables/useOperatorShop'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()
const { isApproved } = useOperatorShop()

// 判断是新增还是编辑
const isEdit = computed(() => !!route.params.id)
const pageTitle = computed(() => isEdit.value ? '编辑商品' : '新增商品')

// 分类列表
const categories = ref<Category[]>([])
const categoryLoading = ref(false)

// 表单数据
const form = ref({
  name: '',
  categoryId: '',
  type: 'physical' as string,
  price: 0,
  stock: 0,
  image: '',
  description: '',
})

// 加载状态
const loading = ref(false)
const initLoading = ref(false)

// 商品类型选项
const typeOptions = [
  { value: 'physical', label: '实物商品' },
  { value: 'virtual', label: '虚拟商品' },
]

// 初始化
onMounted(async () => {
  themeStore.init()
  await loadCategories()
  if (isEdit.value) {
    await loadProduct()
  }
})

// 加载分类列表
async function loadCategories() {
  categoryLoading.value = true
  try {
    const res = await getCategories()
    categories.value = res || []
  } catch (e) {
    console.error('加载分类失败', e)
    throw e
  } finally {
    categoryLoading.value = false
  }
}

// 加载商品信息（编辑模式）
async function loadProduct() {
  const id = route.params.id
  if (!id) return

  const productId = Array.isArray(id) ? id[0] : id
  initLoading.value = true
  try {
    const res = await getProduct(productId)
    if (res) {
      const product = res
      form.value = {
        name: product.name || '',
        categoryId: String(product.categoryId || ''),
        type: product.type || 'physical',
        price: product.price || 0,
        stock: product.stock || 0,
        image: product.image || '',
        description: product.description || '',
      }
    } else {
      message.error('加载商品信息失败')
    }
  } catch (e) {
    throw e
  } finally {
    initLoading.value = false
  }
}

// 保存商品
async function handleSave() {
  // 店铺审核校验
  if (!isApproved()) {
    message.error('您的店铺尚未审核通过，暂时无法添加或编辑商品')
    return
  }

  // 表单验证
  if (!form.value.name.trim()) {
    message.error('请输入商品名称')
    return
  }
  if (!form.value.categoryId) {
    message.error('请选择商品分类')
    return
  }
  if (form.value.price < 0) {
    message.error('价格不能为负数')
    return
  }
  if (form.value.stock < 0) {
    message.error('库存不能为负数')
    return
  }

  loading.value = true
  try {
    const data = {
      name: form.value.name,
      categoryId: Number(form.value.categoryId) || 0,
      type: form.value.type,
      price: Number(form.value.price) || 0,
      stock: Number(form.value.stock) || 0,
      image: form.value.image,
      description: form.value.description,
    }

    let res
    if (isEdit.value) {
      const id = String(route.params.id)
      res = await updateProduct(id, data as any)
      message.success('商品更新成功')
      router.push('/operator/products')
    } else {
      res = await createProduct(data)
      message.success('商品创建成功')
      router.push('/operator/products')
    }
  } catch (e: any) {
    console.error('保存商品失败:', e)
    message.error(e?.message || (e as Error)?.message || '保存商品失败')
    throw e
  } finally {
    loading.value = false
  }
}

// 返回列表
function handleBack() {
  router.push('/operator/products')
}
</script>

<template>
  <div id="page-product-edit">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <!-- 店铺未审核通过提示 -->
      <div v-if="!isApproved()" class="shop-status-banner">
        <i class="fas fa-exclamation-triangle" style="margin-right:8px;"></i>
        您的店铺尚未审核通过，暂时无法添加或编辑商品
      </div>

      <div class="page-head">
        <h2><span class="accent-line"></span>{{ pageTitle }}</h2>
        <button class="cyber-btn" @click="handleBack">
          <i class="fas fa-arrow-left" style="margin-right:6px;"></i>
          返回列表
        </button>
      </div>

      <div class="cyber-card" v-if="!initLoading">
        <form class="product-form" @submit.prevent="handleSave">
          <!-- 商品名称 -->
          <div class="form-item">
            <label>商品名称 <span class="required">*</span></label>
            <input
              v-model="form.name"
              type="text"
              class="cyber-input"
              placeholder="请输入商品名称"
              maxlength="100"
            />
          </div>

          <!-- 分类 -->
          <div class="form-item">
            <label>商品分类 <span class="required">*</span></label>
            <select v-model="form.categoryId" class="cyber-select">
              <option value="">请选择分类</option>
              <option v-for="cat in categories" :key="String(cat.id)" :value="String(cat.id)">
                {{ cat.name }}
              </option>
            </select>
          </div>

          <!-- 商品类型 -->
          <div class="form-item">
            <label>商品类型 <span class="required">*</span></label>
            <div class="type-radio-group">
              <label class="type-radio" v-for="opt in typeOptions" :key="opt.value">
                <input type="radio" v-model="form.type" :value="opt.value" />
                <span class="type-label">{{ opt.label }}</span>
              </label>
            </div>
          </div>

          <!-- 价格和库存 -->
          <div class="form-row">
            <div class="form-item">
              <label>价格（积分） <span class="required">*</span></label>
              <input
                v-model.number="form.price"
                type="number"
                class="cyber-input"
                placeholder="0"
                min="0"
              />
            </div>
            <div class="form-item">
              <label>库存 <span class="required">*</span></label>
              <input
                v-model.number="form.stock"
                type="number"
                class="cyber-input"
                placeholder="0"
                min="0"
              />
            </div>
          </div>

          <!-- 图片URL -->
          <div class="form-item">
            <label>图片URL</label>
            <input
              v-model="form.image"
              type="text"
              class="cyber-input"
              placeholder="请输入图片链接（可选）"
            />
          </div>

          <!-- 描述 -->
          <div class="form-item">
            <label>商品描述</label>
            <textarea
              v-model="form.description"
              class="cyber-textarea"
              placeholder="请输入商品描述（可选）"
              rows="4"
            ></textarea>
          </div>

          <!-- 操作按钮 -->
          <div class="form-actions">
            <button type="submit" class="cyber-btn-primary" :disabled="loading">
              <i class="fas fa-save" style="margin-right:6px;"></i>
              {{ loading ? '保存中...' : '保存商品' }}
            </button>
            <button type="button" class="cyber-btn" @click="handleBack">
              取消
            </button>
          </div>
        </form>
      </div>

      <!-- 加载状态 -->
      <div v-else class="cyber-card loading-card">
        <i class="fas fa-spinner fa-spin" style="font-size:32px;color:var(--accent);"></i>
        <p>加载商品信息中...</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
#page-product-edit {
  min-height: 100vh;
  position: relative;
}

.product-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.required {
  color: var(--accent);
  margin-left: 4px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 640px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.cyber-input,
.cyber-select,
.cyber-textarea {
  width: 100%;
  padding: 12px 16px;
  background: var(--bg-input);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  font-size: 14px;
  transition: all 0.3s ease;
}

.cyber-input:focus,
.cyber-select:focus,
.cyber-textarea:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: var(--accent-glow);
}

.cyber-input::placeholder,
.cyber-textarea::placeholder {
  color: var(--text-muted);
}

.cyber-select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%238892b0' d='M2 4l4 4 4-4'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 16px center;
  padding-right: 40px;
}

.cyber-textarea {
  resize: vertical;
  min-height: 100px;
}

.type-radio-group {
  display: flex;
  gap: 20px;
}

.type-radio {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.type-radio input[type="radio"] {
  width: 18px;
  height: 18px;
  accent-color: var(--accent);
  cursor: pointer;
}

.type-label {
  font-size: 14px;
  color: var(--text-primary);
}

.form-actions {
  display: flex;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-subtle);
}

.loading-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 16px;
  color: var(--text-secondary);
}
</style>