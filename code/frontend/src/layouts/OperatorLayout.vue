<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterView } from 'vue-router'
import { Dropdown, Menu, message } from 'ant-design-vue'
import { useThemeStore } from '@/stores/theme'
import { useSidebarStore } from '@/stores/sidebar'
import { useSidebarMenu } from '@/composables/useSidebarMenu'
import { useOperatorShop } from '@/composables/useOperatorShop'
import { useUserStore } from '@/stores/user'
import ThemeToggleBtn from '@/components/layout/ThemeToggleBtn.vue'
import { getMyShop } from '@/api/operator'
import { authApi } from '@/api/auth'

const themeStore = useThemeStore()
const sidebarStore = useSidebarStore()
const userStore = useUserStore()
const { hasShop, shopStatus, setHasShop } = useOperatorShop()
const { items: navItems, currentLabel, isActive } = useSidebarMenu('/operator', hasShop, shopStatus)

// 修改密码弹窗
const changePwdVisible = ref(false)
const changePwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const changePwdLoading = ref(false)

function openChangePwd() {
  changePwdVisible.value = true
  changePwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
}

async function handleChangePwd() {
  const { oldPassword, newPassword, confirmPassword } = changePwdForm.value
  if (!oldPassword || !newPassword || !confirmPassword) {
    message.warning('请填写完整信息')
    return
  }
  if (newPassword !== confirmPassword) {
    message.error('新密码与确认密码不一致')
    return
  }
  if (newPassword.length < 6) {
    message.error('新密码长度不能少于6位')
    return
  }
  changePwdLoading.value = true
  try {
    await authApi.updatePassword({ oldPassword, newPassword })
    message.success('密码修改成功')
    changePwdVisible.value = false
  } catch (e: any) {
    message.error(e?.message || '修改失败')
  } finally {
    changePwdLoading.value = false
  }
}

function handleLogout() {
  userStore.logout()
}

onMounted(async () => {
  themeStore.init()
  // 未登录不调接口
  if (!userStore.token) return
  try {
    const shop = await getMyShop()
    setHasShop(!!shop && !!shop.id, shop?.status ?? null)
  } catch {
    setHasShop(false, null)
  }
})
</script>

