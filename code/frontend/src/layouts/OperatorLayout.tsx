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
  CircleDollarSign
} from 'lucide-react'
import { useAuthStore } from '@/stores'

interface OperatorLayoutProps {
  children?: React.ReactNode
}

export default function OperatorLayout({ children }: OperatorLayoutProps) {
  const location = useLocation()
  const { userInfo, logout } = useAuthStore()

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

  return (
    <div className="flex h-screen bg-neutral-100">
      {/* Sidebar */}
      <aside className="w-56 bg-white border-r border-neutral-200 flex flex-col">
        {/* Shop Info */}
        <div className="h-16 flex items-center gap-3 px-4 border-b border-neutral-200">
          <div className="w-8 h-8 rounded-lg bg-accent flex items-center justify-center">
            <CircleDollarSign className="w-5 h-5 text-white" />
          </div>
          <div className="flex-1 min-w-0">
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
              className={`flex items-center gap-2.5 px-3 py-2 rounded-md text-sm transition-colors ${
                isActive(path)
                  ? 'bg-accent text-white'
                  : 'text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100'
              }`}
            >
              <Icon className="w-4 h-4" />
              <span>{label}</span>
              {path === '/operator/messages' && unreadCount > 0 && (
                <span className="ml-auto w-5 h-5 rounded-full bg-red-500 text-white text-xs flex items-center justify-center">
                  {unreadCount}
                </span>
              )}
            </Link>
          ))}
        </nav>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        {/* Top Bar */}
        <header className="h-14 flex items-center justify-between px-6 bg-white border-b border-neutral-200">
          <div className="text-sm text-neutral-500">商户后台</div>

          <div className="flex items-center gap-4">
            {/* Message Notification */}
            <Link
              to="/operator/messages"
              type="button"
              className="relative p-2 rounded-full hover:bg-neutral-100 transition-colors"
            >
              <Bell className="w-5 h-5 text-neutral-600" />
              {unreadCount > 0 && (
                <span className="absolute -top-0.5 -right-0.5 w-5 h-5 rounded-full bg-red-500 text-white text-xs flex items-center justify-center">
                  {unreadCount}
                </span>
              )}
            </Link>

            {/* User Dropdown */}
            <div className="relative group">
              <button type="button" className="flex items-center gap-2 px-2 py-1.5 rounded-lg hover:bg-neutral-100 transition-colors">
                <div className="w-7 h-7 rounded-full bg-accent flex items-center justify-center text-white text-sm font-medium">
                  {userInfo?.username?.charAt(0).toUpperCase() || 'O'}
                </div>
                <span className="text-sm text-neutral-700">{userInfo?.nickname}</span>
                <ChevronDown className="w-4 h-4 text-neutral-400" />
              </button>

              {/* Dropdown Menu */}
              <div className="absolute right-0 top-full mt-1 w-44 py-1 bg-white border border-neutral-200 rounded-lg shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all z-50">
                <button
                  type="button"
                  onClick={logout}
                  className="w-full flex items-center gap-2 px-4 py-2 text-sm text-neutral-600 hover:text-neutral-900 hover:bg-neutral-50 transition-colors"
                >
                  <LogOut className="w-4 h-4" />
                  <span>退出登录</span>
                </button>
              </div>
            </div>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 overflow-auto">
          <Outlet />
          {children}
        </main>
      </div>
    </div>
  )
}