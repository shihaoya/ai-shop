import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from '@/router'
import App from './App.vue'
import Antd from 'ant-design-vue'
import '@/styles/global.scss'
import '@/styles/transitions.scss'
import 'uno.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(Antd)

app.mount('#app')