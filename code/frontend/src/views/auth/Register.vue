<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { authApi } from '@/api/auth'
import ThemeToggleBtn from '@/components/layout/ThemeToggleBtn.vue'

const router = useRouter()

const form = ref({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  inviteCode: '',
})
const loading = ref(false)

async function handleRegister() {
  if (!form.value.username || !form.value.nickname || !form.value.password) {
    message.error('请填写完整信息')
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    message.error('两次密码输入不一致')
    return
  }
  if (!form.value.inviteCode) {
    message.error('请输入邀请码')
    return
  }

  loading.value = true
  await authApi.register({
    username: form.value.username,
    nickname: form.value.nickname,
    password: form.value.password,
    confirmPassword: form.value.confirmPassword,
    inviteCode: form.value.inviteCode,
  })
  message.success('注册成功，请等待审核')
  loading.value = false
  router.push('/login')
}
</script>

<template>
  <div id="page-register">
    <!-- BG -->
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;top:-100px;right:-100px;background:rgba(99,102,241,0.12);"></div>
    <div class="cyber-bg-orb" style="width:300px;height:300px;bottom:10%;left:-80px;background:rgba(236,72,153,0.08);"></div>

    <!-- 注册卡片 -->
    <div class="login-card cyber-card">
      <div class="logo">P</div>
      <h1>创建账号</h1>
      <p class="sub">// JOIN_POINTS_MALL</p>

      <form @submit.prevent="handleRegister">
        <div class="field">
          <label>用户名</label>
          <div class="input-wrap">
            <i class="fas fa-user"></i>
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
            />
          </div>
        </div>

        <div class="field">
          <label>昵称</label>
          <div class="input-wrap">
            <i class="fas fa-id-card"></i>
            <input
              v-model="form.nickname"
              type="text"
              placeholder="请输入昵称"
            />
          </div>
        </div>

        <div class="field">
          <label>密码</label>
          <div class="input-wrap">
            <i class="fas fa-lock"></i>
            <input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
            />
          </div>
        </div>

        <div class="field">
          <label>确认密码</label>
          <div class="input-wrap">
            <i class="fas fa-lock"></i>
            <input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
            />
          </div>
        </div>

        <div class="field">
          <label>邀请码</label>
          <div class="input-wrap">
            <i class="fas fa-ticket-alt"></i>
            <input
              v-model="form.inviteCode"
              type="text"
              placeholder="请输入邀请码"
            />
          </div>
        </div>

        <div class="actions">
          <button type="submit" class="cyber-btn-primary" :disabled="loading">
            <i class="fas fa-user-plus" style="margin-right:6px;"></i>
            {{ loading ? '注册中...' : '注 册' }}
          </button>
          <button type="button" class="cyber-btn" @click="router.push('/login')">
            <i class="fas fa-arrow-left" style="margin-right:6px;"></i>
            返回登录
          </button>
        </div>
      </form>
    </div>

    <!-- 主题切换按钮 -->
    <ThemeToggleBtn />
  </div>
</template>

<style scoped>
#page-register {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.login-card {
  position: relative;
  z-index: 1;
  width: 400px;
  max-width: 100%;
  padding: 44px 40px;
  animation: scaleIn 0.5s ease-out, fadeInUp 0.5s ease-out;
  overflow: hidden;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--accent), transparent);
  animation: borderGlow 3s ease-in-out infinite;
}

.logo {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, var(--accent), var(--pink));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 22px;
  font-weight: bold;
  box-shadow: var(--accent-glow);
  margin-bottom: 28px;
}

.login-card h1 {
  font-size: 28px;
  font-weight: 800;
  margin-bottom: 4px;
  background: linear-gradient(135deg, var(--text-primary) 0%, var(--accent-light) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-card .sub {
  font-size: 14px;
  color: var(--text-muted);
  font-family: var(--font-mono);
  margin-bottom: 32px;
  letter-spacing: 0.5px;
}

.field {
  margin-bottom: 18px;
}

.field label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 7px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.input-wrap {
  display: flex;
  align-items: center;
  background: var(--bg-input);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-sm);
  padding: 0 14px;
  transition: all 0.2s ease;
}

.input-wrap:focus-within {
  border-color: var(--border-active);
  box-shadow: 0 0 0 3px rgba(var(--accent-rgb), 0.10);
}

.input-wrap i {
  color: var(--text-muted);
  font-size: 14px;
  margin-right: 10px;
  width: 16px;
  text-align: center;
}

.input-wrap input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 11px 0;
  color: var(--text-primary);
  font-size: 14px;
  outline: none;
  font-family: inherit;
}

.input-wrap input::placeholder {
  color: var(--text-muted);
}

.actions {
  margin-top: 28px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.actions button {
  width: 100%;
  justify-content: center;
  display: flex;
  align-items: center;
}
</style>