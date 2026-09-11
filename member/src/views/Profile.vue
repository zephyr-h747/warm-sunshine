<template>
  <div class="shell profile-shell">
    <div class="page-body">
      <van-nav-bar title="个人中心" left-arrow @click-left="$router.back()" />

      <!-- 头部 -->
      <div class="hero fade-up">
        <div class="hero-deco deco-1"></div>
        <div class="hero-deco deco-2"></div>
        <div class="hero-row">
          <div class="avatar-wrap">
            <van-image round width="64" height="64" :src="form.avatar || 'https://img.yzcdn.cn/vant/cat.jpeg'" />
            <span class="level-chip" :class="form.memberLevel || 'NORMAL'">{{ levelText(form.memberLevel) }}</span>
          </div>
          <div class="hero-info">
            <p class="hero-name">{{ form.realName || form.phone || '未设置昵称' }}</p>
            <p class="hero-phone">{{ maskPhone(form.phone) }}</p>
          </div>
        </div>
        <div class="hero-points">
          <div class="hp-item">
            <p class="hp-num">{{ points }}</p>
            <p class="hp-label">可用积分</p>
          </div>
          <i class="hp-divider"></i>
          <div class="hp-item">
            <p class="hp-num">{{ transactions.length }}</p>
            <p class="hp-label">积分笔数</p>
          </div>
          <i class="hp-divider"></i>
          <div class="hp-item">
            <p class="hp-num">{{ earnedTotal }}</p>
            <p class="hp-label">累计获得</p>
          </div>
        </div>
      </div>

      <!-- 积分明细 -->
      <div class="sec-head fade-up d1">
        <div class="sec-title">积分明细</div>
        <span class="sec-more" @click="loadTransactions">刷新</span>
      </div>
      <div class="app-card pts-card fade-up d1">
        <template v-if="transactions.length">
          <div v-for="t in transactions" :key="t.id" class="pt-row">
            <div class="pt-icon" :class="t.amount >= 0 ? 'plus' : 'minus'">
              <van-icon :name="t.amount >= 0 ? 'arrow-down' : 'arrow-up'" />
            </div>
            <div class="pt-body">
              <p class="pt-remark">{{ t.remark || sourceText(t.sourceType) }}</p>
              <p class="pt-time">{{ fmtFriendly(t.createTime) }}</p>
            </div>
            <p class="pt-amount" :class="t.amount >= 0 ? 'plus' : 'minus'">
              {{ t.amount >= 0 ? '+' : '' }}{{ t.amount }}
            </p>
          </div>
        </template>
        <p v-else class="pts-empty">暂无积分记录，参加活动、完成评测可获得积分</p>
      </div>

      <!-- 资料编辑 -->
      <div class="sec-head fade-up d2">
        <div class="sec-title">基本信息</div>
      </div>
      <div class="app-card form-card fade-up d2">
        <div class="form-row">
          <label>姓名</label>
          <input v-model="form.realName" type="text" placeholder="请输入真实姓名" />
        </div>
        <div class="form-row">
          <label>性别</label>
          <div class="seg">
            <button type="button" class="seg-btn" :class="{ on: form.gender === 'MALE' }" @click="form.gender = 'MALE'">男</button>
            <button type="button" class="seg-btn" :class="{ on: form.gender === 'FEMALE' }" @click="form.gender = 'FEMALE'">女</button>
          </div>
        </div>
        <div class="form-row">
          <label>身高</label>
          <input v-model="form.height" type="number" inputmode="decimal" placeholder="cm，如 165" />
        </div>
        <div class="form-row">
          <label>紧急联系人</label>
          <input v-model="form.emergencyContact" type="text" placeholder="如：张三 13800000000" />
        </div>
        <van-button round block class="save-btn" :loading="saving" @click="onSave">保存信息</van-button>
      </div>

      <!-- 其他操作 -->
      <div class="app-card op-card fade-up d3">
        <button class="op-row press" type="button" @click="showPwd = true">
          <span class="op-icon lock"><van-icon name="lock" /></span>
          <span class="op-label">修改密码</span>
          <van-icon name="arrow" class="op-arrow" />
        </button>
        <button class="op-row press danger" type="button" @click="onLogout">
          <span class="op-icon logout"><van-icon name="revoke" /></span>
          <span class="op-label">退出登录</span>
          <van-icon name="arrow" class="op-arrow" />
        </button>
      </div>

      <!-- 修改密码弹窗 -->
      <van-dialog
        v-model:show="showPwd"
        title="修改密码"
        show-cancel-button
        confirm-button-text="确认修改"
        @confirm="onChangePwd"
      >
        <div class="pwd-form">
          <van-field v-model="pwdForm.oldPassword" type="password" label="旧密码" placeholder="请输入旧密码" />
          <van-field v-model="pwdForm.newPassword" type="password" label="新密码" placeholder="8-32位含字母数字" />
        </div>
      </van-dialog>
    </div>
    <TabBar />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast, showToast } from 'vant'
