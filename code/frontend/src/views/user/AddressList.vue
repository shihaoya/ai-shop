<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/user'
import { message, Modal } from 'ant-design-vue'
import type { Address } from '@/types/api'

const themeStore = useThemeStore()

const loading = ref(false)
const addressList = ref<Address[]>([])
const showModal = ref(false)
const modalTitle = ref('新增地址')
const editingId = ref<string | null>(null)

const form = ref({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0,
})

onMounted(() => {
  themeStore.init()
  loadAddresses()
})

async function loadAddresses() {
  loading.value = true
  try {
    const res = await getAddresses()
    addressList.value = res.map(toAddressItem)
  } catch {
    message.error('获取地址失败')
  } finally {
    loading.value = false
  }
}

function toAddressItem(addr: Address): Address {
  return {
    ...addr,
    id: String(addr.id),
  }
}

function getFullAddress(addr: Address): string {
  return `${addr.province} ${addr.city} ${addr.district} ${addr.detail}`
}

function openAddModal() {
  modalTitle.value = '新增地址'
  editingId.value = null
  form.value = {
    receiver: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: 0,
  }
  showModal.value = true
}

function openEditModal(addr: Address) {
  modalTitle.value = '编辑地址'
  editingId.value = String(addr.id)
  form.value = {
    receiver: addr.receiver,
    phone: addr.phone,
    province: addr.province,
    city: addr.city,
    district: addr.district,
    detail: addr.detail,
    isDefault: addr.isDefault,
  }
  showModal.value = true
}

async function handleSubmit() {
  if (!form.value.receiver || !form.value.phone || !form.value.province || !form.value.city || !form.value.district || !form.value.detail) {
    message.warning('请填写完整信息')
    return
  }

  loading.value = true
  try {
    if (editingId.value) {
      const res = await updateAddress(editingId.value, form.value)
      if (res) {
        message.success('更新成功')
        showModal.value = false
        loadAddresses()
      } else {
        message.error('更新失败')
      }
    } else {
      const res = await createAddress(form.value)
      if (res) {
        message.success('创建成功')
        showModal.value = false
        loadAddresses()
      } else {
        message.error('创建失败')
      }
    }
  } catch {
    message.error('操作失败')
  } finally {
    loading.value = false
  }
}

function handleDelete(addr: Address) {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除该收货地址吗？',
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      loading.value = true
      try {
        const id = String(addr.id)
        const res = await deleteAddress(id)
        if (res) {
          message.success('删除成功')
          loadAddresses()
        } else {
          message.error('删除失败')
        }
      } catch {
        message.error('删除失败')
      } finally {
        loading.value = false
      }
    },
  })
}

async function handleSetDefault(addr: Address) {
  if (addr.isDefault === 1) return
  loading.value = true
  try {
    const id = String(addr.id)
    const res = await setDefaultAddress(id)
    if (res) {
      message.success('设置成功')
      loadAddresses()
    } else {
      message.error('设置失败')
    }
  } catch {
    message.error('设置默认地址失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div id="page-address-list">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <div class="page-head">
        <h2><span class="accent-line"></span>收货地址</h2>
        <button class="cyber-btn-primary" @click="openAddModal">
          <i class="fas fa-plus" style="margin-right:6px;"></i>
          新增地址
        </button>
      </div>

      <div class="address-list" v-if="addressList.length > 0">
        <div v-for="addr in addressList" :key="String(addr.id)" class="address-item cyber-card">
          <div class="address-info">
            <div class="address-main">
              <div class="receiver">{{ addr.receiver }}</div>
              <div class="phone">{{ addr.phone }}</div>
              <div v-if="addr.isDefault === 1" class="default-tag">默认</div>
            </div>
            <div class="address-detail">{{ getFullAddress(addr) }}</div>
          </div>
          <div class="address-actions">
            <button class="cyber-btn" @click="openEditModal(addr)" v-if="addr.isDefault !== 1">
              <i class="fas fa-edit" style="margin-right:4px;"></i>
              编辑
            </button>
            <button class="cyber-btn" @click="handleSetDefault(addr)" v-if="addr.isDefault !== 1">
              <i class="fas fa-star" style="margin-right:4px;"></i>
              设为默认
            </button>
            <button class="cyber-btn-danger" @click="handleDelete(addr)">
              <i class="fas fa-trash" style="margin-right:4px;"></i>
              删除
            </button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state cyber-card">
        <i class="fas fa-map-marker-alt" style="font-size:48px;margin-bottom:16px;opacity:0.5;"></i>
        <p style="font-size:16px;">暂无收货地址</p>
        <button class="cyber-btn-primary" @click="openAddModal" style="margin-top:16px;">
          <i class="fas fa-plus" style="margin-right:6px;"></i>
          新增地址
        </button>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
      <div class="cyber-card modal-content">
        <div class="modal-header">
          <h3>{{ modalTitle }}</h3>
          <button class="close-btn" @click="showModal = false">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <form @submit.prevent="handleSubmit" class="address-form">
          <div class="form-row">
            <div class="form-item">
              <label>收货人</label>
              <input v-model="form.receiver" type="text" placeholder="请输入收货人" />
            </div>
            <div class="form-item">
              <label>联系电话</label>
              <input v-model="form.phone" type="tel" placeholder="请输入联系电话" maxlength="11" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-item">
              <label>省份</label>
              <input v-model="form.province" type="text" placeholder="请输入省份" />
            </div>
            <div class="form-item">
              <label>城市</label>
              <input v-model="form.city" type="text" placeholder="请输入城市" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-item">
              <label>区县</label>
              <input v-model="form.district" type="text" placeholder="请输入区县" />
            </div>
          </div>

          <div class="form-item full">
            <label>详细地址</label>
            <input v-model="form.detail" type="text" placeholder="请输入详细地址" />
          </div>

          <div class="form-actions">
            <button type="button" class="cyber-btn" @click="showModal = false">取消</button>
            <button type="submit" class="cyber-btn-primary" :disabled="loading">
              {{ loading ? '处理中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
#page-address-list {
  min-height: 100vh;
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.page-content {
  max-width: 900px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-head h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
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

.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  transition: all 0.3s ease;
}

.address-item:hover {
  border-color: var(--accent);
}

.address-info {
  flex: 1;
}

.address-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.receiver {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.phone {
  color: var(--text-secondary);
  font-size: 14px;
}

.default-tag {
  background: var(--accent);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.address-detail {
  color: var(--text-secondary);
  font-size: 14px;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  width: 500px;
  max-width: 90%;
  padding: 0;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border);
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.close-btn {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 18px;
  padding: 4px;
  transition: color 0.2s;
}

.close-btn:hover {
  color: var(--text-primary);
}

.address-form {
  padding: 24px;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.form-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item.full {
  margin-bottom: 16px;
}

.form-item label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.form-item input {
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
  transition: all 0.2s;
}

.form-item input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--accent-glow);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

/* 按钮样式 */
.cyber-btn {
  padding: 8px 16px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
}

.cyber-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.cyber-btn-primary {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  background: var(--accent);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  box-shadow: var(--accent-glow);
}

.cyber-btn-primary:hover {
  box-shadow: var(--accent-glow-hover);
}

.cyber-btn-danger {
  padding: 8px 16px;
  border: 1px solid #ef4444;
  border-radius: 8px;
  background: transparent;
  color: #ef4444;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
}

.cyber-btn-danger:hover {
  background: #ef4444;
  color: #fff;
}
</style>