import { useState, useRef, useEffect } from 'react'
import { Palette, Check, ChevronDown } from 'lucide-react'
import { useThemeStore, THEME_PRESETS, type ThemeKey } from '@/stores/theme'

const THEME_KEYS: ThemeKey[] = ['frost', 'aurora', 'crystal', 'midnight']

const THEME_ICONS: Record<string, string> = {
  frost: '❄️',
  aurora: '🌌',
  crystal: '💎',
  midnight: '🌙',
}

const THEME_COLORS: Record<string, string> = {
  frost: '#60a5fa',
  aurora: '#c084fc',
  crystal: '#6366f1',
  midnight: '#a3a3a3',
}

export default function ThemeSwitcher() {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)
  const { current, setTheme } = useThemeStore()

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  return (
    <div ref={ref} className="relative">
      <button
        type="button"
        onClick={() => setOpen(!open)}
        className="glass-btn flex items-center gap-1.5 px-2.5 py-1.5 text-xs"
        title="切换主题"
      >
        <Palette className="w-3.5 h-3.5" />
        <span className="hidden sm:inline">{THEME_PRESETS[current].name}</span>
        <ChevronDown className={`w-3 h-3 transition-transform ${open ? 'rotate-180' : ''}`} />
      </button>

      {open && (
        <div className="absolute right-0 top-full mt-2 w-48 glass p-1.5 z-50">
          {THEME_KEYS.map((key) => {
            const theme = THEME_PRESETS[key]
            const isActive = current === key
            return (
              <button
                key={key}
                type="button"
                onClick={() => {
                  setTheme(key)
                  setOpen(false)
                }}
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-all ${
                  isActive
                    ? 'bg-white/10 text-white'
                    : 'text-white/70 hover:text-white hover:bg-white/5'
                }`}
              >
                <span
                  className="w-5 h-5 rounded-full flex items-center justify-center text-[10px]"
                  style={{ backgroundColor: THEME_COLORS[key] + '30' }}
                >
                  {THEME_ICONS[key]}
                </span>
                <span className="flex-1 text-left">{theme.name}</span>
                {isActive && <Check className="w-3.5 h-3.5 text-white/80" />}
              </button>
            )
          })}
        </div>
      )}
    </div>
  )
}
