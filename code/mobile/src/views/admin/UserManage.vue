<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { getUsers, changeUserStatus, resetPassword, getUserPointsLog } from '@/api/admin'
import type { UserInfo } from '@/types'
import { UserStatusText } from '@/types/enums'

const users = ref<UserInfo[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const size = ref(10)

const tabs = [
  { text: '全部', value: 0 },
  { text: '待审核', value: 1 },
  { text: '正常', value: 2 },
  { text: '已冻结', value: 3 },
]
const activeStatus = ref(0)

const showPwdDialog = ref(false)
const resetPwdValue = ref('')

const showPointsLogDialog = ref(false)
const pointsLogList = ref<any[]>([])
const pointsLogLoading = ref(false)
const pointsLogFinished = ref(false)
const pointsLogPage = ref(1)
const pointsLogSize = ref(20)
const currentPointsUserId = ref('')

async function fetchUsers() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: size.value }
    if (activeStatus.value !== 0) {
      params.status = activeStatus.value
    }
    const res = await getUsers(params)
    if (page.value === 1) users.value = res.list
    else users.value.push(...res.list)
    if (res.list.length < size.value) finished.value = true
    page.value++
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

function onLoad() {
  if (!loading.value && !finished.value) fetchUsers()
}

function onTabChange(status: number) {
  activeStatus.value = status
  users.value = []
  page.value = 1
  finished.value = false
  fetchUsers()
}

async function handleToggleFreeze(u: UserInfo) {
  const action = u.status === 3 ? '解冻' : '冻结'
  try {
    await showConfirmDialog({ title: `确认${action}`, message: `${action}用户 ${u.nickname || u.username}？` })
    if (u.status === 3) {
      await changeUserStatus(u.id, 2)
      showToast('已解冻')
    } else {
      await changeUserStatus(u.id, 3)
      showToast('已冻结')
    }
    refreshList()
  } catch { /* cancelled */ }
}

async function handleResetPwd(u: UserInfo) {
  try {
    await showConfirmDialog({ title: '确认重置', message: `确定重置用户"${u.nickname || u.username}"的密码？` })
    const result = await resetPassword(u.id)
    resetPwdValue.value = result.password
    showPwdDialog.value = true
  } catch { /* cancelled */ }
}

function handleCopyPwd() {
  const input = document.createElement('input')
  input.value = resetPwdValue.value
  document.body.appendChild(input)
  input.select()
  const success = document.execCommand('copy')
  document.body.removeChild(input)
  if (success) {
    showToast('已复制')
  } else {
    showToast('复制失败')
  }
}

function refreshList() {
  users.value = []
  page.value = 1
  finished.value = false
  fetchUsers()
}

async function handleViewPointsLog(u: UserInfo) {
  currentPointsUserId.value = u.id
  pointsLogList.value = []
  pointsLogPage.value = 1
  pointsLogFinished.value = false
  showPointsLogDialog.value = true
  await fetchPointsLog()
}

async function fetchPointsLog() {
  pointsLogLoading.value = true
  try {
    const res = await getUserPointsLog(currentPointsUserId.value, { page: pointsLogPage.value, size: pointsLogSize.value })
    if (pointsLogPage.value === 1) pointsLogList.value = res.list
    else pointsLogList.value.push(...res.list)
    if (res.list.length < pointsLogSize.value) pointsLogFinished.value = true
    pointsLogPage.value++
  } catch {
    showToast('加载失败')
  } finally {
    pointsLogLoading.value = false
  }
}

