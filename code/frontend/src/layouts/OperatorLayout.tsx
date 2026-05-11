import { useState, useRef, useEffect } from 'react'
import { Outlet, Link, useLocation } from 'react-router-dom'
import {
  Bell,
  ChevronLeft,
  ChevronRight,
  CircleDollarSign,
  KeyRound,
  LayoutDashboard,
  LogOut,
  Package,
  ShoppingCart,
  Store,
  Tags,
  Users,
} from 'lucide-react'
import { useAuthStore } from '@/stores'
import ThemeFloatingToggle from '@/components/common/ThemeFloatingToggle'

interface OperatorLayoutProps {
  children?: React.ReactNode
}

export default function OperatorLayout({ children }: OperatorLayoutProps) {
  const location = useLocation()
  const { userInfo, logout } = useAuthStore()
  const [collapsed, setCollapsed] = useState(false)
  const [dropdownOpen, setDropdownOpen] = useState(false)
  const [hoveredItem, setHoveredItem] = useState<string | null>(null)
  const dropdownRef = useRef<HTMLDivElement>(null)

  const navSections = [
    {
      title: '概览',
      items: [
        { path: '/operator/dashboard', label: '工作台', icon: LayoutDashboard },
      ],
    },
    {
      title: '管理',
      items: [
        { path: '/operator/shop', label: '我的店铺', icon: Store },
        { path: '/operator/categories', label: '分类管理', icon: Tags },
        { path: '/operator/products', label: '商品管理', icon: Package },
        { path: '/operator/orders', label: '订单管理', icon: ShoppingCart },
        { path: '/operator/users', label: '用户管理', icon: Users },
        { path: '/operator/invite-code', label: '邀请码', icon: KeyRound },
        { path: '/operator/messages', label: '我的消息', icon: Bell },
      ],
    },
  ]

  const isActive = (path: string) => location.pathname.startsWith(path)
  const unreadCount = 3 // TODO: 从消息服务获取

  // Initialize sidebar width CSS variable
  useEffect(() => {
    document.documentElement.style.setProperty('--sidebar-width', collapsed ? '80px' : '260px')
  }, [collapsed])

  // Click outside to close dropdown
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setDropdownOpen(false)
      }
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const displayName = userInfo?.nickname || userInfo?.username || '店铺用户'
  const userRole = '店铺用户'
  const sidebarWidth = collapsed ? '80px' : '260px'

  return (
    <div style={{ position: 'relative', minHeight: '100vh', background: 'var(--body-bg)' }}>
      {/* Background Orbs */}
      <div
        style={{
          position: 'fixed',
          width: '400px',
          height: '400px',
          borderRadius: '50%',
          background: 'var(--accent)',
          filter: 'blur(80px)',
          top: '-100px',
          right: '-100px',
          opacity: 0.3,
          pointerEvents: 'none',
          animation: 'float 8s ease-in-out infinite',
        }}
      />
      <div
        style={{
          position: 'fixed',
          width: '300px',
          height: '300px',
          borderRadius: '50%',
          background: 'var(--accent)',
          filter: 'blur(80px)',
          bottom: '-50px',
          left: '-50px',
          opacity: 0.25,
          pointerEvents: 'none',
          animation: 'float 8s ease-in-out infinite reverse',
        }}
      />
      <div
        style={{
          position: 'fixed',
          width: '200px',
          height: '200px',
          borderRadius: '50%',
          background: 'var(--accent)',
          filter: 'blur(80px)',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          opacity: 0.15,
          pointerEvents: 'none',
          animation: 'float 8s ease-in-out infinite',
        }}
      />

      {/* Floating Glass Sidebar */}
      <aside
        style={{
          position: 'fixed',
          top: '24px',
          left: '24px',
          bottom: '24px',
          width: sidebarWidth,
          background: 'var(--card-bg)',
          backdropFilter: 'blur(20px)',
          WebkitBackdropFilter: 'blur(20px)',
          border: '1px solid var(--card-border)',
          borderRadius: '20px',
          boxShadow: '0 4px 20px rgba(0, 0, 0, 0.2)',
          transition: 'width 0.3s ease',
          display: 'flex',
          flexDirection: 'column',
          zIndex: 40,
        }}
      >
        {/* Sidebar Header */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: collapsed ? '0' : '12px',
            padding: '20px 16px',
            borderBottom: '1px solid var(--card-border)',
          }}
        >
          <div
            style={{
              width: '36px',
              height: '36px',
              borderRadius: '12px',
              background: 'var(--accent)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            <CircleDollarSign className="w-5 h-5" style={{ color: 'white' }} />
          </div>
          <span
            style={{
              fontWeight: 600,
              fontSize: '16px',
              color: 'var(--text-primary)',
              whiteSpace: 'nowrap',
              opacity: collapsed ? 0 : 1,
              transition: 'opacity 0.3s ease',
            }}
          >
            商户后台
          </span>
        </div>

        {/* Navigation */}
        <nav
          style={{
            flex: 1,
            padding: '16px 12px',
          }}
        >
          {navSections.map((section) => (
            <div key={section.title} style={{ marginBottom: '20px' }}>
              <div
                style={{
                  fontSize: '11px',
                  fontWeight: 600,
                  textTransform: 'uppercase',
                  color: 'var(--text-muted)',
                  padding: '0 12px',
                  marginBottom: '8px',
                  letterSpacing: '0.05em',
                  opacity: collapsed ? 0 : 1,
                  transition: 'opacity 0.3s ease',
                  whiteSpace: 'nowrap',
                }}
              >
                {section.title}
              </div>
              {section.items.map(({ path, label, icon: Icon }) => (
                <div key={path} style={{ position: 'relative' }}>
                  <Link
                    to={path}
                    onMouseEnter={() => {
                      setHoveredItem(path)
                    }}
                    onMouseLeave={() => setHoveredItem(null)}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: collapsed ? '0' : '12px',
                      padding: collapsed ? '10px' : '10px 12px',
                      borderRadius: '10px',
                      color: isActive(path) ? 'var(--accent)' : 'var(--text-secondary)',
                      background: isActive(path) ? 'var(--accent-light)' : 'transparent',
                      textDecoration: 'none',
                      transition: 'all 0.2s ease',
                      justifyContent: collapsed ? 'center' : 'flex-start',
                      marginBottom: '4px',
                    }}
                    onClick={() => setDropdownOpen(false)}
                    onMouseOver={(e) => {
                      if (!isActive(path)) {
                        e.currentTarget.style.background = 'var(--accent-light)'
                        e.currentTarget.style.color = 'var(--accent)'
                      }
                    }}
                    onMouseOut={(e) => {
                      if (!isActive(path)) {
                        e.currentTarget.style.background = 'transparent'
                        e.currentTarget.style.color = 'var(--text-secondary)'
                      }
                    }}
                  >
                    <Icon className="w-[18px] h-[18px]" style={{ flexShrink: 0 }} />
                    <span
                      style={{
                        fontSize: '14px',
                        fontWeight: 500,
                        whiteSpace: 'nowrap',
                        opacity: collapsed ? 0 : 1,
                        transition: 'opacity 0.3s ease',
                        visibility: collapsed ? 'hidden' : 'visible',
                        width: collapsed ? 0 : 'auto',
                        overflow: 'hidden',
                      }}
                    >
                      {label}
                    </span>
                    {/* Unread badge - visible when not collapsed */}
                    {!collapsed && path === '/operator/messages' && unreadCount > 0 && (
                      <span
                        style={{
                          marginLeft: 'auto',
                          width: '18px',
                          height: '18px',
                          borderRadius: '50%',
                          background: '#ef4444',
                          color: 'white',
                          fontSize: '11px',
                          fontWeight: 600,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          flexShrink: 0,
                        }}
                      >
                        {unreadCount > 99 ? '99+' : unreadCount}
                      </span>
                    )}
                  </Link>
                  {/* Tooltip - speech bubble with left-arrow, inside sidebar DOM */}
                  {collapsed && hoveredItem === path && (
                    <div
                      style={{
                        position: 'absolute',
                        left: 'calc(100% + 8px)',
                        top: '50%',
                        transform: 'translateY(-50%)',
                        background: 'var(--card-bg)',
                        backdropFilter: 'blur(20px)',
                        border: '1px solid var(--card-border)',
                        borderRadius: '10px',
                        padding: '6px 12px',
                        boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
                        zIndex: 40,
                        pointerEvents: 'none',
                        whiteSpace: 'nowrap',
                      }}
                    >
                      {/* CSS triangle arrow pointing left */}
                      <span style={{
                        position: 'absolute',
                        left: '-6px',
                        top: '50%',
                        transform: 'translateY(-50%)',
                        width: 0,
                        height: 0,
                        borderTop: '5px solid transparent',
                        borderBottom: '5px solid transparent',
                        borderRight: '6px solid var(--card-border)',
                      }} />
                      {/* Arrow inner (covers border) */}
                      <span style={{
                        position: 'absolute',
                        left: '-5px',
                        top: '50%',
                        transform: 'translateY(-50%)',
                        width: 0,
                        height: 0,
                        borderTop: '4px solid transparent',
                        borderBottom: '4px solid transparent',
                        borderRight: '5px solid var(--card-bg)',
                      }} />
                      <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text-primary)' }}>
                        {label}
                      </span>
                      {/* Red dot for messages in tooltip when collapsed */}
                      {path === '/operator/messages' && unreadCount > 0 && (
                        <span
                          style={{
                            display: 'inline-block',
                            marginLeft: '8px',
                            width: '8px',
                            height: '8px',
                            borderRadius: '50%',
                            background: '#ef4444',
                          }}
                        />
                      )}
                    </div>
                  )}
                </div>
              ))}
            </div>
          ))}
        </nav>

        {/* Sidebar Footer */}
        <div
          style={{
            borderTop: '1px solid var(--card-border)',
            padding: '12px',
            display: 'flex',
            flexDirection: 'column',
            gap: '8px',
          }}
        >
          {/* User Section */}
          <div ref={dropdownRef} style={{ position: 'relative' }}>
            {dropdownOpen && (
              <div
                style={{
                  position: 'absolute',
                  bottom: 'calc(100% + 8px)',
                  left: collapsed ? '50%' : '0',
                  transform: collapsed ? 'translateX(-50%)' : 'none',
                  background: 'var(--card-bg)',
                  backdropFilter: 'blur(20px)',
                  border: '1px solid var(--card-border)',
                  borderRadius: '12px',
                  padding: '6px',
                  minWidth: '160px',
                  boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
                  zIndex: 40,
                }}
              >
                {/* Top arrow pointing down */}
                <span style={{
                  position: 'absolute',
                  bottom: '-6px',
                  left: collapsed ? '50%' : '12px',
                  transform: collapsed ? 'translateX(-50%)' : 'none',
                  width: 0,
                  height: 0,
                  borderLeft: '5px solid transparent',
                  borderRight: '5px solid transparent',
                  borderTop: '6px solid var(--card-border)',
                }} />
                <span style={{
                  position: 'absolute',
                  bottom: '-5px',
                  left: collapsed ? '50%' : '12px',
                  transform: collapsed ? 'translateX(-50%)' : 'none',
                  width: 0,
                  height: 0,
                  borderLeft: '4px solid transparent',
                  borderRight: '4px solid transparent',
                  borderTop: '5px solid var(--card-bg)',
                }} />
                <button
                  type="button"
                  onClick={() => { logout(); setDropdownOpen(false) }}
                  onMouseOver={(e) => { e.currentTarget.style.background = 'var(--accent-light)'; e.currentTarget.style.color = 'var(--accent)' }}
                  onMouseOut={(e) => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = 'var(--text-secondary)' }}
                  onFocus={(e) => { e.currentTarget.style.background = 'var(--accent-light)'; e.currentTarget.style.color = 'var(--accent)' }}
                  onBlur={(e) => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = 'var(--text-secondary)' }}
                  style={{
                    display: 'flex', alignItems: 'center', gap: '8px',
                    width: '100%', padding: '10px 12px', borderRadius: '8px',
                    border: 'none', background: 'transparent',
                    color: 'var(--text-secondary)', fontSize: '14px',
                    cursor: 'pointer', transition: 'all 0.2s ease',
                  }}
                >
                  <LogOut className="w-4 h-4" />
                  退出登录
                </button>
              </div>
            )}
            <button
              type="button"
              onClick={() => setDropdownOpen(!dropdownOpen)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: collapsed ? '0' : '10px',
                width: '100%',
                padding: collapsed ? '8px' : '10px 12px',
                borderRadius: '10px',
                border: '1px solid var(--card-border)',
                background: 'var(--card-bg)',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                justifyContent: collapsed ? 'center' : 'flex-start',
              }}
            >
              <div
                style={{
                  width: '32px', height: '32px',
                  borderRadius: '10px',
                  background: 'var(--accent)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  flexShrink: 0, fontSize: '14px', fontWeight: 600, color: 'white',
                }}
              >
                {displayName.charAt(0)}
              </div>
              <div
                style={{
                  display: 'flex', flexDirection: 'column', alignItems: 'flex-start',
                  opacity: collapsed ? 0 : 1, transition: 'opacity 0.3s ease',
                  width: collapsed ? 0 : 'auto',
                }}
              >
                <span style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text-primary)', whiteSpace: 'nowrap' }}>
                  {displayName}
                </span>
                <span style={{ fontSize: '12px', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                  {userRole}
                </span>
              </div>
            </button>
          </div>

          {/* Toggle Button - Full width row */}
          <button
            type="button"
            onClick={() => setCollapsed(!collapsed)}
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: collapsed ? 'center' : 'flex-start',
              gap: '8px',
              width: '100%',
              padding: collapsed ? '8px' : '8px 12px',
              borderRadius: '10px',
              border: '1px solid var(--card-border)',
              background: 'var(--card-bg)',
              color: 'var(--text-muted)',
              cursor: 'pointer',
              transition: 'all 0.2s',
            }}
            onMouseOver={(e) => {
              e.currentTarget.style.borderColor = 'var(--accent)'
              e.currentTarget.style.color = 'var(--accent)'
              e.currentTarget.style.background = 'var(--accent-light)'
            }}
            onMouseOut={(e) => {
              e.currentTarget.style.borderColor = 'var(--card-border)'
              e.currentTarget.style.color = 'var(--text-muted)'
              e.currentTarget.style.background = 'var(--card-bg)'
            }}
          >
            {collapsed ? (
              <ChevronRight className="w-4 h-4" style={{ flexShrink: 0 }} />
            ) : (
              <>
                <ChevronLeft className="w-4 h-4" />
                <span style={{ fontSize: '13px', whiteSpace: 'nowrap' }}>收起侧栏</span>
              </>
            )}
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main
        style={{
          marginLeft: `calc(var(--sidebar-width) + 24px)`,
          padding: '24px',
          transition: 'margin-left 0.3s ease',
          minHeight: '100vh',
          overflowY: 'auto',
        }}
      >
        <Outlet />
        {children}
      </main>

      {/* Theme Floating Toggle */}
      <ThemeFloatingToggle />

      {/* Global Styles for Animations */}
      <style>
        {`
          @keyframes float {
            0%, 100% { transform: translate(0, 0); }
            50% { transform: translate(20px, 20px); }
          }
        `}
      </style>
    </div>
  )
}