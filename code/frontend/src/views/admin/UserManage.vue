<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { getUsers, changeUserStatus, approveUser, rejectUser, resetPassword } from '@/api/admin'
import { message, Modal } from 'ant-design-vue'
import type { UserInfo } from '@/types/api'
import CyberPagination from '@/components/CyberPagination.vue'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.init()
  loadUsers()
})

const loading = ref(false)
const users = ref<UserInfo[]>([])
const pagination = ref({ page: 1, size: 10, total: 0 })

const searchQuery = ref('')
const roleFilter = ref<number | undefined>()
const statusFilter = ref<number | undefined>()

// 重置密码弹窗
const resetPwdVisible = ref(false)
const resetPwdLoading = ref(false)
const resetPwdForm = ref({ userId: '', username: '', newPassword: '' })

async function loadUsers() {
  loading.value = true
  try {
    const params: any = { page: pagination.value.page, size: pagination.value.size }
    if (searchQuery.value) params.keyword = searchQuery.value
    if (roleFilter.value !== undefined) params.role = roleFilter.value
    if (statusFilter.value !== undefined) params.status = statusFilter.value

    const res = await getUsers(params)
    users.value = res.list.map(u => ({
      ...u,
      id: String(u.id)
    }))
    pagination.value.total = res.total
  } catch (e) {
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
  roleFilter.value = undefined
  statusFilter.value = undefined
  pagination.value.page = 1
  loadUsers()
}

async function handleStatusChange(userId: string, status: number) {
  const actionText = status === 3 ? '冻结' : '启用'
  Modal.confirm({
    title: `确认${actionText}`,
    content: `确定要${actionText}该用户吗？`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      await changeUserStatus(userId, status)
      message.success('状态已更新')
      loadUsers()
    }
  })
}

async function handleApprove(userId: string) {
  Modal.confirm({
    title: '确认审批通过',
    content: '确定要通过该用户的注册申请吗？',
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      await approveUser(userId)
      message.success('已审批通过')
      loadUsers()
    }
  })
}

async function handleReject(userId: string) {
  Modal.confirm({
    title: '确认拒绝',
    content: '确定要拒绝该用户的注册申请吗？',
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      await rejectUser(userId)
      message.success('已拒绝')
      loadUsers()
    }
  })
}

