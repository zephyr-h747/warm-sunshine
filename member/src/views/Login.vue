<template>
  <div class="aurora auth-page">
    <!-- 品牌区 -->
    <div class="brand fade-up">
      <div class="brand-logo">
        <svg viewBox="0 0 48 48" class="logo-svg">
          <defs>
            <linearGradient id="loginSun" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0" stop-color="#00b486" />
              <stop offset="1" stop-color="#3ad6a8" />
            </linearGradient>
          </defs>
          <g stroke="url(#loginSun)" stroke-width="3.6" stroke-linecap="round">
            <line x1="24" y1="3" x2="24" y2="9" />
            <line x1="24" y1="39" x2="24" y2="45" />
            <line x1="3" y1="24" x2="9" y2="24" />
            <line x1="39" y1="24" x2="45" y2="24" />
            <line x1="9.2" y1="9.2" x2="13.4" y2="13.4" />
            <line x1="34.6" y1="34.6" x2="38.8" y2="38.8" />
            <line x1="9.2" y1="38.8" x2="13.4" y2="34.6" />
            <line x1="34.6" y1="13.4" x2="38.8" y2="9.2" />
          </g>
          <circle cx="24" cy="24" r="10" fill="url(#loginSun)" />
          <path d="M19 24.5c1.4 2.2 3 3.3 5 3.3s3.6-1.1 5-3.3" stroke="#fff" stroke-width="2.4" fill="none" stroke-linecap="round" />
        </svg>
      </div>
      <h1 class="brand-name">暖阳 · 智慧养老</h1>
      <p class="brand-slogan">让每一天都被温柔守护</p>
    </div>

    <!-- 登录卡片 -->
    <div class="auth-card fade-up d1">
      <van-form @submit="onSubmit">
        <van-cell-group inset :border="false" class="field-group">
          <van-field
            v-model="form.phone"
            name="phone"
            type="digit"
            maxlength="11"
            left-icon="phone-o"
            placeholder="请输入手机号"
            :rules="phoneRules"
          />
          <van-field
            v-model="form.password"
            name="password"
            :type="showPwd ? 'text' : 'password'"
            left-icon="shield-o"
            placeholder="请输入密码"
            :right-icon="showPwd ? 'eye-o' : 'closed-eye'"
            @click-right-icon="showPwd = !showPwd"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
        </van-cell-group>

        <div class="forgot-line" @click="$router.push('/forgot-password')">忘记密码？</div>

        <van-button round block type="primary" native-type="submit" class="submit-btn" :loading="loading">
          登 录
        </van-button>
      </van-form>
    </div>

    <!-- 注册引导 -->
    <div class="auth-foot fade-up d2">
      还没有账号？
      <span class="link" @click="$router.push('/register')">立即注册</span>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const showPwd = ref(false)
const form = reactive({ phone: '', password: '' })

const phoneRules = [
  { required: true, message: '请输入手机号' },
  { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
]

async function onSubmit() {
  loading.value = true
  try {
    await userStore.login({ ...form })
    showSuccessToast('欢迎回来')
    router.replace(route.query.redirect || '/')
  } catch (e) {
    /* axios 错误由 request 拦截器统一提示；本地业务校验错误（如角色不符）在此提示 */
    if (e && e.message && !(e.isAxiosError || e.response || e.config)) {
      showFailToast(e.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 24px;
}

/* 品牌区 */
.brand {
  text-align: center;
  margin-bottom: 28px;
  position: relative;
  z-index: 1;
}

.brand-logo {
  width: 76px;
  height: 76px;
  margin: 0 auto 14px;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(10px);
  box-shadow: 0 14px 34px rgba(0, 180, 134, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-svg {
  width: 52px;
  height: 52px;
}

.brand-name {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 2px;
  color: var(--ink);
}

.brand-slogan {
  margin: 8px 0 0;
  font-size: 14px;
  color: var(--ink-2);
  letter-spacing: 3px;
}

/* 登录卡片 */
.auth-card {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 22px 8px 18px;
  box-shadow: 0 18px 44px rgba(28, 43, 58, 0.1);
}

.field-group {
  margin: 0;
}

.field-group :deep(.van-field) {
  border-radius: 14px;
  background: rgba(244, 246, 249, 0.9);
  margin-bottom: 12px;
}

.field-group :deep(.van-cell::after) {
  display: none;
}

.field-group :deep(.van-field__left-icon) {
  color: var(--brand);
  font-size: 20px;
  margin-right: 6px;
}

.forgot-line {
  text-align: right;
  font-size: 14px;
  color: var(--muted);
  padding: 2px 14px 12px;
  cursor: pointer;
}

.submit-btn {
  background: var(--brand-grad);
  border: none;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 6px;
  box-shadow: 0 10px 24px rgba(0, 180, 134, 0.32);
}

/* 底部注册引导 */
.auth-foot {
  position: relative;
  z-index: 1;
  text-align: center;
  margin-top: 22px;
  font-size: 15px;
  color: var(--ink-2);
}

.auth-foot .link {
  color: var(--brand-deep);
  font-weight: 700;
  cursor: pointer;
}
</style>
