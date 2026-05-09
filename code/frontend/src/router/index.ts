import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/admin',
    component: () => import('@/views/layout/Layout.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      {
        path: 'shops',
        name: 'AdminShops',
        component: () => import('@/views/admin/ShopManage.vue'),
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
      },
    ],
  },
  {
    path: '/operator',
    component: () => import('@/views/layout/Layout.vue'),
    meta: { requiresAuth: true, roles: ['OPERATOR'] },
    children: [
      {
        path: 'shop',
        name: 'MyShop',
        component: () => import('@/views/operator/MyShop.vue'),
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/operator/CategoryManage.vue'),
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/operator/ProductManage.vue'),
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/operator/OrderManage.vue'),
      },
      {
        path: 'customers',
        name: 'Customers',
        component: () => import('@/views/operator/CustomerManage.vue'),
      },
      {
        path: 'messages',
        name: 'OperatorMessages',
        component: () => import('@/views/operator/Messages.vue'),
      },
    ],
  },
  {
    path: '/user',
    component: () => import('@/views/layout/Layout.vue'),
    meta: { requiresAuth: true, roles: ['USER'] },
    children: [
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('@/views/user/ProductList.vue'),
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('@/views/user/ProductDetail.vue'),
      },
      {
        path: 'orders',
        name: 'MyOrders',
        component: () => import('@/views/user/MyOrders.vue'),
      },
      {
        path: 'addresses',
        name: 'Addresses',
        component: () => import('@/views/user/AddressBook.vue'),
      },
      {
        path: 'messages',
        name: 'MyMessages',
        component: () => import('@/views/user/MyMessages.vue'),
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 导航守卫
router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore()
  const requiresAuth = to.meta.requiresAuth !== false
  const allowedRoles = to.meta.roles as string[] | undefined

  if (requiresAuth && !auth.isLoggedIn) {
    next('/login')
    return
  }

  if (auth.isLoggedIn && (to.name === 'Login' || to.name === 'Register')) {
    next(auth.getHomeRoute())
    return
  }

  if (allowedRoles && auth.userRole && !allowedRoles.includes(auth.userRole)) {
    next(auth.getHomeRoute())
    return
  }

  next()
})

export default router