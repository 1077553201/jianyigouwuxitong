<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCartList, updateCartItem, deleteCartItem } from '@/api/cart'
import { useCartStore } from '@/stores/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import EmptyState from '@/components/EmptyState.vue'

interface CartItem {
  id: number
  productId: number
  productName: string
  price: number
  quantity: number
  stock?: number
  imageUrl?: string
}

const router = useRouter()
const cart = useCartStore()
const cartItems = ref<CartItem[]>([])
const selectedIds = ref<number[]>([])
const loading = ref(false)

function toNum(v: any): number {
  const n = Number(v)
  return isNaN(n) ? 0 : n
}

const isAllSelected = computed(() =>
  cartItems.value.length > 0 && selectedIds.value.length === cartItems.value.length,
)

const totalPrice = computed(() =>
  cartItems.value
    .filter((i) => selectedIds.value.includes(i.id))
    .reduce((sum, i) => sum + toNum(i.price) * toNum(i.quantity), 0),
)

const selectedCount = computed(() => selectedIds.value.length)

function extractItems(res: any): any[] {
  // Try various possible response shapes
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res?.data?.content)) return res.data.content
  if (Array.isArray(res?.data?.list)) return res.data.list
  if (Array.isArray(res?.data?.items)) return res.data.items
  // If res.data is an object with numeric keys, it might be an array-like
  if (res?.data && typeof res.data === 'object') {
    const vals = Object.values(res.data)
    if (vals.length > 0 && typeof vals[0] === 'object') return vals as any[]
  }
  return []
}

