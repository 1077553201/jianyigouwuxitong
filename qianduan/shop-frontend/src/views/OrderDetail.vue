<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderById, cancelOrder } from '@/api/order'
import { createReview } from '@/api/product'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

interface OrderItem {
  id: number
  productId: number
  productName: string
  price: number
  quantity: number
}

interface Order {
  id: number
  orderNo?: string
  status: string
  totalPrice: number
  address: string
  phone: string
  createdAt: string
  items?: OrderItem[]
  orderItems?: OrderItem[]
}

const route = useRoute()
const router = useRouter()
const order = ref<Order | null>(null)
const loading = ref(false)

function toNum(v: any): number {
  const n = Number(v)
  return isNaN(n) ? 0 : n
}

function getPrice(row: any): number {
  return toNum(row.totalPrice ?? row.totalAmount ?? row.total_price ?? row.amount ?? row.money)
}

// Review dialog
const reviewDialogVisible = ref(false)
const reviewSubmitting = ref(false)
const reviewFormRef = ref<FormInstance>()
const reviewForm = ref({
  productId: 0,
  productName: '',
  rating: 5,
  content: '',
})

const statusMap: Record<string, { text: string; type: string }> = {
  pending: { text: '待发货', type: 'warning' },
  shipped: { text: '已发货', type: '' },
  completed: { text: '已完成', type: 'success' },
  cancelled: { text: '已取消', type: 'info' },
}

async function fetchOrder() {
  loading.value = true
  try {
    const res: any = await getOrderById(route.params.id as string)
    order.value = res.data ?? res
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(fetchOrder)

async function handleCancel() {
  if (!order.value) return
  try {
    await ElMessageBox.confirm('确定取消该订单？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    fetchOrder()
  } catch {
    // user cancelled or error
  }
}

function openReview(item: OrderItem) {
  reviewForm.value = {
    productId: item.productId,
    productName: item.productName,
    rating: 5,
    content: '',
  }
  reviewDialogVisible.value = true
}

async function submitReview() {
  if (!order.value) return
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  reviewSubmitting.value = true
  try {
    await createReview({
      orderId: order.value.id,
      productId: reviewForm.value.productId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content,
    })
    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
  } catch {
    // handled by interceptor
  } finally {
    reviewSubmitting.value = false
  }
}
</script>

<template>
  <div class="order-detail-page" v-loading="loading">
    <div class="detail-inner" v-if="order">
      <div class="page-header">
        <h1 class="page-title">订单详情</h1>
        <el-button @click="router.push('/orders')">返回我的订单</el-button>
      </div>

      <div class="detail-section">
        <h2 class="section-title">订单信息</h2>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">订单编号</span>
            <span>#{{ order.orderNo || order.id }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">下单时间</span>
            <span>{{ order.createdAt?.slice(0, 16)?.replace('T', ' ') }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">订单状态</span>
            <el-tag :type="(statusMap[order.status]?.type || '') as any" size="small">
              {{ statusMap[order.status]?.text || order.status }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">收货手机</span>
            <span>{{ order.phone }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">收货地址</span>
            <span>{{ order.address }}</span>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <h2 class="section-title">商品明细</h2>
        <el-table :data="order.items || order.orderItems || []" style="width: 100%">
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column label="单价" width="120">
            <template #default="{ row }">¥{{ toNum(row.price).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              <span style="font-weight: 600; color: var(--shop-accent, #f97316)">
                ¥{{ (toNum(row.price) * toNum(row.quantity)).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" v-if="order.status === 'completed'">
            <template #default="{ row }">
              <el-button type="primary" text size="small" @click="openReview(row)">评价</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="order-total-row">
          合计：<span class="total-price">¥{{ getPrice(order).toFixed(2) }}</span>
        </div>
      </div>

      <div class="action-bar">
        <el-button
          v-if="order.status === 'pending'"
          type="danger"
          @click="handleCancel"
        >
          取消订单
        </el-button>
      </div>
    </div>

    <!-- Review Dialog -->
    <el-dialog v-model="reviewDialogVisible" title="评价商品" width="480px">
      <div class="review-dialog-content">
        <p class="review-product-name">商品：{{ reviewForm.productName }}</p>
        <div class="review-rating">
          <span>评分：</span>
          <el-rate v-model="reviewForm.rating" />
        </div>
        <el-input
          v-model="reviewForm.content"
          type="textarea"
          :rows="4"
          placeholder="请输入评价内容，最多500字"
          maxlength="500"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewSubmitting" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-detail-page {
  padding: 24px;
}

.detail-inner {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
}

.detail-section {
  background: #fff;
  border-radius: var(--shop-radius, 12px);
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: var(--shop-shadow);
  border: 1px solid var(--shop-border);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--shop-border);
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 13px;
  color: var(--shop-text-secondary);
}

.order-total-row {
  text-align: right;
  padding-top: 16px;
  font-size: 14px;
}

.total-price {
  font-size: 22px;
  font-weight: 800;
  color: var(--shop-accent, #f97316);
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.review-product-name {
  font-weight: 500;
  margin-bottom: 16px;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}
</style>
