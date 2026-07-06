<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getProfile, updateProfile } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const auth = useAuthStore()
const formRef = ref<FormInstance>()
const saving = ref(false)

const profile = ref({
  username: '',
  role: '',
  createdAt: '',
  email: '',
  phone: '',
})

const rules: FormRules = {
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  phone: [
    { pattern: /^\d{11}$/, message: '11位数字', trigger: 'blur' },
  ],
}

onMounted(async () => {
  try {
    const res: any = await getProfile()
    const data = res.data ?? res
    profile.value = {
      username: data.username || '',
      role: data.role || '',
      createdAt: data.createdAt?.slice(0, 10) || '',
      email: data.email || '',
      phone: data.phone || '',
    }
  } catch {
    // handled by interceptor
  }
})

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()
  saving.value = true
  try {
    await updateProfile({ email: profile.value.email, phone: profile.value.phone })
    await auth.fetchProfile()
    ElMessage.success('修改成功')
  } catch {
    // handled by interceptor
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="profile-page">
    <div class="profile-inner">
      <h1 class="page-title">个人信息</h1>

      <div class="profile-card">
        <div class="readonly-section">
          <div class="info-row">
            <span class="label">用户名</span>
            <span class="value">{{ profile.username }}</span>
          </div>
          <div class="info-row">
            <span class="label">角色</span>
            <span class="value">{{ profile.role === 'admin' ? '管理员' : '普通用户' }}</span>
          </div>
          <div class="info-row" v-if="profile.createdAt">
            <span class="label">注册时间</span>
            <span class="value">{{ profile.createdAt }}</span>
          </div>
        </div>

        <el-divider />

        <h3 class="edit-title">可修改信息</h3>
        <el-form
          ref="formRef"
          :model="profile"
          :rules="rules"
          label-width="60px"
          style="max-width: 400px"
        >
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="profile.email" placeholder="your@email.com" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="profile.phone" placeholder="11位手机号" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 24px;
}

.profile-inner {
  max-width: 600px;
  margin: 0 auto;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 20px;
}

.profile-card {
  background: #fff;
  border-radius: var(--shop-radius, 12px);
  padding: 32px;
  box-shadow: var(--shop-shadow);
  border: 1px solid var(--shop-border);
}

.readonly-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.info-row .label {
  width: 70px;
  font-size: 14px;
  color: var(--shop-text-secondary);
  flex-shrink: 0;
}

.info-row .value {
  font-size: 14px;
  font-weight: 500;
}

.edit-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
}
</style>
