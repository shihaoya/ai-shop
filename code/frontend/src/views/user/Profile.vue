<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import { getPoints, getPointsLog, userApi } from '@/api/user'
import { getInviteCode as adminGetInviteCode, createInviteCode as adminCreateInviteCode } from '@/api/admin'
import { getInviteCode as operatorGetInviteCode, createInviteCode as operatorCreateInviteCode } from '@/api/operator'
import { message, Modal } from 'ant-design-vue'
import type { PointsLog } from '@/types/api'
import CyberPagination from '@/components/CyberPagination.vue'

const themeStore = useThemeStore()
const userStore = useUserStore()

// 积分
const pointsLoading = ref(false)
const currentPoints = ref(0)

// 积分流水
const loading = ref(false)
const pointsLog = ref<PointsLog[]>([])
const pagination = ref({ page: 1, size: 10, total: 0 })

// 邀请码（admin/operator）
const inviteCodeLoading = ref(false)
const inviteCode = ref('')
const createdAt = ref('')
const generating = ref(false)

// 编辑昵称
const editNicknameVisible = ref(false)
const editNicknameLoading = ref(false)
const nicknameForm = ref('')

function getInviteCodeApi() {
  return userStore.userInfo?.role === 1 ? adminGetInviteCode() : operatorGetInviteCode()
}

function getCreateInviteCodeApi() {
  return userStore.userInfo?.role === 1 ? adminCreateInviteCode() : operatorCreateInviteCode()
}

async function loadInviteCode() {
  inviteCodeLoading.value = true
  try {
    const code = await getInviteCodeApi()
    inviteCode.value = code || ''
  } catch {
    inviteCode.value = ''
  } finally {
    inviteCodeLoading.value = false
  }
}

async function doGenerate() {
  generating.value = true
  try {
    const code = await getCreateInviteCodeApi()
    inviteCode.value = code || ''
    createdAt.value = new Date().toLocaleString('zh-CN')
    message.success('邀请码已生成')
  } catch {
    message.error('生成失败')
  } finally {
    generating.value = false
  }
}

async function handleGenerateInviteCode() {
  if (inviteCode.value) {
    Modal.confirm({
      title: '确认生成新邀请码？',
      content: '生成后旧邀请码将立即失效，是否继续？',
      okText: '确认生成',
      cancelText: '取消',
      okButtonProps: { danger: true },
      onOk() {
        doGenerate()
      },
    })
  } else {
    await doGenerate()
  }
}

function handleCopyInviteCode() {
  if (!inviteCode.value) return
  navigator.clipboard.writeText(inviteCode.value).then(() => {
    message.success('已复制到剪贴板')
  }).catch(() => {
    message.error('复制失败')
  })
}

async function loadPoints() {
  pointsLoading.value = true
  try {
    const res = await getPoints()
    currentPoints.value = res.points
  } catch {
    // ignore
  } finally {
    pointsLoading.value = false
  }
}

