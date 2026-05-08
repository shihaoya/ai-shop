import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 18781,
    proxy: {
      '/api': {
        target: 'http://localhost:18780',
        changeOrigin: true,
      },
      '/admin': {
        target: 'http://localhost:18780',
        changeOrigin: true,
      },
      '/operator': {
        target: 'http://localhost:18780',
        changeOrigin: true,
      },
      '/user': {
        target: 'http://localhost:18780',
        changeOrigin: true,
      },
    },
  },
})
