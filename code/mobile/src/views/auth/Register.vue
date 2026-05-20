<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const nickname = ref('')
const password = ref('')
const confirmPassword = ref('')
const inviteCode = ref('')
const loading = ref(false)

async function handleRegister() {
  if (!username.value || !nickname.value || !password.value || !inviteCode.value) {
    showToast('请填写完整信息')
    return
  }
  if (password.value !== confirmPassword.value) {
    showToast('两次密码不一致')
    return
  }
  if (password.value.length < 6) {
    showToast('密码至少6位')
    return
  }

  loading.value = true
  showLoadingToast({ message: '注册中...', forbidClick: true })

  try {
    const res = await authApi.register({
      username: username.value,
      nickname: nickname.value,
      password: password.value,
      confirmPassword: confirmPassword.value,
      inviteCode: inviteCode.value,
    })
    userStore.setUserInfo(res)
    showToast('注册成功')
    router.replace('/mobile/login')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
    closeToast()
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-header">
      <div class="logo">积分商城</div>
      <p class="subtitle">注册账号</p>
    </div>

    <van-form @submit="handleRegister" class="register-form">
      <van-cell-group inset>
        <van-field
          v-model="username"
          label=""
          placeholder="用户名"
          :rules="[{ required: true, message: '请填写用户名' }]"
        />
        <van-field
          v-model="nickname"
          label=""
          placeholder="昵称"
          :rules="[{ required: true, message: '请填写昵称' }]"
        />
        <van-field
          v-model="password"
          type="password"
          label=""
          placeholder="密码（至少6位）"
          :rules="[{ required: true, message: '请填写密码' }]"
        />
        <van-field
          v-model="confirmPassword"
          type="password"
          label=""
          placeholder="确认密码"
          :rules="[{ required: true, message: '请确认密码' }]"
        />
        <van-field
          v-model="inviteCode"
          label=""
          placeholder="邀请码"
          :rules="[{ required: true, message: '请填写邀请码' }]"
        />
      </van-cell-group>

      <div class="register-actions">
        <van-button
          type="primary"
          block
          round
          :loading="loading"
          native-type="submit"
          class="register-btn"
        >
          注册
        </van-button>
        <van-button
          type="default"
          block
          round
          plain
          @click="router.push('/mobile/login')"
          class="login-btn"
        >
          已有账号，去登录
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 40px 24px;
  background: var(--bg-primary);
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  font-size: 28px;
  font-weight: 700;
  color: var(--accent);
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: var(--text-muted);
}

.register-form {
  width: 100%;
}

.register-actions {
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.register-btn {
  --van-button-primary-background: var(--accent);
  --van-button-primary-border-color: var(--accent);
}

.login-btn {
  --van-button-default-background: transparent;
  --van-button-default-border-color: var(--border-subtle);
  --van-button-default-color: var(--text-secondary);
}
</style>