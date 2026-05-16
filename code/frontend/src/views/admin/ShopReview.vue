<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getShops, auditShop } from '@/api/admin'
import { message, Modal } from 'ant-design-vue'
import { ShopStatus, ShopStatusText, ShopStatusClass } from '@/types/enums'
import type { Shop } from '@/types/api'
import CyberPagination from '@/components/CyberPagination.vue'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
  loadShops()
})

const loading = ref(false)
const shops = ref<Shop[]>([])
const pagination = ref({ page: 1, size: 10, total: 0 })
const keyword = ref('')
const statusFilter = ref<number | undefined>(ShopStatus.PENDING)
const rejectModalVisible = ref(false)
const rejectReason = ref('')
const currentRejectShopId = ref('')

async function loadShops() {
  loading.value = true
  try {
    const res = await getShops({ page: pagination.value.page, size: pagination.value.size })
    shops.value = res.list.map(s => ({
      ...s,
      id: String(s.id)
    }))
    pagination.value.total = res.total
  } catch (e) {
    throw e
  } finally {
    loading.value = false
  }
}

function handleAudit(shopId: string, status: number) {
  if (status === ShopStatus.REJECTED) {
    currentRejectShopId.value = shopId
    rejectReason.value = ''
    rejectModalVisible.value = true
    return
  }
  Modal.confirm({
    title: '确认通过',
    content: '确定要通过该店铺的申请吗？',
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      await auditShop(shopId, status)
      message.success('已通过')
      loadShops()
    },
  })
}

async function handleRejectSubmit() {
  if (!rejectReason.value.trim()) {
    message.warning('请填写拒绝原因')
    return
  }
  await auditShop(currentRejectShopId.value, ShopStatus.REJECTED, rejectReason.value.trim())
  message.success('已拒绝')
  rejectModalVisible.value = false
  loadShops()
}

function getStatusTag(status: number) {
  return {
    text: ShopStatusText[status as keyof typeof ShopStatusText] || '未知',
    class: ShopStatusClass[status as keyof typeof ShopStatusClass] || 'gray',
  }
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadShops()
}
</script>

<template>
  <div id="page-shop-review">
    <!-- BG -->
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <!-- Page Head -->
      <div class="page-head">
        <h2><span class="accent-line"></span>店铺审核</h2>
        <div class="actions">
          <button class="cyber-btn" @click="loadShops">
            <i class="fas fa-sync-alt" style="margin-right:5px;"></i>刷新
          </button>
        </div>
      </div>

      <!-- Table Card -->
      <div class="table-card">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>店铺名称</th>
                <th>负责人</th>
                <th>状态</th>
                <th>营业状态</th>
                <th>申请时间</th>
                <th style="width:120px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="shop in shops" :key="shop.id">
                <td class="id-cell">{{ shop.id }}</td>
                <td>
                  <strong>{{ shop.name }}</strong>
                  <p v-if="shop.description" class="desc">{{ shop.description }}</p>
                </td>
                <td>{{ shop.operatorName || '-' }}</td>
                <td>
                  <span class="status-tag" :class="getStatusTag(shop.status).class">
                    <span class="dot"></span>{{ getStatusTag(shop.status).text }}
                  </span>
                </td>
                <td>
                  <span class="status-tag" :class="shop.isActive ? 'green' : 'gray'">
                    <span class="dot"></span>{{ shop.isActive ? '营业中' : '歇业' }}
                  </span>
                </td>
                <td class="time-cell">{{ formatDate(shop.createdAt) }}</td>
                <td>
                  <template v-if="shop.status === ShopStatus.PENDING">
                    <button class="action-btn green" title="通过" @click="handleAudit(shop.id, ShopStatus.APPROVED)">
                      <i class="fas fa-check"></i>
                    </button>
                    <button class="action-btn red" title="拒绝" @click="handleAudit(shop.id, ShopStatus.REJECTED)">
                      <i class="fas fa-times"></i>
                    </button>
                  </template>
                  <span v-else class="text-muted">已处理</span>
                </td>
              </tr>
              <tr v-if="shops.length === 0 && !loading">
                <td colspan="7" class="empty-cell">
                  <i class="fas fa-inbox" style="font-size:32px;opacity:0.3;"></i>
                  <p>暂无待审核店铺</p>
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

      <!-- Pagination -->
      <div class="pagination-wrap" v-if="pagination.total > 0">
        <CyberPagination
          v-model:current="pagination.page"
          v-model:pageSize="pagination.size"
          :total="pagination.total"
          @change="handlePageChange"
        />
      </div>

      <!-- 拒绝弹窗 -->
      <div class="modal-overlay" v-if="rejectModalVisible" @click.self="rejectModalVisible = false">
        <div class="reject-modal cyber-card">
          <div class="modal-header">
            <h3><i class="fas fa-times-circle" style="color:var(--red);margin-right:8px;"></i>拒绝店铺申请</h3>
            <button class="modal-close" @click="rejectModalVisible = false"><i class="fas fa-times"></i></button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>拒绝原因 <span class="required">*</span></label>
              <textarea
                v-model="rejectReason"
                class="cyber-input"
                placeholder="请输入拒绝原因，将告知店铺申请人"
                rows="4"
                maxlength="500"
              ></textarea>
              <span class="char-count">{{ rejectReason.length }}/500</span>
            </div>
          </div>
          <div class="modal-footer">
            <button class="cyber-btn" @click="rejectModalVisible = false">取消</button>
            <button class="cyber-btn-danger" @click="handleRejectSubmit">确认拒绝</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#page-shop-review {
  min-height: 100vh;
  position: relative;
}

.desc {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time-cell {
  font-size: 12px;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

.id-cell {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
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

.loading-mask {
  position: absolute;
  inset: 0;
  background: var(--loading-mask);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--accent);
}

.action-btn.green {
  color: var(--green);
}

.action-btn.green:hover {
  background: rgba(16, 185, 129, 0.1);
}

.action-btn.red {
  color: var(--red);
}

.action-btn.red:hover {
  background: rgba(239, 68, 68, 0.1);
}

.text-muted {
  color: var(--text-muted);
  font-size: 12px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reject-modal {
  width: 420px;
  padding: 0;
  overflow: hidden;
}

.reject-modal .modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
}

.reject-modal .modal-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  font-size: 18px;
  padding: 4px;
}

.modal-close:hover { color: var(--text-primary); }

.reject-modal .modal-body {
  padding: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.form-group .required { color: var(--red); }

.form-group textarea {
  resize: none;
  border-radius: 8px;
}

.char-count {
  font-size: 12px;
  color: var(--text-muted);
  text-align: right;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 16px 24px;
  border-top: 1px solid var(--border-color);
}

.cyber-btn-danger {
  padding: 8px 20px;
  border-radius: 8px;
  border: 1px solid var(--red);
  background: transparent;
  color: var(--red);
  font-size: 14px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.cyber-btn-danger:hover {
  background: rgba(239, 68, 68, 0.1);
}
</style>