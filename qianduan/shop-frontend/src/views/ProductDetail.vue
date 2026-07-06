<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductById, getProductReviews } from '@/api/product'
import { addToCart } from '@/api/cart'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import EmptyState from '@/components/EmptyState.vue'
import Pagination from '@/components/Pagination.vue'

interface Product {
  id: number
  name: string
  price: number
  stock: number
  imageUrl?: string
  description?: string
  categoryId?: number
}

interface Review {
  id: number
  username: string
  rating: number
  content: string
  createdAt: string
}

const route = useRoute()
const router = useRouter()
const cart = useCartStore()
const auth = useAuthStore()
const product = ref<Product | null>(null)
const reviews = ref<Review[]>([])
const reviewTotal = ref(0)
const reviewPage = ref(0)
const quantity = ref(1)
const loading = ref(false)
const reviewLoading = ref(false)

function toNum(v: any): number {
  const n = Number(v)
  return isNaN(n) ? 0 : n
}

async function fetchProduct() {
  loading.value = true
  try {
    const res: any = await getProductById(route.params.id as string)
    product.value = res.data ?? res
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function fetchReviews(page = 0) {
  reviewLoading.value = true
  try {
    const res: any = await getProductReviews(route.params.id as string, { page, size: 10 })
    const data = res.data ?? res
    reviews.value = data.content ?? data ?? []
    reviewTotal.value = data.totalElements ?? data.total ?? 0
    reviewPage.value = page
  } catch {
    // handled by interceptor
  } finally {
    reviewLoading.value = false
  }
}

onMounted(() => {
  fetchProduct()
  fetchReviews()
})

async function handleAddToCart() {
  if (!auth.token) {
    router.push('/login')
    return
  }
  if (!product.value) return
  try {
    await addToCart({ productId: product.value.id, quantity: quantity.value })
    cart.increment(quantity.value)
    ElMessage.success('添加成功')
  } catch {
    // handled by interceptor
  }
}

async function handleBuyNow() {
  if (!auth.token) {
    router.push('/login')
    return
  }
  if (!product.value) return
  try {
    await addToCart({ productId: product.value.id, quantity: quantity.value })
    cart.increment(quantity.value)
    // Store the product id for checkout
    sessionStorage.setItem('checkoutItemIds', JSON.stringify([product.value.id]))
    router.push('/checkout')
  } catch {
    // handled by interceptor
  }
}

function handleReviewPageChange(page: number) {
  fetchReviews(page - 1)
}
</script>

<template>
  <div class="product-detail" v-loading="loading">
    <div class="detail-inner" v-if="product">
      <!-- Product info card -->
      <div class="product-info">
        <div class="product-image">
          <img
            :src="product.imageUrl || 'https://placehold.co/400x300?text=商品图片'"
            :alt="product.name"
          />
        </div>
        <div class="product-meta">
          <h1 class="product-name">{{ product.name }}</h1>
          <div class="product-price">¥{{ toNum(product.price).toFixed(2) }}</div>
          <div class="product-stock">
            库存：<span :class="{ 'out-of-stock': toNum(product.stock) <= 0 }">
              {{ toNum(product.stock) > 0 ? toNum(product.stock) + ' 件' : '已售罄' }}
            </span>
          </div>

          <div class="quantity-row">
            <span class="label">数量：</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="toNum(product.stock)"
              :disabled="toNum(product.stock) <= 0"
              size="default"
            />
          </div>

          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              :disabled="toNum(product.stock) <= 0"
              @click="handleAddToCart"
            >
              加入购物车
            </el-button>
            <el-button
              plain
              size="large"
              :disabled="toNum(product.stock) <= 0"
              @click="handleBuyNow"
            >
              立即购买
            </el-button>
          </div>
        </div>
      </div>

      <!-- Description section -->
      <section class="section-block" v-if="product.description">
        <h2 class="section-heading">商品描述</h2>
        <div class="description-text">{{ product.description }}</div>
      </section>

      <!-- Reviews section -->
      <section class="section-block">
        <h2 class="section-heading">用户评价（{{ reviewTotal }} 条）</h2>
        <div v-loading="reviewLoading">
          <div v-if="reviews.length" class="review-list">
            <div v-for="r in reviews" :key="r.id" class="review-item">
              <div class="review-header">
                <span class="review-user">{{ r.username }}</span>
                <el-rate :model-value="r.rating" disabled size="small" />
                <span class="review-date">{{ r.createdAt?.slice(0, 10) }}</span>
              </div>
              <p class="review-content">{{ r.content }}</p>
            </div>
          </div>
          <EmptyState v-else-if="!reviewLoading" icon="ChatDotRound" text="暂无评价" />
          <Pagination
            :current-page="reviewPage + 1"
            :page-size="10"
            :total="reviewTotal"
            @change="handleReviewPageChange"
          />
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.product-detail {
  padding: 24px;
  background: var(--shop-bg, #f1f3f9);
  min-height: 100vh;
}

.detail-inner {
  max-width: 1000px;
  margin: 0 auto;
}

/* Product info card */
.product-info {
  display: flex;
  gap: 40px;
  background: var(--shop-surface, #ffffff);
  border-radius: var(--shop-radius, 12px);
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: var(--shop-shadow);
  border: 1px solid var(--shop-border);
}

.product-image {
  width: 400px;
  flex-shrink: 0;
  border-radius: var(--shop-radius-sm, 8px);
  overflow: hidden;
  background: #f0f0f0;
}

.product-image img {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  display: block;
}

.product-meta {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 24px;
  font-weight: 700;
  color: var(--shop-text);
  margin-bottom: 16px;
  line-height: 1.3;
}

/* Price: accent orange, 32px, 800 */
.product-price {
  font-size: 32px;
  font-weight: 800;
  color: var(--shop-accent, #f97316);
  margin-bottom: 12px;
}

.product-stock {
  font-size: 14px;
  color: var(--shop-text-secondary);
  margin-bottom: 20px;
}

.out-of-stock {
  color: #ef4444;
  font-weight: 600;
}

.quantity-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.quantity-row .label {
  font-size: 14px;
  color: var(--shop-text);
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* Description & reviews card */
.section-block {
  background: var(--shop-surface, #ffffff);
  border-radius: var(--shop-radius, 12px);
  padding: 24px 32px;
  margin-bottom: 24px;
  box-shadow: var(--shop-shadow);
  border: 1px solid var(--shop-border);
}

.section-heading {
  font-size: 18px;
  font-weight: 600;
  color: var(--shop-text);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--shop-border);
}

.description-text {
  font-size: 14px;
  color: var(--shop-text);
  line-height: 1.8;
  white-space: pre-wrap;
}

/* Reviews */
.review-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--shop-border);
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

/* Username: bold */
.review-user {
  font-weight: 600;
  font-size: 14px;
  color: var(--shop-text);
}

/* Date: right-aligned */
.review-date {
  font-size: 12px;
  color: var(--shop-text-secondary);
  margin-left: auto;
}

.review-content {
  font-size: 14px;
  color: var(--shop-text);
  line-height: 1.6;
  margin: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .product-info {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }
  .product-image {
    width: 100%;
  }
  .section-block {
    padding: 20px;
  }
}
</style>