function getRoleTag(role: number) {
  const map: Record<number, { text: string; class: string }> = {
    1: { text: '管理员', class: 'shop' },
    2: { text: '店铺用户', class: 'shop' },
    3: { text: '普通用户', class: 'user' },
  }
  return map[role] || { text: '未知', class: 'gray' }
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

async function handleResetPassword(userId: string, username: string) {
  try {
    await new Promise<void>((resolve, reject) => {
      Modal.confirm({
        title: '确认重置密码',
        content: `确定要重置用户 ${username} 的密码吗？重置后将生成新随机密码。`,
        okText: '确认',
        cancelText: '取消',
        async onOk() {
          try {
            const res = await resetPassword(userId)
            resetPwdForm.value = { userId, username, newPassword: res.password }
            resetPwdVisible.value = true
            resolve()
          } catch (e: any) {
            reject(e)
          }
        },
        onCancel: () => reject(new Error('cancel'))
      })
    })
  } catch (e: any) {
    if (e?.message !== 'cancel') {
      message.error(e?.message || (e as Error)?.message || '操作失败')
    }
  }
}

function copyPassword() {
  navigator.clipboard.writeText(resetPwdForm.value.newPassword).then(() => {
    message.success('密码已复制到剪贴板')
  }).catch(() => {
    message.error('复制失败')
  })
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadUsers()
}
</script>

<template>
  <div id="page-user-manage">
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
            style="flex:1;max-width:220px;"
            @keyup.enter="handleSearch"
          />
          <select v-model="roleFilter" class="cyber-input" style="max-width:130px;cursor:pointer;">
            <option :value="undefined">全部角色</option>
            <option :value="2">店铺用户</option>
            <option :value="3">普通用户</option>
          </select>
          <select v-model="statusFilter" class="cyber-input" style="max-width:130px;cursor:pointer;">
            <option :value="undefined">全部状态</option>
            <option :value="1">待审核</option>
            <option :value="2">正常</option>
            <option :value="3">已冻结</option>
          </select>
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
                <th>ID</th>
                <th>用户名</th>
                <th>昵称</th>
                <th>角色</th>
                <th>状态</th>
                <th>积分</th>
                <th>注册时间</th>
                <th style="width:140px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td class="id-cell">{{ user.id }}</td>
                <td><strong>{{ user.username }}</strong></td>
                <td>{{ user.nickname }}</td>
                <td>
                  <span class="role-tag" :class="getRoleTag(user.role).class">
                    <i :class="user.role === 1 ? 'fas fa-crown' : user.role === 2 ? 'fas fa-store' : 'fas fa-user'"></i>
                    {{ getRoleTag(user.role).text }}
                  </span>
                </td>
                <td>
                  <span class="status-tag" :class="getStatusTag(user.status).class">
                    <span class="dot"></span>{{ getStatusTag(user.status).text }}
                  </span>
                </td>
                <td>
                  <strong class="points-cell">{{ user.points ?? 0 }}</strong>
                </td>
                <td class="time-cell">{{ formatDate(user.createdAt) }}</td>
                <td>
                  <template v-if="user.status === 1">
                    <button class="action-btn" title="审批通过" @click="handleApprove(user.id)">
                      <i class="fas fa-check"></i>
                    </button>
                    <button class="action-btn red" title="拒绝" @click="handleReject(user.id)">
                      <i class="fas fa-ban"></i>
                    </button>
                  </template>
                  <template v-else-if="user.status === 2">
                    <button class="action-btn red" title="冻结" @click="handleStatusChange(user.id, 3)">
                      <i class="fas fa-snowflake"></i>
                    </button>
                    <button class="action-btn orange" title="重置密码" @click="handleResetPassword(user.id, user.username)">
                      <i class="fas fa-key"></i>
                    </button>
                  </template>
                  <template v-else-if="user.status === 3">
                    <button class="action-btn green" title="启用" @click="handleStatusChange(user.id, 2)">
                      <i class="fas fa-play"></i>
                    </button>
                  </template>
                </td>
              </tr>
              <tr v-if="users.length === 0 && !loading">
                <td colspan="8" class="empty-cell">
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
      <div class="pagination-wrap" v-if="pagination.total > 0">
        <CyberPagination
          v-model:current="pagination.page"
          v-model:pageSize="pagination.size"
          :total="pagination.total"
          @change="handlePageChange"
        />
      </div>
    </div>

    <!-- 重置密码弹窗 -->
    <div class="modal-overlay" v-if="resetPwdVisible">
      <div class="modal-card">
        <div class="modal-header">
          <h3><i class="fas fa-key" style="margin-right:8px;color:var(--accent);"></i>密码重置成功</h3>
          <button class="modal-close" @click="resetPwdVisible = false">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <p style="margin-bottom:16px;color:var(--text-secondary);">用户 <strong style="color:var(--accent);">{{ resetPwdForm.username }}</strong> 的密码已重置为：</p>
          <div class="password-display">
            <span class="password-text">{{ resetPwdForm.newPassword }}</span>
            <button class="copy-btn" @click="copyPassword" title="复制密码">
              <i class="fas fa-copy"></i>
            </button>
          </div>
          <p style="margin-top:12px;font-size:12px;color:var(--text-muted);">请将此密码告知用户，并提醒其及时修改密码。</p>
        </div>
        <div class="modal-footer">
          <button class="cyber-btn" @click="resetPwdVisible = false">关闭</button>
          <button class="cyber-btn-primary" @click="copyPassword">
            <i class="fas fa-copy" style="margin-right:5px;"></i>复制密码
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#page-user-manage {
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

.action-btn.red:hover {
  background: rgba(239, 68, 68, 0.1);
}

.action-btn.orange:hover {
  background: rgba(249, 115, 22, 0.1);
  color: #f97316;
}

.password-display {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--bg-input);
  border: 1px solid var(--border-glow);
  border-radius: var(--radius-xs);
  margin-bottom: 8px;
}

.password-text {
  flex: 1;
  font-family: var(--font-mono);
  font-size: 20px;
  font-weight: 600;
  color: var(--accent);
  letter-spacing: 2px;
}

.copy-btn {
  padding: 8px 12px;
  border: 1px solid var(--border-glow);
  background: transparent;
  color: var(--text-secondary);
  border-radius: var(--radius-xs);
  cursor: pointer;
  transition: all 0.2s;
}

.copy-btn:hover {
  background: var(--bg-card-hover);
  color: var(--accent);
}
</style>