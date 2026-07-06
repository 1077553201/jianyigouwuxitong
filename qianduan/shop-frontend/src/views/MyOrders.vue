<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrders, cancelOrder } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import EmptyState from '@/components/EmptyState.vue'
import Pagination from '@/components/Pagination.vue'

interface OrderItem {
  id: number
  productId: number
  productName: string
  price: number
  quantity: number
  imageUrl?: string
}

interface Order {
  id: number
  orderNo?: string
  status: string
  totalPrice: number
  createdAt: string
  items?: OrderItem[]
  orderItems?: OrderItem[]
}

const router = useRouter()
const activeTab = ref('all')
const orders = ref<Order[]>([])
const total = ref(0)
const page = ref(0)
const pageSize = 10
const loading = ref(false)

function toNum(v: any): number {
  const n = Number(v)
  return isNaN(n) ? 0 : n
}

function getPrice(row: any): number {
  return toNum(row.totalPrice ?? row.totalAmount ?? row.total_price ?? row.amount ?? row.money)
}

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待发货', value: 'pending' },
  { label: '已发货', value: 'shipped' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
]

const statusMap: Record<string, { text: string; type: string }> = {
  pending: { text: '待发货', type: 'warning' },
  shipped: { text: '已发货', type: '' },
  completed: { text: '已完成', type: 'success' },
  cancelled: { text: '已取消', type: 'info' },
}

async function fetchOrders() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: page.value, size: pageSize }
    const res: any = await getOrders(params)
    const data = res.data ?? res
    let list: any[] = data.content ?? data ?? []

    // Frontend filter by status if backend doesn't support it
    if (activeTab.value !== 'all') {
      list = list.filter((o: any) => o.status === activeTab.value)
    }

    // Normalize item fields: backend may nest product info under item.product
    orders.value = list.map((order: any) => ({
      ...order,
      items: (order.items || order.orderItems || []).map((item: any) => {
        const p = item.product ?? {}
        return {
          id: item.id,
          productId: item.productId ?? p.id,
          productName: p.name ?? item.productName ?? item.product_name ?? '未知商品',
          price: toNum(p.price ?? item.price),
          quantity: toNum(item.quantity) || 1,
          imageUrl: p.imageUrl ?? item.imageUrl ?? p.image_url ?? item.image_url,
        }
      }),
    }))
    total.value = data.totalElements ?? data.total ?? 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(fetchOrders)
watch([activeTab, page], fetchOrders)

function handlePageChange(p: number) {
  page.value = p - 1
}

async function handleCancel(order: Order) {
  try {
    await ElMessageBox.confirm('确定取消该订单？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch {
    // user cancelled or error
  }
}

function getStatusText(status: string) {
  return statusMap[status]?.text || status
}

function getStatusType(status: string) {
  return statusMap[status]?.type || ''
}
</script>

<template>
  <div class="orders-page">
    <div class="orders-inner">
      <h1 class="page-title">我的订单</h1>

      <el-tabs v-model="activeTab" class="order-tabs">
        <el-tab-pane
          v-for="tab in tabs"
          :key="tab.value"
          :label="tab.label"
          :name="tab.value"
        />
      </el-tabs>

      <div v-loading="loading" min-height="300">
        <div v-if="orders.length" class="order-list">
          <div v-for="order in orders" :key="order.id" class="order-card">
            <div class="order-header">
              <span class="order-no">订单号：#{{ order.orderNo || order.id }}</span>
              <span class="order-date">{{ order.createdAt?.slice(0, 16)?.replace('T', ' ') }}</span>
              <el-tag :type="getStatusType(order.status) as any" size="small">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
            <div class="order-items">
              <div
                v-for="item in (order.items || order.orderItems || [])"
                :key="item.id"
                class="order-item"
              >
                <div class="oi-image">
                  <img :src="item.imageUrl || 'https://placehold.co/50x50/f1f3f9/6b7280?text=IMG'" :alt="item.productName" />
                </div>
                <div class="oi-info">
                  <span class="oi-name">{{ item.productName }}</span>
                  <span class="oi-price">¥{{ toNum(item.price).toFixed(2) }}</span>
                </div>
                <span class="oi-qty">× {{ item.quantity }}</span>
              </div>
            </div>
            <div class="order-footer">
              <span class="order-total">合计：¥{{ getPrice(order).toFixed(2) }}</span>
              <div class="order-actions">
                <el-button size="small" @click="router.push(`/orders/${order.id}`)">查看详情</el-button>
                <el-button
                  v-if="order.status === 'pending'"
                  type="danger"
                  size="small"
                  @click="handleCancel(order)"
                >
                  取消订单
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <EmptyState v-else-if="!loading" icon="Document" text="暂无订单">
          <el-button type="primary" @click="router.push('/products')">去购物</el-button>
        </EmptyState>
      </div>

      <Pagination
        :current-page="page + 1"
        :page-size="pageSize"
        :total="total"
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.orders-page {
  padding: 24px;
}

.orders-inner {
  max-width: 900px;
  margin: 0 auto;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 16px;
}

.order-tabs {
  margin-bottom: 16px;
}

.order-card {
  background: #fff;
  border-radius: var(--shop-radius, 12px);
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: var(--shop-shadow);
  border: 1px solid var(--shop-border);
}

.order-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--shop-border);
}

.order-no {
  font-weight: 600;
  font-size: 14px;
}

.order-date {
  font-size: 13px;
  color: var(--shop-text-secondary);
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.oi-image {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  overflow: hidden;
  background: #f0f0f0;
  flex-shrink: 0;
}

.oi-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.oi-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.oi-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--shop-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.oi-price {
  font-size: 13px;
  color: var(--shop-accent, #f97316);
  font-weight: 600;
}

.oi-qty {
  font-size: 13px;
  color: var(--shop-text-secondary);
  flex-shrink: 0;
}

.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--shop-border);
}

.order-total {
  font-size: 16px;
  font-weight: 600;
  color: var(--shop-accent, #f97316);
}

.order-actions {
  display: flex;
  gap: 8px;
}
</style>