async function loadPointsLog() {
  loading.value = true
  try {
    const res = await getPointsLog({
      page: pagination.value.page,
      size: pagination.value.size,
    })
    pointsLog.value = res.list.map(log => ({ ...log, id: String(log.id) }))
    pagination.value.total = res.total
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadPointsLog()
}

// 编辑昵称
function openEditNickname() {
  nicknameForm.value = userStore.userInfo?.nickname || ''
  editNicknameVisible.value = true
}

async function handleSaveNickname() {
  const nickname = nicknameForm.value.trim()
  if (!nickname) {
    message.warning('昵称不能为空')
    return
  }
  editNicknameLoading.value = true
  try {
    const res = await userApi.updateUserInfo({ nickname })
    userStore.setUserInfo(res as any)
    message.success('昵称修改成功')
    editNicknameVisible.value = false
  } catch {
    message.error('修改失败')
  } finally {
    editNicknameLoading.value = false
  }
}

function formatDate(date?: string) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

function getTypeInfo(type: number) {
  if (type === 1 || type === 4) {
    return { text: type === 1 ? '增加' : '退款', class: 'increase', icon: 'fa-arrow-up', sign: '+' }
  }
  return { text: type === 2 ? '扣除' : '兑换', class: 'decrease', icon: 'fa-arrow-down', sign: '-' }
}

onMounted(() => {
  themeStore.init()
  loadPoints()
  if (userStore.userInfo?.role === 3) {
    loadPointsLog()
  } else {
    loadInviteCode()
  }
})
</script>

<template>
  <div id="page-profile">
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:500px;height:500px;top:-150px;right:-150px;background:rgba(99,102,241,0.10);"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;bottom:10%;left:-100px;background:rgba(236,72,153,0.06);"></div>

    <div class="page-content">
      <div class="page-head">
        <h2><span class="accent-line"></span>个人中心</h2>
      </div>

      <!-- 用户信息卡片 -->
      <div class="cyber-card profile-card">
        <div class="profile-avatar-wrap">
          <div class="profile-avatar">
            {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0).toUpperCase() }}
          </div>
          <button class="edit-btn" @click="openEditNickname">
            <i class="fas fa-pen"></i>
          </button>
        </div>
        <div class="profile-info">
          <h3 class="profile-nickname" @click="openEditNickname" title="点击修改昵称">
            {{ userStore.userInfo?.nickname || '-' }}
            <i class="fas fa-pen nickname-edit-icon"></i>
          </h3>
          <div class="profile-meta">
            <span class="profile-tag">
              <i class="fas fa-user"></i> {{ userStore.userInfo?.username }}
            </span>
            <span class="profile-tag">
              <i class="fas fa-tag"></i>
              {{ userStore.userInfo?.role === 3 ? '普通用户' : userStore.userInfo?.role === 2 ? '店铺用户' : '管理员' }}
            </span>
            <span class="profile-tag" v-if="userStore.userInfo?.createdAt">
              <i class="fas fa-calendar"></i> {{ formatDate(userStore.userInfo?.createdAt) }}
            </span>
          </div>
          <div class="profile-points" v-if="userStore.userInfo?.role === 3">
            <i class="fas fa-gem"></i>
            <span class="points-value">{{ currentPoints.toLocaleString() }}</span>
            <span class="points-label">积分</span>
          </div>
        </div>
      </div>

      <!-- 邀请码（admin/operator） -->
      <div v-if="userStore.userInfo?.role !== 3" class="cyber-card invite-card" :class="{ 'has-code': !!inviteCode }">
        <div class="invite-card-inner">
          <div class="invite-icon">
            <i class="fas fa-qrcode"></i>
          </div>

          <template v-if="inviteCode">
            <div class="invite-code-display">{{ inviteCode }}</div>
            <div v-if="createdAt" class="invite-meta">生成于 {{ createdAt }}</div>
            <div class="invite-actions">
              <button class="cyber-btn" @click="handleCopyInviteCode">
                <i class="fas fa-copy"></i>复制邀请码
              </button>
              <button class="cyber-btn-primary" @click="handleGenerateInviteCode" :disabled="generating">
                <i class="fas fa-sync-alt" :class="{ 'fa-spin': generating }"></i>生成新码
              </button>
            </div>
          </template>

          <template v-else>
            <div class="invite-empty">暂无可用邀请码</div>
            <div class="invite-actions">
              <button class="cyber-btn-primary" @click="handleGenerateInviteCode" :disabled="generating">
                <i class="fas fa-plus" :class="{ 'fa-spin': generating }"></i>生成邀请码
              </button>
            </div>
          </template>
        </div>
      </div>

      <!-- 积分记录 -->
      <div v-if="userStore.userInfo?.role === 3" class="cyber-card">
        <h3 class="section-title">积分记录</h3>

        <a-spin :spinning="loading">
          <div v-if="pointsLog.length === 0" class="empty-state">
            <i class="fas fa-receipt"></i>
            <p>暂无积分记录</p>
          </div>

          <div v-else class="points-log-list">
            <div v-for="log in pointsLog" :key="log.id" class="log-item">
              <div class="log-main">
                <div class="log-type">
                  <span :class="['type-badge', getTypeInfo(log.type).class]">
                    <i :class="['fas', getTypeInfo(log.type).icon]"></i>
                    {{ getTypeInfo(log.type).text }}
                  </span>
                </div>
                <div class="log-amount" :class="getTypeInfo(log.type).class">
                  <span class="amount-prefix">{{ getTypeInfo(log.type).sign }}</span>
                  <span class="amount-value">{{ Math.abs(log.amount) }}</span>
                </div>
              </div>
              <div class="log-details">
                <div class="log-row">
                  <span class="log-label">余额</span>
                  <span class="log-value">{{ log.balance.toLocaleString() }}</span>
                </div>
                <div class="log-row" v-if="log.remark">
                  <span class="log-label">备注</span>
                  <span class="log-value remark">{{ log.remark }}</span>
                </div>
                <div class="log-row">
                  <span class="log-label">时间</span>
                  <span class="log-value time">{{ formatDate(log.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </a-spin>

        <!-- 分页 -->
        <div v-if="pointsLog.length > 0" class="pagination-wrapper">
          <CyberPagination
            v-model:current="pagination.page"
            v-model:pageSize="pagination.size"
            :total="pagination.total"
            @change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <!-- 编辑昵称弹窗 -->
    <a-modal
      v-model:open="editNicknameVisible"
      title="修改昵称"
      :centered="true"
      :width="380"
      :confirm-loading="editNicknameLoading"
      ok-text="保存"
      cancel-text="取消"
      class="cyber-modal"
      @ok="handleSaveNickname"
    >
      <div class="edit-nickname-form">
        <label>昵称</label>
        <input
          v-model="nicknameForm"
          class="cyber-input"
          placeholder="请输入昵称"
          maxlength="20"
        />
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
#page-profile {
  min-height: 100vh;
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.page-content {
  position: relative;
  z-index: 1;
  max-width: 720px;
  margin: 0 auto;
}

/* ===== Page Head ===== */
.page-head {
  margin-bottom: 28px;
}

.page-head h2 {
  font-size: 26px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 0;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

.accent-line {
  display: inline-block;
  width: 5px;
  height: 24px;
  background: linear-gradient(180deg, var(--accent), var(--accent-dark));
  border-radius: 3px;
  box-shadow: var(--accent-glow-text);
}

/* ===== Cyber Card Base ===== */
.cyber-card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius);
  position: relative;
  overflow: visible;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.cyber-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border-glow), transparent);
  opacity: 0.6;
}

