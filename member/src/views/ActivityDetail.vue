<template>
  <div class="shell detail-shell">
    <div class="page-body">
      <van-nav-bar title="活动详情" left-arrow @click-left="$router.back()" />

      <div v-if="act" class="detail-body">
        <!-- 渐变头 -->
        <div class="hero fade-up" :class="'hero-' + (act.id % 4)">
          <div class="hero-deco deco-1"></div>
          <div class="hero-deco deco-2"></div>
          <div class="hero-icon"><van-icon name="flag-o" /></div>
          <p class="hero-title">{{ act.title }}</p>
          <span class="hero-status" :style="statusStyle(act.status)">{{ statusText(act.status) }}</span>
        </div>

        <!-- 关键信息 -->
        <div class="app-card info-card fade-up d1">
          <div class="info-row">
            <span class="info-label"><van-icon name="clock-o" /> 活动时间</span>
            <span class="info-value">{{ fmtDateTime(act.activityStart) }} ~ {{ fmtDateTime(act.activityEnd) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label"><van-icon name="edit" /> 报名时间</span>
            <span class="info-value">{{ fmtDateTime(act.registrationStart) }} ~ {{ fmtDateTime(act.registrationEnd) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label"><van-icon name="friends-o" /> 剩余名额</span>
            <span class="info-value">
              <b :class="{ hot: act.remaining <= 3 }">{{ act.remaining }}</b> / {{ act.maxParticipants }}
            </span>
          </div>
          <div class="cap-track">
            <i
              class="cap-bar"
              :class="{ hot: filledRate >= 0.8 }"
              :style="{ width: filledRate * 100 + '%' }"
            ></i>
          </div>
        </div>

        <!-- 活动介绍 -->
        <div class="app-card content-card fade-up d2">
          <p class="card-title">活动介绍</p>
          <p class="content-text">{{ act.content }}</p>
        </div>

        <!-- 报名状态 -->
        <div v-if="act.registered && act.status === 'REGISTRATING'" class="checked-banner fade-up d3">
          <van-icon name="checked" />
          您已成功报名，活动开始后可现场签到
        </div>

        <!-- 签到状态 -->
        <div v-if="checkinStatus === 'CHECKED_IN'" class="checked-banner fade-up d3">
          <van-icon name="checked" />
          您已签到，感谢参与！
        </div>

        <!-- 底部操作 -->
        <div class="cta-bar">
          <div class="cta-info">
            <p class="cta-main">{{ ctaMainText }}</p>
            <p class="cta-sub">{{ ctaSubText }}</p>
          </div>
          <van-button
            v-if="act.status === 'REGISTRATING' && !act.registered"
            round
            class="cta-btn"
            :loading="busy"
            @click="onRegister"
          >
            立即报名
          </van-button>
          <van-button
            v-else-if="act.status === 'REGISTRATING'"
            round
            class="cta-btn cta-done"
            disabled
          >
            已报名
          </van-button>
          <van-button
            v-else-if="act.status === 'IN_PROGRESS' && act.registered && checkinStatus !== 'CHECKED_IN'"
            round
            class="cta-btn"
            :loading="busy"
            @click="onCheckin"
          >
            现场签到
          </van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { showConfirmDialog, showSuccessToast } from 'vant'
import { getActivity, registerActivity, checkinActivity } from '../api/activity'
import { useUserStore } from '../stores/user'
import { ACT_STATUS, fmtDateTime } from '../utils/format'

const route = useRoute()
const userStore = useUserStore()
const act = ref(null)
const busy = ref(false)
const checkinStatus = ref('')

const filledRate = computed(() => {
  if (!act.value) return 0
  const max = act.value.maxParticipants || 1
  const used = max - (act.value.remaining ?? 0)
  return Math.max(0, Math.min(1, used / max))
})

const ctaMainText = computed(() => {
  const s = act.value?.status
  if (s === 'REGISTRATING') return act.value.registered ? '您已成功报名' : '报名通道开放中'
  if (s === 'IN_PROGRESS') {
    if (checkinStatus.value === 'CHECKED_IN') return '已完成签到'
    return act.value.registered ? '活动进行中 · 待签到' : '活动进行中'
  }
  return '活动已结束'
})

const ctaSubText = computed(() => {
  const s = act.value?.status
  if (s === 'REGISTRATING') return act.value.registered ? '活动开始后可到现场签到' : '报名即锁定名额，免费参加'
  if (s === 'IN_PROGRESS') {
    if (checkinStatus.value === 'CHECKED_IN') return '感谢您的参与'
    return act.value.registered ? '到场签到可获 50 积分奖励' : '本次活动您未报名'
  }
  return '期待下次活动与您相见'
})

function statusText(s) {
  return ACT_STATUS[s]?.text || s
}

function statusStyle(s) {
  const st = ACT_STATUS[s]
  return st ? { color: st.color, background: st.bg } : {}
}

async function onRegister() {
  try {
    await showConfirmDialog({
      title: '报名活动',
      message: `确定报名「${act.value.title}」吗？`,
      confirmButtonText: '确定报名'
    })
  } catch (e) {
    return
  }
  busy.value = true
  try {
    await registerActivity(act.value.id)
    showSuccessToast('报名成功')
    // 重新拉取详情：刷新已报名状态与剩余名额
    await load()
  } catch (e) { /* 统一提示 */ } finally {
    busy.value = false
  }
}

async function onCheckin() {
  busy.value = true
  try {
    await checkinActivity(act.value.id)
    showSuccessToast('签到成功 +50 积分')
    if (userStore.userInfo?.points != null) {
      userStore.userInfo.points = userStore.userInfo.points + 50
    }
    await load()
  } catch (e) { /* 统一提示 */ } finally {
    busy.value = false
  }
}

async function load() {
  try {
    const data = await getActivity(route.params.id)
    act.value = data
    checkinStatus.value = data.checkInStatus || ''
  } catch (e) { /* 统一提示 */ }
}

onMounted(load)
</script>

<style scoped>
.detail-shell {
  background:
    radial-gradient(90% 22% at 50% 0%, rgba(255, 159, 46, 0.08), transparent 70%),
    var(--bg);
}

.detail-body {
  padding-bottom: 100px;
}

/* 头部 */
.hero {
  position: relative;
  margin: 8px 14px;
  border-radius: var(--r-lg);
  padding: 22px 18px;
  overflow: hidden;
  color: #fff;
}

.hero-0 { background: linear-gradient(150deg, #f79020, #ff9f2e, #ffca4d); box-shadow: 0 12px 28px rgba(255, 159, 46, 0.3); }
.hero-1 { background: linear-gradient(150deg, #6d4df6, #7c5cff, #a78bfa); box-shadow: 0 12px 28px rgba(124, 92, 255, 0.3); }
.hero-2 { background: linear-gradient(150deg, #2f6cf6, #3f7cff, #6fa5ff); box-shadow: 0 12px 28px rgba(63, 124, 255, 0.3); }
.hero-3 { background: linear-gradient(150deg, #f0436a, #ff5b7f, #ff90a9); box-shadow: 0 12px 28px rgba(255, 91, 127, 0.3); }

.hero-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.14);
}

.deco-1 { width: 140px; height: 140px; top: -60px; right: -40px; }
.deco-2 { width: 64px; height: 64px; bottom: -26px; left: 16%; opacity: 0.7; }

.hero-icon {
  position: relative;
  width: 50px;
  height: 50px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 25px;
}

.hero-title {
  position: relative;
  margin: 12px 0 0;
  font-size: 21px;
  font-weight: 800;
  line-height: 1.4;
}

.hero-status {
  position: relative;
  display: inline-block;
  margin-top: 10px;
  font-size: 12px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.92);
  padding: 4px 12px;
  border-radius: 999px;
}

/* 信息卡 */
.info-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 9px 0;
}

.info-row + .info-row {
  border-top: 1px dashed var(--line);
}

.info-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--muted);
  flex-shrink: 0;
}

.info-label .van-icon {
  font-size: 15px;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  text-align: right;
}

.info-value b {
  font-size: 17px;
  color: var(--sun);
}

.info-value b.hot {
  color: var(--rose);
}

.cap-track {
  height: 6px;
  background: var(--bg);
  border-radius: 999px;
  overflow: hidden;
  margin-top: 10px;
}

.cap-bar {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: var(--sun-grad);
  transition: width 0.8s ease;
}

.cap-bar.hot {
  background: linear-gradient(90deg, #ff5b7f, #ff8fa5);
}

/* 介绍 */
.card-title {
  margin: 0 0 10px;
  font-size: 16px;
  font-weight: 700;
  color: var(--ink);
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title::before {
  content: '';
  width: 4px;
  height: 15px;
  border-radius: 2px;
  background: var(--sun-grad);
}

.content-text {
  margin: 0;
  font-size: 15px;
  line-height: 1.85;
  color: var(--ink-2);
  white-space: pre-wrap;
}

/* 已签到横幅 */
.checked-banner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  margin: 0 14px;
  padding: 13px;
  border-radius: var(--r-md);
  background: var(--brand-soft);
  color: var(--brand-deep);
  font-size: 15px;
  font-weight: 700;
}

.checked-banner .van-icon {
  font-size: 18px;
}

/* 底部操作条 */
.cta-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  max-width: 480px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(16px);
  border-top: 1px solid var(--line);
  z-index: 10;
}

.cta-main {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
}

.cta-sub {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--muted);
}

.cta-btn {
  flex-shrink: 0;
  margin-left: auto;
  border: none;
  background: var(--sun-grad);
  color: #fff;
  font-weight: 700;
  padding: 0 26px;
  box-shadow: 0 8px 18px rgba(255, 159, 46, 0.35);
}

/* 已报名按钮（禁用态） */
.cta-btn.cta-done {
  background: var(--brand-grad);
  box-shadow: none;
  opacity: 0.85;
}
</style>