async function fetchCart() {
  loading.value = true
  try {
    const res: any = await getCartList()
    const raw = extractItems(res)
    cartItems.value = raw.map((i: any) => {
      const p = i.product ?? {}
      return {
        id: i.id,
        productId: i.productId ?? p.id,
        productName: p.name ?? i.productName ?? '',
        price: toNum(p.price ?? i.price),
        quantity: toNum(i.quantity) || 1,
        stock: toNum(p.stock),
        imageUrl: p.imageUrl ?? i.imageUrl,
      }
    })
    // Auto-select all
    selectedIds.value = cartItems.value.map((i) => i.id)
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(fetchCart)

function toggleAll() {
  if (isAllSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = cartItems.value.map((i) => i.id)
  }
}

function toggleSelect(id: number) {
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
}

async function handleQuantityChange(item: CartItem, val: number) {
  if (val < 1) return
  if (item.stock && val > item.stock) {
    ElMessage.warning('超出库存')
    return
  }
  try {
    await updateCartItem(item.id, { quantity: val })
    item.quantity = val
    cart.fetchCount()
  } catch {
    // handled by interceptor
  }
}

async function handleDelete(item: CartItem) {
  try {
    await ElMessageBox.confirm('确定删除该商品？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteCartItem(item.id)
    cartItems.value = cartItems.value.filter((i) => i.id !== item.id)
    selectedIds.value = selectedIds.value.filter((id) => id !== item.id)
    cart.fetchCount()
    ElMessage.success('已删除')
  } catch {
    // user cancelled or error
  }
}

function goCheckout() {
  if (selectedIds.value.length === 0) return
  sessionStorage.setItem('checkoutItemIds', JSON.stringify(selectedIds.value))
  router.push('/checkout')
}
</script>

<template>
  <div class="cart-page">
    <div class="cart-inner">
      <div class="title-card">
        <h1 class="page-title">购物车</h1>
      </div>

      <div v-if="cartItems.length === 0 && !loading">
        <EmptyState icon="ShoppingCart" text="购物车是空的">
          <el-button type="primary" @click="router.push('/products')">去购物</el-button>
        </EmptyState>
      </div>

      <div v-else class="cart-layout" v-loading="loading">
        <!-- Cart items card -->
        <div class="cart-items">
          <!-- Header -->
          <div class="cart-header">
            <el-checkbox
              :model-value="isAllSelected"
              @change="toggleAll"
            >
              全选
            </el-checkbox>
          </div>

          <!-- Items -->
          <div v-for="item in cartItems" :key="item.id" class="cart-item">
            <el-checkbox
              :model-value="selectedIds.includes(item.id)"
              @change="toggleSelect(item.id)"
            />
            <div class="item-image">
              <img
                :src="item.imageUrl || 'https://placehold.co/100x100?text=商品'"
                :alt="item.productName"
              />
            </div>
            <div class="item-info">
              <router-link :to="`/products/${item.productId}`" class="item-name">
                {{ item.productName }}
              </router-link>
              <div class="item-price">¥{{ toNum(item.price).toFixed(2) }}</div>
            </div>
            <div class="item-quantity">
              <el-input-number
                :model-value="item.quantity"
                :min="1"
                :max="item.stock || 999"
                size="small"
                @change="(val: number) => handleQuantityChange(item, val)"
              />
            </div>
            <div class="item-subtotal">
              ¥{{ (toNum(item.price) * toNum(item.quantity)).toFixed(2) }}
            </div>
            <el-button
              type="danger"
              text
              @click="handleDelete(item)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>

          <div class="continue-link">
            <el-button text @click="router.push('/products')">继续购物</el-button>
          </div>
        </div>

        <!-- Summary card -->
        <div class="cart-summary">
          <h3 class="summary-heading">订单摘要</h3>
          <div class="summary-row">
            <span>已选 {{ selectedCount }} 件</span>
          </div>
          <div class="summary-total">
            <span class="total-label">合计：</span>
            <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <el-button
            type="primary"
            size="large"
            :disabled="selectedCount === 0"
            style="width: 100%"
            @click="goCheckout"
          >
            去结算
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cart-page {
  padding: 24px;
  background: var(--shop-bg, #f1f3f9);
  min-height: 100vh;
}

.cart-inner {
  max-width: 1000px;
  margin: 0 auto;
}

/* Title card */
.title-card {
  background: var(--shop-surface, #ffffff);
  border-radius: var(--shop-radius, 12px);
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: var(--shop-shadow);
  border: 1px solid var(--shop-border);
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--shop-text);
  margin: 0;
}

/* Layout */
.cart-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* Cart items list */
.cart-items {
  flex: 1;
  min-width: 0;
  background: var(--shop-surface, #ffffff);
  border-radius: var(--shop-radius, 12px);
  padding: 20px;
  box-shadow: var(--shop-shadow);
  border: 1px solid var(--shop-border);
}

.cart-header {
  padding-bottom: 12px;
  border-bottom: 1px solid var(--shop-border);
  margin-bottom: 4px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--shop-border);
}

.cart-item:last-of-type {
  border-bottom: none;
}

/* Product image */
.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--shop-text);
  text-decoration: none;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.15s;
}

.item-name:hover {
  color: var(--shop-primary, #5b4fe8);
}

/* Unit price: gray, 13px */
.item-price {
  font-size: 13px;
  color: var(--shop-text-secondary);
  margin-top: 6px;
}

.item-quantity {
  flex-shrink: 0;
}

/* Subtotal: accent orange, 16px, 600 */
.item-subtotal {
  font-size: 16px;
  font-weight: 600;
  color: var(--shop-accent, #f97316);
  width: 90px;
  text-align: right;
  flex-shrink: 0;
}

.continue-link {
  padding-top: 16px;
}

/* Summary card */
.cart-summary {
  width: 280px;
  flex-shrink: 0;
  background: var(--shop-surface, #ffffff);
  border-radius: var(--shop-radius, 12px);
  padding: 24px;
  height: fit-content;
  position: sticky;
  top: 84px;
  box-shadow: var(--shop-shadow);
  border: 1px solid var(--shop-border);
}

.summary-heading {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--shop-border);
  color: var(--shop-text);
}

.summary-row {
  font-size: 14px;
  color: var(--shop-text-secondary);
  margin-bottom: 12px;
}

.summary-total {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 20px;
}

.total-label {
  font-size: 14px;
  color: var(--shop-text);
}

/* Total price: accent orange, 26px, 800 */
.total-price {
  font-size: 26px;
  font-weight: 800;
  color: var(--shop-accent, #f97316);
}

/* Responsive */
@media (max-width: 768px) {
  .cart-layout {
    flex-direction: column;
  }
  .cart-summary {
    width: 100%;
    position: static;
  }
  .cart-item {
    flex-wrap: wrap;
  }
  .item-subtotal {
    width: auto;
  }
}
</style>
