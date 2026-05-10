import { createBrowserRouter, Navigate, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from '@/layouts/AdminLayout'
import OperatorLayout from '@/layouts/OperatorLayout'
import UserLayout from '@/layouts/UserLayout'
import { useEffect } from 'react'

// ============================================================================
// 路由守卫组件
// ============================================================================

/** 受保护路由 - 检查登录状态 */
function AuthGuard({ children }: { children: React.ReactNode }) {
  const navigate = useNavigate()
  const token = useAuthStore((s) => s.token)
  useEffect(() => {
    if (!token) {
      navigate('/auth/login', { replace: true })
    }
  }, [token, navigate])
  if (!token) {
    return null
  }
  return <>{children}</>
}

/** 角色守卫 - 检查用户角色 */
function RoleGuard({
  children,
  allowedRoles,
}: {
  children: React.ReactNode
  allowedRoles: number[]
}) {
  const navigate = useNavigate()
  const userInfo = useAuthStore((s) => s.userInfo)
  useEffect(() => {
    if (!userInfo || !allowedRoles.includes(userInfo.role)) {
      if (userInfo?.role === 1) {
        navigate('/admin/dashboard', { replace: true })
      } else if (userInfo?.role === 2) {
        navigate('/operator/dashboard', { replace: true })
      } else {
        navigate('/user/products', { replace: true })
      }
    }
  }, [userInfo, allowedRoles, navigate])
  if (!userInfo || !allowedRoles.includes(userInfo.role)) {
    return null
  }
  return <>{children}</>
}

/** 已登录用户访问登录页时重定向 */
function AuthRedirect({ children }: { children: React.ReactNode }) {
  const navigate = useNavigate()
  const token = useAuthStore((s) => s.token)
  const userInfo = useAuthStore((s) => s.userInfo)
  useEffect(() => {
    if (token && userInfo) {
      if (userInfo.role === 1) {
        navigate('/admin/dashboard', { replace: true })
      } else if (userInfo.role === 2) {
        navigate('/operator/dashboard', { replace: true })
      } else {
        navigate('/user/products', { replace: true })
      }
    }
  }, [token, userInfo, navigate])
  if (token && userInfo) {
    return null
  }
  return <>{children}</>
}

// ============================================================================
// 懒加载页面组件
// ============================================================================

const LoginPage = React.lazy(() => import('@/pages/auth/login'))
const RegisterPage = React.lazy(() => import('@/pages/auth/register'))

// Admin
const AdminDashboard = React.lazy(() => import('@/pages/admin/dashboard'))
const AdminShops = React.lazy(() => import('@/pages/admin/shops'))
const AdminUsers = React.lazy(() => import('@/pages/admin/users'))
const AdminInviteCode = React.lazy(() => import('@/pages/admin/invite-code'))

// Operator
const OperatorDashboard = React.lazy(() => import('@/pages/operator/dashboard'))
const OperatorShop = React.lazy(() => import('@/pages/operator/shop'))
const OperatorCategories = React.lazy(() => import('@/pages/operator/categories'))
const OperatorProducts = React.lazy(() => import('@/pages/operator/products'))
const OperatorOrders = React.lazy(() => import('@/pages/operator/orders'))
const OperatorUsers = React.lazy(() => import('@/pages/operator/users'))
const OperatorInviteCode = React.lazy(() => import('@/pages/operator/invite-code'))
const OperatorMessages = React.lazy(() => import('@/pages/operator/messages'))

// User
const UserProducts = React.lazy(() => import('@/pages/user/products'))
const UserProductDetail = React.lazy(() => import('@/pages/user/products/[id]'))
const UserOrders = React.lazy(() => import('@/pages/user/orders'))
const UserOrderDetail = React.lazy(() => import('@/pages/user/orders/[id]'))
const UserCenter = React.lazy(() => import('@/pages/user/center'))
const UserAddresses = React.lazy(() => import('@/pages/user/addresses'))
const UserMessages = React.lazy(() => import('@/pages/user/messages'))

// ============================================================================
// 路由配置
// ============================================================================

import React from 'react'

const router = createBrowserRouter([
  // ============================================================================
  // 公开路由
  // ============================================================================
  {
    path: '/auth/login',
    element: (
      <React.Suspense fallback={<PageLoading />}>
        <AuthRedirect>
          <LoginPage />
        </AuthRedirect>
      </React.Suspense>
    ),
  },
  {
    path: '/auth/register',
    element: (
      <React.Suspense fallback={<PageLoading />}>
        <AuthRedirect>
          <RegisterPage />
        </AuthRedirect>
      </React.Suspense>
    ),
  },

  // ============================================================================
  // Admin 路由 - role = 1
  // ============================================================================
  {
    path: '/admin',
    element: (
      <React.Suspense fallback={<PageLoading />}>
        <AuthGuard>
          <RoleGuard allowedRoles={[1]}>
            <AdminLayout><></></AdminLayout>
          </RoleGuard>
        </AuthGuard>
      </React.Suspense>
    ),
    children: [
      { index: true, element: <Navigate to="/admin/dashboard" replace /> },
      { path: 'dashboard', element: <AdminDashboard /> },
      { path: 'shops', element: <AdminShops /> },
      { path: 'users', element: <AdminUsers /> },
      { path: 'invite-code', element: <AdminInviteCode /> },
    ],
  },

  // ============================================================================
  // Operator 路由 - role = 2
  // ============================================================================
  {
    path: '/operator',
    element: (
      <React.Suspense fallback={<PageLoading />}>
        <AuthGuard>
          <RoleGuard allowedRoles={[2]}>
            <OperatorLayout><></></OperatorLayout>
          </RoleGuard>
        </AuthGuard>
      </React.Suspense>
    ),
    children: [
      { index: true, element: <Navigate to="/operator/dashboard" replace /> },
      { path: 'dashboard', element: <OperatorDashboard /> },
      { path: 'shop', element: <OperatorShop /> },
      { path: 'categories', element: <OperatorCategories /> },
      { path: 'products', element: <OperatorProducts /> },
      { path: 'orders', element: <OperatorOrders /> },
      { path: 'users', element: <OperatorUsers /> },
      { path: 'invite-code', element: <OperatorInviteCode /> },
      { path: 'messages', element: <OperatorMessages /> },
    ],
  },

  // ============================================================================
  // User 路由 - role = 3
  // ============================================================================
  {
    path: '/user',
    element: (
      <React.Suspense fallback={<PageLoading />}>
        <AuthGuard>
          <RoleGuard allowedRoles={[3]}>
            <UserLayout><></></UserLayout>
          </RoleGuard>
        </AuthGuard>
      </React.Suspense>
    ),
    children: [
      { index: true, element: <Navigate to="/user/products" replace /> },
      { path: 'products', element: <UserProducts /> },
      { path: 'products/:id', element: <UserProductDetail /> },
      { path: 'orders', element: <UserOrders /> },
      { path: 'orders/:id', element: <UserOrderDetail /> },
      { path: 'center', element: <UserCenter /> },
      { path: 'addresses', element: <UserAddresses /> },
      { path: 'messages', element: <UserMessages /> },
    ],
  },

  // ============================================================================
  // 错误路由
  // ============================================================================
  {
    path: '/error-test',
    element: <div>Error test page - no auth needed</div>,
  },
  // ============================================================================
  // 默认重定向
  // ============================================================================
  {
    path: '/',
    element: <Navigate to="/error-test" replace />,
  },
  {
    path: '*',
    element: <Navigate to="/error-test" replace />,
  },
])

export { router }

// ============================================================================
// Loading Component
// ============================================================================

function PageLoading() {
  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900" />
    </div>
  )
}
