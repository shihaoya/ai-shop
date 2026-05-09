import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './style.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// 初始化用户信息
import { useAuthStore } from './stores/auth'
const authStore = useAuthStore()
if (authStore.token) {
  authStore.fetchUserInfo()
}

app.mount('#app')