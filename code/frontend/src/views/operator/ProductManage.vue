<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { operatorApi } from '@/api/operator'
import type { Product, Category } from '@/types'

const loading = ref(false)
const products = ref<Product[]>([])
const categories = ref<Category[]>([])
const pagination = ref({ page: 1, pageSize: 10, total: 0 })
const viewMode = ref<'card' | 'table'>('card')
const showForm = ref(false)
const editingId = ref<string | null>(null)

const form = ref({
  name: '',
  description: '',
  image: '',
  points: 0,
  stock: 0,
  categoryId: '',
})
const error = ref('')

async function fetchProducts() {
  loading.value = true
  try {
    const res = await operatorApi.getProducts({ page: pagination.value.page, pageSize: pagination.value.pageSize })
    products.value = res.data.list
    pagination.value.total = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const res = await operatorApi.getCategories()
    categories.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

function openForm(product?: Product) {
  if (product) {
    editingId.value = product.id
    form.value = {
      name: product.name,
      description: product.description || '',
      image: product.image || '',
      points: product.points,
      stock: product.stock,
      categoryId: product.categoryId,
    }
  } else {
    editingId.value = null
    form.value = { name: '', description: '', image: '', points: 0, stock: 0, categoryId: '' }
  }
  error.value = ''
  showForm.value = true
}

async function handleSubmit() {
  if (!form.value.name || !form.value.categoryId) {
    error.value = '请填写必填项'
    return
  }
  try {
    if (editingId.value) {
      await operatorApi.updateProduct(editingId.value, form.value)
    } else {
      await operatorApi.createProduct(form.value)
    }
    showForm.value = false
    await fetchProducts()
  } catch (e: any) {
    error.value = e.message
  }
}

async function handleDelete(id: string) {
  if (!confirm('确定要删除该商品吗？')) return
  try {
    await operatorApi.deleteProduct(id)
    await fetchProducts()
  } catch (e) {
    console.error(e)
  }
}

async function handleToggleStatus(product: Product) {
  const newStatus = product.status === 1 ? 0 : 1
  try {
    await operatorApi.updateProductStatus(product.id, newStatus)
    await fetchProducts()
  } catch (e) {
    console.error(e)
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  fetchProducts()
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-bold text-gray-800">商品管理</h2>
      <div class="flex gap-3">
        <div class="flex border border-gray-300 rounded-lg overflow-hidden">
          <button
            @click="viewMode = 'card'"
            :class="['px-3 py-1 text-sm', viewMode === 'card' ? 'bg-purple-600 text-white' : 'bg-white text-gray-600']"
          >
            卡片
          </button>
          <button
            @click="viewMode = 'table'"
            :class="['px-3 py-1 text-sm', viewMode === 'table' ? 'bg-purple-600 text-white' : 'bg-white text-gray-600']"
          >
            表格
          </button>
        </div>
        <button
          @click="openForm()"
          class="px-4 py-2 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700 transition-colors"
        >
          新增商品
        </button>
      </div>
    </div>

    <!-- 卡片视图 -->
    <div v-if="viewMode === 'card'" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="product in products"
        :key="product.id"
        class="bg-white rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow"
      >
        <div class="aspect-square bg-gray-100 flex items-center justify-center">
          <span v-if="!product.image" class="text-4xl">📦</span>
          <img v-else :src="product.image" :alt="product.name" class="w-full h-full object-cover" />
        </div>
        <div class="p-4">
          <h3 class="font-medium text-gray-800">{{ product.name }}</h3>
          <p class="text-sm text-gray-500 mt-1">{{ product.points }} 积分</p>
          <div class="flex items-center justify-between mt-3">
            <span
              :class="['px-2 py-1 text-xs font-medium rounded-full', product.status === 1 ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800']"
            >
              {{ product.status === 1 ? '上架' : '下架' }}
            </span>
            <div class="flex gap-2">
              <button @click="handleToggleStatus(product)" class="text-sm text-blue-600 hover:underline">
                {{ product.status === 1 ? '下架' : '上架' }}
              </button>
              <button @click="openForm(product)" class="text-sm text-blue-600 hover:underline">编辑</button>
              <button @click="handleDelete(product.id)" class="text-sm text-red-600 hover:underline">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 表格视图 -->
    <div v-else class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">商品</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">分类</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">积分</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">库存</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="product in products" :key="product.id" class="hover:bg-gray-50">
            <td class="px-6 py-4">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center">
                  <span v-if="!product.image">📦</span>
                  <img v-else :src="product.image" class="w-full h-full object-cover rounded-lg" />
                </div>
                <span class="font-medium text-gray-800">{{ product.name }}</span>
              </div>
            </td>
            <td class="px-6 py-4 text-sm text-gray-600">{{ product.categoryName }}</td>
            <td class="px-6 py-4 text-sm text-gray-600">{{ product.points }}</td>
            <td class="px-6 py-4 text-sm text-gray-600">{{ product.stock }}</td>
            <td class="px-6 py-4">
              <span
                :class="['px-2 py-1 text-xs font-medium rounded-full', product.status === 1 ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800']"
              >
                {{ product.status === 1 ? '上架' : '下架' }}
              </span>
            </td>
            <td class="px-6 py-4">
              <div class="flex gap-2">
                <button @click="handleToggleStatus(product)" class="text-sm text-blue-600 hover:underline">
                  {{ product.status === 1 ? '下架' : '上架' }}
                </button>
                <button @click="openForm(product)" class="text-sm text-blue-600 hover:underline">编辑</button>
                <button @click="handleDelete(product.id)" class="text-sm text-red-600 hover:underline">删除</button>
              </div>
            </td>
          </tr>
          <tr v-if="products.length === 0">
            <td colspan="6" class="px-6 py-12 text-center text-gray-500">暂无商品</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="flex justify-center">
      <div class="flex gap-2">
        <button
          v-for="page in Math.ceil(pagination.total / pagination.pageSize)"
          :key="page"
          @click="handlePageChange(page)"
          :class="[
            'px-3 py-1 text-sm rounded-md',
            page === pagination.page ? 'bg-purple-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          ]"
        >
          {{ page }}
        </button>
      </div>
    </div>

    <!-- 表单弹窗 -->
    <div v-if="showForm" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
        <h3 class="text-lg font-bold text-gray-800 mb-4">{{ editingId ? '编辑商品' : '新增商品' }}</h3>
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">商品名称 *</label>
            <input
              v-model="form.name"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 outline-none"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">分类 *</label>
            <select
              v-model="form.categoryId"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 outline-none"
            >
              <option value="">请选择分类</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">积分 *</label>
              <input v-model="form.points" type="number" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 outline-none" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">库存 *</label>
              <input v-model="form.stock" type="number" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 outline-none" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">图片URL</label>
            <input v-model="form.image" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 outline-none" placeholder="https://..." />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">描述</label>
            <textarea v-model="form.description" rows="2" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 outline-none"></textarea>
          </div>
          <div v-if="error" class="text-red-500 text-sm">{{ error }}</div>
          <div class="flex gap-3">
            <button type="submit" class="flex-1 px-4 py-2 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700">
              确定
            </button>
            <button type="button" @click="showForm = false" class="px-4 py-2 bg-gray-200 text-gray-700 font-medium rounded-lg hover:bg-gray-300">
              取消
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>