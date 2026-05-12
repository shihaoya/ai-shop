import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { setupRouterGuard } from './guards'

const routes: RouteRecordRaw[] = [
  // 根路径重定向到登录
  { path: '/', redirect: '/login' },

  // 认证页（无布局，居中玻璃卡片）
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { layout: 'auth', title: '登录', guest: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { layout: 'auth', title: '注册', guest: true },
  },

  // 管理员
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { layout: 'admin', role: 1 },
    children: [
      { path: '', redirect: 'shops' },
      { path: 'shops', name: 'AdminShops', component: () => import('@/views/admin/ShopReview.vue'), meta: { title: '店铺管理', sidebar: true, icon: 'fa-store' } },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/UserManage.vue'), meta: { title: '用户管理', sidebar: true, icon: 'fa-users' } },
      { path: 'invite-code', name: 'AdminInviteCode', component: () => import('@/views/admin/InviteCode.vue'), meta: { title: '邀请码', sidebar: true, icon: 'fa-qrcode' } },
    ],
  },

  // 店铺用户
  {
    path: '/operator',
    component: () => import('@/layouts/OperatorLayout.vue'),
    meta: { layout: 'operator', role: 2 },
    children: [
      { path: '', redirect: 'shop' },
      { path: 'shop', name: 'OpShop', component: () => import('@/views/operator/ShopManage.vue'), meta: { title: '我的店铺', sidebar: true, icon: 'fa-store' } },
      { path: 'categories', name: 'OpCategories', component: () => import('@/views/operator/CategoryManage.vue'), meta: { title: '分类管理', sidebar: true, icon: 'fa-tags' } },
      { path: 'products', name: 'OpProducts', component: () => import('@/views/operator/ProductManage.vue'), meta: { title: '商品管理', sidebar: true, icon: 'fa-box' } },
      { path: 'products/create', name: 'OpProductCreate', component: () => import('@/views/operator/ProductEdit.vue'), meta: { title: '新增商品' } },
      { path: 'products/:id/edit', name: 'OpProductEdit', component: () => import('@/views/operator/ProductEdit.vue'), meta: { title: '编辑商品' } },
      { path: 'orders', name: 'OpOrders', component: () => import('@/views/operator/OrderManage.vue'), meta: { title: '订单管理', sidebar: true, icon: 'fa-receipt' } },
      { path: 'orders/:id', name: 'OpOrderDetail', component: () => import('@/views/operator/OrderDetail.vue'), meta: { title: '订单详情' } },
      { path: 'users', name: 'OpUsers', component: () => import('@/views/operator/UserPoints.vue'), meta: { title: '用户管理', sidebar: true, icon: 'fa-users' } },
      { path: 'users/approve', name: 'OpUserApprove', component: () => import('@/views/operator/UserApprove.vue'), meta: { title: '用户审核' } },
      { path: 'users/:id/points', name: 'OpUserPointsLog', component: () => import('@/views/operator/PointsLog.vue'), meta: { title: '积分流水' } },
      { path: 'invite-code', name: 'OpInviteCode', component: () => import('@/views/operator/InviteCodeManage.vue'), meta: { title: '邀请码', sidebar: true, icon: 'fa-qrcode' } },
      { path: 'messages', name: 'OpMessages', component: () => import('@/views/operator/MessageManage.vue'), meta: { title: '消息', sidebar: true, icon: 'fa-envelope' } },
    ],
  },

  // 普通用户
  {
    path: '/user',
    component: () => import('@/layouts/UserLayout.vue'),
    meta: { layout: 'user', role: 3 },
    redirect: '/user/products',
    children: [
      { path: 'products', name: 'UserProducts', component: () => import('@/views/user/ProductList.vue'), meta: { title: '商品列表', sidebar: true, icon: 'fa-store' } },
      { path: 'products/:id', name: 'UserProductDetail', component: () => import('@/views/user/ProductDetail.vue'), meta: { title: '商品详情' } },
      { path: 'orders', name: 'UserOrders', component: () => import('@/views/user/OrderList.vue'), meta: { title: '我的订单', sidebar: true, icon: 'fa-receipt' } },
      { path: 'orders/:id', name: 'UserOrderDetail', component: () => import('@/views/user/OrderDetail.vue'), meta: { title: '订单详情' } },
      { path: 'points', name: 'UserPoints', component: () => import('@/views/user/PointsInfo.vue'), meta: { title: '积分中心', sidebar: true, icon: 'fa-star' } },
      { path: 'addresses', name: 'UserAddresses', component: () => import('@/views/user/AddressList.vue'), meta: { title: '地址簿', sidebar: true, icon: 'fa-map-marker-alt' } },
      { path: 'messages', name: 'UserMessages', component: () => import('@/views/user/MessageList.vue'), meta: { title: '我的消息', sidebar: true, icon: 'fa-envelope' } },
    ],
  },

  // 默认重定向
  { path: '/', redirect: '/login' },
  { path: '/:pathMatch(.*)*', redirect: '/login' },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

setupRouterGuard(router)

export default router