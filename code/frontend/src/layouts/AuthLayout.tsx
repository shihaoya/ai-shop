import { Outlet } from 'react-router-dom'
import ThemeFloatingToggle from '@/components/common/ThemeFloatingToggle'

export default function AuthLayout() {
  return (
    <div
      className="min-h-screen flex flex-col items-center justify-center relative overflow-hidden"
      style={{ background: 'var(--body-bg)', color: 'var(--text-primary)', transition: 'background 0.3s' }}
    >
      {/* ================================================================
          Grid pattern overlay (fades at edges)
          ================================================================ */}
      <div
        className="fixed inset-0 pointer-events-none"
        style={{
          backgroundImage: `
            linear-gradient(var(--accent) 1px, transparent 1px),
            linear-gradient(90deg, var(--accent) 1px, transparent 1px)
          `,
          backgroundSize: '60px 60px',
          backgroundPosition: 'center center',
          maskImage: 'radial-gradient(ellipse 80% 80% at 50% 50%, black 20%, transparent 100%)',
          WebkitMaskImage: 'radial-gradient(ellipse 80% 80% at 50% 50%, black 20%, transparent 100%)',
          opacity: 0.06,
          transition: 'background 0.3s, opacity 0.3s',
        }}
      />

      {/* ================================================================
          Floating orbs
          ================================================================ */}
      <div className="fixed inset-0 pointer-events-none overflow-hidden">
        {/* Orb top-right */}
        <div
          className="orb orb-1"
          style={{
            background: `radial-gradient(circle, var(--accent-glow), transparent 70%)`,
            opacity: 0.6,
          }}
        />
        {/* Orb bottom-left */}
        <div
          className="orb orb-2"
          style={{
            background: 'radial-gradient(circle, rgba(139, 92, 246, 0.4), transparent 70%)',
            opacity: 0.5,
          }}
        />
        {/* Orb center-left */}
        <div
          className="orb orb-3"
          style={{
            background: `radial-gradient(circle, var(--accent) 0%, transparent 70%)`,
            opacity: 0.3,
          }}
        />
      </div>

      {/* ================================================================
          Content
          ================================================================ */}
      <Outlet />

      {/* ================================================================
          Theme Toggle
          ================================================================ */}
      <ThemeFloatingToggle />
    </div>
  )
}