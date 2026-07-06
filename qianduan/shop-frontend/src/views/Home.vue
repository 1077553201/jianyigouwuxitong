<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProducts, getCategories } from '@/api/product'
import { addToCart } from '@/api/cart'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import ProductCard from '@/components/ProductCard.vue'

interface Category {
  id: number
  name: string
}

interface Product {
  id: number
  name: string
  price: number
  stock: number
  imageUrl?: string
}

const router = useRouter()
const cart = useCartStore()
const auth = useAuthStore()
const categories = ref<Category[]>([])
const products = ref<Product[]>([])
const loading = ref(false)

const catColors = ['#5b4fe8', '#f97316', '#10b981', '#f59e0b', '#ec4899', '#06b6d4', '#8b5cf6', '#ef4444']

onMounted(async () => {
  loading.value = true
  try {
    const [catRes, prodRes]: any[] = await Promise.all([
      getCategories(),
      getProducts({ size: 8 }),
    ])
    categories.value = catRes.data ?? catRes ?? []
    products.value = prodRes.data?.content ?? prodRes?.content ?? prodRes?.data ?? prodRes ?? []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})

function goToCategory(id: number) {
  router.push({ path: '/products', query: { categoryId: String(id) } })
}

async function handleAddToCart(product: Product) {
  if (!auth.token) {
    router.push('/login')
    return
  }
  try {
    await addToCart({ productId: product.id, quantity: 1 })
    cart.increment(1)
    ElMessage.success('添加成功')
  } catch {
    // handled by interceptor
  }
}
</script>

<template>
  <div class="home" v-loading="loading">
    <!-- Banner -->
    <section class="banner">
      <div class="banner-inner">
        <div class="eyebrow">✦ 精选品质好物</div>
        <h1 class="banner-title">
          发现你喜爱的<br />
          <span class="title-accent">每一件好物</span>
        </h1>
        <p class="banner-sub">汇聚全球优质商品，为你的生活增添一抹精彩</p>
        <el-button
          type="primary"
          size="large"
          class="banner-btn"
          @click="router.push('/products')"
        >
          立即购物
          <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </section>

    <!-- Categories -->
    <section class="section" v-if="categories.length">
      <div class="section-inner">
        <h2 class="section-title">商品分类</h2>
        <div class="category-grid">
          <div
            v-for="(cat, index) in categories"
            :key="cat.id"
            class="category-card"
            @click="goToCategory(cat.id)"
          >
            <span
              class="cat-dot"
              :style="{ background: catColors[index % catColors.length] }"
            ></span>
            <span class="cat-name">{{ cat.name }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Recommended Products -->
    <section class="section" v-if="products.length">
      <div class="section-inner">
        <h2 class="section-title">推荐商品</h2>
        <el-row :gutter="20">
          <el-col
            v-for="p in products"
            :key="p.id"
            :xs="24" :sm="12" :md="8" :lg="6"
            class="product-col"
          >
            <ProductCard :product="p" @add-to-cart="handleAddToCart" />
          </el-col>
        </el-row>
        <div class="more-link">
          <el-button plain size="large" @click="router.push('/products')">
            查看全部商品
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ── Banner ─────────────────────────────────────────── */
.banner {
  background:
    radial-gradient(ellipse 65% 80% at 80% 40%, rgba(91, 79, 232, 0.4) 0%, transparent 55%),
    radial-gradient(ellipse 50% 70% at 15% 70%, rgba(124, 111, 245, 0.25) 0%, transparent 50%),
    linear-gradient(150deg, #0c1120 0%, #131a35 50%, #1a1545 100%);
  padding: 120px 24px;
}

.banner-inner {
  max-width: 580px;
  margin: 0 auto;
}

.eyebrow {
  display: inline-block;
  background: rgba(91, 79, 232, 0.18);
  color: #a5b4fc;
  border: 1px solid rgba(165, 180, 252, 0.3);
  border-radius: 100px;
  padding: 5px 14px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  margin-bottom: 20px;
}

.banner-title {
  font-size: clamp(36px, 5vw, 56px);
  font-weight: 800;
  color: #ffffff;
  line-height: 1.2;
  letter-spacing: -1.5px;
  margin: 0 0 0 0;
}

.title-accent {
  background: linear-gradient(135deg, #a5b4fc 0%, #818cf8 50%, #c4b5fd 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.banner-sub {
  color: rgba(255, 255, 255, 0.6);
  font-size: 17px;
  margin-top: 16px;
  margin-bottom: 36px;
  line-height: 1.6;
}

.banner-btn {
  border-radius: 12px !important;
  padding: 14px 36px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  height: auto !important;
}

/* ── Sections ────────────────────────────────────────── */
.section {
  padding: 56px 0;
}

.section-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--shop-text);
  margin-bottom: 24px;
  letter-spacing: -0.3px;
  position: relative;
  padding-left: 14px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: var(--shop-primary);
  border-radius: 2px;
}

/* ── Categories ──────────────────────────────────────── */
.category-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.category-card {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--shop-surface);
  border-radius: 10px;
  padding: 12px 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: var(--shop-text);
  box-shadow: var(--shop-shadow);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.category-card:hover {
  box-shadow: var(--shop-shadow-lg);
  transform: translateY(-2px);
}

.cat-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.cat-name {
  white-space: nowrap;
}

/* ── Products ────────────────────────────────────────── */
.product-col {
  margin-bottom: 20px;
}

.more-link {
  text-align: center;
  margin-top: 36px;
}
</style>
