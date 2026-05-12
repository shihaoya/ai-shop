import { defineStore } from 'pinia'
import { ref } from 'vue'

type ThemeMode = 'light' | 'dark' | 'auto'

interface ThemeState {
  mode: ThemeMode
  accentColor: string
}

const DEFAULT_ACCENT = '#6366f1'
const STORAGE_KEY = 'cyber-theme-config'

function loadTheme(): ThemeState {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) return JSON.parse(saved)
  } catch { /* ignore */ }
  return { mode: 'auto', accentColor: DEFAULT_ACCENT }
}

function getSystemMode(): 'light' | 'dark' {
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export const useThemeStore = defineStore('theme', () => {
  const saved = loadTheme()
  const mode = ref<ThemeMode>(saved.mode)
  const accentColor = ref(saved.accentColor)
  const effectiveMode = ref<'light' | 'dark'>(mode.value === 'auto' ? getSystemMode() : mode.value)

  function hexToRgb(hex: string) {
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
    return result ? {
      r: parseInt(result[1], 16),
      g: parseInt(result[2], 16),
      b: parseInt(result[3], 16)
    } : { r: 99, g: 102, b: 241 }
  }

  function apply() {
    const actualMode = mode.value === 'auto' ? getSystemMode() : mode.value
    effectiveMode.value = actualMode

    document.documentElement.setAttribute('data-theme', actualMode)

    const rgb = hexToRgb(accentColor.value)
    document.documentElement.style.setProperty('--accent', accentColor.value)
    document.documentElement.style.setProperty('--accent-rgb', `${rgb.r}, ${rgb.g}, ${rgb.b}`)
    document.documentElement.style.setProperty('--accent-light', `rgb(${Math.min(255, rgb.r + 40)}, ${Math.min(255, rgb.g + 40)}, ${Math.min(255, rgb.b + 40)})`)
    document.documentElement.style.setProperty('--accent-dark', `rgb(${Math.max(0, rgb.r - 40)}, ${Math.max(0, rgb.g - 40)}, ${Math.max(0, rgb.b - 40)})`)

    const isLight = actualMode === 'light'
    document.documentElement.style.setProperty('--accent-glow',
      isLight
        ? `0 0 15px rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.12), 0 0 30px rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.05)`
        : `0 0 20px rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.25), 0 0 40px rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.10)`)
    document.documentElement.style.setProperty('--accent-glow-hover',
      isLight
        ? `0 0 20px rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.22), 0 0 40px rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.08)`
        : `0 0 30px rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.4), 0 0 60px rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.15)`)
    document.documentElement.style.setProperty('--accent-glow-text',
      isLight
        ? `0 0 8px rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.3)`
        : `0 0 10px rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.5)`)

    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      mode: mode.value,
      accentColor: accentColor.value,
    }))
  }

  function setMode(m: ThemeMode) {
    mode.value = m
    apply()
  }

  function setAccentColor(color: string) {
    accentColor.value = color
    apply()
  }

  function reset() {
    mode.value = 'dark'
    accentColor.value = DEFAULT_ACCENT
    apply()
  }

  let mediaQuery: MediaQueryList | null = null

  function init() {
    apply()
    mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQuery.addEventListener('change', () => {
      if (mode.value === 'auto') apply()
    })
  }

  function destroy() {
    mediaQuery?.removeEventListener('change', () => {})
  }

  return { mode, accentColor, effectiveMode, setMode, setAccentColor, reset, init, destroy }
})