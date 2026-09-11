<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside width="230px" class="aside">
      <div class="aside-glow"></div>
      <div class="logo">
        <div class="logo-badge">
          <el-icon :size="22"><Platform /></el-icon>
        </div>
        <div class="logo-txt">
          <p class="logo-name">智慧养老社区</p>
          <p class="logo-sub">ADMIN CONSOLE</p>
        </div>
      </div>

      <el-menu :default-active="activeMenu" router class="tech-menu" :collapse="false">
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-sub-menu index="member-group">
          <template #title>
            <el-icon><User /></el-icon>
            <span>会员管理</span>
          </template>
          <el-menu-item index="/member">会员列表</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="appointment-group">
          <template #title>
            <el-icon><Calendar /></el-icon>
            <span>体检管理</span>
          </template>
          <el-menu-item index="/appointment/package">体检套餐</el-menu-item>
          <el-menu-item index="/appointment/list">预约记录</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/assessment">
          <el-icon><DocumentChecked /></el-icon>
          <span>健康评测</span>
        </el-menu-item>
        <el-menu-item index="/activity">
          <el-icon><Flag /></el-icon>
          <span>社区活动</span>
        </el-menu-item>
        <el-menu-item index="/message">
          <el-icon><Bell /></el-icon>
          <span>消息通知</span>
        </el-menu-item>
        <el-menu-item index="/config">
          <el-icon><Setting /></el-icon>
          <span>系统配置</span>
        </el-menu-item>
      </el-menu>

      <div class="aside-foot">
        <div class="foot-pulse"></div>
        <span>系统运行正常</span>
      </div>
    </el-aside>

    <el-container class="right-container">
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-left">
          <span class="crumb-current">{{ crumbTitle }}</span>
          <span class="crumb-sep">/</span>
          <span class="crumb-path">智慧养老社区管理后台</span>
        </div>
        <div class="header-right">
          <div class="clock">
            <el-icon><Clock /></el-icon>
            <span>{{ clockText }}</span>
          </div>
          <el-divider direction="vertical" />
          <div class="admin-chip">
            <span class="admin-avatar">{{ adminChar }}</span>
            <span class="admin-name">{{ adminName }}</span>
          </div>
          <el-button link type="danger" @click="handleLogout">
            <el-icon style="margin-right: 4px"><SwitchButton /></el-icon>退出
          </el-button>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="view-fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Clock, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const adminName = computed(() => {
  const info = userStore.adminInfo
  return (info && info.realName) || '管理员'
})
const adminChar = computed(() => adminName.value.charAt(0))

const crumbTitle = computed(() => route.meta.title || '仪表盘')

/* 实时时钟 */
const clockText = ref('')
let timer = null

function tick() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  clockText.value = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

onMounted(() => {
  tick()
  timer = setInterval(tick, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
})

async function handleLogout() {
  await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
  await userStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}

/* ============ 侧边栏 ============ */
.aside {
  position: relative;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #081c30 0%, #0a2238 55%, #0b2942 100%);
  overflow: hidden;
}

/* 顶部品牌光晕 */
.aside-glow {
  position: absolute;
  width: 260px;
  height: 260px;
  top: -120px;
  left: -60px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(19, 194, 145, 0.22), transparent 70%);
  pointer-events: none;
}

/* Logo */
.logo {
  position: relative;
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 18px 18px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
}

.logo-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 13px;
  color: #fff;
  background: linear-gradient(135deg, #0e9f78, #13c291);
  box-shadow: 0 8px 18px rgba(14, 159, 120, 0.45);
  flex-shrink: 0;
}

.logo-txt {
  min-width: 0;
}

.logo-name {
  margin: 0;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 1px;
  line-height: 1.3;
}

.logo-sub {
  margin: 2px 0 0;
  color: rgba(255, 255, 255, 0.38);
  font-size: 9.5px;
  letter-spacing: 2.5px;
}

/* 菜单 */
.tech-menu {
  flex: 1;
  padding: 10px 12px;
  border-right: none !important;
  background: transparent !important;
  overflow-y: auto;
}

/* 覆盖 el-menu 深色样式 */
.tech-menu :deep(.el-menu) {
  background: transparent;
}

.tech-menu :deep(.el-menu-item),
.tech-menu :deep(.el-sub-menu__title) {
  height: 46px;
  margin: 4px 0;
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.62) !important;
  background: transparent !important;
  transition: all 0.22s ease;
}

.tech-menu :deep(.el-menu-item:hover),
.tech-menu :deep(.el-sub-menu__title:hover) {
  color: #fff !important;
  background: rgba(255, 255, 255, 0.07) !important;
}

.tech-menu :deep(.el-menu-item.is-active) {
  color: #fff !important;
  background: linear-gradient(135deg, #0e9f78, #13c291) !important;
  box-shadow: 0 8px 20px rgba(14, 159, 120, 0.42);
}

/* 选中项左侧发光条 */
.tech-menu :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 22px;
  border-radius: 2px;
  background: #fff;
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.8);
}

.tech-menu :deep(.el-menu-item .el-icon),
.tech-menu :deep(.el-sub-menu__title .el-icon) {
  color: inherit;
}

.tech-menu :deep(.el-sub-menu .el-menu) {
  background: rgba(0, 0, 0, 0.18) !important;
  border-radius: 12px;
  padding: 4px 0;
  margin: 2px 0 6px;
}

.tech-menu :deep(.el-sub-menu .el-menu .el-menu-item) {
  height: 40px;
  margin: 0;
  border-radius: 10px;
}

/* 底部状态 */
.aside-foot {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.07);
  color: rgba(255, 255, 255, 0.45);
  font-size: 12px;
}

.foot-pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #13c291;
  box-shadow: 0 0 0 0 rgba(19, 194, 145, 0.6);
  animation: pulse-dot 2s ease-out infinite;
}

@keyframes pulse-dot {
  0% {
    box-shadow: 0 0 0 0 rgba(19, 194, 145, 0.55);
  }
  70% {
    box-shadow: 0 0 0 7px rgba(19, 194, 145, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(19, 194, 145, 0);
  }
}

/* ============ 右侧 ============ */
.right-container {
  min-width: 0;
}

/* 顶栏 */
.header {
  position: relative;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 62px;
  padding: 0 22px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid var(--line);
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 10px;
  min-width: 0;
}

.crumb-current {
  font-size: 16px;
  font-weight: 800;
  color: var(--ink);
}

.crumb-sep {
  color: #c6d1dc;
}

.crumb-path {
  font-size: 12.5px;
  color: var(--muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.clock {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--ink-2);
  font-variant-numeric: tabular-nums;
}

.clock .el-icon {
  color: var(--brand);
}

.admin-chip {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 5px 14px 5px 6px;
  border-radius: 999px;
  background: #f2f6fa;
  border: 1px solid var(--line);
}

.admin-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  color: #fff;
  font-size: 14px;
  font-weight: 800;
  background: linear-gradient(135deg, #0e9f78, #13c291);
}

.admin-name {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--ink);
}

/* 内容区 */
.main {
  overflow-y: auto;
  background:
    radial-gradient(60% 30% at 85% 0%, rgba(14, 159, 120, 0.05), transparent 70%),
    radial-gradient(50% 26% at 0% 100%, rgba(63, 124, 255, 0.04), transparent 70%),
    #f3f6fa;
}

/* 路由切换动画 */
.view-fade-enter-active,
.view-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.view-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.view-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
