import { useState, useRef, useEffect } from 'react'
import { Outlet, Link, useLocation } from 'react-router-dom'
import {
  Store,
  Users,
  KeyRound,
  ChevronDown,
  LogOut,
  Moon,
  PanelLeftClose,
  PanelLeft,
} from 'lucide-react'
import { useAuthStore } from '@/stores'

interface AdminLayoutProps {
  children?: React.ReactNode
}

export default function AdminLayout({ children }: AdminLayoutProps) {
  const location = useLocation()
  const { userInfo, logout } = useAuthStore()
  const [collapsed, setCollapsed] = useState(false)
  const [dropdownOpen, setDropdownOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)

  const navItems = [
    { path: '/admin/shops', label: '店铺管理', icon: Store },
    { path: '/admin/users', label: '用户管理', icon: Users },
    { path: '/admin/invite-code', label: '邀请码', icon: KeyRound },
  ]

  const isActive = (path: string) => location.pathname.startsWith(path)

  // 点击外部关闭下拉
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setDropdownOpen(false)
      }
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const sidebarWidth = collapsed ? 'w-16' : 'w-64'
  const displayName = userInfo?.nickname || '管理员'

  return (
    <div className="flex h-screen bg-neutral-950 text-neutral-100">
      {/* Sidebar */}
      <aside
        className={`${sidebarWidth} bg-neutral-900 border-r border-neutral-800 flex flex-col transition-all duration-300`}
      >
        {/* Logo */}
        <div className="h-16 flex items-center gap-3 px-4 border-b border-neutral-800 overflow-hidden">
          <div className="w-8 h-8 min-w-8 rounded-lg bg-accent flex items-center justify-center">
            <Moon className="w-5 h-5 text-white" />
          </div>
          <span
            className={`font-semibold text-nowrap transition-opacity duration-300 ${
              collapsed ? 'opacity-0 w-0' : 'opacity-100'
            }`}
          >
            积分商城管理后台
          </span>
        </div>

        {/* Navigation */}
        <nav className="flex-1 py-4 px-2 space-y-1 overflow-y-auto">
          {navItems.map(({ path, label, icon: Icon }) => (
            <Link
              key={path}
              to={path}
              title={collapsed ? label : undefined}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors ${
                isActive(path)
                  ? 'bg-accent text-white'
                  : 'text-neutral-400 hover:text-neutral-100 hover:bg-neutral-800'
              }`}
            >
              <Icon className="w-5 h-5 min-w-5" />
              <span
                className={`text-nowrap transition-opacity duration-300 ${
                  collapsed ? 'opacity-0 w-0 overflow-hidden' : 'opacity-100'
                }`}
              >
                {label}
              </span>
            </Link>
          ))}
        </nav>

        {/* Bottom: Collapse Toggle + User Info */}
        <div className="border-t border-neutral-800">
          {/* Collapse Toggle */}
          <button
            type="button"
            onClick={() => setCollapsed(!collapsed)}
            className="w-full flex items-center justify-center h-10 text-neutral-500 hover:text-neutral-100 hover:bg-neutral-800 transition-colors"
            title={collapsed ? '展开侧栏' : '收起侧栏'}
          >
            {collapsed ? (
              <PanelLeft className="w-5 h-5" />
            ) : (
              <PanelLeftClose className="w-5 h-5" />
            )}
          </button>

          {/* User Info (only show when expanded) */}
          <div
            className={`relative ${collapsed ? 'hidden' : 'block'}`}
            ref={dropdownRef}
          >
            <button
              type="button"
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="w-full flex items-center gap-3 px-4 py-3 hover:bg-neutral-800 transition-colors"
            >
              <div className="w-8 h-8 min-w-8 rounded-full bg-accent flex items-center justify-center text-sm font-medium">
                {displayName.charAt(0).toUpperCase()}
              </div>
              <div className="flex-1 min-w-0 text-left">
                <div className="text-sm truncate">{displayName}</div>
                <div className="text-xs text-neutral-500">管理员</div>
              </div>
              <ChevronDown
                className={`w-4 h-4 text-neutral-500 transition-transform ${
                  dropdownOpen ? 'rotate-180' : ''
                }`}
              />
            </button>

            {dropdownOpen && (
              <div className="absolute bottom-full left-0 right-0 mb-1 mx-2 py-1 bg-neutral-800 border border-neutral-700 rounded-lg shadow-xl z-50">
                <button
                  type="button"
                  onClick={() => {
                    logout()
                    setDropdownOpen(false)
                  }}
                  className="w-full flex items-center gap-2 px-4 py-2 text-sm text-neutral-400 hover:text-neutral-100 hover:bg-neutral-700 transition-colors"
                >
                  <LogOut className="w-4 h-4" />
                  <span>退出登录</span>
                </button>
              </div>
            )}
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-w-0 transition-all duration-300">
        {/* Top Bar — 只剩下页面标题区域 */}
        <header className="h-16 flex items-center px-6 border-b border-neutral-800">
          <div className="text-sm text-neutral-500">管理后台</div>
        </header>

        {/* Page Content */}
        <main className="flex-1 p-6 overflow-auto">
          <Outlet />
          {children}
        </main>
      </div>
    </div>
  )
}