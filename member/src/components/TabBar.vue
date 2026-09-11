<template>
  <div class="tabbar-spacer">
    <nav class="tabbar">
      <button
        v-for="t in tabs"
        :key="t.path"
        class="tab press"
        :class="{ on: active === t.path }"
        type="button"
        @click="go(t.path)"
      >
        <span class="tab-icon">
          <van-icon :name="active === t.path ? t.iconOn : t.icon" />
          <i v-if="t.path === '/profile' && unread > 0" class="tab-dot"></i>
        </span>
        <span class="tab-label">{{ t.label }}</span>
      </button>
    </nav>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { unreadCount } from '../api/message'

const route = useRoute()
const router = useRouter()
const unread = ref(0)

const tabs = [
  { path: '/', label: '首页', icon: 'wap-home-o', iconOn: 'wap-home' },
  { path: '/health', label: '健康', icon: 'records', iconOn: 'records' },
  { path: '/appointment', label: '预约', icon: 'calendar-o', iconOn: 'calendar-o' },
  { path: '/activity', label: '活动', icon: 'friends-o', iconOn: 'friends' },
  { path: '/profile', label: '我的', icon: 'user-o', iconOn: 'user' }
]

const active = computed(() => {
  const path = route.path
  if (path.startsWith('/health')) return '/health'
  if (path.startsWith('/appointment')) return '/appointment'
  if (path.startsWith('/activity')) return '/activity'
  if (path.startsWith('/profile')) return '/profile'
  return '/'
})

function go(path) {
  if (route.path !== path) {
    router.push(path)
  }
}

onMounted(async () => {
  try {
    const data = await unreadCount()
    unread.value = data?.unreadCount || 0
  } catch (e) {
    /* 未登录或接口异常时忽略 */
  }
})
</script>

<style scoped>
.tabbar-spacer {
  position: sticky;
  bottom: 0;
  margin-top: auto;
  padding: 10px 14px calc(10px + env(safe-area-inset-bottom));
  z-index: 100;
}

.tabbar {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border-radius: 24px;
  box-shadow: 0 10px 30px rgba(28, 43, 58, 0.12);
  padding: 8px 6px;
}

.tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  background: none;
  border: none;
  padding: 4px 0;
  cursor: pointer;
  color: var(--muted);
  transition: color 0.2s ease;
}

.tab-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 26px;
  font-size: 22px;
  transition: transform 0.22s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.tab.on {
  color: var(--brand-deep);
}

.tab.on .tab-icon {
  transform: translateY(-2px) scale(1.08);
}

/* 激活时图标底部的胶囊高亮 */
.tab.on .tab-icon::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  width: 18px;
  height: 3px;
  border-radius: 2px;
  background: var(--brand-grad);
}

.tab-dot {
  position: absolute;
  top: -1px;
  right: 1px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--rose);
  box-shadow: 0 0 0 2px #fff;
}

.tab-label {
  font-size: 12px;
  font-weight: 600;
}
</style>
