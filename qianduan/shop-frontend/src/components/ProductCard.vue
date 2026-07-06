<script setup lang="ts">
const props = defineProps<{
  product: {
    id: number
    name: string
    price: number
    stock: number
    imageUrl?: string
    description?: string
    categoryId?: number
  }
}>()

const emit = defineEmits<{
  'add-to-cart': [product: typeof props.product]
}>()

function toNum(v: any): number {
  const n = Number(v)
  return isNaN(n) ? 0 : n
}

function handleAddToCart() {
  emit('add-to-cart', props.product)
}
</script>

<template>
  <div class="product-card" @click="$router.push(`/products/${product.id}`)">
    <div class="card-image">
      <img
        :src="product.imageUrl || 'https://placehold.co/400x300?text=商品图片'"
        :alt="product.name"
        loading="lazy"
      />
    </div>
    <div class="card-body">
      <h3 class="card-title">{{ product.name }}</h3>
      <div class="card-price">¥{{ toNum(product.price).toFixed(2) }}</div>
      <div class="card-meta">
        <span class="stock-text">库存 {{ product.stock }} 件</span>
      </div>
      <el-button
        type="primary"
        size="small"
        :disabled="toNum(product.stock) <= 0"
        @click.stop="handleAddToCart"
        class="add-btn"
      >
        {{ toNum(product.stock) > 0 ? '加入购物车' : '已售罄' }}
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.product-card {
  background: var(--shop-surface);
  border-radius: var(--shop-radius);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--shop-shadow);
  border-left: 3px solid transparent;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-left-color 0.25s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shop-shadow-lg);
  border-left-color: var(--shop-primary);
}

.card-image {
  position: relative;
  padding-top: 75%;
  overflow: hidden;
  background: #f8f9fc;
}

.card-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.product-card:hover .card-image img {
  transform: scale(1.04);
}

.card-body {
  padding: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--shop-text);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 44px;
  margin: 0 0 8px;
}

.card-price {
  font-size: 20px;
  font-weight: 700;
  color: var(--shop-accent);
  margin-bottom: 4px;
}

.card-meta {
  margin-bottom: 12px;
}

.stock-text {
  font-size: 12px;
  color: var(--shop-text-secondary);
}

.add-btn {
  width: 100%;
  border-radius: 8px !important;
}
</style>
