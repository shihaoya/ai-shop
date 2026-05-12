<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import ThemeToggleBtn from '@/components/layout/ThemeToggleBtn.vue'

const router = useRouter()
const themeStore = useThemeStore()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: '',
})
const loading = ref(false)

onMounted(() => {
  themeStore.init()
})

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    return
  }
  loading.value = true
  try {
    await userStore.login(form.value)
    // login 内部已完成 router.push(homePath)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div id="page-login">
    <!-- BG -->
    <div class="cyber-bg-grid"></div>
    <div class="cyber-bg-orb" style="width:400px;height:400px;top:-100px;right:-100px;background:rgba(99,102,241,0.12);"></div>
    <div class="cyber-bg-orb" style="width:300px;height:300px;bottom:10%;left:-80px;background:rgba(236,72,153,0.08);"></div>

    <!-- 登录卡片 -->
    <div class="login-card cyber-card">
      <div class="logo">P</div>
      <h1>欢迎回来</h1>
      <p class="sub">// POINTS_MALL_SYSTEM v3.1</p>

      <form @submit.prevent="handleLogin">
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

        <div class="actions">
          <button type="submit" class="cyber-btn-primary" :disabled="loading">
            <i class="fas fa-sign-in-alt" style="margin-right:6px;"></i>
            {{ loading ? '登录中...' : '登 录' }}
          </button>
          <button type="button" class="cyber-btn" @click="router.push('/register')">
            <i class="fas fa-user-plus" style="margin-right:6px;"></i>
            注册账号
          </button>
        </div>
      </form>

      <p class="footer-text">
        还没有账号？<a href="#" @click.prevent="router.push('/register')">立即注册</a>
      </p>
    </div>

    <!-- 主题切换按钮 -->
    <ThemeToggleBtn />
  </div>
</template>

<style scoped>
#page-login {
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

.footer-text {
  text-align: center;
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 20px;
}

.footer-text a {
  color: var(--accent-light);
  text-decoration: none;
  font-weight: 600;
  text-shadow: var(--accent-glow-text);
}
</style>
