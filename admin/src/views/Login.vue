<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-glow glow-1"></div>
    <div class="bg-glow glow-2"></div>
    <div class="bg-grid"></div>

    <div class="login-card">
      <div class="brand">
        <div class="brand-logo">
          <el-icon :size="30"><Sunrise /></el-icon>
        </div>
        <h1 class="brand-title">智慧养老社区</h1>
        <p class="brand-sub">ELDERCARE · ADMIN CONSOLE</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="管理员手机号" :prefix-icon="User" maxlength="11" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="submit" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="tip">仅限管理员账号（ADMIN 角色）登录</div>
    </div>

    <p class="footer">© 2026 智慧养老社区 · 用心守护每一位长者</p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ phone: '', password: '' })

const rules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login({ ...form })
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  overflow: hidden;
  background: linear-gradient(150deg, #0a2e4a 0%, #10456b 45%, #17638f 100%);
}

/* 光斑 */
.bg-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.5;
  animation: float 9s ease-in-out infinite;
}

.glow-1 {
  width: 420px;
  height: 420px;
  background: #1e9f78;
  top: -120px;
  left: -100px;
}

.glow-2 {
  width: 380px;
  height: 380px;
  background: #3f7cff;
  bottom: -110px;
  right: -80px;
  animation-delay: 4.5s;
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(30px, 24px) scale(1.08);
  }
}

/* 网格底纹 */
.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px);
  background-size: 44px 44px;
  mask-image: radial-gradient(ellipse 70% 60% at 50% 45%, #000 30%, transparent 75%);
}

/* 登录卡 */
.login-card {
  position: relative;
  width: 400px;
  max-width: calc(100vw - 40px);
  padding: 40px 38px 30px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.97);
  box-shadow:
    0 24px 60px rgba(4, 22, 38, 0.45),
    0 0 0 1px rgba(255, 255, 255, 0.16) inset;
  animation: rise 0.7s cubic-bezier(0.22, 0.85, 0.32, 1) both;
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(26px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 品牌 */
.brand {
  text-align: center;
  margin-bottom: 28px;
}

.brand-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(135deg, #0e9f78, #13c291);
  box-shadow: 0 12px 26px rgba(14, 159, 120, 0.4);
  margin-bottom: 14px;
}

.brand-title {
  margin: 0;
  font-size: 23px;
  font-weight: 800;
  color: #173049;
  letter-spacing: 1px;
}

.brand-sub {
  margin: 8px 0 0;
  font-size: 11px;
  letter-spacing: 3px;
  color: #93a7ba;
}

/* 表单 */
:deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 14px;
  box-shadow: 0 0 0 1.5px #dfe7ef inset;
  transition: box-shadow 0.2s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1.5px #bcd6e8 inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px #13c291 inset, 0 4px 14px rgba(19, 194, 145, 0.16);
}

.submit {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 6px;
  background: linear-gradient(135deg, #0e9f78, #13c291);
  box-shadow: 0 10px 22px rgba(14, 159, 120, 0.36);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.submit:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 28px rgba(14, 159, 120, 0.46);
}

.submit:active {
  transform: translateY(0);
}

.tip {
  font-size: 12px;
  color: #93a7ba;
  text-align: center;
  margin-top: 2px;
}

/* 底部 */
.footer {
  position: relative;
  margin-top: 34px;
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, 0.55);
}
</style>
