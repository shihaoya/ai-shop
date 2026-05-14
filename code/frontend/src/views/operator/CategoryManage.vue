<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/operator'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { useOperatorShop } from '@/composables/useOperatorShop'
import type { Category } from '@/types/api'

const themeStore = useThemeStore()
const userStore = useUserStore()
const { isApproved } = useOperatorShop()

onMounted(() => {
  themeStore.init()
  loadCategories()
})

const loading = ref(false)
const categories = ref<Category[]>([])

async function loadCategories() {
  loading.value = true
  try {
    const res = await getCategories()
    categories.value = res.map((c: Category) => ({
      ...c,
      id: String(c.id)
    }))
  } catch (e: any) {
    message.error(e?.message || '加载分类失败')
  } finally {
    loading.value = false
  }
}

// 弹框
const modalVisible = ref(false)
const modalTitle = ref('新增分类')
const editingCategory = ref<Category | null>(null)
const formState = ref({ name: '', sort: 0 })
const submitLoading = ref(false)

function openAddModal() {
  modalTitle.value = '新增分类'
  editingCategory.value = null
  formState.value = { name: '', sort: 0 }
  modalVisible.value = true
}

function openEditModal(category: Category) {
  modalTitle.value = '编辑分类'
  editingCategory.value = category
  formState.value = { name: category.name, sort: category.sort }
  modalVisible.value = true
}

async function handleSubmit() {
  if (!formState.value.name.trim()) {
    message.warning('请输入分类名称')
    return
  }
  submitLoading.value = true
  try {
    if (editingCategory.value) {
      await updateCategory(editingCategory.value.id, formState.value.name, formState.value.sort)
      message.success('分类更新成功')
    } else {
      await createCategory(formState.value.name, formState.value.sort)
      message.success('分类创建成功')
    }
    modalVisible.value = false
    loadCategories()
  } catch (e: any) {
    message.error(e?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(category: Category) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除分类「${category.name}」吗？`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deleteCategory(category.id)
        message.success('删除成功')
        loadCategories()
      } catch (e: any) {
        message.error(e?.message || '删除失败')
      }
    }
  })
}
</script>

<template>
  <div id="page-category-manage">
    <!-- BG -->
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <!-- 店铺未审核通过提示 -->
      <div v-if="userStore.token && !isApproved()" class="shop-status-banner">
        <i class="fas fa-exclamation-triangle" style="margin-right:8px;"></i>
        您的店铺尚未审核通过，暂时无法管理分类
      </div>

      <!-- Page Head -->
      <div class="page-head">
        <h2><span class="accent-line"></span>分类管理</h2>
        <div class="actions">
          <button class="cyber-btn" @click="loadCategories">
            <i class="fas fa-sync-alt" style="margin-right:5px;"></i>刷新
          </button>
          <button class="cyber-btn cyber-btn-primary" :disabled="!isApproved()" @click="openAddModal">
            <i class="fas fa-plus" style="margin-right:5px;"></i>新增分类
          </button>
        </div>
      </div>

      <!-- Table Card -->
      <div class="table-card">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>分类名称</th>
                <th>商品数量</th>
                <th>排序</th>
                <th style="width:160px;">操作</th>
              </tr>
            </thead>
            <tbody v-if="!loading && categories.length > 0">
              <tr v-for="category in categories" :key="category.id">
                <td><strong>{{ category.name }}</strong></td>
                <td>
                  <span class="count-badge">{{ category.productCount ?? 0 }}</span>
                </td>
                <td>{{ category.sort }}</td>
                <td>
                  <button class="action-btn accent" :disabled="!isApproved()" @click="openEditModal(category)" title="编辑">
                    <i class="fas fa-edit"></i>
                  </button>
                  <button class="action-btn red" style="margin-left:8px;" @click="handleDelete(category)" title="删除">
                    <i class="fas fa-trash"></i>
                  </button>
                </td>
              </tr>
            </tbody>
            <tbody v-else-if="loading">
              <tr>
                <td colspan="4" class="empty-cell">加载中...</td>
              </tr>
            </tbody>
            <tbody v-else>
              <tr>
                <td colspan="4" class="empty-cell">
                  <i class="fas fa-folder-open" style="font-size:32px;opacity:0.3;"></i>
                  <p>暂无分类数据</p>
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
    </div>

    <!-- 新增/编辑弹框 -->
    <div class="modal-overlay" v-if="modalVisible" @click.self="modalVisible = false">
      <div class="modal-card">
        <div class="modal-header">
          <h3>{{ modalTitle }}</h3>
          <button class="modal-close" @click="modalVisible = false">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div style="margin-bottom:16px;">
            <label style="display:block;margin-bottom:6px;color:var(--text-secondary);font-size:13px;">分类名称</label>
            <input
              v-model="formState.name"
              class="cyber-input"
              type="text"
              placeholder="请输入分类名称"
              style="width:100%;"
              @keyup.enter="handleSubmit"
            />
          </div>
          <div style="margin-bottom:20px;">
            <label style="display:block;margin-bottom:6px;color:var(--text-secondary);font-size:13px;">排序</label>
            <input
              v-model.number="formState.sort"
              class="cyber-input"
              type="number"
              min="0"
              placeholder="数值越小越靠前"
              style="width:100%;"
            />
          </div>
          <div style="display:flex;gap:12px;justify-content:flex-end;">
            <button class="cyber-btn" @click="modalVisible = false">取消</button>
            <button class="cyber-btn cyber-btn-primary" :disabled="submitLoading" @click="handleSubmit">
              <span v-if="submitLoading"><i class="fas fa-spinner fa-spin" style="margin-right:5px;"></i>提交中</span>
              <span v-else>确认</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#page-category-manage {
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

.cyber-btn-primary:hover {
  background: linear-gradient(135deg, rgba(99,102,241,0.5), rgba(139,92,246,0.5));
  border-color: var(--accent);
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

.cyber-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  pointer-events: none;
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
  width: 420px;
  max-width: 90vw;
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

/* 表格已有全局样式，这里只写业务相关的 */
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

.empty-cell {
  text-align: center;
  padding: 40px !important;
  color: var(--text-muted);
}

.empty-cell i {
  display: block;
  margin-bottom: 8px;
}

.count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 24px;
  padding: 0 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: rgba(var(--accent-rgb), 0.10);
  color: var(--accent);
}
</style>