import { create } from 'zustand'
import { persist } from 'zustand/middleware'

/** 5种主题色 */
export type AccentKey = 'blue' | 'purple' | 'green' | 'rose' | 'amber'

export const ACCENT_COLORS: Record<AccentKey, { primary: string; glow: string; light: string; name: string }> = {
  blue: { primary: '#3b82f6', glow: 'rgba(59, 130, 246, 0.3)', light: 'rgba(59, 130, 246, 0.15)', name: '冰蓝' },
  purple: { primary: '#8b5cf6', glow: 'rgba(139, 92, 246, 0.3)', light: 'rgba(139, 92, 246, 0.15)', name: '极光紫' },
  green: { primary: '#10b981', glow: 'rgba(16, 185, 129, 0.3)', light: 'rgba(16, 185, 129, 0.15)', name: '翠绿' },
  rose: { primary: '#f43f5e', glow: 'rgba(244, 63, 94, 0.3)', light: 'rgba(244, 63, 94, 0.15)', name: '玫瑰' },
  amber: { primary: '#f59e0b', glow: 'rgba(245, 158, 11, 0.3)', light: 'rgba(245, 158, 11, 0.15)', name: '琥珀' },
}

/** 暗色/亮色模式 */
export type ModeKey = 'dark' | 'light'

export const MODE_COLORS: Record<ModeKey, { bodyBg: string; textPrimary: string; textSecondary: string; cardBg: string; cardBorder: string; inputBg: string; inputBorder: string }> = {
  dark: {
    bodyBg: '#0a0f1a',
    textPrimary: '#f1f5f9',
    textSecondary: '#94a3b8',
    cardBg: 'rgba(15, 23, 42, 0.7)',
    cardBorder: 'rgba(59, 130, 246, 0.25)',
    inputBg: 'rgba(30, 41, 59, 0.6)',
    inputBorder: 'rgba(59, 130, 246, 0.2)',
  },
  light: {
    bodyBg: '#e8f0fe',
    textPrimary: '#0f172a',
    textSecondary: '#64748b',
    cardBg: 'rgba(255, 255, 255, 0.88)',
    cardBorder: 'rgba(59, 130, 246, 0.2)',
    inputBg: 'rgba(241, 245, 249, 0.8)',
    inputBorder: 'rgba(59, 130, 246, 0.15)',
  },
}

/** 向后兼容：旧的THEME_PRESETS结构 */
export interface ThemeConfig {
  name: string
  nameEn: string
  bgGradient: string
  glassBg: string
  glassBgDark: string
  glassBorder: string
  glassBorderDark: string
  glassShadow: string
  glassBlur: string
  accent: string
  accentLight: string
  accentBg: string
  textPrimary: string
  textSecondary: string
  sidebarBg: string
  headerBg: string
}

export const THEME_PRESETS: Record<string, ThemeConfig> = {
  frost: {
    name: '霜', nameEn: 'Frost',
    bgGradient: 'from-blue-950/90 via-slate-900/90 to-indigo-950/90',
    glassBg: 'rgba(255, 255, 255, 0.08)', glassBgDark: 'rgba(15, 23, 42, 0.6)',
    glassBorder: 'rgba(255, 255, 255, 0.15)', glassBorderDark: 'rgba(148, 163, 184, 0.15)',
    glassShadow: '0 8px 32px rgba(0, 0, 0, 0.25)', glassBlur: '16px',
    accent: '#60a5fa', accentLight: '#93c5fd', accentBg: 'rgba(96, 165, 250, 0.15)',
    textPrimary: '#f1f5f9', textSecondary: '#94a3b8',
    sidebarBg: 'rgba(15, 23, 42, 0.75)', headerBg: 'rgba(15, 23, 42, 0.6)',
  },
  aurora: {
    name: '极光', nameEn: 'Aurora',
    bgGradient: 'from-purple-950/90 via-indigo-900/90 to-teal-950/90',
    glassBg: 'rgba(255, 255, 255, 0.06)', glassBgDark: 'rgba(88, 28, 135, 0.4)',
    glassBorder: 'rgba(255, 255, 255, 0.12)', glassBorderDark: 'rgba(192, 132, 252, 0.2)',
    glassShadow: '0 8px 32px rgba(0, 0, 0, 0.3)', glassBlur: '20px',
    accent: '#c084fc', accentLight: '#d8b4fe', accentBg: 'rgba(192, 132, 252, 0.15)',
    textPrimary: '#f3e8ff', textSecondary: '#a78bfa',
    sidebarBg: 'rgba(88, 28, 135, 0.5)', headerBg: 'rgba(88, 28, 135, 0.35)',
  },
  crystal: {
    name: '水晶', nameEn: 'Crystal',
    bgGradient: 'from-white/95 via-blue-50/90 to-indigo-50/90',
    glassBg: 'rgba(255, 255, 255, 0.7)', glassBgDark: 'rgba(255, 255, 255, 0.15)',
    glassBorder: 'rgba(255, 255, 255, 0.4)', glassBorderDark: 'rgba(255, 255, 255, 0.2)',
    glassShadow: '0 8px 32px rgba(0, 0, 0, 0.08)', glassBlur: '12px',
    accent: '#6366f1', accentLight: '#818cf8', accentBg: 'rgba(99, 102, 241, 0.1)',
    textPrimary: '#1e293b', textSecondary: '#64748b',
    sidebarBg: 'rgba(255, 255, 255, 0.75)', headerBg: 'rgba(255, 255, 255, 0.6)',
  },
  midnight: {
    name: '暗夜', nameEn: 'Midnight',
    bgGradient: 'from-neutral-950 via-slate-950 to-zinc-950',
    glassBg: 'rgba(255, 255, 255, 0.03)', glassBgDark: 'rgba(38, 38, 38, 0.6)',
    glassBorder: 'rgba(255, 255, 255, 0.06)', glassBorderDark: 'rgba(163, 163, 163, 0.12)',
    glassShadow: '0 8px 32px rgba(0, 0, 0, 0.5)', glassBlur: '24px',
    accent: '#a3a3a3', accentLight: '#d4d4d4', accentBg: 'rgba(163, 163, 163, 0.1)',
    textPrimary: '#fafafa', textSecondary: '#a3a3a3',
    sidebarBg: 'rgba(23, 23, 23, 0.8)', headerBg: 'rgba(23, 23, 23, 0.5)',
  },
}

export type ThemeKey = keyof typeof THEME_PRESETS

interface ThemeState {
  /** 当前主题色 */
  accent: AccentKey
  /** 当前模式 */
  mode: ModeKey
  /** 向后兼容：当前主题key */
  current: ThemeKey
  /** 切换主题色 */
  setAccent: (accent: AccentKey) => void
  /** 切换模式 */
  setMode: (mode: ModeKey) => void
  /** 向后兼容：切换主题 */
  setTheme: (key: ThemeKey) => void
  /** 切换暗/亮 */
  toggleMode: () => void
}

export const useThemeStore = create<ThemeState>()(
  persist(
    (set, get) => ({
      accent: 'blue',
      mode: 'dark',
      current: 'frost',

      setAccent: (accent) => set({ accent }),
      setMode: (mode) => set({ mode }),
      setTheme: (key) => {
        const oldToNew: Record<string, AccentKey> = {
          frost: 'blue',
          aurora: 'purple',
          crystal: 'blue',
          midnight: 'purple',
        }
        set({ current: key, accent: oldToNew[key] || 'blue' })
      },
      toggleMode: () => set({ mode: get().mode === 'dark' ? 'light' : 'dark' }),
    }),
    { name: 'theme-storage-v2' }
  )
)