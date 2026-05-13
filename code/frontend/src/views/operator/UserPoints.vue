<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { adjustPoints, getUsers, approveUser, rejectUser } from '@/api/operator'
import { useThemeStore } from '@/stores/theme'
import type { UserInfo } from '@/types/api'

const themeStore = useThemeStore()
const router = useRouter()

onMounted(() => {
  themeStore.init()
  loadUsers()
})

const loading = ref(false)
const users = ref<UserInfo[]>([])
const pagination = ref({ page: 1, size: 10, total: 0 })
const searchQuery = ref('')

// 调整积分弹窗
const adjustModalVisible = ref(false)
const adjustForm = ref({ userId: '', username: '', amount: 0, remark: '' })
const adjustLoading = ref(false)

async function loadUsers() {
  loading.value = true
  try {
    const params: any = { page: pagination.value.page, size: pagination.value.size }
    if (searchQuery.value) params.keyword = searchQuery.value

    const res = await getUsers(params)
    users.value = res.list.map((u: UserInfo) => ({
      ...u,
      id: String(u.id)
    }))
    pagination.value.total = res.total
  } catch (e: any) {
    console.error('加载用户列表失败:', e)
    message.error(e?.message || (e as Error)?.message || '加载失败')
    throw e
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.page = 1
  loadUsers()
}

function handleReset() {
  searchQuery.value = ''
  pagination.value.page = 1
  loadUsers()
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadUsers()
}

function openAdjustModal(user: UserInfo) {
  adjustForm.value = {
    userId: user.id,
    username: user.username,
    amount: 0,
    remark: ''
  }
  adjustModalVisible.value = true
}

async function handleAdjustPoints() {
  if (!adjustForm.value.amount) {
    message.warning('请输入积分数量')
    return
  }

  adjustLoading.value = true
  try {
    await adjustPoints(adjustForm.value.userId, adjustForm.value.amount, adjustForm.value.remark)
    message.success('积分调整成功')
    adjustModalVisible.value = false
    loadUsers()
  } catch (e: any) {
    console.error('调整用户积分失败:', e)
    message.error(e?.message || (e as Error)?.message || '调整失败')
    throw e
  } finally {
    adjustLoading.value = false
  }
}

async function handleApprove(userId: string) {
  try {
    await new Promise<void>((resolve, reject) => {
      Modal.confirm({
        title: '确认通过',
        content: '确定要通过该用户的注册申请吗？',
        okText: '确认',
        cancelText: '取消',
        onOk: () => resolve(),
        onCancel: () => reject(new Error('cancel'))
      })
    })
    await approveUser(userId)
    message.success('已通过审核')
    loadUsers()
  } catch (e: any) {
    if (e?.message !== 'cancel') {
      message.error(e?.message || (e as Error)?.message || '操作失败')
    }
  }
}

async function handleReject(userId: string) {
  try {
    await new Promise<void>((resolve, reject) => {
      Modal.confirm({
        title: '确认拒绝',
        content: '确定要拒绝该用户的注册申请吗？拒绝后将软删除该用户。',
        okText: '确认',
        cancelText: '取消',
        onOk: () => resolve(),
        onCancel: () => reject(new Error('cancel'))
      })
    })
    await rejectUser(userId)
    message.success('已拒绝')
    loadUsers()
  } catch (e: any) {
    if (e?.message !== 'cancel') {
      message.error(e?.message || (e as Error)?.message || '操作失败')
    }
  }
}

function goToPointsLog(userId: string) {
  router.push({ name: 'OpUserPointsLog', params: { id: userId } })
}

function getStatusTag(status: number) {
  const map: Record<number, { text: string; class: string }> = {
    1: { text: '待审核', class: 'orange' },
    2: { text: '正常', class: 'green' },
    3: { text: '已冻结', class: 'red' },
  }
  return map[status] || { text: '未知', class: 'gray' }
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}
</script>

<template>
  <div id="page-user-points">
    <!-- BG -->
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <!-- Page Head -->
      <div class="page-head">
        <h2><span class="accent-line"></span>用户管理</h2>
      </div>

      <!-- Search Bar -->
      <div class="search-bar">
        <div class="inner">
          <input
            v-model="searchQuery"
            class="cyber-input"
            type="text"
            placeholder="搜索用户名、昵称..."
            style="flex:1;max-width:280px;"
            @keyup.enter="handleSearch"
          />
          <button class="cyber-btn-primary" style="padding:9px 16px;" @click="handleSearch">
            <i class="fas fa-search" style="margin-right:5px;"></i>搜索
          </button>
          <button class="cyber-btn" style="padding:9px 16px;" @click="handleReset">
            <i class="fas fa-undo"></i>
          </button>
        </div>
      </div>

      <!-- Table Card -->
      <div class="table-card">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>用户名</th>
                <th>昵称</th>
                <th>积分</th>
                <th>状态</th>
                <th>注册时间</th>
                <th style="width:180px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td><strong>{{ user.username }}</strong></td>
                <td>{{ user.nickname }}</td>
                <td>
                  <strong class="points-cell">{{ user.points ?? 0 }}</strong>
                </td>
                <td>
                  <span class="status-tag" :class="getStatusTag(user.status).class">
                    <span class="dot"></span>{{ getStatusTag(user.status).text }}
                  </span>
                </td>
                <td class="time-cell">{{ formatDate(user.createdAt) }}</td>
                <td>
                  <button v-if="user.status === 1" class="action-btn green" title="通过" @click="handleApprove(user.id)">
                    <i class="fas fa-check"></i>
                  </button>
                  <button v-if="user.status === 1" class="action-btn red" title="拒绝" @click="handleReject(user.id)">
                    <i class="fas fa-times"></i>
                  </button>
                  <button v-if="user.status !== 1" class="action-btn accent" title="调整积分" @click="openAdjustModal(user)">
                    <i class="fas fa-coins"></i>
                  </button>
                  <button v-if="user.status !== 1" class="action-btn" title="积分流水" @click="goToPointsLog(user.id)">
                    <i class="fas fa-history"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="users.length === 0 && !loading">
                <td colspan="6" class="empty-cell">
                  <i class="fas fa-inbox" style="font-size:32px;opacity:0.3;"></i>
                  <p>暂无数据</p>
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
      <div class="pagination" v-if="pagination.total > 0">
        <span># TOTAL: {{ pagination.total }} RECORDS</span>
        <button class="page-btn" :disabled="pagination.page <= 1" @click="handlePageChange(pagination.page - 1)">
          <i class="fas fa-chevron-left"></i>
        </button>
        <button class="page-btn active">{{ pagination.page }}</button>
        <button class="page-btn" :disabled="pagination.page * pagination.size >= pagination.total" @click="handlePageChange(pagination.page + 1)">
          <i class="fas fa-chevron-right"></i>
        </button>
      </div>
    </div>

    <!-- 调整积分弹窗 -->
    <div class="modal-overlay" v-if="adjustModalVisible" @click.self="adjustModalVisible = false">
      <div class="modal-card">
        <div class="modal-header">
          <h3><i class="fas fa-coins" style="margin-right:8px;color:var(--accent);"></i>调整积分</h3>
          <button class="modal-close" @click="adjustModalVisible = false">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>用户</label>
            <input
              class="cyber-input"
              type="text"
              :value="adjustForm.username"
              disabled
              style="background:var(--bg-input);opacity:0.7;"
            />
          </div>
          <div class="form-group">
            <label>积分数量</label>
            <input
              v-model.number="adjustForm.amount"
              class="cyber-input"
              type="number"
              placeholder="正数增加，负数扣除"
            />
            <span class="form-hint">正数增加，负数扣除</span>
          </div>
          <div class="form-group">
            <label>备注</label>
            <input
              v-model="adjustForm.remark"
              class="cyber-input"
              type="text"
              placeholder="调整原因（可选）"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button class="cyber-btn" @click="adjustModalVisible = false">取消</button>
          <button class="cyber-btn-primary" :disabled="adjustLoading" @click="handleAdjustPoints">
            <i v-if="adjustLoading" class="fas fa-spinner fa-spin" style="margin-right:5px;"></i>
            确认调整
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#page-user-points {
  min-height: 100vh;
  position: relative;
}

.id-cell {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
}

.points-cell {
  color: var(--accent-light);
  text-shadow: var(--accent-glow-text);
}

.time-cell {
  font-size: 12px;
  color: var(--text-muted);
  font-family: var(--font-mono);
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
  background: rgba(7, 8, 22, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--accent);
}

.action-btn {
  padding: 6px 10px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  border-radius: var(--radius-xs);
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.action-btn:hover {
  background: var(--bg-card-hover);
  color: var(--accent);
}

.action-btn.accent:hover {
  background: rgba(99, 102, 241, 0.1);
  color: var(--accent);
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
}

.modal-close {
  padding: 4px 8px;
  border: none;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  border-radius: var(--radius-xs);
  transition: all 0.2s;
}

.modal-close:hover {
  background: var(--bg-card-hover);
  color: var(--text-primary);
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.form-hint {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: var(--text-muted);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid var(--border-subtle);
}
</style>
