<template>
  <div class="shell msg-shell">
    <div class="page-body">
      <van-nav-bar title="消息通知" left-arrow @click-left="$router.back()">
        <template #right>
          <span v-if="unreadNum" class="nav-unread">{{ unreadNum }} 条未读</span>
        </template>
      </van-nav-bar>

      <template v-if="list.length">
        <div
          v-for="(m, i) in list"
          :key="m.id"
          class="msg-card press fade-up"
          :class="{ unread: m.isRead === 0 }"
          :style="{ animationDelay: Math.min(i * 0.05, 0.25) + 's' }"
          @click="onOpen(m)"
        >
          <div class="msg-icon" :style="iconStyle(m.type)">
            <van-icon :name="typeInfo(m.type).icon" />
          </div>
          <div class="msg-body">
            <div class="msg-head">
              <p class="msg-title">{{ m.title }}</p>
              <i v-if="m.isRead === 0" class="msg-dot"></i>
            </div>
            <p class="msg-content">{{ m.content }}</p>
            <div class="msg-foot">
              <span class="msg-type" :style="{ color: typeInfo(m.type).color }">{{ typeInfo(m.type).text }}</span>
              <span class="msg-time">{{ fmtFriendly(m.createTime) }}</span>
            </div>
          </div>
        </div>
      </template>
      <van-empty v-else description="暂无消息" class="fade-up" />
    </div>
    <TabBar />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { showDialog } from 'vant'
import TabBar from '../components/TabBar.vue'
import { listMessages, markAsRead, getMessage } from '../api/message'
import { MSG_TYPES, fmtFriendly } from '../utils/format'

const list = ref([])

const unreadNum = computed(() => list.value.filter((m) => m.isRead === 0).length)

function typeInfo(t) {
  return MSG_TYPES[t] || MSG_TYPES.SYSTEM
}

function iconStyle(t) {
  const info = typeInfo(t)
  return { background: info.bg, color: info.color }
}

async function load() {
  try {
    const data = await listMessages({ pageNum: 1, pageSize: 50 })
    list.value = data?.list || []
  } catch (e) { /* 忽略 */ }
}

async function onOpen(m) {
  if (m.isRead === 0) {
    try {
      await markAsRead(m.id)
    } catch (e) { /* 忽略 */ }
    m.isRead = 1
  }
  let full = m
  try {
    full = await getMessage(m.id)
  } catch (e) { /* 用列表数据 */ }
  showDialog({
    title: full.title,
    message: full.content || '',
    showCancelButton: false,
    confirmButtonText: '知道了'
  })
}

onMounted(load)
</script>

<style scoped>
.msg-shell {
  background:
    radial-gradient(90% 20% at 50% 0%, rgba(255, 91, 127, 0.06), transparent 70%),
    var(--bg);
}

.nav-unread {
  font-size: 12px;
  color: var(--rose);
  font-weight: 600;
}

/* 消息卡 */
.msg-card {
  display: flex;
  gap: 12px;
  background: #fff;
  border-radius: var(--r-md);
  box-shadow: var(--shadow-1);
  padding: 14px;
  margin: 0 14px 10px;
  cursor: pointer;
}

.msg-card.unread {
  background: linear-gradient(135deg, rgba(255, 91, 127, 0.035), #fff 40%);
  border: 1px solid rgba(255, 91, 127, 0.16);
}

.msg-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  font-size: 21px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-head {
  display: flex;
  align-items: center;
  gap: 7px;
}

.msg-title {
  margin: 0;
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-card.unread .msg-title {
  font-weight: 800;
}

.msg-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--rose);
  flex-shrink: 0;
  animation: pulse-soft 1.6s ease-in-out infinite;
}

.msg-content {
  margin: 5px 0 0;
  font-size: 13.5px;
  color: var(--ink-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 7px;
}

.msg-type {
  font-size: 11px;
  font-weight: 700;
}

.msg-time {
  font-size: 11px;
  color: var(--muted);
}
</style>
