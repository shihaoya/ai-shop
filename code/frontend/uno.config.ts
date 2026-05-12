import { defineConfig, presetUno, presetAttributify } from 'unocss'

const themeColors = {
  primary: 'var(--primary-color)',
  primaryLight: 'var(--primary-light)',
  textPrimary: 'var(--text-primary)',
  textSecondary: 'var(--text-secondary)',
  textInverse: 'var(--text-inverse)',
  glass: 'var(--glass-bg)',
  glassStrong: 'var(--glass-bg-strong)',
  nav: 'var(--nav-bg)',
  pageStart: 'var(--bg-page-start)',
  pageEnd: 'var(--bg-page-end)',
}

export default defineConfig({
  presets: [presetUno(), presetAttributify()],
  shortcuts: {
    // 玻璃基础
    'glass': 'bg-[var(--glass-bg)] backdrop-blur-4 border border-[var(--glass-border)] rounded-2xl shadow-lg',
    'glass-strong': 'bg-[var(--glass-bg-strong)] backdrop-blur-4 border border-[var(--glass-border)] rounded-2xl shadow-lg',
    'glass-hover': 'hover:-translate-y-0.5 hover:shadow-[var(--glass-shadow-lg)]',
    'glass-card': 'glass glass-hover transition-all duration-300 ease-out',

    // 悬浮气泡导航
    'nav-bubble': 'bg-[var(--nav-bg)] backdrop-blur-5 border border-[var(--nav-border)] shadow-[var(--nav-shadow)] rounded-2xl',
    'nav-bubble-hover': 'hover:bg-[var(--nav-hover-bg)] transition-all duration-200',
    'nav-item': 'nav-bubble-hover cursor-pointer rounded-xl px-3 py-2 flex items-center gap-3',
    'nav-item-active': 'bg-[var(--primary-light)]! text-[var(--primary-color)]! rounded-xl',

    // 输入/按钮
    'glass-input': 'bg-[var(--glass-bg)] backdrop-blur-2 border border-[var(--glass-border)] rounded-xl text-[var(--text-primary)]',
    'glass-btn': 'bg-[var(--glass-bg-strong)] backdrop-blur-2 border border-[var(--glass-border)] rounded-xl hover:bg-[var(--nav-hover-bg)] active:scale-[0.98] transition-all duration-200',

    // 渐变与背景
    'primary-gradient': 'bg-gradient-to-br from-[var(--primary-color)] to-[#764ba2]',
    'page-bg': 'min-h-screen',
    'page-bg-light': 'bg-gradient-to-br from-[#e0e5ec] to-[#f5f7fa]',
    'page-bg-dark': 'bg-gradient-to-br from-[#0f0f1a] to-[#1a1a2e]',

    // 文字
    'text-muted': 'text-[var(--text-secondary)]',
    'text-primary': 'text-[var(--text-primary)]',
    'text-accent': 'text-[var(--primary-color)]',
  },
  rules: [
    ['animate-float', { animation: 'float 6s ease-in-out infinite' }],
    ['animate-glow', { animation: 'glow 3s ease-in-out infinite alternate' }],
    ['animate-bubble-rise', { animation: 'bubble-rise 8s ease-in-out infinite' }],
    ['animate-bubble-float', { animation: 'bubble-float 4s ease-in-out infinite' }],
  ],
  preflights: [
    {
      getCSS: () => `
        @keyframes float {
          0%, 100% { transform: translateY(0px); }
          50% { transform: translateY(-10px); }
        }
        @keyframes glow {
          0% { box-shadow: 0 0 20px rgba(102, 126, 234, 0.3); }
          100% { box-shadow: 0 0 40px rgba(118, 75, 162, 0.5); }
        }
        @keyframes bubble-rise {
          0%, 100% { transform: translateY(0) scale(1); opacity: 0.6; }
          50% { transform: translateY(-20px) scale(1.05); opacity: 1; }
        }
        @keyframes bubble-float {
          0%, 100% { transform: translateY(0px) rotate(0deg); }
          33% { transform: translateY(-6px) rotate(1deg); }
          66% { transform: translateY(3px) rotate(-1deg); }
        }
        @keyframes fadeInUp {
          from { opacity: 0; transform: translateY(20px); }
          to { opacity: 1; transform: translateY(0); }
        }
        @keyframes slideInRight {
          from { opacity: 0; transform: translateX(30px); }
          to { opacity: 1; transform: translateX(0); }
        }
        @keyframes scaleIn {
          from { opacity: 0; transform: scale(0.95); }
          to { opacity: 1; transform: scale(1); }
        }
        @keyframes shimmer {
          0% { background-position: -200% 0; }
          100% { background-position: 200% 0; }
        }
        @keyframes neonPulse {
          0%, 100% { box-shadow: 0 0 15px rgba(99, 102, 241, 0.2), 0 0 30px rgba(99, 102, 241, 0.05); }
          50% { box-shadow: 0 0 25px rgba(99, 102, 241, 0.35), 0 0 50px rgba(99, 102, 241, 0.12); }
        }
        @keyframes borderGlow {
          0%, 100% { border-color: rgba(99, 102, 241, 0.15); }
          50% { border-color: rgba(99, 102, 241, 0.40); }
        }
        @keyframes slideInLeft {
          from { opacity: 0; transform: translateX(-20px); }
          to { opacity: 1; transform: translateX(0); }
        }
        @keyframes tooltipIn {
          from { opacity: 0; transform: translateY(-50%) translateX(-4px); }
          to   { opacity: 1; transform: translateY(-50%) translateX(0); }
        }
        *, *::before, *::after {
          transition: background-color 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease, color 0.3s ease;
        }
        body {
          background: var(--bg-page);
          min-height: 100vh;
          color: var(--text-primary);
        }
      `
    }
  ]
})