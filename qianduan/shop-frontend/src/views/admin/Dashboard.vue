<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUsers } from '@/api/admin'
import { getProducts } from '@/api/product'
import { getAdminOrders } from '@/api/admin'

const userCount = ref(0)
const productCount = ref(0)
const recentOrders = ref<any[]>([])
const loading = ref(false)

function toNum(v: any): number {
  const n = Number(v)
  return isNaN(n) ? 0 : n
}

function getPrice(row: any): number {
  return toNum(row.totalPrice ?? row.totalAmount ?? row.total_price ?? row.amount ?? row.money)
}

const statusMap: Record<string, { text: string; type: string }> = {
  pending: { text: '待发货', type: 'warning' },
  shipped: { text: '已发货', type: '' },
  completed: { text: '已完成', type: 'success' },
  cancelled: { text: '已取消', type: 'info' },
}

onMounted(async () => {
  loading.value = true
  try {
    const [usersRes, productsRes, ordersRes]: any[] = await Promise.all([
      getUsers({ page: 0, size: 1 }),
      getProducts({ page: 0, size: 1 }),
      getAdminOrders({ page: 0, size: 10 }),
    ])
    userCount.value = usersRes.data?.totalElements ?? usersRes?.totalElements ?? 0
    productCount.value = productsRes.data?.totalElements ?? productsRes?.totalElements ?? 0
    const ordersData = ordersRes.data ?? ordersRes
    recentOrders.value = ordersData.content ?? ordersData ?? []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="dashboard" v-loading="loading">
    <h1 class="page-title">数据概览</h1>

    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6f7ff; color: #1890ff">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ userCount.toLocaleString() }}</span>
          <span class="stat-label">用户总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #fff7e6; color: #fa8c16">
          <el-icon :size="28"><Goods /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ productCount.toLocaleString() }}</span>
          <span class="stat-label">商品总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #f6ffed; color: #52c41a">
          <el-icon :size="28"><List /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ recentOrders.length }}</span>
          <span class="stat-label">近期订单</span>
        </div>
      </div>
    </div>

    <div class="recent-section">
      <h2 class="section-title">最近订单</h2>
      <el-table :data="recentOrders" style="width: 100%">
        <el-table-column label="订单号" width="120">
          <template #default="{ row }">#{{ row.orderNo || row.id }}</template>
        </el-table-column>
        <el-table-column prop="username" label="用户" width="120">
          <template #default="{ row }">{{ row.username || row.userId }}</template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">¥{{ getPrice(row).toFixed(2) }}</template>
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
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 24px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--shop-text);
  display: block;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--shop-text-secondary);
}

.recent-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}
</style>
