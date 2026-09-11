<template>
  <div class="aurora auth-page">
    <div class="auth-head fade-up">
      <van-icon name="arrow-left" class="back-ic" @click="$router.back()" />
      <div>
        <h1 class="head-title">找回密码</h1>
        <p class="head-sub">通过注册手机号验证身份后重置</p>
      </div>
    </div>

    <div class="auth-card fade-up d1">
      <van-form @submit="onSubmit">
        <van-cell-group inset :border="false" class="field-group">
          <van-field
            v-model="form.phone"
            type="digit"
            maxlength="11"
            left-icon="phone-o"
            placeholder="请输入注册手机号"
            :rules="phoneRules"
          />
          <van-field
            v-model="form.code"
            type="digit"
            maxlength="6"
            center
            left-icon="envelop-o"
            placeholder="短信验证码"
            :rules="[{ required: true, message: '请输入验证码' }]"
          >
            <template #button>
              <button
                type="button"
                class="code-btn press"
                :disabled="counting > 0 || sending"
                @click="onSendCode"
              >
                {{ counting > 0 ? `${counting}s 后重发` : '获取验证码' }}
              </button>
            </template>
          </van-field>
          <van-field
            v-model="form.newPassword"
            :type="showPwd ? 'text' : 'password'"
            left-icon="shield-o"
            placeholder="新密码（8-32位，含字母和数字）"
            :right-icon="showPwd ? 'eye-o' : 'closed-eye'"
            @click-right-icon="showPwd = !showPwd"
            :rules="pwdRules"
          />
        </van-cell-group>

        <van-button round block type="primary" native-type="submit" class="submit-btn" :loading="loading">
          重置密码
        </van-button>
      </van-form>
    </div>

    <div class="auth-foot fade-up d2">
      想起密码了？
      <span class="link" @click="$router.push('/login')">返回登录</span>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { sendSmsCode, resetPassword } from '../api/auth'

const router = useRouter()

const loading = ref(false)
const sending = ref(false)
const counting = ref(0)
const showPwd = ref(false)
const form = reactive({ phone: '', code: '', newPassword: '' })

const phoneRules = [
  { required: true, message: '请输入手机号' },
  { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
]

const pwdRules = [
  { required: true, message: '请设置新密码' },
  { pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,32}$/, message: '密码需 8-32 位且同时包含字母和数字' }
]

async function onSendCode() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    showSuccessToast('请先输入正确的手机号')
    return
  }
  sending.value = true
  try {
    await sendSmsCode(form.phone)
    showSuccessToast('验证码已发送')
    counting.value = 60
    const timer = setInterval(() => {
      counting.value--
      if (counting.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    /* 统一提示 */
  } finally {
    sending.value = false
  }
}

async function onSubmit() {
  loading.value = true
  try {
    await resetPassword({ ...form })
    showSuccessToast('密码已重置，请重新登录')
    router.replace('/login')
  } catch (e) {
    /* 统一提示 */
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

.auth-head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
}

.back-ic {
  font-size: 22px;
  color: var(--ink);
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  padding: 8px;
  cursor: pointer;
}

.head-title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: var(--ink);
}

.head-sub {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--ink-2);
}

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

.code-btn {
  border: none;
  background: var(--brand-soft);
  color: var(--brand-deep);
  font-size: 13px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 999px;
  cursor: pointer;
}

.code-btn:disabled {
  color: var(--muted);
  background: var(--line);
}

.submit-btn {
  background: var(--brand-grad);
  border: none;
  font-size: 17px;
  font-weight: 600;
  box-shadow: 0 10px 24px rgba(0, 180, 134, 0.32);
}

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
