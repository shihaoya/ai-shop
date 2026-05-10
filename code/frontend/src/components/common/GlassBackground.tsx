import { useThemeStore, THEME_PRESETS } from '@/stores/theme'

const GRADIENTS: Record<string, string> = {
  frost: 'radial-gradient(ellipse 80% 60% at 20% 20%, rgba(96, 165, 250, 0.15) 0%, transparent 60%), radial-gradient(ellipse 60% 50% at 80% 80%, rgba(99, 102, 241, 0.1) 0%, transparent 50%)',
  aurora: 'radial-gradient(ellipse 80% 60% at 15% 25%, rgba(192, 132, 252, 0.2) 0%, transparent 60%), radial-gradient(ellipse 60% 50% at 80% 70%, rgba(45, 212, 191, 0.1) 0%, transparent 50%), radial-gradient(ellipse 70% 40% at 50% 50%, rgba(99, 102, 241, 0.08) 0%, transparent 50%)',
  crystal: 'radial-gradient(ellipse 80% 60% at 20% 20%, rgba(99, 102, 241, 0.08) 0%, transparent 60%), radial-gradient(ellipse 60% 50% at 80% 80%, rgba(147, 197, 253, 0.06) 0%, transparent 50%)',
  midnight: 'radial-gradient(ellipse 70% 50% at 30% 20%, rgba(163, 163, 163, 0.05) 0%, transparent 60%), radial-gradient(ellipse 50% 40% at 70% 80%, rgba(115, 115, 115, 0.03) 0%, transparent 50%)',
}

export default function GlassBackground() {
  const current = useThemeStore((s) => s.current)
  const theme = THEME_PRESETS[current]

  return (
    <div
      className="fixed inset-0 -z-10"
      style={{
        background: `linear-gradient(135deg, ${getGradientColors(theme.bgGradient)})`,
        transition: 'background 0.8s ease',
      }}
    >
      <div
        className="absolute inset-0"
        style={{
          background: GRADIENTS[current],
          animation: 'bgShift 20s ease-in-out infinite alternate',
        }}
      />
      {/* Noise texture overlay */}
      <div
        className="absolute inset-0 opacity-[0.03]"
        style={{
          backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E")`,
          backgroundRepeat: 'repeat',
          backgroundSize: '256px 256px',
        }}
      />
      <style>{`@keyframes bgShift { 0% { transform: translate(0,0) scale(1); } 50% { transform: translate(2%,1%) scale(1.05); } 100% { transform: translate(-1%,-1%) scale(1.02); } }`}</style>
    </div>
  )
}

function getGradientColors(bgGradient: string): string {
  // Parse Tailwind gradient classes into actual colors
  const map: Record<string, string> = {
    'blue-950': '#172554',
    'slate-900': '#0f172a',
    'indigo-950': '#1e1b4b',
    'purple-950': '#3b0764',
    'indigo-900': '#312e81',
    'teal-950': '#042f2e',
    'white': '#ffffff',
    'blue-50': '#eff6ff',
    'indigo-50': '#eef2ff',
    'neutral-950': '#0a0a0a',
    'slate-950': '#020617',
    'zinc-950': '#09090b',
  }

  const parts = bgGradient.split(' ')
  const colors = parts
    .filter((p) => p.includes('-'))
    .map((p) => p.replace(/,/g, '').replace('\/', '/'))
    .map((p) => {
      const [name] = p.split('/')
      return map[name] || name
    })

  if (colors.length >= 2) {
    const deg = 135
    return `${deg}deg, ${colors.slice(0, 3).join(', ')}`
  }
  return '135deg, #0f172a, #1e1b4b'
}
