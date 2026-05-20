<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { getAddresses, deleteAddress, setDefaultAddress } from '@/api/user'
import type { Address } from '@/types'

const addresses = ref<Address[]>([])
const loading = ref(false)

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

function goAddAddress() {
  window.location.href = '/mobile/user/address/add'
}

onMounted(() => {
  fetchAddresses()
})
</script>

<template>
  <div class="address-list-page">
    <van-nav-bar title="收货地址" left-arrow @click-left="history.back()" />
    <div class="content">
      <div v-for="a in addresses" :key="a.id" class="address-card">
        <div class="address-top">
          <div class="address-receiver">{{ a.receiver }}</div>
          <div class="address-phone">{{ a.phone }}</div>
          <van-tag v-if="a.isDefault" type="success" size="medium">默认</van-tag>
        </div>
        <div class="address-detail">{{ a.province }} {{ a.city }} {{ a.district }} {{ a.detail }}</div>
        <div class="address-actions">
          <van-button v-if="!a.isDefault" size="small" class="action-btn" @click="handleSetDefault(a.id)">设为默认</van-button>
          <van-button size="small" class="action-btn btn-danger" @click="handleDelete(a.id)">删除</van-button>
        </div>
      </div>
      <van-empty v-if="!loading && addresses.length === 0" description="暂无地址" />
    </div>
    <van-button type="primary" block class="add-btn" @click="goAddAddress">新增地址</van-button>
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

.add-btn {
  margin: 12px;
  border-radius: 8px;
}
</style>