<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!username.value || !password.value) {
    showToast('请填写用户名和密码')
    return
  }

  loading.value = true

  try {
    const res = await authApi.login({ username: username.value, password: password.value })
    userStore.setToken(res.token)
    userStore.setUserInfo(res.userinfo)

    const role = res.userinfo.role
    let redirectUrl = '/mobile/user/products'
    if (role === 3) redirectUrl = '/mobile/user/products'
    else if (role === 2) redirectUrl = '/mobile/operator/products'
    else if (role === 1) redirectUrl = '/mobile/admin/shops'

    await router.replace(redirectUrl)
  } catch (e: any) {
    console.error('Login error:', e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-header">
      <div class="logo">积分商城</div>
      <p class="subtitle">移动端</p>
    </div>

    <van-form @submit="handleLogin" class="login-form">
      <van-cell-group inset>
        <van-field
          v-model="username"
          label=""
          placeholder="用户名"
          :rules="[{ required: true, message: '请填写用户名' }]"
        />
        <van-field
          v-model="password"
          type="password"
          label=""
          placeholder="密码"
          :rules="[{ required: true, message: '请填写密码' }]"
        />
      </van-cell-group>

      <div class="login-actions">
        <van-button
          type="primary"
          block
          round
          :loading="loading"
          native-type="submit"
          class="login-btn"
        >
          登录
        </van-button>
        <van-button
          type="default"
          block
          round
          plain
          @click="router.push('/mobile/register')"
          class="register-btn"
        >
          注册账号
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 40px 24px;
  background: var(--bg-primary);
}

.login-header {
  text-align: center;
  margin-bottom: 48px;
}

.logo {
  font-size: 32px;
  font-weight: 700;
  color: var(--accent);
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: var(--text-muted);
}

.login-form {
  width: 100%;
}

.login-actions {
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.login-btn {
  --van-button-primary-background: var(--accent);
  --van-button-primary-border-color: var(--accent);
}

.register-btn {
  --van-button-default-background: transparent;
  --van-button-default-border-color: var(--border-subtle);
  --van-button-default-color: var(--text-secondary);
}
</style>