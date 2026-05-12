<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getMyShop, applyShop, changeShopStatus } from '@/api/operator'
import { message, Modal } from 'ant-design-vue'
import type { Shop } from '@/types/api'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
  loadMyShop()
})

const loading = ref(false)
const shop = ref<Shop | null>(null)
const applyModalVisible = ref(false)
const applyForm = ref({ name: '', description: '' })
const applyLoading = ref(false)

// 店铺审核状态 0=待审核, 1=通过, 2=拒绝
const reviewStatusMap: Record<number, { text: string; class: string }> = {
  0: { text: '待审核', class: 'orange' },
  1: { text: '已通过', class: 'green' },
  2: { text: '已拒绝', class: 'red' },
}

// 营业状态 isActive: 0=歇业, 1=营业
const activeStatusMap: Record<number, { text: string; class: string }> = {
  0: { text: '歇业中', class: 'gray' },
  1: { text: '营业中', class: 'green' },
}

// 判断店铺状态
const shopStatus = computed(() => {
  if (!shop.value) return 'none' // 无店铺
  if (shop.value.status === 0) return 'pending' // 待审核
  if (shop.value.status === 2) return 'rejected' // 被拒绝
  return 'approved' // 已通过
})

async function loadMyShop() {
  loading.value = true
  try {
    const res = await getMyShop()
    if (res) {
      shop.value = {
        ...res,
        id: String(res.id)
      }
    } else {
      shop.value = null
    }
  } catch (e: any) {
    console.error('加载失败:', e)
    message.error(e?.message || (e as Error)?.message || '加载失败')
    throw e // 让错误继续传播，不要静默吞掉
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
    message.warning('请输入店铺名称')
    return
  }
  applyLoading.value = true
  try {
    await applyShop(applyForm.value.name.trim(), applyForm.value.description.trim())
    message.success('申请已提交，请等待审核')
    applyModalVisible.value = false
    loadMyShop()
  } catch (e: any) {
    console.error('提交店铺申请失败:', e)
    message.error(e?.message || (e as Error)?.message || '申请失败')
    throw e
  } finally {
    applyLoading.value = false
  }
}

