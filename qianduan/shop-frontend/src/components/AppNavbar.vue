<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const keyword = ref('')

function handleSearch() {
  if (keyword.value.trim()) {
    router.push({ path: '/products', query: { keyword: keyword.value.trim() } })
  }
}

function handleLogout() {
  auth.logout()
  cart.reset()
  router.push('/')
}

function getUserInitial(): string {
  return auth.user?.username?.charAt(0)?.toUpperCase() ?? '?'
}
</script>

<template>
  <header class="navbar">
    <div class="navbar-inner">
      <!-- Left: Logo + Nav -->
      <div class="navbar-left">
        <router-link to="/" class="logo">
          <span class="logo-icon"></span>
          <span class="logo-text">
            <span class="logo-mh">Mh</span><span class="logo-platform">平台</span>
          </span>
        </router-link>
        <nav class="nav-links">
          <router-link to="/">首页</router-link>
          <router-link to="/products">商品</router-link>
        </nav>
      </div>

      <!-- Center: Search -->
      <div class="navbar-center">
        <el-input
          v-model="keyword"
          placeholder="搜索商品..."
          clearable
          @keyup.enter="handleSearch"
          class="search-input"
        >
          <template #append>
            <el-button @click="handleSearch" class="search-btn">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>

      <!-- Right: Cart + User -->
      <div class="navbar-right">
        <router-link to="/cart" class="cart-link">
          <el-badge :value="cart.count" :hidden="cart.count === 0" :max="99">
            <el-icon :size="22"><ShoppingCart /></el-icon>
          </el-badge>
        </router-link>

        <template v-if="auth.token">
          <el-dropdown trigger="click">
            <span class="user-dropdown">
              <span class="user-avatar">{{ getUserInitial() }}</span>
              <span class="user-name">{{ auth.user?.username }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">个人信息</el-dropdown-item>
                <el-dropdown-item @click="router.push('/orders')">我的订单</el-dropdown-item>
                <el-dropdown-item
                  v-if="auth.user?.role === 'admin'"
                  @click="router.push('/admin')"
                >
                  管理后台
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <template v-else>
          <router-link to="/login" class="auth-login">登录</router-link>
          <router-link to="/register" class="auth-register">注册</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--shop-navbar-bg);
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.06);
}

.navbar-inner {
  max-width: 1280px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 24px;
}

/* ── Logo ── */
.navbar-left {
  display: flex;
  align-items: center;
  gap: 32px;
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.logo-icon {
  display: inline-block;
  width: 10px;
  height: 36px;
  border-radius: 4px;
  background: linear-gradient(160deg, #818cf8 0%, #5b4fe8 100%);
  flex-shrink: 0;
}

.logo-text {
  font-size: 19px;
  font-weight: 700;
  letter-spacing: -0.3px;
  line-height: 1;
}

.logo-mh {
  color: #ffffff;
}

.logo-platform {
  color: #a5b4fc;
}

/* ── Nav Links ── */
.nav-links {
  display: flex;
  gap: 4px;
}

.nav-links a {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.65);
  padding: 6px 12px;
  border-radius: 6px;
  text-decoration: none;
  transition: color 0.2s, background 0.2s;
}

.nav-links a:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.08);
}

.nav-links a.router-link-exact-active {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.1);
}

/* ── Search ── */
.navbar-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.search-input {
  width: 100%;
  max-width: 440px;
}

.search-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.18) inset;
  border-radius: 8px 0 0 8px;
}

.search-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.32) inset;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--shop-primary) inset !important;
}

.search-input :deep(.el-input__inner) {
  color: #ffffff;
}

.search-input :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.38);
}

.search-input :deep(.el-input__clear) {
  color: rgba(255, 255, 255, 0.5);
}

.search-input :deep(.el-input-group__append) {
  background: var(--shop-primary);
  border: none;
  border-radius: 0 8px 8px 0;
  padding: 0 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.search-input :deep(.el-input-group__append:hover) {
  background: var(--shop-primary-hover);
}

.search-input :deep(.el-input-group__append .el-icon) {
  color: #ffffff;
}

/* ── Right section ── */
.navbar-right {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-shrink: 0;
}

.cart-link {
  display: flex;
  align-items: center;
  color: rgba(255, 255, 255, 0.75);
  transition: color 0.2s;
}

.cart-link:hover {
  color: #ffffff;
}

.cart-link :deep(.el-badge__content) {
  background: var(--shop-accent);
  border-color: var(--shop-navbar-bg);
}

/* User dropdown trigger */
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #818cf8 0%, #5b4fe8 100%);
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.85);
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.user-dropdown:hover .user-name {
  color: #ffffff;
}

.dropdown-arrow {
  color: rgba(255, 255, 255, 0.45);
  font-size: 12px;
}

/* Auth links */
.auth-login {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.75);
  text-decoration: none;
  transition: color 0.2s;
}

.auth-login:hover {
  color: #ffffff;
}

.auth-register {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 16px;
  border-radius: 8px;
  background: var(--shop-primary);
  color: #ffffff;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: background 0.2s;
}

.auth-register:hover {
  background: var(--shop-primary-hover);
}
</style>