function onPointsLogLoad() {
  if (!pointsLogLoading.value && !pointsLogFinished.value) fetchPointsLog()
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="user-manage-page">
    <van-nav-bar title="用户管理" />
    <div class="content">
      <van-tabs v-model:active="activeStatus" @change="onTabChange" shrink>
        <van-tab v-for="tab in tabs" :key="tab.value" :title="tab.text" :name="tab.value">
          <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
            <div v-for="u in users" :key="u.id" class="user-item">
              <div class="user-top">
                <div class="user-avatar">{{ (u.nickname || u.username || 'U').charAt(0).toUpperCase() }}</div>
                <div class="user-body">
                  <div class="name-row">
                    <span class="user-name">{{ u.nickname || '-' }}</span>
                    <span class="role-tag" :class="'role-' + u.role">{{ u.role === 1 ? '管理员' : u.role === 2 ? '店铺' : '用户' }}</span>
                  </div>
                  <div class="user-meta">
                    {{ u.username }}
                  </div>
                  <div class="user-sub">
                    <span :class="'status-' + u.status">{{ UserStatusText[u.status] }}</span>
                    <span v-if="u.points !== undefined" class="points">{{ u.points }}积分</span>
                    <span v-if="u.createdAt" class="date">{{ u.createdAt.slice(0, 10) }}注册</span>
                  </div>
                </div>
              </div>
              <div class="user-actions">
                <van-button
                  v-if="u.status !== 1"
                  size="small"
                  class="action-btn btn-theme"
                  @click="handleToggleFreeze(u)"
                >
                  {{ u.status === 3 ? '解冻' : '冻结' }}
                </van-button>
                <van-button size="small" class="action-btn btn-default" @click="handleViewPointsLog(u)">积分流水</van-button>
                <van-button size="small" class="action-btn btn-default" @click="handleResetPwd(u)">重置密码</van-button>
              </div>
            </div>
            <van-empty v-if="!loading && users.length === 0" description="暂无用户" />
          </van-list>
        </van-tab>
      </van-tabs>
    </div>

    <van-popup v-model:show="showPwdDialog" position="bottom" round :close-on-click-overlay="false">
      <div class="pwd-dialog">
        <div class="dialog-title">新密码</div>
        <div class="pwd-value">{{ resetPwdValue }}</div>
        <div class="dialog-tip">请告知用户新密码</div>
        <div class="dialog-actions">
          <van-button type="default" round @click="handleCopyPwd">复制</van-button>
          <van-button type="primary" round @click="showPwdDialog = false">知道了</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 积分流水弹窗 -->
    <van-popup v-model:show="showPointsLogDialog" position="bottom" round :close-on-click-overlay="false" style="height: 70%;">
      <div class="points-log-dialog">
        <div class="dialog-title">积分流水</div>
        <van-list
          v-model:loading="pointsLogLoading"
          :finished="pointsLogFinished"
          finished-text="没有更多了"
          @load="onPointsLogLoad"
          class="points-log-list"
        >
          <div v-for="log in pointsLogList" :key="log.id" class="log-item">
            <div class="log-info">
              <div class="log-type" :class="log.type === 1 ? 'plus' : 'minus'">
                {{ log.type === 1 ? '增加' : '扣除' }} {{ Math.abs(log.amount) }}
              </div>
              <div class="log-remark">{{ log.remark || '-' }}</div>
            </div>
            <div class="log-time">{{ log.createdAt?.slice(0, 16).replace('T', ' ') }}</div>
          </div>
          <van-empty v-if="!pointsLogLoading && pointsLogList.length === 0" description="暂无记录" />
        </van-list>
        <van-button type="default" block round @click="showPointsLogDialog = false" style="margin-top: 12px;">关闭</van-button>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.user-manage-page {
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

.user-item {
  margin: 10px 12px;
  background: var(--bg-card);
  border-radius: 12px;
  overflow: hidden;
}

.user-top {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
}

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
}

.user-body { flex: 1; }

.name-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 3px;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.role-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.role-tag.role-1 { background: #fef2f2; color: #ef4444; }
.role-tag.role-2 { background: #fff7e6; color: #d97706; }
.role-tag.role-3 { background: #eff6ff; color: #3b82f6; }

.user-meta {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.user-sub {
  display: flex;
  gap: 10px;
  font-size: 12px;
  flex-wrap: wrap;
}

.status-1 { color: #faad14; }
.status-2 { color: #52c41a; }
.status-3 { color: #ff4d4f; }

.points { color: var(--accent); font-weight: 500; }
.date { color: var(--text-muted); }

.user-actions {
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

.btn-default { color: #666; }
.btn-theme { color: var(--accent); }

.pwd-dialog {
  padding: 24px 20px;
  text-align: center;
  background: var(--bg-card);
}

.dialog-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.dialog-actions .van-button {
  flex: 1;
}

.dialog-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.pwd-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--accent);
  letter-spacing: 2px;
  margin-bottom: 6px;
}

.dialog-tip {
  font-size: 13px;
  color: #999;
  margin-bottom: 16px;
  text-align: center;
}

.points-log-dialog {
  padding: 24px 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.points-log-dialog .dialog-title {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
}

.points-log-list {
  flex: 1;
  overflow-y: auto;
}

.log-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-subtle);
}

.log-info { flex: 1; }

.log-type {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.log-type.plus { color: #52c41a; }
.log-type.minus { color: #ff4d4f; }

.log-remark {
  font-size: 13px;
  color: var(--text-muted);
}

.log-time {
  font-size: 12px;
  color: var(--text-muted);
}
</style>