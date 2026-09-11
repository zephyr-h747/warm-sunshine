<template>
  <div class="shell home-shell">
    <div class="page-body">
      <!-- 渐变头部 -->
      <header class="hero">
        <div class="hero-deco deco-1"></div>
        <div class="hero-deco deco-2"></div>
        <div class="hero-deco deco-3"></div>

        <div class="hero-top fade-up">
          <div class="hello">
            <p class="hello-line">{{ greet }}，{{ name }}</p>
            <p class="date-line">{{ today }}</p>
          </div>
          <button class="bell press" type="button" @click="$router.push('/message')">
            <van-icon name="chat-o" />
            <i v-if="unread > 0" class="bell-badge">{{ unread > 99 ? '99+' : unread }}</i>
          </button>
        </div>

        <!-- 积分卡 -->
        <div class="points-card fade-up d1 press" @click="$router.push('/profile')">
          <div class="points-left">
            <div class="coin"><van-icon name="gold-coin-o" /></div>
            <div>
              <p class="points-label">我的积分</p>
              <p class="points-num">{{ displayPoints }}</p>
            </div>
          </div>
          <div class="points-right">
            <span class="level-chip" :class="level">{{ levelName }}</span>
            <span class="points-more">明细 <van-icon name="arrow" /></span>
          </div>
        </div>
      </header>

      <!-- 快捷入口 -->
      <div class="quick-card fade-up d2">
        <button v-for="q in quicks" :key="q.path" class="quick press" type="button" @click="$router.push(q.path)">
          <span class="quick-icon" :style="{ background: q.grad }">
            <van-icon :name="q.icon" />
          </span>
          <span class="quick-label">{{ q.label }}</span>
        </button>
      </div>

      <!-- 今日健康 -->
      <div class="sec-head fade-up d3">
        <div class="sec-title">今日健康</div>
        <span class="sec-more" @click="$router.push('/health/trend')">趋势图 <van-icon name="arrow" /></span>
      </div>
      <div v-if="latest" class="app-card vitals-card fade-up d3">
        <div class="vital" v-for="v in vitalTiles" :key="v.key">
          <p class="vital-name">{{ v.name }}</p>
          <p class="vital-value" :class="v.status">
            {{ v.text }}<span class="vital-unit">{{ v.unit }}</span>
          </p>
          <p class="vital-status">{{ v.statusText }}</p>
        </div>
        <div class="vital-footer" @click="$router.push('/health')">
          记录今日数据 <van-icon name="edit" />
        </div>
      </div>
      <div v-else class="app-card empty-health fade-up d3 press" @click="$router.push('/health')">
        <div class="empty-icon"><van-icon name="records" /></div>
        <div>
          <p class="empty-title">还没有健康记录</p>
          <p class="empty-sub">每天记录血压血糖，守护更安心</p>
        </div>
        <van-icon name="arrow" class="empty-arrow" />
      </div>

      <!-- 最近活动 -->
      <template v-if="activities.length">
        <div class="sec-head fade-up d4">
          <div class="sec-title">社区活动</div>
          <span class="sec-more" @click="$router.push('/activity')">全部 <van-icon name="arrow" /></span>
        </div>
        <div
          v-for="act in activities"
          :key="act.id"
          class="app-card act-card fade-up d4 press"
          @click="$router.push(`/activity/${act.id}`)"
        >
          <div class="act-cover" :class="`cover-${act.id % 4}`">
            <span class="act-tag">{{ act.status === 'REGISTRATING' ? '报名中' : '进行中' }}</span>
          </div>
          <div class="act-body">
            <p class="act-title">{{ act.title }}</p>
            <div class="act-meta">
              <van-icon name="clock-o" />
              <span>{{ fmtDate(act.activityStart) }}</span>
              <em></em>
              <van-icon name="friends-o" />
              <span>余 {{ act.remaining }} 名额</span>
            </div>
          </div>
        </div>
      </template>

      <!-- 即将到来的预约 -->
      <template v-if="nextAppt">
        <div class="sec-head fade-up d5">
          <div class="sec-title">体检提醒</div>
          <span class="sec-more" @click="$router.push('/appointment/mine')">我的预约 <van-icon name="arrow" /></span>
        </div>
        <div class="app-card appt-card fade-up d5">
          <div class="appt-icon"><van-icon name="calendar-o" /></div>
          <div class="appt-info">
            <p class="appt-name">{{ nextAppt.packageName }}</p>
            <p class="appt-time">{{ fmtDate(nextAppt.appointDate) }} {{ nextAppt.timeRange }}</p>
          </div>
          <span class="appt-status">{{ apptText(nextAppt.status) }}</span>
        </div>
      </template>
    </div>

    <TabBar />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import TabBar from '../components/TabBar.vue'
