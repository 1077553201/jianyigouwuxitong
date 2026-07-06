<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '3-20位字母数字', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '6-20位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_r: any, value: string, callback: Function) => {
        if (value !== form.password) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^\d{11}$/, message: '11位数字', trigger: 'blur' },
  ],
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    await register({
      username: form.username,
      password: form.password,
      email: form.email,
      phone: form.phone,
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '注册失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <!-- Left brand panel -->
    <div class="brand-panel">
      <div class="brand-panel-inner">
        <div class="brand-logo">
          <el-icon :size="36" color="#ffffff"><ShoppingCart /></el-icon>
          <span class="brand-name">Mh平台</span>
        </div>
        <h2 class="brand-slogan">加入我们，<br />发现好物世界</h2>
        <p class="brand-desc">注册即可享受专属优惠，开启你的品质购物之旅</p>
        <div class="brand-deco"></div>
      </div>
    </div>

    <!-- Right form area -->
    <div class="form-panel">
      <div class="form-inner">
        <div class="form-header">
          <h1 class="form-title">创建账号</h1>
          <p class="form-subtitle">填写以下信息完成注册</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @keyup.enter="handleSubmit"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="3-20位字母数字"
              size="large"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="6-20位"
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="再次输入密码"
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="form.email"
              placeholder="your@email.com"
              size="large"
            />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="11位手机号"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="submitting"
              style="width: 100%"
              @click="handleSubmit"
            >
              注 册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          已有账号？
          <router-link to="/login" class="form-link">去登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  min-height: calc(100vh - 64px);
}

/* ── Left brand panel ──────────────────────────────── */
.brand-panel {
  flex: 0 0 45%;
  background:
    radial-gradient(ellipse 70% 60% at 30% 50%, rgba(91, 79, 232, 0.5) 0%, transparent 60%),
    radial-gradient(ellipse 50% 70% at 80% 80%, rgba(124, 111, 245, 0.2) 0%, transparent 50%),
    linear-gradient(150deg, #0c1120 0%, #131a35 50%, #1a1545 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  position: relative;
  overflow: hidden;
}

.brand-panel-inner {
  max-width: 320px;
  position: relative;
  z-index: 1;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 40px;
}

.brand-name {
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.5px;
}

.brand-slogan {
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
  line-height: 1.4;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
}

.brand-desc {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.7;
}

.brand-deco {
  width: 48px;
  height: 3px;
  background: linear-gradient(90deg, #818cf8, #c4b5fd);
  border-radius: 2px;
  margin-top: 32px;
  opacity: 0.6;
}

/* ── Right form panel ──────────────────────────────── */
.form-panel {
  flex: 1;
  background: var(--shop-surface);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
}

.form-inner {
  width: 100%;
  max-width: 380px;
}

.form-header {
  margin-bottom: 36px;
}

.form-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--shop-text);
  margin-bottom: 6px;
  letter-spacing: -0.5px;
}

.form-subtitle {
  font-size: 14px;
  color: var(--shop-text-secondary);
}

.form-footer {
  text-align: center;
  font-size: 14px;
  color: var(--shop-text-secondary);
  margin-top: 20px;
}

.form-link {
  color: var(--shop-primary);
  font-weight: 500;
  text-decoration: none;
}

.form-link:hover {
  color: var(--shop-primary-hover);
}

/* ── Mobile ────────────────────────────────────────── */
@media (max-width: 768px) {
  .auth-page {
    flex-direction: column;
  }

  .brand-panel {
    flex: 0 0 auto;
    padding: 40px 24px;
    display: none;
  }

  .form-panel {
    padding: 40px 20px;
  }
}
</style>
