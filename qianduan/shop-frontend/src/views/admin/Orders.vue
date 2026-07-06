<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { getAdminOrders, updateOrderStatus } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'

interface Order {
  id: number
  orderNo?: string
  username?: string
  userId?: number
  totalPrice: number
  phone: string
  status: string
  createdAt: string
}

const orders = ref<Order[]>([])
const total = ref(0)
const page = ref(0)
const pageSize = 10
const loading = ref(false)
const statusFilter = ref('')

function toNum(v: any): number {
  const n = Number(v)
  return isNaN(n) ? 0 : n
}

function getPrice(row: any): number {
  return toNum(row.totalPrice ?? row.totalAmount ?? row.total_price ?? row.amount ?? row.money)
}

const statusOptions = [
  { label: '全部', value: '' },
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
    if (statusFilter.value) params.status = statusFilter.value
    const res: any = await getAdminOrders(params)
    const data = res.data ?? res
    orders.value = data.content ?? data ?? []
    total.value = data.totalElements ?? data.total ?? 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(fetchOrders)
watch([statusFilter, page], () => {
  page.value = 0
  fetchOrders()
})

function handlePageChange(p: number) {
  page.value = p - 1
  fetchOrders()
}

async function handleShip(order: Order) {
  try {
    await ElMessageBox.confirm(`确定发货订单 #${order.orderNo || order.id}？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await updateOrderStatus(order.id, { status: 'shipped' })
    ElMessage.success('已发货')
    fetchOrders()
  } catch {
    // user cancelled or error
  }
}

async function handleComplete(order: Order) {
  try {
    await ElMessageBox.confirm(`确定完成订单 #${order.orderNo || order.id}？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await updateOrderStatus(order.id, { status: 'completed' })
    ElMessage.success('已完成')
    fetchOrders()
  } catch {
    // user cancelled or error
  }
}
</script>

<template>
  <div class="orders-page">
    <div class="page-header">
      <h1 class="page-title">订单管理</h1>
      <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 140px">
        <el-option
          v-for="opt in statusOptions"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
    </div>

    <div class="table-card" v-loading="loading">
      <el-table :data="orders" style="width: 100%">
        <el-table-column label="订单号" width="120">
          <template #default="{ row }">#{{ row.orderNo || row.id }}</template>
        </el-table-column>
        <el-table-column label="用户" width="120">
          <template #default="{ row }">{{ row.username || row.userId }}</template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">¥{{ getPrice(row).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="收货人" width="120">
          <template #default="{ row }">{{ row.phone }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="(statusMap[row.status]?.type || '') as any" size="small">
              {{ statusMap[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间">
          <template #default="{ row }">
            {{ row.createdAt?.slice(0, 16)?.replace('T', ' ') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="primary"
              text
              size="small"
              @click="handleShip(row)"
            >
              发货
            </el-button>
            <el-button
              v-if="row.status === 'shipped'"
              type="success"
              text
              size="small"
              @click="handleComplete(row)"
            >
              确认完成
            </el-button>
            <span
              v-if="row.status === 'completed' || row.status === 'cancelled'"
              class="no-action"
            >
              —
            </span>
          </template>
        </el-table-column>
      </el-table>

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

.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.no-action {
  color: var(--shop-text-secondary);
  font-size: 13px;
}
</style>
