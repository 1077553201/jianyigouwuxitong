<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCartList, deleteCartItem } from '@/api/cart'
import { createOrder } from '@/api/order'
import { getProfile } from '@/api/user'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

interface CartItem {
  id: number
  productId: number
  productName: string
  price: number
  quantity: number
  imageUrl?: string
}

const router = useRouter()
const cart = useCartStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const items = ref<CartItem[]>([])

const form = ref({
  phone: '',
  address: '',
})

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^\d{11}$/, message: '11位数字', trigger: 'blur' },
  ],
  address: [
    { required: true, message: '请输入收货地址', trigger: 'blur' },
    { min: 5, message: '不少于5个字符', trigger: 'blur' },
  ],
}

function toNum(v: any): number {
  const n = Number(v)
  return isNaN(n) ? 0 : n
}

function extractItems(res: any): any[] {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res?.data?.content)) return res.data.content
  if (Array.isArray(res?.data?.list)) return res.data.list
  if (Array.isArray(res?.data?.items)) return res.data.items
  if (res?.data && typeof res.data === 'object') {
    const vals = Object.values(res.data)
    if (vals.length > 0 && typeof vals[0] === 'object') return vals as any[]
  }
  return []
}

const totalPrice = computed(() =>
  items.value.reduce((sum, i) => sum + toNum(i.price) * toNum(i.quantity), 0),
)

onMounted(async () => {
  const ids: number[] = JSON.parse(sessionStorage.getItem('checkoutItemIds') || '[]')
  if (ids.length === 0) {
    router.push('/cart')
    return
  }

  try {
    const [cartRes, profileRes]: any[] = await Promise.all([
      getCartList(),
      getProfile(),
    ])
    const allItems = extractItems(cartRes)
    items.value = allItems
      .filter((i: any) => ids.includes(i.id))
      .map((i: any) => {
        const p = i.product ?? {}
        return {
          id: i.id,
          productId: i.productId ?? p.id,
          productName: p.name ?? i.productName ?? '',
          price: toNum(p.price ?? i.price),
          quantity: toNum(i.quantity) || 1,
          imageUrl: p.imageUrl ?? i.imageUrl,
        }
      })
    const profile = profileRes.data ?? profileRes
    if (profile?.phone) form.value.phone = profile.phone
  } catch {
    // handled by interceptor
  }
})

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    const res: any = await createOrder({
      address: form.value.address,
      phone: form.value.phone,
      items: items.value.map((i) => ({
        productId: i.productId,
        quantity: i.quantity,
      })),
    })
    const orderId = res.data?.id ?? res.id

    // Delete cart items
    await Promise.all(items.value.map((i) => deleteCartItem(i.id)))
    cart.fetchCount()

    ElMessage.success('下单成功')
    router.push(`/orders/${orderId}`)
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '下单失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="checkout-page">
    <div class="checkout-inner">
      <div class="title-card">
        <h1 class="page-title">确认订单</h1>
      </div>

      <!-- Shipping info -->
      <div class="checkout-section">
        <h2 class="section-title">收货信息</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="收货人手机号" />
          </el-form-item>
          <el-form-item label="地址" prop="address">
            <el-input v-model="form.address" placeholder="详细收货地址" />
          </el-form-item>
        </el-form>
      </div>

      <!-- Product list -->
      <div class="checkout-section">
        <h2 class="section-title">商品清单</h2>
        <div v-for="item in items" :key="item.id" class="checkout-item">
          <div class="ci-image">
            <img
              :src="item.imageUrl || 'https://placehold.co/60x60?text=商品'"
              :alt="item.productName"
            />
          </div>
          <div class="ci-info">
            <span class="ci-name">{{ item.productName }}</span>
            <span class="ci-price">¥{{ toNum(item.price).toFixed(2) }}</span>
          </div>
          <span class="ci-qty">× {{ item.quantity }}</span>
          <span class="ci-subtotal">¥{{ (toNum(item.price) * toNum(item.quantity)).toFixed(2) }}</span>
        </div>
      </div>

      <!-- Footer: total + submit -->
      <div class="checkout-footer">
        <div class="checkout-total">
          <span class="total-label">合计：</span>
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          @click="handleSubmit"
        >
          提交订单
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.checkout-page {
  padding: 24px;
  background: var(--shop-bg, #f1f3f9);
  min-height: 100vh;
}

.checkout-inner {
  max-width: 800px;
  margin: 0 auto;
}

/* Title card */
.title-card {
  background: var(--shop-surface, #ffffff);
  border-radius: var(--shop-radius, 12px);
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: var(--shop-shadow);
  border: 1px solid var(--shop-border);
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--shop-text);
  margin: 0;
}

/* Section blocks */
.checkout-section {
  background: var(--shop-surface, #ffffff);
  border-radius: var(--shop-radius, 12px);
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: var(--shop-shadow);
  border: 1px solid var(--shop-border);
}

/* Section title with left indigo bar */
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--shop-text);
  margin: 0 0 16px 0;
  padding-left: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--shop-border);
  position: relative;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 2px;
  bottom: 14px; /* stops above the border-bottom */
  width: 4px;
  border-radius: 2px;
  background: var(--shop-primary, #5b4fe8);
}

/* Checkout item row */
.checkout-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid var(--shop-border);
}

.checkout-item:last-child {
  border-bottom: none;
}

/* Product thumbnail: 60x60, radius 8px */
.ci-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
}

.ci-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.ci-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.ci-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--shop-text);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.ci-price {
  font-size: 13px;
  color: var(--shop-text-secondary);
}

.ci-qty {
  font-size: 14px;
  color: var(--shop-text-secondary);
  flex-shrink: 0;
}

/* Item subtotal: accent orange */
.ci-subtotal {
  font-size: 15px;
  font-weight: 600;
  color: var(--shop-accent, #f97316);
  width: 80px;
  text-align: right;
  flex-shrink: 0;
}

/* Footer: total + submit button */
.checkout-footer {
  background: var(--shop-surface, #ffffff);
  border-radius: var(--shop-radius, 12px);
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 24px;
  box-shadow: var(--shop-shadow);
  border: 1px solid var(--shop-border);
}

.checkout-total {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.total-label {
  font-size: 14px;
  color: var(--shop-text);
}

/* Total price: accent orange */
.total-price {
  font-size: 26px;
  font-weight: 800;
  color: var(--shop-accent, #f97316);
}

/* Responsive */
@media (max-width: 768px) {
  .checkout-footer {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  .checkout-total {
    justify-content: flex-end;
  }
  .ci-subtotal {
    width: auto;
  }
}
</style>
