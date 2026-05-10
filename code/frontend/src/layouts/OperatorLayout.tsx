import { useState, useRef, useEffect } from 'react'
import { Outlet, Link, useLocation } from 'react-router-dom'
import {
  LayoutDashboard,
  Store,
  Tags,
  Package,
  ShoppingCart,
  Users,
  KeyRound,
  Bell,
  ChevronDown,
  LogOut,
  CircleDollarSign,
  PanelLeftClose,
  PanelLeft,
} from 'lucide-react'
import { useAuthStore } from '@/stores'

interface OperatorLayoutProps {
  children?: React.ReactNode
}

export default function OperatorLayout({ children }: OperatorLayoutProps) {
  const location = useLocation()
  const { userInfo, logout } = useAuthStore()
  const [collapsed, setCollapsed] = useState(false)
  const [dropdownOpen, setDropdownOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)

  const navItems = [
    { path: '/operator/dashboard', label: '工作台', icon: LayoutDashboard },
    { path: '/operator/shop', label: '我的店铺', icon: Store },
    { path: '/operator/categories', label: '分类管理', icon: Tags },
    { path: '/operator/products', label: '商品管理', icon: Package },
    { path: '/operator/orders', label: '订单管理', icon: ShoppingCart },
    { path: '/operator/users', label: '用户管理', icon: Users },
    { path: '/operator/invite-code', label: '邀请码', icon: KeyRound },
    { path: '/operator/messages', label: '我的消息', icon: Bell },
  ]

  const isActive = (path: string) => location.pathname.startsWith(path)
  const unreadCount = 3 // TODO: 从消息服务获取

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

  const sidebarWidth = collapsed ? 'w-16' : 'w-56'
  const displayName = userInfo?.nickname || userInfo?.username || '店铺用户'

  return (
    <div className="flex h-screen bg-neutral-100">
      {/* Sidebar */}
      <aside
        className={`${sidebarWidth} bg-white border-r border-neutral-200 flex flex-col transition-all duration-300`}
      >
        {/* Logo / Shop Info */}
        <div className="h-16 flex items-center gap-3 px-4 border-b border-neutral-200 overflow-hidden">
          <div className="w-8 h-8 min-w-8 rounded-lg bg-accent flex items-center justify-center">
            <CircleDollarSign className="w-5 h-5 text-white" />
          </div>
          <div
            className={`flex-1 min-w-0 transition-opacity duration-300 ${
              collapsed ? 'opacity-0 w-0' : 'opacity-100'
            }`}
          >
            <div className="text-sm font-medium text-neutral-900 truncate">
              {userInfo?.nickname || '我的店铺'}
            </div>
            <div className="text-xs text-neutral-500">营业中</div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 py-3 px-2 space-y-0.5 overflow-y-auto">
          {navItems.map(({ path, label, icon: Icon }) => (
            <Link
              key={path}
              to={path}
              title={collapsed ? label : undefined}
              className={`flex items-center gap-2.5 px-3 py-2 rounded-md text-sm transition-colors ${
                isActive(path)
                  ? 'bg-accent text-white'
                  : 'text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100'
              }`}
            >
              <Icon className="w-4 h-4 min-w-4" />
              <span
                className={`text-nowrap transition-opacity duration-300 ${
                  collapsed ? 'opacity-0 w-0 overflow-hidden' : 'opacity-100'
                }`}
              >
                {label}
              </span>
              {!collapsed && path === '/operator/messages' && unreadCount > 0 && (
                <span className="ml-auto w-5 h-5 rounded-full bg-red-500 text-white text-xs flex items-center justify-center flex-shrink-0">
                  {unreadCount}
                </span>
              )}
            </Link>
          ))}
        </nav>

        {/* Bottom: Collapse Toggle + User Info */}
        <div className="border-t border-neutral-200">
          {/* Collapse Toggle */}
          <button
            type="button"
            onClick={() => setCollapsed(!collapsed)}
            className="w-full flex items-center justify-center h-10 text-neutral-400 hover:text-neutral-600 hover:bg-neutral-100 transition-colors"
            title={collapsed ? '展开侧栏' : '收起侧栏'}
          >
            {collapsed ? (
              <PanelLeft className="w-5 h-5" />
            ) : (
              <PanelLeftClose className="w-5 h-5" />
            )}
          </button>

          {/* User Info */}
          <div
            className={`relative ${collapsed ? 'hidden' : 'block'}`}
            ref={dropdownRef}
          >
            <button
              type="button"
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="w-full flex items-center gap-3 px-4 py-3 hover:bg-neutral-50 transition-colors"
            >
              <div className="w-8 h-8 min-w-8 rounded-full bg-accent flex items-center justify-center text-white text-sm font-medium">
                {displayName.charAt(0).toUpperCase()}
              </div>
              <div className="flex-1 min-w-0 text-left">
                <div className="text-sm font-medium text-neutral-900 truncate">
                  {displayName}
                </div>
                <div className="text-xs text-neutral-500">店铺用户</div>
              </div>
              <ChevronDown
                className={`w-4 h-4 text-neutral-400 transition-transform ${
                  dropdownOpen ? 'rotate-180' : ''
                }`}
              />
            </button>

            {dropdownOpen && (
              <div className="absolute bottom-full left-0 right-0 mb-1 mx-2 py-1 bg-white border border-neutral-200 rounded-lg shadow-xl z-50">
                <button
                  type="button"
                  onClick={() => {
                    logout()
                    setDropdownOpen(false)
                  }}
                  className="w-full flex items-center gap-2 px-4 py-2 text-sm text-neutral-600 hover:text-neutral-900 hover:bg-neutral-50 transition-colors"
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
        {/* Top Bar — 简约头部 */}
        <header className="h-14 flex items-center justify-between px-6 bg-white border-b border-neutral-200">
          <div className="text-sm text-neutral-500">
            {collapsed ? '' : '商户后台'}
          </div>

          {/* 右侧保留消息铃铛 */}
          <div className="flex items-center gap-4">
            <Link
              to="/operator/messages"
              className="relative p-2 rounded-full hover:bg-neutral-100 transition-colors"
            >
              <Bell className="w-5 h-5 text-neutral-600" />
              {unreadCount > 0 && (
                <span className="absolute -top-0.5 -right-0.5 w-5 h-5 rounded-full bg-red-500 text-white text-xs flex items-center justify-center">
                  {unreadCount}
                </span>
              )}
            </Link>
          </div>
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