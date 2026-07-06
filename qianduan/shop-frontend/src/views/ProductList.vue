<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProducts, getCategories } from '@/api/product'
import { addToCart } from '@/api/cart'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import Pagination from '@/components/Pagination.vue'

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

const route = useRoute()
const router = useRouter()
const cart = useCartStore()
const auth = useAuthStore()
const categories = ref<Category[]>([])
const products = ref<Product[]>([])
const total = ref(0)
const loading = ref(false)

const filters = reactive({
  keyword: (route.query.keyword as string) || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : null as number | null,
  minPrice: null as number | null,
  maxPrice: null as number | null,
  sortBy: null as string | null,
  sortDir: 'desc',
  page: 0,
  size: 12,
})

const sortOptions = [
  { label: '综合', sortBy: null },
  { label: '价格↑', sortBy: 'price', sortDir: 'asc' },
  { label: '价格↓', sortBy: 'price', sortDir: 'desc' },
  { label: '最新', sortBy: 'createdAt', sortDir: 'desc' },
]

async function fetchProducts() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: filters.page,
      size: filters.size,
    }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.categoryId) params.categoryId = filters.categoryId
    if (filters.minPrice != null) params.minPrice = filters.minPrice
    if (filters.maxPrice != null) params.maxPrice = filters.maxPrice
    if (filters.sortBy) {
      params.sortBy = filters.sortBy
      params.sortDir = filters.sortDir
    }

    const res: any = await getProducts(params)
    const data = res.data ?? res
    products.value = data.content ?? data ?? []
    total.value = data.totalElements ?? data.total ?? 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const res: any = await getCategories()
    categories.value = res.data ?? res ?? []
  } catch {
    // handled by interceptor
  }
}

onMounted(() => {
  fetchCategories()
  fetchProducts()
})

// Sync URL query changes
watch(() => route.query.keyword, (val) => {
  if (val !== undefined) {
    filters.keyword = (val as string) || ''
    filters.page = 0
  }
})

watch(() => route.query.categoryId, (val) => {
  if (val !== undefined) {
    filters.categoryId = val ? Number(val) : null
    filters.page = 0
  }
})

watch(filters, () => fetchProducts(), { deep: true })

function selectSort(opt: { sortBy: string | null; sortDir?: string }) {
  filters.sortBy = opt.sortBy
  filters.sortDir = opt.sortDir || 'desc'
  filters.page = 0
}

function handlePageChange(page: number) {
  filters.page = page - 1
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function applyPriceFilter() {
  filters.page = 0
}

function selectCategory(id: number | null) {
  filters.categoryId = id
  filters.page = 0
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
  <div class="product-list-page">
    <div class="page-inner">
      <!-- Page title card -->
      <div class="title-card">
        <h1 class="page-title">商品列表</h1>
      </div>

      <div class="list-layout">
        <!-- Sidebar -->
        <aside class="sidebar">
          <div class="filter-section">
            <h3 class="filter-title">商品分类</h3>
            <div
              class="filter-item"
              :class="{ active: filters.categoryId === null }"
              @click="selectCategory(null)"
            >
              全部分类
            </div>
            <div
              v-for="cat in categories"
              :key="cat.id"
              class="filter-item"
              :class="{ active: filters.categoryId === cat.id }"
              @click="selectCategory(cat.id)"
            >
              {{ cat.name }}
            </div>
          </div>

          <div class="filter-section">
            <h3 class="filter-title">价格区间</h3>
            <div class="price-filter">
              <el-input-number
                v-model="filters.minPrice"
                :min="0"
                :precision="0"
                controls-position="right"
                placeholder="最低"
                size="small"
                style="width: 100%"
              />
              <span class="price-sep">~</span>
              <el-input-number
                v-model="filters.maxPrice"
                :min="0"
                :precision="0"
                controls-position="right"
                placeholder="最高"
                size="small"
                style="width: 100%"
              />
              <el-button
                size="small"
                type="primary"
                @click="applyPriceFilter"
                style="width: 100%; margin-top: 8px"
              >
                确定
              </el-button>
            </div>
          </div>
        </aside>

        <!-- Main content -->
        <div class="main-area">
          <!-- Sort bar -->
          <div class="sort-bar">
            <div class="sort-buttons">
              <button
                v-for="opt in sortOptions"
                :key="opt.label"
                class="sort-btn"
                :class="{ active: filters.sortBy === opt.sortBy }"
                @click="selectSort(opt)"
              >
                {{ opt.label }}
              </button>
            </div>
            <span class="result-count">共 {{ total }} 件商品</span>
          </div>

          <!-- Products grid -->
          <div v-loading="loading" min-height="400">
            <el-row v-if="products.length" :gutter="16">
              <el-col
                v-for="p in products"
                :key="p.id"
                :xs="24" :sm="12" :md="8" :lg="6"
                style="margin-bottom: 16px"
              >
                <ProductCard :product="p" @add-to-cart="handleAddToCart" />
              </el-col>
            </el-row>

            <EmptyState
              v-else-if="!loading"
              text="没有找到符合条件的商品"
            >
              <el-button @click="router.push('/products')">查看全部商品</el-button>
            </EmptyState>
          </div>

          <Pagination
            :current-page="filters.page + 1"
            :page-size="filters.size"
            :total="total"
            @change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-list-page {
  padding: 24px;
  background: var(--shop-bg, #f1f3f9);
  min-height: 100vh;
}

.page-inner {
  max-width: 1200px;
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
.list-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* Sidebar */
.sidebar {
  width: 200px;
  flex-shrink: 0;
}

.filter-section {
  background: var(--shop-surface, #ffffff);
  border-radius: var(--shop-radius, 12px);
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: var(--shop-shadow);
  border: 1px solid var(--shop-border);
}

.filter-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: var(--shop-text);
}

.filter-item {
  padding: 8px 12px;
  font-size: 13px;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.15s, color 0.15s;
  color: var(--shop-text);
  margin-bottom: 2px;
}

.filter-item:last-child {
  margin-bottom: 0;
}

.filter-item:hover,
.filter-item.active {
  background: var(--shop-primary-light, #f0eeff);
  color: var(--shop-primary, #5b4fe8);
}

.price-filter {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.price-sep {
  text-align: center;
  color: var(--shop-text-secondary);
  font-size: 12px;
}

/* Main area */
.main-area {
  flex: 1;
  min-width: 0;
}

/* Sort bar */
.sort-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--shop-surface, #ffffff);
  border-radius: var(--shop-radius, 12px);
  padding: 12px 16px;
  margin-bottom: 16px;
  box-shadow: var(--shop-shadow);
  border: 1px solid var(--shop-border);
}

.sort-buttons {
  display: flex;
  gap: 4px;
}

.sort-btn {
  padding: 6px 16px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: var(--shop-text);
  border-radius: 6px;
  transition: background 0.15s, color 0.15s;
}

.sort-btn:hover,
.sort-btn.active {
  background: var(--shop-primary, #5b4fe8);
  color: #fff;
}

.result-count {
  font-size: 13px;
  color: var(--shop-text-secondary);
}

/* Responsive */
@media (max-width: 768px) {
  .list-layout {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
  }
}
</style>
