<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/operator'
import { message, Modal } from 'ant-design-vue'
import type { Category } from '@/types/api'

const themeStore = useThemeStore()

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
  } catch (e) {
    throw e
  } finally {
    loading.value = false
  }
}

// 弹框状态
const modalVisible = ref(false)
const modalTitle = ref('新增分类')
const editingCategory = ref<Category | null>(null)
const formState = ref({
  name: '',
  sort: 0
})

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
    console.error('提交分类操作失败:', e)
    message.error(e?.message || (e as Error)?.message || '操作失败')
    throw e
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
      } catch (e) {
        throw e
      }
    }
  })
}
</script>

<template>
  <div id="page-category-manage">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <div class="page-head">
        <h2><span class="accent-line"></span>分类管理</h2>
        <div class="actions">
          <button class="cyber-btn" @click="loadCategories">
            <i class="fas fa-sync-alt" style="margin-right:5px;"></i>刷新
          </button>
          <button class="cyber-btn cyber-btn-primary" @click="openAddModal">
            <i class="fas fa-plus" style="margin-right:5px;"></i>新增分类
          </button>
        </div>
      </div>

      <div class="table-card">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>分类名称</th>
                <th>排序</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody v-if="!loading && categories.length > 0">
              <tr v-for="category in categories" :key="category.id">
                <td>{{ category.id }}</td>
                <td>{{ category.name }}</td>
                <td>{{ category.sort }}</td>
                <td>
                  <button class="cyber-btn" style="padding:4px 12px;font-size:12px;" @click="openEditModal(category)">
                    <i class="fas fa-edit"></i>
                  </button>
                  <button class="cyber-btn" style="padding:4px 12px;font-size:12px;margin-left:8px;" @click="handleDelete(category)">
                    <i class="fas fa-trash"></i>
                  </button>
                </td>
              </tr>
            </tbody>
            <tbody v-else-if="loading">
              <tr>
                <td colspan="4" style="text-align:center;padding:40px;">加载中...</td>
              </tr>
            </tbody>
            <tbody v-else>
              <tr>
                <td colspan="4" style="text-align:center;padding:40px;color:var(--text-secondary);">
                  暂无分类数据
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :footer="null"
      :width="400"
      :destroyOnClose="true"
    >
      <div style="padding:20px 0;">
        <div style="margin-bottom:16px;">
          <label style="display:block;margin-bottom:6px;color:var(--text-secondary);font-size:13px;">分类名称</label>
          <a-input
            v-model:value="formState.name"
            placeholder="请输入分类名称"
            style="width:100%;"
          />
        </div>
        <div style="margin-bottom:16px;">
          <label style="display:block;margin-bottom:6px;color:var(--text-secondary);font-size:13px;">排序</label>
          <a-input-number
            v-model:value="formState.sort"
            :min="0"
            style="width:100%;"
            placeholder="数值越小越靠前"
          />
        </div>
        <div style="display:flex;gap:12px;justify-content:flex-end;">
          <button class="cyber-btn" @click="modalVisible = false">取消</button>
          <button class="cyber-btn cyber-btn-primary" @click="handleSubmit">确认</button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
#page-category-manage {
  min-height: 100vh;
  position: relative;
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
  background: linear-gradient(180deg, #6366f1, #8b5cf6);
  border-radius: 2px;
}

.actions {
  display: flex;
  gap: 12px;
}

.cyber-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 6px;
  color: var(--text-primary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.cyber-btn:hover {
  background: rgba(255,255,255,0.1);
  border-color: rgba(255,255,255,0.2);
}

.cyber-btn-primary {
  background: linear-gradient(135deg, rgba(99,102,241,0.3), rgba(139,92,246,0.3));
  border-color: rgba(99,102,241,0.5);
}

.cyber-btn-primary:hover {
  background: linear-gradient(135deg, rgba(99,102,241,0.5), rgba(139,92,246,0.5));
  border-color: rgba(99,102,241,0.7);
}

.table-card {
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 12px;
  overflow: hidden;
}

.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  background: rgba(255,255,255,0.03);
  padding: 14px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

td {
  padding: 14px 16px;
  font-size: 13px;
  color: var(--text-primary);
  border-bottom: 1px solid rgba(255,255,255,0.03);
}

tr:last-child td {
  border-bottom: none;
}

tr:hover td {
  background: rgba(255,255,255,0.02);
}
</style>
