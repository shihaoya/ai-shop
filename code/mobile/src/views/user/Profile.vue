<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { authApi } from '@/api/auth'
import { getPoints } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const currentPoints = ref(0)
const themeVisible = ref(false)
const changePwdVisible = ref(false)

const themeMode = ref(themeStore.mode)
const themeColor = ref(themeStore.accentColor)

async function loadPoints() {
  try {
    const res = await getPoints()
    currentPoints.value = res.points
  } catch {
    // ignore
  }
}

function openThemeSetting() {
  themeMode.value = themeStore.mode
  themeColor.value = themeStore.accentColor
  themeVisible.value = true
}

function onThemeConfirm() {
  themeStore.setMode(themeMode.value)
  themeStore.setAccentColor(themeColor.value)
  themeVisible.value = false
  showToast('主题已更新')
}

function openChangePwd() {
  changePwdVisible.value = true
}

async function handleChangePwd({ oldPassword, newPassword }: { oldPassword: string; newPassword: string }) {
  try {
    await authApi.updatePassword({ oldPassword, newPassword })
    showToast('密码修改成功')
    changePwdVisible.value = false
  } catch {
    showToast('密码修改失败')
  }
}

async function handleLogout() {
  try {
    await showConfirmDialog({ title: '确认退出', message: '确定要退出登录吗？' })
    await authApi.logout()
  } catch { /* cancelled */ }
  userStore.logout()
  router.replace('/mobile/login')
}

onMounted(() => {
  loadPoints()
})
</script>

<template>
  <div class="profile-page">
    <van-nav-bar title="个人中心" />
    <div class="content">
      <!-- 用户信息卡片 -->
      <div class="user-card">
        <div class="avatar">
          {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0).toUpperCase() }}
        </div>
        <div class="user-info">
          <div class="nickname">{{ userStore.userInfo?.nickname || '-' }}</div>
          <div class="role-tag">
            {{ userStore.userInfo?.role === 3 ? '普通用户' : userStore.userInfo?.role === 2 ? '店铺用户' : '管理员' }}
          </div>
        </div>
      </div>

      <!-- 积分卡片（普通用户） -->
      <div v-if="userStore.userInfo?.role === 3" class="points-card">
        <div class="points-icon-wrap">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/></svg>
        </div>
        <div class="points-info">
          <div class="points-label">我的积分</div>
          <div class="points-value">{{ currentPoints.toLocaleString() }}</div>
        </div>
      </div>

      <!-- 功能菜单 -->
      <div class="menu-section">
        <div class="menu-title">常用功能</div>
        <div class="menu-grid">
          <div v-if="userStore.userInfo?.role === 3" class="menu-item" @click="router.push('/mobile/user/addresses')">
            <div class="menu-icon" style="background: #e8f5e9; color: #4caf50;">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            </div>
            <span class="menu-label">收货地址</span>
          </div>
          <div v-if="userStore.userInfo?.role === 3" class="menu-item" @click="router.push('/mobile/user/messages')">
            <div class="menu-icon" style="background: #e3f2fd; color: #2196f3;">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            </div>
            <span class="menu-label">我的消息</span>
          </div>
          <div class="menu-item" @click="openThemeSetting">
            <div class="menu-icon" style="background: #f3e5f5; color: #9c27b0;">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="5"/><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>
            </div>
            <span class="menu-label">主题设置</span>
          </div>
          <div class="menu-item" @click="openChangePwd">
            <div class="menu-icon" style="background: #fff3e0; color: #ff9800;">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
            </div>
            <span class="menu-label">修改密码</span>
          </div>
        </div>
      </div>

      <!-- 退出登录 -->
      <div class="logout-section">
        <van-button type="default" block class="logout-btn" @click="handleLogout">退出登录</van-button>
      </div>
    </div>

    <!-- 主题设置弹窗 -->
    <van-popup v-model:show="themeVisible" position="bottom" round>
      <div class="theme-panel">
        <div class="panel-title">主题设置</div>
        <div class="theme-section">
          <div class="section-title">外观</div>
          <van-radio-group v-model="themeMode" direction="horizontal">
            <van-radio name="light" icon-size="18px">浅色</van-radio>
            <van-radio name="dark" icon-size="18px">深色</van-radio>
            <van-radio name="auto" icon-size="18px">跟随系统</van-radio>
          </van-radio-group>
        </div>
        <div class="theme-section">
          <div class="section-title">主题色</div>
          <van-radio-group v-model="themeColor" direction="horizontal">
            <van-radio name="#6366f1" icon-size="18px">紫色</van-radio>
            <van-radio name="#3b82f6" icon-size="18px">蓝色</van-radio>
            <van-radio name="#10b981" icon-size="18px">绿色</van-radio>
          </van-radio-group>
        </div>
        <van-button type="primary" block @click="onThemeConfirm">确认</van-button>
      </div>
    </van-popup>

    <!-- 修改密码弹窗 -->
    <van-popup v-model:show="changePwdVisible" position="bottom" round>
      <div class="pwd-panel">
        <div class="panel-title">修改密码</div>
        <van-form @submit="handleChangePwd">
          <van-cell-group inset>
            <van-field name="oldPassword" type="password" placeholder="原密码" :rules="[{ required: true, message: '请填写原密码' }]" />
            <van-field name="newPassword" type="password" placeholder="新密码（至少6位）" :rules="[{ required: true, message: '请填写新密码' }]" />
          </van-cell-group>
          <div class="btn-wrap">
            <van-button type="primary" block native-type="submit">确认修改</van-button>
          </div>
        </van-form>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.profile-page {
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
  padding-bottom: env(safe-area-inset-bottom);
}

.user-card {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 16px 12px;
  padding: 20px 16px;
  background: var(--bg-card);
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
}

.nickname {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 6px;
}

.role-tag {
  display: inline-block;
  font-size: 12px;
  padding: 3px 10px;
  background: var(--accent);
  color: #fff;
  border-radius: 12px;
}

.points-card {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 12px;
  padding: 18px 20px;
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  border-radius: 16px;
  color: #fff;
}

.points-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.points-info {
  flex: 1;
}

.points-label {
  font-size: 13px;
  opacity: 0.9;
  margin-bottom: 2px;
}

.points-value {
  font-size: 28px;
  font-weight: 700;
}

.menu-section {
  margin: 16px 12px;
}

.menu-title {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 12px;
  padding-left: 4px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  background: var(--bg-card);
  border-radius: 12px;
}

.menu-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.logout-section {
  margin: 24px 12px 12px;
}

.logout-btn {
  border-radius: 10px;
  color: #ee0a24;
  border-color: #ee0a24;
}

.theme-panel,
.pwd-panel {
  padding: 20px 16px;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 20px;
}

.theme-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.btn-wrap {
  margin-top: 20px;
  padding: 0 16px;
}
</style>