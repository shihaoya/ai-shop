<script setup lang="ts">
import { ref } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { useRouter } from 'vue-router'
import { getMyShop, applyShop, changeShopStatus } from '@/api/operator'
import { ShopStatusText } from '@/types/enums'

const router = useRouter()

const shop = ref<any>(null)
const loading = ref(false)
const applyModalVisible = ref(false)
const applyForm = ref({ name: '', description: '' })
const applyLoading = ref(false)

async function loadMyShop() {
  loading.value = true
  try {
    const res: any = await getMyShop()
    if (res && res.hasShop === false) {
      shop.value = null
    } else if (res) {
      shop.value = {
        id: String(res.id),
        name: res.name,
        description: res.description,
        status: res.status,
        isActive: res.isActive,
        createdAt: res.createdAt,
        rejectReason: res.rejectReason,
      }
    } else {
      shop.value = null
    }
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

function openApplyModal() {
  applyForm.value = { name: '', description: '' }
  applyModalVisible.value = true
}

async function handleApply() {
  if (!applyForm.value.name.trim()) {
    showToast('请输入店铺名称')
    return
  }
  applyLoading.value = true
  try {
    await applyShop(applyForm.value.name.trim(), applyForm.value.description.trim())
    showToast('申请已提交，请等待审核')
    applyModalVisible.value = false
    loadMyShop()
  } catch {
    showToast('申请失败')
  } finally {
    applyLoading.value = false
  }
}

function handleToggleStatus() {
  if (!shop.value) return
  const newStatus = shop.value.isActive === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '营业' : '歇业'

  showConfirmDialog({
    title: '切换营业状态',
    message: `确定要将店铺设置为"${actionText}"吗？`,
  }).then(async () => {
    try {
      await changeShopStatus(newStatus)
      showToast(`店铺已设置为${actionText}`)
      loadMyShop()
    } catch {
      showToast('操作失败')
    }
  }).catch(() => {})
}

function getStatusText(status: number) {
  return ShopStatusText[status] || '未知'
}

const statusClass = (status: number) => {
  switch (status) {
    case 1: return 'pending'
    case 2: return 'approved'
    case 3: return 'rejected'
    case 4: return 'disabled'
    default: return ''
  }
}

loadMyShop()
</script>

<template>
  <div class="shop-page">
    <van-nav-bar title="我的店铺" left-arrow @click-left="router.back()" />

    <!-- 无店铺 -->
    <div v-if="!shop" class="no-shop">
      <div class="no-shop-icon">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
      </div>
      <div class="no-shop-title">暂无店铺</div>
      <div class="no-shop-desc">开通店铺后，您可以发布商品、管理订单</div>
      <van-button type="primary" round @click="openApplyModal" class="open-btn">立刻开通</van-button>
    </div>

    <!-- 有店铺 -->
    <div v-else class="shop-card">
      <div class="shop-header">
        <div class="shop-avatar">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
        </div>
        <div class="shop-info">
          <div class="shop-name">{{ shop.name }}</div>
          <div class="shop-tags">
            <span class="status-tag" :class="shop.isActive === 1 ? 'active' : 'inactive'">
              {{ shop.isActive === 1 ? '营业中' : '已歇业' }}
            </span>
            <span class="status-tag" :class="statusClass(shop.status)">
              {{ getStatusText(shop.status) }}
            </span>
          </div>
        </div>
      </div>

      <div class="shop-detail">
        <div v-if="shop.description" class="detail-row">
          <span class="detail-label">店铺描述</span>
          <span class="detail-value">{{ shop.description }}</span>
        </div>
        <div v-if="shop.createdAt" class="detail-row">
          <span class="detail-label">创建时间</span>
          <span class="detail-value">{{ shop.createdAt.slice(0, 16).replace('T', ' ') }}</span>
        </div>
        <div v-if="shop.status === 3 && shop.rejectReason" class="detail-row">
          <span class="detail-label">拒绝原因</span>
          <span class="detail-value error">{{ shop.rejectReason }}</span>
        </div>
      </div>

      <div v-if="shop.status === 2" class="shop-actions">
        <van-button
          :type="shop.isActive === 1 ? 'default' : 'primary'"
          size="small"
          round
          @click="handleToggleStatus"
        >
          {{ shop.isActive === 1 ? '设为歇业' : '设为营业' }}
        </van-button>
      </div>
    </div>

    <!-- 申请弹窗 -->
    <van-popup v-model:show="applyModalVisible" position="bottom" round>
      <div class="apply-panel">
        <div class="panel-title">开通店铺</div>
        <van-field v-model="applyForm.name" label="店铺名称" placeholder="请输入店铺名称" />
        <van-field v-model="applyForm.description" type="textarea" label="店铺描述" placeholder="请输入店铺描述（选填）" rows="2" />
        <van-button type="primary" block round class="submit-btn" :loading="applyLoading" @click="handleApply">提交申请</van-button>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.shop-page {
  background: var(--bg-primary);
  min-height: 100vh;
  height: 100dvh;
}

.no-shop {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 80px;
}

.no-shop-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--bg-card);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  margin-bottom: 16px;
}

.no-shop-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.no-shop-desc {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 24px;
}

.open-btn {
  width: 200px;
}

.shop-card {
  margin: 12px;
  background: var(--bg-card);
  border-radius: 12px;
  overflow: hidden;
}

.shop-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 16px;
}

.shop-avatar {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.shop-info { flex: 1; }

.shop-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.shop-tags {
  display: flex;
  gap: 8px;
}

.status-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.status-tag.active { background: #dcfce7; color: #16a34a; }
.status-tag.inactive { background: #f5f5f5; color: #8b8ba7; }
.status-tag.pending { background: #fef3c7; color: #d97706; }
.status-tag.approved { background: #dbeafe; color: #2563eb; }
.status-tag.rejected { background: #fee2e2; color: #ef4444; }
.status-tag.disabled { background: #f5f5f5; color: #8b8ba7; }

.shop-detail {
  padding: 0 16px 16px;
}

.detail-row {
  display: flex;
  gap: 8px;
  font-size: 13px;
  margin-bottom: 8px;
}

.detail-label { color: var(--text-muted); }
.detail-value { color: var(--text-primary); }
.detail-value.error { color: #ef4444; }

.shop-actions {
  padding: 12px 16px;
  border-top: 1px solid var(--border-subtle);
}

.apply-panel {
  padding: 20px 16px;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 20px;
}

.submit-btn {
  margin-top: 16px;
}
</style>