import TabBar from '../components/TabBar.vue'
import { getProfile, updateProfile, changePassword } from '../api/profile'
import { listPointTransactions } from '../api/points'
import { useUserStore } from '../stores/user'
import { LEVEL_NAMES, fmtFriendly, maskPhone } from '../utils/format'

const router = useRouter()
const userStore = useUserStore()
const form = reactive({ realName: '', gender: '', height: '', emergencyContact: '', avatar: '', phone: '', memberLevel: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '' })
const saving = ref(false)
const showPwd = ref(false)
const transactions = ref([])

const points = computed(() => userStore.userInfo?.points ?? '--')

const earnedTotal = computed(() =>
  transactions.value.filter((t) => t.amount > 0).reduce((s, t) => s + t.amount, 0)
)

function levelText(l) {
  return (LEVEL_NAMES[l] || '普通会员').replace('会员', '')
}

function sourceText(s) {
  return { ACTIVITY: '社区活动', ASSESSMENT: '健康评测', APPOINTMENT: '体检预约', REGISTER: '注册奖励', ADMIN: '管理员调整' }[s] || '积分变动'
}

async function loadTransactions() {
  try {
    const data = await listPointTransactions({ pageNum: 1, pageSize: 10 })
    transactions.value = data?.list || []
  } catch (e) { /* 忽略 */ }
}

async function onSave() {
  saving.value = true
  try {
    await updateProfile({
      realName: form.realName,
      gender: form.gender,
      height: form.height ? Number(form.height) : null,
      emergencyContact: form.emergencyContact
    })
    showSuccessToast('保存成功')
    userStore.refreshUserInfo()
  } catch (e) { /* 统一提示 */ } finally {
    saving.value = false
  }
}

async function onChangePwd() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    showToast('请填写完整')
    return
  }
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    showSuccessToast('修改成功，请重新登录')
    pwdForm.oldPassword = pwdForm.newPassword = ''
    showPwd.value = false
    await userStore.logout()
    router.replace('/login')
  } catch (e) { /* 统一提示 */ }
}

async function onLogout() {
  try {
    await showConfirmDialog({ title: '退出登录', message: '确定退出登录吗？', confirmButtonText: '退出' })
  } catch (e) {
    return
  }
  await userStore.logout()
  router.replace('/login')
}

onMounted(async () => {
  try {
    const data = await getProfile()
    Object.assign(form, {
      realName: data.realName || '',
      gender: data.gender || '',
      height: data.height || '',
      emergencyContact: data.emergencyContact || '',
      avatar: data.avatar || '',
      phone: data.phone,
      memberLevel: data.memberLevel
    })
  } catch (e) { /* 统一提示 */ }
  loadTransactions()
})
</script>

<style scoped>
.profile-shell {
  background:
    radial-gradient(90% 24% at 50% 0%, rgba(0, 180, 134, 0.08), transparent 70%),
    var(--bg);
}

/* 头部 */
.hero {
  position: relative;
  margin: 8px 14px 6px;
  border-radius: var(--r-lg);
  background: linear-gradient(150deg, #00926e 0%, #00b486 50%, #3ad6a8 100%);
  padding: 20px 18px 18px;
  overflow: hidden;
  color: #fff;
  box-shadow: 0 12px 28px rgba(0, 180, 134, 0.3);
}

.hero-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}

