<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { useRouter } from 'vue-router'
import { getAddresses, deleteAddress, setDefaultAddress, createAddress, updateAddress } from '@/api/user'
import type { Address } from '@/types'

const router = useRouter()

const addresses = ref<Address[]>([])
const loading = ref(false)

const showForm = ref(false)
const editingId = ref<string | null>(null)
const form = ref({ receiver: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })

async function fetchAddresses() {
  loading.value = true
  try {
    addresses.value = await getAddresses()
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

async function handleSetDefault(id: string) {
  try {
    await setDefaultAddress(id)
    showToast('设置成功')
    fetchAddresses()
  } catch {
    showToast('设置失败')
  }
}

async function handleDelete(id: string) {
  try {
    await deleteAddress(id)
    showToast('已删除')
    fetchAddresses()
  } catch {
    showToast('删除失败')
  }
}

function openAdd() {
  editingId.value = null
  form.value = { receiver: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false }
  showForm.value = true
}

function openEdit(a: Address) {
  editingId.value = a.id
  form.value = {
    receiver: a.receiver,
    phone: a.phone,
    province: a.province,
    city: a.city,
    district: a.district,
    detail: a.detail,
    isDefault: !!a.isDefault,
  }
  showForm.value = true
}

async function handleSave() {
  if (!form.value.receiver || !form.value.phone) {
    showToast('请填写收货人和电话')
    return
  }
  if (!form.value.province || !form.value.city || !form.value.district || !form.value.detail) {
    showToast('请填写完整地址')
    return
  }
  try {
    if (editingId.value) {
      await updateAddress(editingId.value, form.value)
    } else {
      await createAddress(form.value)
    }
    showToast('保存成功')
    showForm.value = false
    fetchAddresses()
  } catch {
    showToast('保存失败')
  }
}

onMounted(() => {
  fetchAddresses()
})
</script>

<template>
  <div class="address-list-page">
    <van-nav-bar title="收货地址" left-arrow @click-left="router.back()">
      <template #right>
        <van-button size="small" type="primary" round @click="openAdd">新增</van-button>
      </template>
    </van-nav-bar>
    <div class="content">
      <div v-for="a in addresses" :key="a.id" class="address-card">
        <div class="address-top">
          <div class="address-receiver">{{ a.receiver }}</div>
          <div class="address-phone">{{ a.phone }}</div>
          <van-tag v-if="a.isDefault" type="success" size="medium">默认</van-tag>
        </div>
        <div class="address-detail">{{ a.province }} {{ a.city }} {{ a.district }} {{ a.detail }}</div>
        <div class="address-actions">
          <van-button size="small" class="action-btn" @click="openEdit(a)">编辑</van-button>
          <van-button v-if="!a.isDefault" size="small" class="action-btn" @click="handleSetDefault(a.id)">设为默认</van-button>
          <van-button size="small" class="action-btn btn-danger" @click="handleDelete(a.id)">删除</van-button>
        </div>
      </div>
      <van-empty v-if="!loading && addresses.length === 0" description="暂无地址" />
    </div>

    <van-popup v-model:show="showForm" position="bottom" round>
      <div class="form-panel">
        <div class="panel-title">{{ editingId ? '编辑地址' : '新增地址' }}</div>
        <van-field v-model="form.receiver" label="收货人" placeholder="请输入" />
        <van-field v-model="form.phone" label="电话" type="tel" placeholder="请输入" maxlength="11" />
        <van-field v-model="form.province" label="省份" placeholder="请输入" />
        <van-field v-model="form.city" label="城市" placeholder="请输入" />
        <van-field v-model="form.district" label="区县" placeholder="请输入" />
        <van-field v-model="form.detail" label="详细地址" placeholder="请输入" rows="2" type="textarea" />
        <van-button type="primary" block class="submit-btn" @click="handleSave">保存</van-button>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.address-list-page {
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

.address-card {
  margin: 10px 12px;
  background: var(--bg-card);
  border-radius: 12px;
  overflow: hidden;
}

.address-top {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 14px 8px;
}

.address-receiver {
  font-size: 15px;
  font-weight: 600;
}

.address-phone {
  font-size: 14px;
  color: var(--text-muted);
  flex: 1;
}

.address-detail {
  font-size: 13px;
  color: var(--text-secondary);
  padding: 0 14px 12px;
  line-height: 1.4;
}

.address-actions {
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

.btn-danger { color: #ef4444; background: #fff5f5; }

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