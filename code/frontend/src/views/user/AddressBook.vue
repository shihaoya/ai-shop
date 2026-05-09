<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'
import type { Address } from '@/types'

const loading = ref(false)
const addresses = ref<Address[]>([])
const showForm = ref(false)
const editingId = ref<string | null>(null)
const form = ref({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false,
})
const error = ref('')

async function fetchAddresses() {
  loading.value = true
  try {
    const res = await userApi.getAddresses()
    addresses.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openForm(address?: Address) {
  if (address) {
    editingId.value = address.id
    form.value = {
      name: address.name,
      phone: address.phone,
      province: address.province,
      city: address.city,
      district: address.district,
      detail: address.detail,
      isDefault: address.isDefault,
    }
  } else {
    editingId.value = null
    form.value = { name: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false }
  }
  error.value = ''
  showForm.value = true
}

async function handleSubmit() {
  if (!form.value.name || !form.value.phone || !form.value.province || !form.value.city || !form.value.district || !form.value.detail) {
    error.value = '请填写所有必填项'
    return
  }
  try {
    if (editingId.value) {
      await userApi.updateAddress(editingId.value, form.value)
    } else {
      await userApi.createAddress(form.value)
    }
    showForm.value = false
    await fetchAddresses()
  } catch (e: any) {
    error.value = e.message
  }
}

async function handleDelete(id: string) {
  if (!confirm('确定要删除该地址吗？')) return
  try {
    await userApi.deleteAddress(id)
    await fetchAddresses()
  } catch (e) {
    console.error(e)
  }
}

async function handleSetDefault(id: string) {
  try {
    await userApi.setDefaultAddress(id)
    await fetchAddresses()
  } catch (e) {
    console.error(e)
  }
}

onMounted(fetchAddresses)
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-bold text-gray-800">地址簿</h2>
      <button
        @click="openForm()"
        class="px-4 py-2 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700 transition-colors"
      >
        新增地址
      </button>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-500">加载中...</div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div
        v-for="addr in addresses"
        :key="addr.id"
        :class="[
          'bg-white rounded-xl shadow-sm p-4 relative',
          addr.isDefault ? 'ring-2 ring-purple-500' : ''
        ]"
      >
        <div v-if="addr.isDefault" class="absolute top-2 right-2 px-2 py-0.5 text-xs bg-purple-100 text-purple-600 rounded-full">
          默认
        </div>
        <h3 class="font-medium text-gray-800">{{ addr.name }} {{ addr.phone }}</h3>
        <p class="text-sm text-gray-500 mt-1">
          {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}
        </p>
        <div class="flex gap-2 mt-4">
          <button
            @click="openForm(addr)"
            class="px-3 py-1 text-sm text-blue-600 hover:bg-blue-50 rounded-md transition-colors"
          >
            编辑
          </button>
          <button
            v-if="!addr.isDefault"
            @click="handleSetDefault(addr.id)"
            class="px-3 py-1 text-sm text-purple-600 hover:bg-purple-50 rounded-md transition-colors"
          >
            设为默认
          </button>
          <button
            @click="handleDelete(addr.id)"
            class="px-3 py-1 text-sm text-red-600 hover:bg-red-50 rounded-md transition-colors"
          >
            删除
          </button>
        </div>
      </div>

      <div v-if="addresses.length === 0" class="col-span-2 text-center py-12 text-gray-500 bg-white rounded-xl">
        暂无地址
      </div>
    </div>

    <!-- 表单弹窗 -->
    <div v-if="showForm" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
        <h3 class="text-lg font-bold text-gray-800 mb-4">{{ editingId ? '编辑地址' : '新增地址' }}</h3>
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">收货人 *</label>
              <input
                v-model="form.name"
                type="text"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 outline-none"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">手机号 *</label>
              <input
                v-model="form.phone"
                type="tel"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 outline-none"
              />
            </div>
          </div>
          <div class="grid grid-cols-3 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">省 *</label>
              <input v-model="form.province" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 outline-none" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">市 *</label>
              <input v-model="form.city" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 outline-none" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">区 *</label>
              <input v-model="form.district" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 outline-none" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">详细地址 *</label>
            <input
              v-model="form.detail"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 outline-none"
            />
          </div>
          <div class="flex items-center gap-2">
            <input
              v-model="form.isDefault"
              type="checkbox"
              id="isDefault"
              class="w-4 h-4 text-purple-600 border-gray-300 rounded focus:ring-purple-500"
            />
            <label for="isDefault" class="text-sm text-gray-700">设为默认地址</label>
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