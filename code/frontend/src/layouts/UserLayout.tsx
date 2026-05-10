import { Outlet, Link, useLocation } from 'react-router-dom'
import {
  Coins,
  ChevronDown,
  LogOut,
  User
} from 'lucide-react'
import { useAuthStore } from '@/stores'

interface UserLayoutProps {
  children?: React.ReactNode
}

export default function UserLayout({ children }: UserLayoutProps) {
  const location = useLocation()
  const { userInfo, logout } = useAuthStore()

  const navItems = [
    { path: '/user/products', label: '商品列表' },
    { path: '/user/orders', label: '我的订单' },
  ]

  const isActive = (path: string) => location.pathname.startsWith(path)

  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Top Navigation Bar */}
      <header className="h-14 bg-white border-b border-neutral-200 sticky top-0 z-40">
        <div className="h-full max-w-6xl mx-auto px-4 flex items-center justify-between">
          {/* Left: Logo + Nav */}
          <div className="flex items-center gap-8">
            {/* Logo */}
            <Link to="/user/products" className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-accent flex items-center justify-center">
                <Coins className="w-5 h-5 text-white" />
              </div>
              <span className="font-semibold text-neutral-900">积分商城</span>
            </Link>

            {/* Nav Links */}
            <nav className="flex items-center gap-1">
              {navItems.map(({ path, label }) => (
                <Link
                  key={path}
                  to={path}
                  className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                    isActive(path)
                      ? 'text-accent bg-accent/10'
                      : 'text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100'
                  }`}
                >
                  {label}
                </Link>
              ))}
            </nav>
          </div>

          {/* Right: Points + User Dropdown */}
          <div className="flex items-center gap-4">
            {/* Points Display */}
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-accent/10 border border-accent/20">
              <Coins className="w-4 h-4 text-accent" />
              <span className="text-sm font-medium text-accent">
                {userInfo?.points ?? 0} 积分
              </span>
            </div>

            {/* User Dropdown */}
            <div className="relative group">
              <button type="button" className="flex items-center gap-2 px-2 py-1.5 rounded-lg hover:bg-neutral-100 transition-colors">
                <div className="w-7 h-7 rounded-full bg-neutral-200 flex items-center justify-center">
                  <User className="w-4 h-4 text-neutral-600" />
                </div>
                <span className="text-sm text-neutral-700">{userInfo?.nickname}</span>
                <ChevronDown className="w-4 h-4 text-neutral-400" />
              </button>

              {/* Dropdown Menu */}
              <div className="absolute right-0 top-full mt-1 w-44 py-1 bg-white border border-neutral-200 rounded-lg shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all z-50">
                <Link
                  to="/user/center"
                  className="flex items-center gap-2 px-4 py-2 text-sm text-neutral-600 hover:text-neutral-900 hover:bg-neutral-50 transition-colors"
                >
                  <User className="w-4 h-4" />
                  <span>个人中心</span>
                </Link>
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
        </div>
      </header>

      {/* Content Area */}
      <main className="max-w-6xl mx-auto px-4 py-6">
        <Outlet />
        {children}
      </main>
    </div>
  )
}