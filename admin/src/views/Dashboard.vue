<template>
  <div class="page dash-page">
    <!-- 欢迎横幅 -->
    <div class="welcome-hero">
      <div class="hero-deco deco-1"></div>
      <div class="hero-deco deco-2"></div>
      <div class="hero-inner">
        <div class="hero-logo">
          <el-icon :size="30"><Sunrise /></el-icon>
        </div>
        <div class="hero-text">
          <h2>{{ greeting }}，{{ adminName }}</h2>
          <p>欢迎使用智慧养老社区管理后台 · {{ todayText }}</p>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div v-for="item in stats" :key="item.label" class="stat-card" :style="{ '--accent': item.color }">
        <div class="stat-icon">
          <el-icon :size="26"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
        <i class="stat-glow"></i>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="table-card shortcut-card">
      <div class="section-head">
        <h3 class="section-title">快捷入口</h3>
      </div>
      <div class="shortcut-grid">
        <div v-for="s in shortcuts" :key="s.label" class="shortcut-item" @click="$router.push(s.to)">
          <span class="sc-icon" :style="{ background: s.grad }">
            <el-icon :size="20"><component :is="s.icon" /></el-icon>
          </span>
          <p class="sc-label">{{ s.label }}</p>
        </div>
      </div>
    </div>

    <!-- 使用指引 -->
    <div class="table-card guide-card">
      <div class="section-head">
        <h3 class="section-title">管理指引</h3>
      </div>
      <p class="guide-text">
        可在左侧菜单管理会员档案、体检预约、健康评测、社区活动、消息通知与系统配置；
        会员详情页现已支持近 6 个月健康指标趋势分析。
      </p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getStatistics } from '../api/dashboard'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const adminName = userStore.adminInfo?.realName || userStore.adminInfo?.phone || '管理员'

const stats = ref([
  { label: '会员总数', value: 0, icon: 'User', color: '#3f7cff' },
  { label: '今日新增会员', value: 0, icon: 'Plus', color: '#13c291' },
  { label: '今日体检预约', value: 0, icon: 'Calendar', color: '#f79020' },
  { label: '今日活动报名', value: 0, icon: 'Flag', color: '#f0436a' },
  { label: '待确认预约', value: 0, icon: 'Bell', color: '#7c5cff' }
])

const shortcuts = [
  { label: '会员管理', icon: 'User', to: '/member', grad: 'linear-gradient(135deg,#3f7cff,#6fa5ff)' },
  { label: '体检套餐', icon: 'ShoppingCart', to: '/appointment/package', grad: 'linear-gradient(135deg,#0e9f78,#13c291)' },
  { label: '预约记录', icon: 'Calendar', to: '/appointment/list', grad: 'linear-gradient(135deg,#f79020,#ffc35b)' },
  { label: '社区活动', icon: 'Flag', to: '/activity', grad: 'linear-gradient(135deg,#f0436a,#ff8fa5)' },
  { label: '健康评测', icon: 'DataAnalysis', to: '/assessment', grad: 'linear-gradient(135deg,#7c5cff,#a78bfa)' },
  { label: '消息通知', icon: 'Message', to: '/message', grad: 'linear-gradient(135deg,#00b8d9,#5bd6f0)' },
  { label: '系统配置', icon: 'Setting', to: '/config', grad: 'linear-gradient(135deg,#5b6b7c,#93a0af)' }
]

const greeting = (() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})()

const todayText = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

onMounted(async () => {
  try {
    const data = await getStatistics()
    stats.value[0].value = data.memberCount
    stats.value[1].value = data.todayNewMember
    stats.value[2].value = data.todayAppointment
    stats.value[3].value = data.todayActivityRegistration
    stats.value[4].value = data.pendingAppointment
  } catch (e) {
    /* 未登录或接口异常：由拦截器统一处理 */
  }
})
</script>

<style scoped>
.dash-page {
  max-width: 1280px;
  margin: 0 auto;
}

/* 欢迎横幅 */
.welcome-hero {
  position: relative;
  overflow: hidden;
  border-radius: 18px;
  padding: 26px 30px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #0b3d5c 0%, #14537e 45%, #1e7aa8 100%);
  box-shadow: 0 16px 36px rgba(20, 83, 126, 0.28);
  color: #fff;
}

.hero-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.deco-1 {
  width: 220px;
  height: 220px;
  top: -100px;
  right: -50px;
}

.deco-2 {
  width: 100px;
  height: 100px;
  bottom: -40px;
  left: 30%;
  opacity: 0.6;
}

.hero-inner {
  position: relative;
  display: flex;
  align-items: center;
  gap: 18px;
}

.hero-logo {
  width: 60px;
  height: 60px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.18);
  border: 1.5px solid rgba(255, 255, 255, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.hero-text h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  line-height: 1.3;
}

.hero-text p {
  margin: 6px 0 0;
  font-size: 13.5px;
  opacity: 0.88;
}

/* 统计卡片 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 22px 20px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #edf1f6;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(28, 43, 58, 0.1);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  font-size: 26px;
  color: #fff;
  border-radius: 15px;
  background: var(--accent);
  box-shadow: 0 8px 16px color-mix(in srgb, var(--accent) 35%, transparent);
  flex-shrink: 0;
}

.stat-glow {
  position: absolute;
  right: -28px;
  top: -28px;
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: var(--accent);
  opacity: 0.07;
}

.stat-value {
  font-size: 27px;
  font-weight: 800;
  line-height: 1.2;
  color: #1c2b3a;
}

.stat-label {
  font-size: 13px;
  color: #8d9aa9;
  margin-top: 2px;
}

/* 快捷入口 */
.shortcut-card {
  margin-top: 16px;
  border: 1px solid #edf1f6;
  border-radius: 16px;
}

.section-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 14px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1c2b3a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 15px;
  border-radius: 2px;
  background: linear-gradient(180deg, #1e7aa8, #14537e);
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 14px;
}

.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 9px;
  padding: 16px 8px;
  border-radius: 14px;
  background: #f7f9fc;
  border: 1px solid #eef2f7;
  cursor: pointer;
  transition: all 0.2s ease;
}

.shortcut-item:hover {
  background: #fff;
  border-color: #d5e3f5;
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(28, 43, 58, 0.08);
}

.sc-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 13px;
  color: #fff;
}

.sc-label {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: #4a5a6b;
}

/* 指引 */
.guide-card {
  margin-top: 16px;
  border: 1px solid #edf1f6;
  border-radius: 16px;
}

.guide-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.9;
  color: #5b6b7c;
}
</style>
