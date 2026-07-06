<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getProducts, getCategories } from '@/api/product'
import { createProduct, updateProduct, deleteProduct } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import type { FormInstance, FormRules } from 'element-plus'

interface Product {
  id: number
  name: string
  price: number
  stock: number
  imageUrl?: string
  description?: string
  categoryId?: number
}

interface Category {
  id: number
  name: string
}

const products = ref<Product[]>([])
const categories = ref<Category[]>([])
const total = ref(0)
const page = ref(0)
const pageSize = 10
const loading = ref(false)

// Dialog
const dialogVisible = ref(false)
const dialogTitle = ref('新增商品')
const submitting = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const form = reactive({
  name: '',
  categoryId: null as number | null,
  price: null as number | null,
  stock: null as number | null,
  imageUrl: '',
  description: '',
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
}

async function fetchProducts() {
  loading.value = true
  try {
    const res: any = await getProducts({ page: page.value, size: pageSize })
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
  fetchProducts()
  fetchCategories()
})

function handlePageChange(p: number) {
  page.value = p - 1
  fetchProducts()
}

function openAdd() {
  editingId.value = null
  dialogTitle.value = '新增商品'
  Object.assign(form, {
    name: '',
    categoryId: null,
    price: null,
    stock: null,
    imageUrl: '',
    description: '',
  })
  dialogVisible.value = true
}

function openEdit(product: Product) {
  editingId.value = product.id
  dialogTitle.value = '编辑商品'
  Object.assign(form, {
    name: product.name,
    categoryId: product.categoryId,
    price: product.price,
    stock: product.stock,
    imageUrl: product.imageUrl || '',
    description: product.description || '',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = { ...form }
    if (editingId.value) {
      await updateProduct(editingId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createProduct(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchProducts()
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(product: Product) {
  try {
    await ElMessageBox.confirm(`确定下架商品「${product.name}」？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteProduct(product.id)
    ElMessage.success('已下架')
    fetchProducts()
  } catch {
    // user cancelled or error
  }
}
</script>

<template>
  <div class="products-page">
    <div class="page-header">
      <h1 class="page-title">商品管理</h1>
      <el-button type="danger" @click="openAdd">
        <el-icon><Plus /></el-icon>
        新增商品
      </el-button>
    </div>

    <div class="table-card" v-loading="loading">
      <el-table :data="products" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名" min-width="150" />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            {{ categories.find(c => c.id === row.categoryId)?.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120">
          <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.stock > 0 ? 'success' : 'danger'" size="small">
              {{ row.stock > 0 ? '上架' : '已售罄' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">下架</el-button>
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

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.imageUrl" placeholder="留空使用占位图" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
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
</style>
