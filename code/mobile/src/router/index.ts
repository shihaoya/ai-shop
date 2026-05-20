import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/mobile' },

  // 认证页
  {
    path: '/mobile/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', guest: true },
  },
  {
    path: '/mobile/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', guest: true },
  },

  // 普通用户
  {
    path: '/mobile/user',
    component: () => import('@/layouts/UserLayout.vue'),
    meta: { role: 3 },
    redirect: '/mobile/user/products',
    children: [
      { path: 'products', name: 'UserProducts', component: () => import('@/views/user/ProductList.vue'), meta: { title: '商品列表' } },
      { path: 'orders', name: 'UserOrders', component: () => import('@/views/user/OrderList.vue'), meta: { title: '我的订单' } },
      { path: 'profile', name: 'UserProfile', component: () => import('@/views/user/Profile.vue'), meta: { title: '个人中心' } },
      { path: 'addresses', name: 'UserAddresses', component: () => import('@/views/user/AddressList.vue'), meta: { title: '地址管理' } },
      { path: 'messages', name: 'UserMessages', component: () => import('@/views/user/MessageList.vue'), meta: { title: '我的消息' } },
    ],
  },

  // 店铺用户
  {
    path: '/mobile/operator',
    component: () => import('@/layouts/OperatorLayout.vue'),
    meta: { role: 2 },
    redirect: '/mobile/operator/products',
    children: [
      { path: 'products', name: 'OpProducts', component: () => import('@/views/operator/ProductManage.vue'), meta: { title: '商品管理' } },
      { path: 'products/add', name: 'OpProductAdd', component: () => import('@/views/operator/ProductForm.vue'), meta: { title: '新增商品' } },
      { path: 'products/edit/:id', name: 'OpProductEdit', component: () => import('@/views/operator/ProductForm.vue'), meta: { title: '编辑商品' } },
      { path: 'categories', name: 'OpCategories', component: () => import('@/views/operator/CategoryManage.vue'), meta: { title: '分类管理' } },
      { path: 'orders', name: 'OpOrders', component: () => import('@/views/operator/OrderManage.vue'), meta: { title: '订单管理' } },
      { path: 'users', name: 'OpUsers', component: () => import('@/views/operator/UserPoints.vue'), meta: { title: '用户积分' } },
      { path: 'messages', name: 'OpMessages', component: () => import('@/views/operator/MessageManage.vue'), meta: { title: '消息管理' } },
      { path: 'shop', name: 'OpShop', component: () => import('@/views/operator/ShopInfo.vue'), meta: { title: '我的店铺' } },
      { path: 'profile', name: 'OpProfile', component: () => import('@/views/operator/Profile.vue'), meta: { title: '个人中心' } },
    ],
  },

  // 管理用户
  {
    path: '/mobile/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { role: 1 },
    redirect: '/mobile/admin/shops',
    children: [
      { path: 'shops', name: 'AdminShops', component: () => import('@/views/admin/ShopReview.vue'), meta: { title: '店铺审批' } },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/UserManage.vue'), meta: { title: '用户管理' } },
      { path: 'profile', name: 'AdminProfile', component: () => import('@/views/admin/Profile.vue'), meta: { title: '个人中心' } },
    ],
  },

  { path: '/:pathMatch(.*)*', redirect: '/mobile/login' },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, _from) => {
  // 优先从 localStorage 读取，避免 Pinia 初始化时序问题
  const token = localStorage.getItem('mobile_token')
  const userInfoStr = localStorage.getItem('mobile_user_info')
  const userInfo = userInfoStr ? JSON.parse(userInfoStr) : null

  if (to.meta.guest) {
    // 登录注册页：如果已有token且有userInfo，跳转对应首页
    if (token && userInfo) {
      const role = userInfo.role
      if (role === 1) return '/mobile/admin/shops'
      else if (role === 2) return '/mobile/operator/products'
      else if (role === 3) return '/mobile/user/products'
    }
    return true
  }

  if (!token) {
    return '/mobile/login'
  }

  if (to.meta.role && to.meta.role !== userInfo?.role) {
    const role = userInfo?.role
    if (role === 3) return '/mobile/user/products'
    else if (role === 2) return '/mobile/operator/products'
    else if (role === 1) return '/mobile/admin/shops'
    else return '/mobile/login'
  }

  return true
})

export default router