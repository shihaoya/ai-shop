<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/operator'
import type { Category } from '@/types'

const categories = ref<Category[]>([])
const loading = ref(false)
const showForm = ref(false)
const formName = ref('')
const editingId = ref<string | null>(null)

async function fetchCategories() {
  loading.value = true
  try {
    categories.value = await getCategories()
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editingId.value = null
  formName.value = ''
  showForm.value = true
}

function openEdit(c: Category) {
  editingId.value = c.id
  formName.value = c.name
  showForm.value = true
}

async function handleSave() {
  if (!formName.value.trim()) {
    showToast('请输入分类名称')
    return
  }
  try {
    if (editingId.value) {
      await updateCategory(editingId.value, formName.value)
    } else {
      await createCategory(formName.value)
    }
    showToast('保存成功')
    showForm.value = false
    fetchCategories()
  } catch {
    showToast('保存失败')
  }
}

async function handleDelete(id: string) {
  try {
    await deleteCategory(id)
    showToast('删除成功')
    fetchCategories()
  } catch {
    showToast('删除失败')
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<template>
  <div class="category-manage-page">
    <van-nav-bar title="分类管理" />
    <div class="content">
      <div v-for="c in categories" :key="c.id" class="category-card">
        <div class="category-top">
          <div class="category-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
          </div>
          <div class="category-body">
            <div class="category-name">{{ c.name }}</div>
            <div class="category-meta">商品 {{ c.productCount || 0 }}</div>
          </div>
        </div>
        <div class="category-actions">
          <van-button size="small" class="action-btn btn-default" @click="openEdit(c)">编辑</van-button>
          <van-button size="small" class="action-btn btn-danger" @click="handleDelete(c.id)">删除</van-button>
        </div>
      </div>
      <van-empty v-if="!loading && categories.length === 0" description="暂无分类" />
    </div>
    <van-button type="primary" block class="add-btn" @click="openAdd">新增分类</van-button>

    <van-popup v-model:show="showForm" position="bottom" round>
      <div class="form-panel">
        <div class="panel-title">{{ editingId ? '编辑分类' : '新增分类' }}</div>
        <van-field v-model="formName" placeholder="请输入分类名称" />
        <van-button type="primary" block class="submit-btn" @click="handleSave">保存</van-button>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.category-manage-page {
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

.category-card {
  margin: 10px 12px;
  background: var(--bg-card);
  border-radius: 12px;
  overflow: hidden;
}

.category-top {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
}

.category-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.category-body { flex: 1; }

.category-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 2px;
}

.category-meta {
  font-size: 12px;
  color: var(--text-muted);
}

.category-actions {
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

.btn-default { color: #666; }
.btn-danger { color: #ef4444; background: #fff5f5; }

.add-btn {
  margin: 12px;
  border-radius: 8px;
}

.form-panel {
  padding: 20px 16px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
}

.submit-btn {
  margin-top: 16px;
  border-radius: 8px;
}
</style>