.cyber-card:hover {
  border-color: var(--border-glow);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

/* ===== User Profile Card ===== */
.profile-card {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 28px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, var(--bg-card) 0%, rgba(99, 102, 241, 0.03) 100%);
}

.profile-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  position: relative;
  box-shadow:
    0 0 0 3px var(--bg-card),
    0 0 0 5px var(--accent),
    0 0 30px rgba(99, 102, 241, 0.4);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.profile-avatar::after {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), var(--pink));
  opacity: 0;
  z-index: -1;
  transition: opacity 0.3s ease;
}

.profile-avatar-wrap:hover .profile-avatar {
  transform: scale(1.05);
  box-shadow:
    0 0 0 3px var(--bg-card),
    0 0 0 5px var(--accent-light),
    0 0 40px rgba(99, 102, 241, 0.6);
}

.edit-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid var(--bg-card);
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  cursor: pointer;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.25s ease;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.4);
}

.profile-avatar-wrap:hover .edit-btn {
  opacity: 1;
  transform: scale(1);
}

.edit-btn:hover {
  background: var(--accent-light);
  box-shadow: var(--accent-glow-hover);
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.profile-nickname {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 10px;
  letter-spacing: 0.5px;
  text-shadow: 0 0 20px rgba(99, 102, 241, 0.2);
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.nickname-edit-icon {
  font-size: 12px;
  color: var(--text-tertiary);
  opacity: 0;
  transition: all 0.2s ease;
}

.profile-nickname:hover .nickname-edit-icon {
  opacity: 1;
  color: var(--accent);
}

.profile-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.profile-tag {
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-input);
  padding: 6px 12px;
  border-radius: 20px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid var(--border-subtle);
  transition: all 0.2s ease;
}

.profile-tag:hover {
  border-color: var(--border-glow);
  background: var(--bg-card-hover);
}

.profile-tag i {
  color: var(--accent);
  font-size: 10px;
}

/* ===== Inline Points in Profile Card ===== */
.profile-points {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 8px 16px;
  background: linear-gradient(135deg, rgba(var(--accent-rgb), 0.08), rgba(var(--accent-rgb), 0.04));
  border: 1px solid rgba(var(--accent-rgb), 0.2);
  border-radius: 24px;
}

.profile-points i {
  color: var(--accent);
  font-size: 16px;
}

.points-value {
  font-size: 20px;
  font-weight: 700;
  font-family: var(--font-mono);
  color: var(--text-primary);
}

.points-label {
  font-size: 12px;
  color: var(--text-secondary);
}

/* ===== Invite Code Section (matches InviteCodeManage.vue style) ===== */
.invite-card {
  position: relative;
  z-index: 1;
  overflow: hidden;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: 16px;
  padding: 48px 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  margin-bottom: 20px;
}

.invite-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--accent), transparent);
  animation: borderGlow 3s ease-in-out infinite;
}

