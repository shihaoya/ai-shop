<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { getShops, auditShop } from '@/api/admin'
import type { Shop } from '@/types'
import { ShopStatusText } from '@/types/enums'

const shops = ref<Shop[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const size = ref(10)

const tabs = [
  { text: '全部', value: 0 },
  { text: '待审核', value: 1 },
  { text: '已通过', value: 2 },
  { text: '已拒绝', value: 3 },
  { text: '已禁用', value: 4 },
]
const activeStatus = ref(0)

async function fetchShops() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: size.value }
    if (activeStatus.value !== 0) {
      params.status = activeStatus.value
    }
    const res = await getShops(params)
    if (page.value === 1) shops.value = res.list
    else shops.value.push(...res.list)
    if (res.list.length < size.value) finished.value = true
    page.value++
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

function onLoad() {
  if (!loading.value && !finished.value) fetchShops()
}

function onTabChange(status: number) {
  activeStatus.value = status
  shops.value = []
  page.value = 1
  finished.value = false
  fetchShops()
}

async function handleApprove(s: Shop) {
  try {
    await showConfirmDialog({ title: '确认通过', message: `确定通过店铺"${s.name}"的申请？` })
    await auditShop(s.id, 2)
    showToast('已通过')
    shops.value = []
    page.value = 1
    fetchShops()
  } catch { /* cancelled */ }
}

async function handleReject(s: Shop) {
  try {
    await showConfirmDialog({ title: '确认拒绝', message: `确定拒绝店铺"${s.name}"的申请？` })
    await auditShop(s.id, 3)
    showToast('已拒绝')
    shops.value = []
    page.value = 1
    fetchShops()
  } catch { /* cancelled */ }
}

onMounted(() => {
  fetchShops()
})
</script>

<template>
  <div class="shop-review-page">
    <van-nav-bar title="店铺审批" />
    <div class="content">
      <van-tabs v-model:active="activeStatus" @change="onTabChange" shrink>
        <van-tab v-for="tab in tabs" :key="tab.value" :title="tab.text" :name="tab.value">
          <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
            <div v-for="s in shops" :key="s.id" class="shop-card">
              <div class="shop-top">
                <div class="shop-avatar">{{ s.name.charAt(0) }}</div>
                <div class="shop-body">
                  <div class="name-row">
                    <span class="shop-name">{{ s.name }}</span>
                    <span class="status-tag" :class="'status-' + s.status">{{ ShopStatusText[s.status] }}</span>
                  </div>
                  <div class="shop-meta">
                    店主：{{ s.ownerName || '-' }}
                    <template v-if="s.createdAt">
                      <span class="dot">·</span>
                      {{ s.createdAt.slice(0, 10) }}
                    </template>
                  </div>
                </div>
              </div>
              <div v-if="s.description" class="shop-desc">{{ s.description }}</div>
              <div v-if="s.status === 1" class="shop-actions">
                <van-button size="small" class="action-btn btn-danger" @click="handleReject(s)">拒绝</van-button>
                <van-button size="small" class="action-btn btn-primary" @click="handleApprove(s)">通过</van-button>
              </div>
              <div v-if="s.status === 3 && s.rejectReason" class="reject-reason">
                拒绝原因：{{ s.rejectReason }}
              </div>
            </div>
            <van-empty v-if="!loading && shops.length === 0" description="暂无店铺" />
          </van-list>
        </van-tab>
      </van-tabs>
    </div>
  </div>
</template>

<style scoped>
.shop-review-page {
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

.shop-card {
  margin: 10px 12px;
  background: var(--bg-card);
  border-radius: 12px;
  overflow: hidden;
}

.shop-top {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
}

.shop-avatar {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
}

.shop-body { flex: 1; }

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.shop-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.status-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.status-tag.status-1 { background: #fef3c7; color: #d97706; }
.status-tag.status-2 { background: #dcfce7; color: #16a34a; }
.status-tag.status-3 { background: #fee2e2; color: #ef4444; }

.shop-meta {
  font-size: 12px;
  color: var(--text-muted);
}

.dot { margin: 0 4px; }

.shop-desc {
  font-size: 13px;
  color: var(--text-secondary);
  background: var(--bg-primary);
  padding: 10px 14px;
  margin: 0 12px 12px;
  border-radius: 8px;
}

.shop-actions {
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

.btn-primary { color: var(--accent); }
.btn-danger { color: #ef4444; background: #fff5f5; }

.reject-reason {
  font-size: 12px;
  color: #ef4444;
  padding: 10px 14px;
  background: #fff5f5;
  border-top: 1px solid var(--border-subtle);
}
</style>