import { Outlet, Link, useLocation } from 'react-router-dom'
import {
  Store,
  Users,
  KeyRound,
  ChevronDown,
  LogOut,
  Moon
} from 'lucide-react'
import { useAuthStore } from '@/stores'

interface AdminLayoutProps {
  children?: React.ReactNode
}

export default function AdminLayout({ children }: AdminLayoutProps) {
  const location = useLocation()
  const { logout } = useAuthStore()

  const navItems = [
    { path: '/admin/shops', label: '店铺管理', icon: Store },
    { path: '/admin/users', label: '用户管理', icon: Users },
    { path: '/admin/invite-code', label: '邀请码', icon: KeyRound },
  ]

  const isActive = (path: string) => location.pathname.startsWith(path)

  return (
    <div className="flex h-screen bg-neutral-950 text-neutral-100">
      {/* Sidebar */}
      <aside className="w-64 bg-neutral-900 border-r border-neutral-800 flex flex-col">
        {/* Logo */}
        <div className="h-16 flex items-center gap-3 px-6 border-b border-neutral-800">
          <div className="w-8 h-8 rounded-lg bg-accent flex items-center justify-center">
            <Moon className="w-5 h-5 text-white" />
          </div>
          <span className="font-semibold text-lg">积分商城管理后台</span>
        </div>

        {/* Navigation */}
        <nav className="flex-1 py-4 px-3 space-y-1">
          {navItems.map(({ path, label, icon: Icon }) => (
            <Link
              key={path}
              to={path}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors ${
                isActive(path)
                  ? 'bg-accent text-white'
                  : 'text-neutral-400 hover:text-neutral-100 hover:bg-neutral-800'
              }`}
            >
              <Icon className="w-5 h-5" />
              <span>{label}</span>
            </Link>
          ))}
        </nav>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        {/* Top Bar */}
        <header className="h-16 flex items-center justify-end px-6 border-b border-neutral-800">
          <div className="flex items-center gap-3">
            {/* User Dropdown */}
            <div className="relative group">
              <button type="button" className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-neutral-800 transition-colors">
                <div className="w-8 h-8 rounded-full bg-accent flex items-center justify-center text-sm font-medium">
                  A
                </div>
                <span className="text-sm">管理员</span>
                <ChevronDown className="w-4 h-4 text-neutral-500" />
              </button>

              {/* Dropdown Menu */}
              <div className="absolute right-0 top-full mt-1 w-48 py-1 bg-neutral-900 border border-neutral-800 rounded-lg shadow-xl opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all z-50">
                <button
                  type="button"
                  onClick={logout}
                  className="w-full flex items-center gap-2 px-4 py-2 text-sm text-neutral-400 hover:text-neutral-100 hover:bg-neutral-800 transition-colors"
                >
                  <LogOut className="w-4 h-4" />
                  <span>退出登录</span>
                </button>
              </div>
            </div>
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