import { useUserStore } from '../stores/user'
import { listHealthRecords } from '../api/health'
import { listActivities } from '../api/activity'
import { listMyAppointments } from '../api/appointment'
import { unreadCount } from '../api/message'
import {
  greeting, dateLine, fmtDate, animateNumber,
  LEVEL_NAMES, APPT_STATUS, vitalStatus, VITAL_RANGES
} from '../utils/format'

const userStore = useUserStore()
const unread = ref(0)
const latest = ref(null)
const activities = ref([])
const nextAppt = ref(null)
const displayPoints = ref(0)

const greet = greeting()
const today = dateLine()

const name = computed(() => {
  const n = userStore.userInfo?.realName
  return n ? n.slice(0, 6) : (userStore.userInfo?.phone ? '家人' : '您')
})

const level = computed(() => userStore.userInfo?.memberLevel || 'NORMAL')
const levelName = computed(() => LEVEL_NAMES[level.value] || '普通会员')

const quicks = [
  { label: '健康记录', icon: 'records', path: '/health', grad: 'linear-gradient(135deg,#00b486,#3ad6a8)' },
  { label: '健康评测', icon: 'notes-o', path: '/assessment', grad: 'linear-gradient(135deg,#7c5cff,#a78bfa)' },
  { label: '体检预约', icon: 'calendar-o', path: '/appointment', grad: 'linear-gradient(135deg,#3f7cff,#6fa5ff)' },
  { label: 'AI 助手', icon: 'chat-o', path: '/chat', grad: 'linear-gradient(135deg,#ff9f2e,#ffca4d)' }
]

/* 最新记录的指标瓦片 */
const vitalTiles = computed(() => {
  const r = latest.value
  if (!r) return []
  const tiles = [
    { key: 'bp', name: '血压', text: r.systolic || r.diastolic ? `${r.systolic ?? '-'}/${r.diastolic ?? '-'}` : '--', unit: '' },
    { key: 'bloodSugar', name: '血糖', text: r.bloodSugar ?? '--', unit: 'mmol/L' },
    { key: 'heartRate', name: '心率', text: r.heartRate ?? '--', unit: '次/分' },
    { key: 'bmi', name: 'BMI', text: r.bmi ?? '--', unit: '' }
  ]
  return tiles.map((t) => {
    let status = 'none'
    let statusText = '未记录'
    if (t.key === 'bp' && (r.systolic || r.diastolic)) {
      const s1 = vitalStatus('systolic', r.systolic)
      const s2 = vitalStatus('diastolic', r.diastolic)
      status = s1 === 'ok' && s2 === 'ok' ? 'ok' : 'warn'
      statusText = status === 'ok' ? '正常' : '请留意'
    } else if (t.key !== 'bp' && r[t.key] != null) {
      status = vitalStatus(t.key, r[t.key])
      statusText = status === 'ok' ? '正常' : '请留意'
    }
    return { ...t, status: status === 'none' ? '' : status, statusText }
  })
})

function apptText(s) {
  return APPT_STATUS[s]?.text || s
}

onMounted(async () => {
  /* 刷新用户信息（积分/等级） */
  userStore.refreshUserInfo().then((info) => {
    if (info?.points != null) {
      animateNumber((v) => (displayPoints.value = v), info.points)
    }
  }).catch(() => {})

  try {
    const data = await unreadCount()
    unread.value = data?.unreadCount || 0
  } catch (e) { /* 忽略 */ }

  try {
    const data = await listHealthRecords({ pageNum: 1, pageSize: 1 })
    latest.value = data?.list?.[0] || null
  } catch (e) { /* 忽略 */ }

  try {
    const data = await listActivities({ status: 'REGISTRATING', pageNum: 1, pageSize: 2 })
    activities.value = data?.list || []
  } catch (e) { /* 忽略 */ }

  try {
    const data = await listMyAppointments({ pageNum: 1, pageSize: 10 })
    const list = (data?.list || []).filter(
      (a) => a.status === 'PENDING' || a.status === 'CONFIRMED'
    )
    nextAppt.value = list[0] || null
  } catch (e) { /* 忽略 */ }
})
</script>

<style scoped>
.home-shell {
  background:
    radial-gradient(90% 30% at 50% 0%, rgba(0, 180, 134, 0.06), transparent 70%),
    var(--bg);
}