.invite-card:hover {
  border-color: var(--border-glow);
  box-shadow: 0 0 24px rgba(var(--accent-rgb), 0.12);
}

.invite-card.has-code {
  border-color: var(--border-glow);
  box-shadow: 0 0 30px rgba(var(--accent-rgb), 0.10);
}

.invite-card-inner {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.invite-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(var(--accent-rgb), 0.10);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--accent);
  margin-bottom: 8px;
}

.invite-code-display {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 4px;
  color: var(--accent);
  font-family: 'Courier New', monospace;
  background: rgba(var(--accent-rgb), 0.06);
  border: 1px dashed rgba(var(--accent-rgb), 0.30);
  border-radius: 8px;
  padding: 16px 32px;
  user-select: all;
}

.invite-meta {
  font-size: 12px;
  color: var(--text-muted);
}

.invite-empty {
  font-size: 16px;
  color: var(--text-muted);
}

.invite-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.invite-actions .cyber-btn,
.invite-actions .cyber-btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 8px;
  border: 1px solid var(--border-subtle);
  background: var(--bg-card);
  color: var(--text-primary);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.invite-actions .cyber-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.invite-actions .cyber-btn-primary {
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  border-color: transparent;
  color: #fff;
}

.invite-actions .cyber-btn-primary:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(var(--accent-rgb), 0.30);
}

.invite-actions .cyber-btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@keyframes borderGlow {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}

/* ===== Points Balance Card (unused - kept for migration) ===== */
/* ===== Points Log Section ===== */
.points-balance-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, var(--bg-card) 0%, rgba(99, 102, 241, 0.08) 50%, rgba(236, 72, 153, 0.05) 100%);
  position: relative;
  overflow: hidden;
}

.points-balance-card::after {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.15) 0%, transparent 70%);
  pointer-events: none;
}

.balance-content {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px 8px;
  position: relative;
  z-index: 1;
}

.balance-icon {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: linear-gradient(135deg, var(--accent) 0%, var(--pink) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #fff;
  flex-shrink: 0;
  box-shadow:
    0 4px 20px rgba(99, 102, 241, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  animation: gem-pulse 3s ease-in-out infinite;
}

@keyframes gem-pulse {
  0%, 100% { box-shadow: 0 4px 20px rgba(99, 102, 241, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.2); }
  50% { box-shadow: 0 4px 30px rgba(99, 102, 241, 0.6), inset 0 1px 0 rgba(255, 255, 255, 0.2); }
}

.balance-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.balance-label {
  font-size: 14px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 2px;
  font-weight: 500;
}

.balance-value {
  font-size: 48px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1;
  font-family: var(--font-mono);
  letter-spacing: -1px;
  text-shadow:
    0 0 30px rgba(99, 102, 241, 0.4),
    0 0 60px rgba(99, 102, 241, 0.2);
  background: linear-gradient(135deg, var(--text-primary) 0%, var(--accent-light) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* ===== Points Log Section ===== */
.cyber-card:has(.section-title) {
  padding: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: 0.5px;
}

.section-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, var(--accent), var(--pink));
  border-radius: 2px;
  box-shadow: var(--accent-glow-text);
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-muted);
}

.empty-state i {
  font-size: 52px;
  opacity: 0.25;
  margin-bottom: 16px;
  color: var(--accent);
}

.empty-state p {
  font-size: 14px;
  letter-spacing: 0.5px;
}

.points-log-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  position: relative;
}

/* Timeline connecting line */
.points-log-list::before {
  content: '';
  position: absolute;
  left: 28px;
  top: 20px;
  bottom: 20px;
  width: 2px;
  background: linear-gradient(180deg, var(--border-glow), var(--border-subtle), var(--border-glow));
  border-radius: 1px;
}

.log-item {
  position: relative;
  padding: 16px 16px 16px 56px;
  transition: background 0.2s ease;
}

