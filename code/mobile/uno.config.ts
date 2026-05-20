import { defineConfig, presetAttributify, presetIcons, presetUno } from 'unocss'

export default defineConfig({
  presets: [
    presetUno(),
    presetAttributify(),
    presetIcons({
      scale: 1.2,
      cdn: 'https://cdn.jsdelivr.net/npm/@vant/icons@latest/'
    }),
  ],
  theme: {
    colors: {
      primary: 'var(--van-primary-color, #6366f1)',
    },
  },
})