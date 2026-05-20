<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { getUsers, adjustPoints, getPointsLog, resetPassword } from '@/api/operator'
import type { UserInfo } from '@/types'

const users = ref<UserInfo[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const size = ref(100)
const keyword = ref('')

const showAdjust = ref(false)
const adjustUserId = ref('')
const adjustUserName = ref('')
const adjustPointsValue = ref<number | string>('')
const adjustType = ref<'add' | 'sub'>('add')
const remark = ref('')

const showFlow = ref(false)
const flowUserName = ref('')
const flowList = ref<any[]>([])
const flowLoading = ref(false)

async function handleResetPassword(u: UserInfo) {
  try {
    const res = await resetPassword(u.id)
    showToast(`新密码：${res.password}`)
  } catch {
    showToast('重置失败')
  }
}

async function openPointsFlow(u: UserInfo) {
  flowUserName.value = u.nickname || u.username || '用户'
  flowList.value = []
  showFlow.value = true
  flowLoading.value = true
  try {
    const res = await getPointsLog(u.id, { page: 1, size: 50 })
    flowList.value = res.list
  } catch {
    showToast('加载失败')
  } finally {
    flowLoading.value = false
  }
}

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUsers({ page: page.value, size: size.value, keyword: keyword.value })
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

function onSearch() {
  users.value = []
  page.value = 1
  finished.value = false
  fetchUsers()
}

function onLoad() {
  if (!loading.value && !finished.value) fetchUsers()
}

function openAdjust(u: UserInfo) {
  adjustUserId.value = u.id
  adjustUserName.value = u.nickname || u.username || '用户'
  adjustPointsValue.value = ''
  adjustType.value = 'add'
  remark.value = ''
  showAdjust.value = true
}

async function handleAdjust() {
  const val = Number(adjustPointsValue.value)
  if (!val || val <= 0) {
    showToast('请输入积分数')
    return
  }
  const amount = adjustType.value === 'add' ? val : -val
  try {
    await adjustPoints(adjustUserId.value, amount, remark.value)
    showToast('操作成功')
    showAdjust.value = false
    users.value = []
    page.value = 1
    finished.value = false
    fetchUsers()
  } catch {
    showToast('操作失败')
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="user-points-page">
    <van-nav-bar title="用户积分" />
    <div class="search-bar">
      <van-search v-model="keyword" placeholder="搜索用户名/昵称" @search="onSearch" />
    </div>
    <div class="content">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div v-for="u in users" :key="u.id" class="user-card">
          <div class="user-top">
            <div class="user-avatar">{{ (u.nickname || u.username || 'U').charAt(0).toUpperCase() }}</div>
            <div class="user-body">
              <div class="user-name">{{ u.nickname || '-' }}</div>
              <div class="user-phone">{{ u.username }}</div>
            </div>
            <div class="user-points">{{ u.points ?? 0 }}积分</div>
          </div>
          <div class="user-actions">
            <van-button size="small" class="action-btn btn-primary" @click="openAdjust(u)">调整积分</van-button>
            <van-button size="small" class="action-btn btn-default" @click="openPointsFlow(u)">积分流水</van-button>
            <van-button size="small" class="action-btn btn-danger" @click="handleResetPassword(u)">重置密码</van-button>
          </div>
        </div>
        <van-empty v-if="!loading && users.length === 0" description="暂无用户" />
      </van-list>
    </div>

    <van-popup v-model:show="showAdjust" position="bottom" round>
      <div class="adjust-panel">
        <div class="panel-title">调整积分</div>
        <div class="target-user">
          <span class="label">用户</span>
          <span class="value">{{ adjustUserName }}</span>
        </div>
        <van-radio-group v-model="adjustType" direction="horizontal" class="type-group">
          <van-radio name="add" icon-size="18px">增加</van-radio>
          <van-radio name="sub" icon-size="18px">扣除</van-radio>
        </van-radio-group>
        <van-field v-model="adjustPointsValue" type="number" label="积分" placeholder="请输入积分数量" input-align="right" />
        <van-field v-model="remark" label="备注" placeholder="请输入操作原因" />
        <van-button type="primary" block class="submit-btn" @click="handleAdjust">确认调整</van-button>
      </div>
    </van-popup>

    <van-popup v-model:show="showFlow" position="bottom" round style="height: 70%">
      <div class="flow-panel">
        <div class="panel-title">{{ flowUserName }} - 积分流水</div>
        <div v-if="flowLoading" class="flow-loading">
          <van-loading>加载中...</van-loading>
        </div>
        <div v-else class="flow-list">
          <div v-for="item in flowList" :key="item.id" class="flow-item">
            <div class="flow-info">
              <div class="flow-top">
                <span class="flow-type" :class="item.type === 1 ? 'plus' : 'minus'">{{ item.type === 1 ? '获得' : '消费' }}</span>
                <span class="flow-time">{{ item.createdAt?.slice(0, 16).replace('T', ' ') }}</span>
              </div>
              <div class="flow-detail">
                <span class="flow-amount" :class="item.amount > 0 ? 'positive' : 'negative'">{{ item.amount > 0 ? '+' : '' }}{{ item.amount }}</span>
                <span v-if="item.balance != null" class="flow-balance">余额 {{ item.balance }}</span>
              </div>
              <div v-if="item.remark" class="flow-desc">{{ item.remark }}</div>
            </div>
          </div>
          <van-empty v-if="flowList.length === 0" description="暂无流水" />
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.user-points-page {
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

.search-bar {
  background: var(--bg-card);
  padding: 0 12px;
}

.user-card {
  margin: 10px 12px;
  background: var(--bg-card);
  border-radius: 12px;
  overflow: hidden;
}

.user-top {
  display: flex;
  align-items: center;
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

.user-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 2px;
}

.user-phone {
  font-size: 12px;
  color: var(--text-muted);
}

.user-points {
  font-size: 15px;
  font-weight: 700;
  color: var(--accent);
}

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
  color: var(--accent);
}

.adjust-panel {
  padding: 20px 16px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
}

.target-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--bg-primary);
  border-radius: 8px;
  margin-bottom: 16px;
}

.target-user .label {
  font-size: 13px;
  color: var(--text-muted);
}

.target-user .value {
  font-size: 15px;
  font-weight: 600;
}

.type-group {
  margin-bottom: 16px;
}

.submit-btn {
  margin-top: 16px;
  border-radius: 8px;
}

.flow-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px 16px;
}

.flow-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.flow-list {
  flex: 1;
  overflow-y: auto;
}

.flow-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--border-subtle);
}

.flow-info { flex: 1; }

.flow-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.flow-type {
  font-size: 13px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}
.flow-type.plus { background: #dcfce7; color: #16a34a; }
.flow-type.minus { background: #fee2e2; color: #ef4444; }

.flow-time {
  font-size: 12px;
  color: var(--text-muted);
}

.flow-detail {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.flow-amount {
  font-size: 18px;
  font-weight: 700;
}
.flow-amount.positive { color: #16a34a; }
.flow-amount.negative { color: #ef4444; }

.flow-balance {
  font-size: 12px;
  color: var(--text-muted);
}

.flow-desc {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>