.log-item::before {
  content: '';
  position: absolute;
  left: 23px;
  top: 24px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--bg-card);
  border: 2px solid var(--accent);
  box-shadow: 0 0 10px rgba(99, 102, 241, 0.3);
  z-index: 1;
  transition: all 0.2s ease;
}

.log-item:hover {
  background: rgba(99, 102, 241, 0.03);
}

.log-item:hover::before {
  background: var(--accent);
  box-shadow: 0 0 15px rgba(99, 102, 241, 0.6);
}

.log-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.log-type {
  display: flex;
  align-items: center;
}

.type-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.type-badge.increase {
  background: rgba(16, 185, 129, 0.12);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.type-badge.increase:hover {
  background: rgba(16, 185, 129, 0.2);
  box-shadow: 0 0 15px rgba(16, 185, 129, 0.2);
}

.type-badge.decrease {
  background: rgba(239, 68, 68, 0.10);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.type-badge.decrease:hover {
  background: rgba(239, 68, 68, 0.18);
  box-shadow: 0 0 15px rgba(239, 68, 68, 0.2);
}

.type-badge i {
  font-size: 10px;
}

.log-amount {
  font-size: 18px;
  font-weight: 700;
  font-family: var(--font-mono);
}

.log-amount.increase {
  color: #10b981;
  text-shadow: 0 0 15px rgba(16, 185, 129, 0.3);
}

.log-amount.decrease {
  color: #ef4444;
  text-shadow: 0 0 15px rgba(239, 68, 68, 0.3);
}

.amount-prefix {
  font-size: 14px;
  margin-right: 2px;
}

.amount-value {
  font-size: 18px;
}

.log-details {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding-left: 4px;
}

.log-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.log-label {
  color: var(--text-muted);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.log-value {
  color: var(--text-secondary);
  font-weight: 500;
}

.log-value.remark {
  color: var(--text-primary);
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.log-value.time {
  font-size: 11px;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

/* ===== Pagination ===== */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-subtle);
}

:deep(.ant-pagination) {
  display: flex;
  align-items: center;
  gap: 4px;
}

:deep(.ant-pagination-item) {
  background: var(--bg-card);
  border-color: var(--border);
  border-radius: var(--radius-xs);
  transition: all 0.2s ease;
}

:deep(.ant-pagination-item:hover) {
  border-color: var(--accent);
  background: var(--bg-card-hover);
}

:deep(.ant-pagination-item-active) {
  background: var(--accent) !important;
  border-color: var(--accent) !important;
}

:deep(.ant-pagination-item-active a) {
  color: #fff !important;
}

:deep(.ant-pagination-prev button),
:deep(.ant-pagination-next button) {
  background: var(--bg-card);
  border-color: var(--border);
  border-radius: var(--radius-xs);
  color: var(--text-secondary);
  transition: all 0.2s ease;
}

:deep(.ant-pagination-prev:hover button),
:deep(.ant-pagination-next:hover button) {
  border-color: var(--accent);
  color: var(--accent);
}

:deep(.ant-pagination-options) {
  margin-left: 12px;
}

:deep(.ant-select-selector) {
  background: var(--bg-card) !important;
  border-color: var(--border) !important;
  border-radius: var(--radius-xs) !important;
}

:deep(.ant-select:hover .ant-select-selector) {
  border-color: var(--accent) !important;
}

/* ===== Edit Nickname Form ===== */
.edit-nickname-form {
  padding: 8px 0;
}

.edit-nickname-form label {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 10px;
  font-weight: 500;
}

.edit-nickname-form input {
  width: 100%;
}

/* ===== Responsive ===== */
@media (max-width: 600px) {
  #page-profile {
    padding: 16px;
  }

  .page-head h2 {
    font-size: 22px;
  }

  .profile-card {
    flex-direction: column;
    text-align: center;
    padding: 24px;
    gap: 20px;
  }

  .profile-meta {
    justify-content: center;
  }

  .profile-nickname {
    font-size: 20px;
  }

  .balance-content {
    flex-direction: column;
    text-align: center;
    gap: 16px;
    padding: 24px 8px;
  }

  .balance-value {
    font-size: 40px;
  }

  .log-details {
    flex-direction: column;
    gap: 8px;
  }

  .points-log-list::before {
    left: 20px;
  }

  .log-item {
    padding-left: 44px;
  }

  .log-item::before {
    left: 15px;
  }

  .pagination-wrapper {
    justify-content: center;
    overflow-x: auto;
  }
}
</style>