function handleToggleStatus() {
  if (!shop.value) return
  const newStatus = shop.value.isActive === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '营业' : '歇业'

  Modal.confirm({
    title: '切换营业状态',
    content: `确定要将店铺设置为"${actionText}"吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await changeShopStatus(newStatus)
        message.success(`店铺已设置为${actionText}`)
        loadMyShop()
      } catch (e: any) {
        console.error('切换营业状态失败:', e)
        message.error(e?.message || (e as Error)?.message || '操作失败')
        throw e
      }
    }
  })
}
</script>

<template>
  <div id="page-shop-manage">
    <!-- BG -->
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <!-- Page Head -->
      <div class="page-head">
        <h2><span class="accent-line"></span>我的店铺</h2>
        <div class="actions">
          <button class="cyber-btn" @click="loadMyShop" :disabled="loading">
            <i class="fas fa-sync-alt" style="margin-right:5px;"></i>刷新
          </button>
        </div>
      </div>

      <!-- 无店铺 - 申请入口 -->
      <div v-if="shopStatus === 'none'" class="no-shop-card">
        <div class="no-shop-icon">
          <i class="fas fa-store"></i>
        </div>
        <h3>您还没有申请店铺</h3>
        <p>成为店铺用户后，您可以发布商品、管理订单等功能</p>
        <button class="cyber-btn-primary" @click="openApplyModal">
          <i class="fas fa-plus" style="margin-right:5px;"></i>申请店铺
        </button>
      </div>

      <!-- 待审核状态 -->
      <div v-else-if="shopStatus === 'pending'" class="pending-card">
        <div class="pending-icon">
          <i class="fas fa-clock"></i>
        </div>
        <h3>店铺申请正在审核中</h3>
        <p>您的店铺申请已提交，请耐心等待管理员审核</p>
        <div class="status-info">
          <span class="status-tag" :class="reviewStatusMap[0].class">
            <i class="fas fa-hourglass-half" style="margin-right:5px;"></i>{{ reviewStatusMap[0].text }}
          </span>
        </div>
      </div>

      <!-- 被拒绝状态 -->
      <div v-else-if="shopStatus === 'rejected'" class="rejected-card">
        <div class="rejected-icon">
          <i class="fas fa-times-circle"></i>
        </div>
        <h3>店铺申请被拒绝</h3>
        <p>抱歉，您的店铺申请未通过审核</p>
        <button class="cyber-btn-primary" @click="openApplyModal">
          <i class="fas fa-redo" style="margin-right:5px;"></i>重新申请
        </button>
      </div>

      <!-- 已通过 - 显示店铺信息和营业状态切换 -->
      <div v-else class="shop-info-card cyber-card" v-loading="loading">
        <div class="shop-header">
          <div class="shop-avatar">
            <i class="fas fa-store"></i>
          </div>
          <div class="shop-title">
            <h3>{{ shop?.name }}</h3>
            <span class="status-tag" :class="activeStatusMap[shop?.isActive ?? 0].class">
              <i :class="shop?.isActive === 1 ? 'fas fa-power-off' : 'fas fa-moon'" style="margin-right:5px;"></i>
              {{ activeStatusMap[shop?.isActive ?? 0].text }}
            </span>
          </div>
        </div>

        <div class="shop-detail">
          <div class="detail-row" v-if="shop?.description">
            <span class="detail-label">店铺描述</span>
            <span class="detail-value">{{ shop.description || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">店铺ID</span>
            <span class="detail-value id-value">{{ shop?.id }}</span>
          </div>
          <div class="detail-row" v-if="shop?.createdAt">
            <span class="detail-label">创建时间</span>
            <span class="detail-value">{{ new Date(shop!.createdAt!).toLocaleString('zh-CN') }}</span>
          </div>
        </div>

        <div class="shop-actions">
          <button
            class="cyber-btn-primary"
            @click="handleToggleStatus"
            :class="shop?.isActive === 1 ? 'cyber-btn-warning' : 'cyber-btn-success'"
          >
            <i :class="shop?.isActive === 1 ? 'fas fa-moon' : 'fas fa-power-off'" style="margin-right:5px;"></i>
            {{ shop?.isActive === 1 ? '设置为歇业' : '设置为营业' }}
          </button>
        </div>
      </div>

      <!-- 审核状态说明 -->
      <div v-if="shopStatus === 'approved'" class="status-legend">
        <h4>审核状态</h4>
        <div class="legend-items">
          <span class="status-tag green"><i class="fas fa-check" style="margin-right:5px;"></i>已通过</span>
        </div>
      </div>
    </div>

    <!-- 申请店铺弹窗 -->
    <div class="modal-overlay" v-if="applyModalVisible" @click.self="applyModalVisible = false">
      <div class="modal-content cyber-card">
        <div class="modal-header">
          <h3>申请店铺</h3>
          <button class="modal-close" @click="applyModalVisible = false">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>店铺名称 <span class="required">*</span></label>
            <input
              type="text"
              v-model="applyForm.name"
              placeholder="请输入店铺名称"
              class="cyber-input"
              maxlength="50"
            />
          </div>
          <div class="form-group">
            <label>店铺描述</label>
            <textarea
              v-model="applyForm.description"
              placeholder="请输入店铺描述（选填）"
              class="cyber-textarea"
              rows="4"
              maxlength="200"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cyber-btn" @click="applyModalVisible = false">取消</button>
          <button class="cyber-btn-primary" @click="handleApply" :disabled="applyLoading">
            <span v-if="applyLoading"><i class="fas fa-spinner fa-spin" style="margin-right:5px;"></i>提交中...</span>
            <span v-else><i class="fas fa-paper-plane" style="margin-right:5px;"></i>提交申请</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#page-shop-manage {
  min-height: 100vh;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
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
  gap: 10px;
  margin: 0;
}

.accent-line {
  display: inline-block;
  width: 4px;
  height: 20px;
  background: var(--accent);
  border-radius: 2px;
}

.actions {
  display: flex;
  gap: 10px;
}

/* 无店铺卡片 */
.no-shop-card,
.pending-card,
.rejected-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 48px 24px;
  text-align: center;
  backdrop-filter: blur(10px);
}

.no-shop-icon,
.pending-icon,
.rejected-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 32px;
}

.no-shop-icon {
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}

.pending-icon {
  background: rgba(249, 115, 22, 0.1);
  color: #f97316;
}

.rejected-icon {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.no-shop-card h3,
.pending-card h3,
.rejected-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 10px;
}

.no-shop-card p,
.pending-card p,
.rejected-card p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 24px;
}

.status-info {
  margin-top: 16px;
}

/* 店铺信息卡片 */
.shop-info-card {
  padding: 24px;
}

.shop-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
}

.shop-avatar {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  background: rgba(99, 102, 241, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #6366f1;
}

.shop-title h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.shop-detail {
  margin-bottom: 24px;
}

.detail-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  width: 100px;
  font-size: 14px;
  color: var(--text-secondary);
}

.detail-value {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
}

.id-value {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  color: var(--accent);
}

.shop-actions {
  display: flex;
  gap: 12px;
}

/* 审核状态说明 */
.status-legend {
  margin-top: 24px;
  padding: 16px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.status-legend h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px;
}

.legend-items {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 90%;
  max-width: 480px;
  padding: 0;
  animation: modalIn 0.2s ease;
}

@keyframes modalIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
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
  font-size: 18px;
  padding: 4px;
  line-height: 1;
}

.modal-close:hover {
  color: var(--text-primary);
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--border-color);
}

/* 表单样式 */
.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.required {
  color: #ef4444;
}

.cyber-input,
.cyber-textarea {
  width: 100%;
  padding: 10px 14px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  transition: all 0.2s;
}

.cyber-input:focus,
.cyber-textarea:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.cyber-input::placeholder,
.cyber-textarea::placeholder {
  color: var(--text-secondary);
}

.cyber-textarea {
  resize: vertical;
  min-height: 100px;
}

/* 按钮样式增强 */
.cyber-btn-primary.cyber-btn-success {
  background: linear-gradient(135deg, #10b981, #059669);
}

.cyber-btn-primary.cyber-btn-warning {
  background: linear-gradient(135deg, #f97316, #ea580c);
}
</style>
