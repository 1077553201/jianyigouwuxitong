import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // Default layout routes
    {
      path: '/',
      component: () => import('@/layouts/DefaultLayout.vue'),
      children: [
        { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
        { path: 'products', name: 'ProductList', component: () => import('@/views/ProductList.vue') },
        { path: 'products/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue') },
        { path: 'login', name: 'Login', component: () => import('@/views/Login.vue') },
        { path: 'register', name: 'Register', component: () => import('@/views/Register.vue') },
        { path: 'cart', name: 'Cart', component: () => import('@/views/Cart.vue'), meta: { requiresAuth: true } },
        { path: 'checkout', name: 'Checkout', component: () => import('@/views/Checkout.vue'), meta: { requiresAuth: true } },
        { path: 'orders', name: 'MyOrders', component: () => import('@/views/MyOrders.vue'), meta: { requiresAuth: true } },
        { path: 'orders/:id', name: 'OrderDetail', component: () => import('@/views/OrderDetail.vue'), meta: { requiresAuth: true } },
        { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { requiresAuth: true } },
      ],
    },
    // Admin layout routes
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAdmin: true },
      children: [
        { path: '', redirect: '/admin/dashboard' },
        { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue') },
        { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/Users.vue') },
        { path: 'products', name: 'AdminProducts', component: () => import('@/views/admin/Products.vue') },
        { path: 'orders', name: 'AdminOrders', component: () => import('@/views/admin/Orders.vue') },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresAdmin && auth.user?.role !== 'admin') {
    return { path: '/' }
  }

  if ((to.path === '/login' || to.path === '/register') && auth.token) {
    return { path: '/' }
  }
})

export default router