.deco-1 { width: 130px; height: 130px; top: -55px; right: -35px; }
.deco-2 { width: 60px; height: 60px; bottom: -24px; left: 18%; opacity: 0.7; }

.hero-row {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.avatar-wrap .level-chip {
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  box-shadow: 0 3px 8px rgba(0, 60, 45, 0.35);
}

.hero-info {
  flex: 1;
  min-width: 0;
}

.hero-name {
  margin: 0;
  font-size: 21px;
  font-weight: 800;
  letter-spacing: 1px;
}

.hero-phone {
  margin: 5px 0 0;
  font-size: 13px;
  opacity: 0.85;
}

/* 统计 */
.hero-points {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1px 1fr 1px 1fr;
  align-items: center;
  margin-top: 18px;
  padding: 12px 0 4px;
  border-top: 1px solid rgba(255, 255, 255, 0.18);
}

.hp-item {
  text-align: center;
}

.hp-num {
  margin: 0;
  font-size: 21px;
  font-weight: 800;
}

.hp-label {
  margin: 3px 0 0;
  font-size: 11px;
  opacity: 0.8;
}

.hp-divider {
  height: 26px;
  background: rgba(255, 255, 255, 0.18);
}

/* 积分明细 */
.pts-card {
  padding: 4px 16px;
}

.pt-row {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 12px 0;
}

.pt-row + .pt-row {
  border-top: 1px solid var(--line);
}

.pt-icon {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  flex-shrink: 0;
}

.pt-icon.plus { background: var(--brand-soft); color: var(--brand-deep); }
.pt-icon.minus { background: var(--sun-soft); color: var(--sun); }

.pt-body {
  flex: 1;
  min-width: 0;
}

.pt-remark {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pt-time {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--muted);
}

.pt-amount {
  margin: 0;
  font-size: 17px;
  font-weight: 800;
  flex-shrink: 0;
}

.pt-amount.plus { color: var(--brand-deep); }
.pt-amount.minus { color: var(--sun); }

.pts-empty {
  margin: 0;
  padding: 10px 0;
  font-size: 13px;
  color: var(--muted);
  text-align: center;
}

/* 资料表单 */
.form-card {
  padding: 6px 16px 18px;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 0;
}

.form-row + .form-row {
  border-top: 1px solid var(--line);
}

.form-row label {
  width: 88px;
  font-size: 15px;
  color: var(--ink-2);
  flex-shrink: 0;
}

.form-row input {
  flex: 1;
  border: none;
  outline: none;
  background: var(--bg);
  border-radius: 11px;
  padding: 9px 13px;
  font-size: 15px;
  color: var(--ink);
  min-width: 0;
}

.form-row input::placeholder {
  color: var(--muted);
}

/* 性别分段 */
.seg {
  display: flex;
  background: var(--bg);
  border-radius: 11px;
  padding: 3px;
}

.seg-btn {
  border: none;
  background: none;
  font-size: 14px;
  color: var(--muted);
  padding: 7px 18px;
  border-radius: 9px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.seg-btn.on {
  background: #fff;
  color: var(--brand-deep);
  font-weight: 700;
  box-shadow: var(--shadow-1);
}

.save-btn {
  margin-top: 16px;
  border: none;
  background: var(--brand-grad);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  box-shadow: 0 10px 22px rgba(0, 180, 134, 0.3);
}

/* 操作 */
.op-card {
  padding: 2px 16px;
  margin-bottom: 24px;
}

.op-row {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  border: none;
  background: none;
  padding: 14px 0;
  text-align: left;
  cursor: pointer;
}

.op-row + .op-row {
  border-top: 1px solid var(--line);
}

.op-icon {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  flex-shrink: 0;
}

.op-icon.lock { background: rgba(63, 124, 255, 0.1); color: var(--sky); }
.op-icon.logout { background: rgba(255, 91, 77, 0.09); color: var(--danger); }

.op-label {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: var(--ink);
}

.op-row.danger .op-label {
  color: var(--danger);
}

.op-arrow {
  color: #c3ccd6;
}

/* 密码弹窗 */
.pwd-form {
  padding: 10px 0 16px;
}
</style>
