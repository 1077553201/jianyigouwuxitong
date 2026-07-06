<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUsers, updateUserStatus } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'

interface User {
  id: number
  username: string
  email: string
  phone: string
  role: string
  status: number
  createdAt: string
}

const users = ref<User[]>([])
const total = ref(0)
const page = ref(0)
const pageSize = 10
const loading = ref(false)

async function fetchUsers() {
  loading.value = true
  try {
    const res: any = await getUsers({ page: page.value, size: pageSize })
    const data = res.data ?? res
    users.value = data.content ?? data ?? []
    total.value = data.totalElements ?? data.total ?? 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(fetchUsers)

function handlePageChange(p: number) {
  page.value = p - 1
  fetchUsers()
}

async function toggleStatus(user: User) {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}用户 ${user.username}？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await updateUserStatus(user.id, { status: newStatus })
    ElMessage.success(`已${action}`)
    fetchUsers()
  } catch {
    // user cancelled or error
  }
}
</script>

<template>
  <div class="users-page">
    <h1 class="page-title">用户管理</h1>

    <div class="table-card" v-loading="loading">
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              text
              size="small"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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
.page-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 20px;
}

.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}
</style>