/* ========== 头部 ========== */
.hero {
  position: relative;
  background: linear-gradient(160deg, #00a07a 0%, #00b486 45%, #2ecf9f 100%);
  border-radius: 0 0 28px 28px;
  padding: 26px 18px 54px;
  overflow: hidden;
}

.hero-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.deco-1 { width: 160px; height: 160px; top: -70px; right: -40px; }
.deco-2 { width: 90px; height: 90px; top: 40px; left: -30px; opacity: 0.7; }
.deco-3 { width: 60px; height: 60px; bottom: -10px; right: 60px; opacity: 0.5; }

.hero-top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  color: #fff;
}

.hello-line {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 1px;
}

.date-line {
  margin: 6px 0 0;
  font-size: 13px;
  opacity: 0.85;
}

.bell {
  position: relative;
  border: none;
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
  width: 40px;
  height: 40px;
  border-radius: 13px;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.bell-badge {
  position: absolute;
  top: -5px;
  right: -7px;
  background: #ff5b4d;
  color: #fff;
  font-size: 10px;
  font-style: normal;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 999px;
  box-shadow: 0 0 0 2px rgba(0, 160, 122, 0.9);
}

/* 积分卡（悬浮玻璃） */
.points-card {
  position: relative;
  z-index: 2;
  margin-top: 18px;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 18px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  cursor: pointer;
}

.points-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.coin {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: linear-gradient(135deg, #ffca4d, #ff9f2e);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  box-shadow: 0 6px 14px rgba(255, 159, 46, 0.45);
}

.points-label {
  margin: 0;
  font-size: 12px;
  opacity: 0.85;
}

.points-num {
  margin: 2px 0 0;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 1px;
}

.points-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.points-more {
  font-size: 12px;
  opacity: 0.85;
  display: flex;
  align-items: center;
}

/* ========== 快捷入口 ========== */
.quick-card {
  position: relative;
  z-index: 2;
  margin: -32px 14px 0;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(28, 43, 58, 0.08);
  padding: 16px 8px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
}

.quick {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px 0;
}

.quick-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  box-shadow: 0 6px 14px rgba(28, 43, 58, 0.12);
}

.quick-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-2);
}

/* ========== 今日健康 ========== */
.vitals-card {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  position: relative;
}

.vital {
  background: var(--bg);
  border-radius: 14px;
  padding: 12px 14px;
}

.vital-name {
  margin: 0;
  font-size: 13px;
  color: var(--muted);
}

.vital-value {
  margin: 4px 0 2px;
  font-size: 22px;
  font-weight: 800;
  color: var(--ink);
}

.vital-value.ok { color: var(--brand-deep); }
.vital-value.warn { color: #ff7a00; }

.vital-unit {
  font-size: 11px;
  font-weight: 400;
  color: var(--muted);
  margin-left: 3px;
}

.vital-status {
  margin: 0;
  font-size: 11px;
  color: var(--muted);
}

.vital-footer {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  color: var(--brand-deep);
  background: var(--brand-soft);
  border-radius: 12px;
  padding: 9px 0;
  cursor: pointer;
}

/* 空健康引导 */
.empty-health {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
}

.empty-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: var(--brand-soft);
  color: var(--brand-deep);
  font-size: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.empty-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--ink);
}

.empty-sub {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--muted);
}

.empty-arrow {
  margin-left: auto;
  color: var(--muted);
}

/* ========== 活动卡 ========== */
.act-card {
  display: flex;
  padding: 0;
  overflow: hidden;
  cursor: pointer;
}

.act-cover {
  width: 92px;
  flex-shrink: 0;
  position: relative;
}

.cover-0 { background: linear-gradient(160deg, #ff9f2e, #ffd166); }
.cover-1 { background: linear-gradient(160deg, #7c5cff, #a78bfa); }
.cover-2 { background: linear-gradient(160deg, #3f7cff, #6fa5ff); }
.cover-3 { background: linear-gradient(160deg, #ff5b7f, #ff90a9); }

.act-tag {
  position: absolute;
  left: 10px;
  bottom: 10px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--ink);
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 999px;
}

.act-body {
  flex: 1;
  padding: 14px;
  min-width: 0;
}

.act-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.act-meta {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--muted);
}

.act-meta em {
  width: 1px;
  height: 10px;
  background: var(--line);
}

/* ========== 预约提醒 ========== */
.appt-card {
  display: flex;
  align-items: center;
  gap: 12px;
}

.appt-icon {
  width: 46px;
  height: 46px;
  border-radius: 15px;
  background: rgba(63, 124, 255, 0.12);
  color: var(--sky);
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.appt-info {
  flex: 1;
  min-width: 0;
}

.appt-name {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.appt-time {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--muted);
}

.appt-status {
  font-size: 12px;
  font-weight: 700;
  color: var(--sun);
  background: var(--sun-soft);
  padding: 4px 10px;
  border-radius: 999px;
  flex-shrink: 0;
}
</style>
