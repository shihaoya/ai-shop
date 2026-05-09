<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { operatorApi } from '@/api/operator'
import type { Category } from '@/types'

const loading = ref(false)
const categories = ref<Category[]>([])
const showForm = ref(false)
const editingId = ref<string | null>(null)
const form = ref({ name: '' })
const error = ref('')

async function fetchCategories() {
  loading.value = true
  try {
    const res = await operatorApi.getCategories()
    categories.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openForm(category?: Category) {
  if (category) {
    editingId.value = category.id
    form.value.name = category.name
  } else {
    editingId.value = null
    form.value.name = ''
  }
  error.value = ''
  showForm.value = true
}

async function handleSubmit() {
  if (!form.value.name) {
    error.value = '请输入分类名称'
    return
  }

  try {
    if (editingId.value) {
      await operatorApi.updateCategory(editingId.value, form.value)
    } else {
      await operatorApi.createCategory(form.value)
    }
    showForm.value = false
    await fetchCategories()
  } catch (e: any) {
    error.value = e.message
  }
}

async function handleDelete(id: string) {
  if (!confirm('确定要删除该分类吗？')) return
  try {
    await operatorApi.deleteCategory(id)
    await fetchCategories()
  } catch (e) {
    console.error(e)
  }
}

onMounted(fetchCategories)
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-bold text-gray-800">分类管理</h2>
      <button
        @click="openForm()"
        class="px-4 py-2 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700 transition-colors"
      >
        新增分类
      </button>
    </div>

    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">分类名称</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">创建时间</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="cat in categories" :key="cat.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm text-gray-500">{{ cat.id }}</td>
            <td class="px-6 py-4 text-sm font-medium text-gray-800">{{ cat.name }}</td>
            <td class="px-6 py-4 text-sm text-gray-500">{{ cat.createTime }}</td>
            <td class="px-6 py-4">
              <div class="flex gap-2">
                <button
                  @click="openForm(cat)"
                  class="px-3 py-1 text-xs font-medium text-blue-600 hover:bg-blue-50 rounded-md transition-colors"
                >
                  编辑
                </button>
                <button
                  @click="handleDelete(cat.id)"
                  class="px-3 py-1 text-xs font-medium text-red-600 hover:bg-red-50 rounded-md transition-colors"
                >
                  删除
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="categories.length === 0">
            <td colspan="4" class="px-6 py-12 text-center text-gray-500">暂无分类</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 表单弹窗 -->
    <div v-if="showForm" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
        <h3 class="text-lg font-bold text-gray-800 mb-4">{{ editingId ? '编辑分类' : '新增分类' }}</h3>
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">分类名称</label>
            <input
              v-model="form.name"
              type="text"
              placeholder="请输入分类名称"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent outline-none"
            />
          </div>
          <div v-if="error" class="text-red-500 text-sm">{{ error }}</div>
          <div class="flex gap-3">
            <button
              type="submit"
              class="flex-1 px-4 py-2 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700 transition-colors"
            >
              确定
            </button>
            <button
              type="button"
              @click="showForm = false"
              class="px-4 py-2 bg-gray-200 text-gray-700 font-medium rounded-lg hover:bg-gray-300 transition-colors"
            >
              取消
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>