<template>
  <div class="operator-layout">
    <!-- Header -->
    <header class="cyber-header">
      <div class="left">
        <div class="brand">P</div>
        <div class="breadcrumb">
          {{ currentLabel || '店铺运营' }}
        </div>
      </div>
      <div class="right">
        <button class="icon-btn">
          <i class="far fa-bell"></i>
          <span class="dot"></span>
        </button>
        <button class="icon-btn">
          <i class="fas fa-expand"></i>
        </button>
        <Dropdown trigger="hover">
          <div class="user-tag">
            <div class="avatar">O</div>
            <span class="name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '运营人员' }}</span>
            <i class="fas fa-chevron-down" style="font-size:10px;color:var(--text-muted);margin-left:2px;"></i>
          </div>
          <template #overlay>
            <Menu>
              <div class="dropdown-userinfo">
                <span class="dropdown-name">{{ userStore.userInfo?.nickname }}</span>
                <span class="dropdown-username">({{ userStore.userInfo?.username }})</span>
              </div>
              <Menu.Divider />
              <Menu.Item key="pwd" @click="openChangePwd">
                <i class="fas fa-key" style="margin-right:8px;"></i>修改密码
              </Menu.Item>
              <Menu.Item key="logout" @click="handleLogout">
                <i class="fas fa-sign-out-alt" style="margin-right:8px;"></i>退出登录
              </Menu.Item>
            </Menu>
          </template>
        </Dropdown>
      </div>
    </header>

    <!-- Sidebar -->
    <aside class="cyber-sidebar" :class="{ collapsed: sidebarStore.collapsed }">
      <div class="collapse-btn">
        <button @click="sidebarStore.toggle()">
          <i class="fas fa-bars"></i>
        </button>
      </div>
      <nav class="cyber-nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="cyber-nav-item"
          :class="{ active: isActive(item.path) }"
          :data-tip="item.label"
        >
          <div class="active-glow" :style="{ display: isActive(item.path) ? 'block' : 'none' }"></div>
          <i :class="['fas', item.icon, 'icon']"></i>
          <span class="label">{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>

    <!-- Main Content -->
    <main class="main-content">
      <RouterView v-slot="{ Component, route: curRoute }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" :key="curRoute.path" />
        </transition>
      </RouterView>
    </main>

    <!-- Theme Toggle -->
    <ThemeToggleBtn />

    <!-- 修改密码弹窗 -->
    <div class="modal-overlay" v-if="changePwdVisible">
      <div class="modal-card" style="max-width:420px;">
        <div class="modal-header">
          <h3><i class="fas fa-key" style="margin-right:8px;color:var(--accent);"></i>修改密码</h3>
          <button class="modal-close" @click="changePwdVisible = false">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>原密码</label>
            <input
              v-model="changePwdForm.oldPassword"
              class="cyber-input"
              type="password"
              autocomplete="off"
              placeholder="请输入原密码"
            />
          </div>
          <div class="form-group">
            <label>新密码</label>
            <input
              v-model="changePwdForm.newPassword"
              class="cyber-input"
              type="password"
              autocomplete="new-password"
              placeholder="请输入新密码（至少6位）"
            />
          </div>
          <div class="form-group">
            <label>确认密码</label>
            <input
              v-model="changePwdForm.confirmPassword"
              class="cyber-input"
              type="password"
              autocomplete="new-password"
              placeholder="请再次输入新密码"
              @keyup.enter="handleChangePwd"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button class="cyber-btn" @click="changePwdVisible = false">取消</button>
          <button class="cyber-btn-primary" :disabled="changePwdLoading" @click="handleChangePwd">
            <i v-if="changePwdLoading" class="fas fa-spinner fa-spin" style="margin-right:5px;"></i>
            确认修改
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.operator-layout {
  min-height: 100vh;
  position: relative;
}

.main-content {
  margin-left: calc(var(--sidebar-width, 200px) + 16px);
  margin-top: 68px;
  padding: 20px;
  transition: margin-left 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

:global(.cyber-sidebar.collapsed),
:global(.cyber-sidebar.collapsed .cyber-nav) {
  overflow: visible !important;
}

.user-tag {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-tag:hover {
  background: var(--bg-hover);
}

:global(.ant-dropdown .ant-dropdown-menu) {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5);
  padding: 4px 0;
  min-width: 200px;
}

:global(.ant-dropdown .ant-dropdown-menu-item) {
  color: var(--text-secondary);
  font-size: 13px;
  padding: 10px 16px;
}

:global(.ant-dropdown .ant-dropdown-menu-item:hover) {
  background: var(--bg-hover);
  color: var(--accent);
}

:global(.ant-dropdown .ant-dropdown-menu-divider) {
  background: var(--border-subtle);
  margin: 4px 0;
}

.dropdown-userinfo {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.dropdown-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.dropdown-username {
  color: var(--text-muted);
  font-size: 12px;
}

.user-menu-wrapper {
  position: relative;
}

.user-tag {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-tag:hover {
  background: var(--bg-hover);
}

.user-dropdown {
  display: none;
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  min-width: 200px;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5);
  z-index: 1000;
  overflow: hidden;
  padding: 4px 0;
}

.user-dropdown:not(.dropdown-hidden) {
  display: block;
}

.dropdown-userinfo {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.dropdown-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.dropdown-username {
  color: var(--text-muted);
  font-size: 12px;
}

.dropdown-divider {
  height: 1px;
  background: var(--border-subtle);
  margin: 4px 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 10px 16px;
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  text-align: left;
}

.dropdown-item:hover {
  background: var(--bg-hover);
  color: var(--accent);
}
</style>
