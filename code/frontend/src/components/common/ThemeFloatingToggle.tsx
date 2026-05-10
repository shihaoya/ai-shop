import { useEffect } from 'react'
import { Moon, Sun } from 'lucide-react'
import { useThemeStore, ACCENT_COLORS, type AccentKey } from '@/stores/theme'

const ACCENT_KEYS: AccentKey[] = ['blue', 'purple', 'green', 'rose', 'amber']

export default function ThemeFloatingToggle() {
  const { accent, mode, setAccent, setMode } = useThemeStore()

  // Sync store to DOM on mount and changes
  useEffect(() => {
    document.body.setAttribute('data-theme', mode)
    document.body.setAttribute('data-accent', accent)
  }, [mode, accent])

  const handleModeToggle = () => {
    const newMode = mode === 'dark' ? 'light' : 'dark'
    setMode(newMode)
    document.body.setAttribute('data-theme', newMode)
  }

  const handleAccentChange = (key: AccentKey) => {
    setAccent(key)
    document.body.setAttribute('data-accent', key)
  }

  return (
    <div
      className="fixed bottom-6 right-6 z-50 flex items-center gap-1 px-3 py-2 rounded-full"
      style={{
        background: 'var(--card-bg)',
        backdropFilter: 'blur(20px)',
        WebkitBackdropFilter: 'blur(20px)',
        border: '1px solid var(--card-border)',
        boxShadow: '0 8px 32px rgba(0, 0, 0, 0.2)',
        transition: 'background 0.3s, border-color 0.3s',
      }}
    >
      {/* Dark/Light Toggle */}
      <button
        type="button"
        onClick={handleModeToggle}
        className="flex items-center gap-1.5 px-2 py-1.5 rounded-full transition-all"
        style={{
          background: mode === 'dark' ? 'rgba(59, 130, 246, 0.15)' : 'rgba(59, 130, 246, 0.1)',
        }}
        title={mode === 'dark' ? '切换到亮色' : '切换到暗色'}
      >
        {mode === 'dark' ? (
          <Moon className="w-4 h-4" style={{ color: 'var(--accent)' }} />
        ) : (
          <Sun className="w-4 h-4" style={{ color: 'var(--accent)' }} />
        )}
      </button>

      {/* Divider */}
      <div
        className="w-px h-5 rounded-full mx-1"
        style={{ background: mode === 'dark' ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)' }}
      />

      {/* Accent Color Swatches */}
      <div className="flex items-center gap-1.5">
        {ACCENT_KEYS.map((key) => (
          <button
            key={key}
            type="button"
            onClick={() => handleAccentChange(key)}
            className="w-5 h-5 rounded-full transition-all hover:scale-110"
            style={{
              background: ACCENT_COLORS[key].primary,
              boxShadow: accent === key ? `0 0 0 2px ${mode === 'dark' ? '#0a0f1a' : '#e8f0fe'}, 0 0 8px ${ACCENT_COLORS[key].glow}` : 'none',
              transform: accent === key ? 'scale(1.15)' : 'scale(1)',
            }}
            title={ACCENT_COLORS[key].name}
          />
        ))}
      </div>
    </div>
  )
}