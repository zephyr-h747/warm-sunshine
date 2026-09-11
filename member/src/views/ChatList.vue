<template>
  <div class="shell chat-shell">
    <div class="page-body">
      <van-nav-bar title="AI 助手" left-arrow @click-left="$router.back()" />

      <!-- AI 头部 -->
      <div class="hero fade-up">
        <div class="hero-deco deco-1"></div>
        <div class="hero-deco deco-2"></div>
        <div class="bot-avatar">
          <i class="halo halo-1"></i>
          <i class="halo halo-2"></i>
          <span class="bot-face"><van-icon name="chat-o" /></span>
        </div>
        <p class="hero-title">您好，我是福琛</p>
        <p class="hero-desc">您的专属健康助手 · 有问必答</p>
      </div>

      <!-- 新建对话 -->
      <button class="new-chat press fade-up d1" type="button" @click="startNew()">
        <span class="new-ico"><van-icon name="plus" /></span>
        <span class="new-main">
          <b>新建对话</b>
          <i>直接输入您想咨询的健康问题</i>
        </span>
        <van-icon name="arrow" class="new-arrow" />
      </button>

      <!-- 能力卡 -->
      <div class="cap-grid fade-up d1">
        <div v-for="c in caps" :key="c.label" class="cap-card press" @click="startNew(c.q)">
          <span class="cap-icon" :style="{ background: c.grad }">
            <van-icon :name="c.icon" />
          </span>
          <p class="cap-label">{{ c.label }}</p>
          <p class="cap-desc">{{ c.desc }}</p>
        </div>
      </div>

      <!-- 快捷提问 -->
      <div class="sec-head fade-up d2">
        <div class="sec-title">试试这样问我</div>
      </div>
      <div class="app-card ask-card fade-up d2">
        <button
          v-for="q in quickQuestions"
          :key="q"
          type="button"
          class="ask-row press"
          @click="startNew(q)"
        >
          <span class="ask-icon"><van-icon name="question-o" /></span>
          <span class="ask-text">{{ q }}</span>
          <van-icon name="arrow" class="ask-arrow" />
        </button>
      </div>

      <!-- 最近对话 -->
      <template v-if="sessions.length">
        <div class="sec-head fade-up d3">
          <div class="sec-title">最近对话</div>
        </div>
        <div class="app-card sess-card fade-up d3">
          <div v-for="s in sessions" :key="s.id" class="sess-row press" @click="openSession(s.id)">
            <span class="sess-icon"><van-icon name="chat-o" /></span>
            <div class="sess-main">
              <p class="sess-name">{{ s.sessionName || '新对话' }}</p>
              <p class="sess-preview">{{ s.lastMessage || '暂无消息' }}</p>
            </div>
            <button type="button" class="sess-del" @click.stop="removeSession(s)">
              <van-icon name="delete-o" />
            </button>
          </div>
        </div>
      </template>

      <div class="notice fade-up d3">
        <van-icon name="info-o" />
        AI 建议仅供参考，不能替代专业医疗诊断；如有不适请及时就医。
      </div>
    </div>
    <TabBar />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import TabBar from '../components/TabBar.vue'
import { listSessions, deleteSession } from '../api/chat'

const router = useRouter()
const sessions = ref([])

const caps = [
  { label: '健康咨询', desc: '指标怎么看', icon: 'heart', q: '血压偏高要注意什么？', grad: 'linear-gradient(135deg,#ff5b7f,#ff90a9)' },
  { label: '生活建议', desc: '饮食与运动', icon: 'gift-o', q: '老年人适合哪些运动？', grad: 'linear-gradient(135deg,#00b486,#3ad6a8)' },
  { label: '用药提醒', desc: '安全用药常识', icon: 'warning-o', q: '降压药什么时候吃最好？', grad: 'linear-gradient(135deg,#ff9f2e,#ffca4d)' }
]

const quickQuestions = [
  '血压偏高要注意什么？',
  '老年人适合哪些运动？',
  '降压药什么时候吃最好？',
  '晚上睡不好怎么办？'
]

async function loadSessions() {
  try {
    const page = await listSessions({ pageNum: 1, pageSize: 10 })
    sessions.value = page.list || []
  } catch (e) {
    // 列表加载失败不阻断页面
  }
}

function startNew(q) {
  router.push({ path: '/chat/new', query: q ? { q } : {} })
}

function openSession(id) {
  router.push(`/chat/${id}`)
}

async function removeSession(s) {
  try {
    await showConfirmDialog({ title: '删除对话', message: '删除后聊天记录不可恢复，确认删除吗？' })
  } catch (e) {
    return
  }
  try {
    await deleteSession(s.id)
    sessions.value = sessions.value.filter((x) => x.id !== s.id)
    showToast('已删除')
  } catch (e) {
    // 错误提示由拦截器统一处理
  }
}

onMounted(loadSessions)
</script>

<style scoped>
.chat-shell {
  background:
    radial-gradient(90% 26% at 50% 0%, rgba(255, 159, 46, 0.1), transparent 70%),
    var(--bg);
}

/* 头部 */
.hero {
  position: relative;
  margin: 8px 14px 6px;
  border-radius: var(--r-lg);
  background: linear-gradient(150deg, #f79020 0%, #ff9f2e 45%, #ffca4d 100%);
  padding: 26px 16px 24px;
  overflow: hidden;
  text-align: center;
  color: #fff;
  box-shadow: 0 12px 28px rgba(255, 159, 46, 0.32);
}

.hero-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.14);
}

.deco-1 { width: 130px; height: 130px; top: -55px; left: -35px; }
.deco-2 { width: 70px; height: 70px; bottom: -28px; right: 24%; opacity: 0.7; }

/* 机器人头像 + 呼吸光环 */
.bot-avatar {
  position: relative;
  width: 74px;
  height: 74px;
  margin: 0 auto;
}

.halo {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 1.5px solid rgba(255, 255, 255, 0.55);
  animation: halo 2.6s ease-out infinite;
}

.halo-2 {
  animation-delay: 1.3s;
}

@keyframes halo {
  0% { transform: scale(0.85); opacity: 0.9; }
  100% { transform: scale(1.55); opacity: 0; }
}

.bot-face {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.24);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
}

.hero-title {
  position: relative;
  margin: 12px 0 0;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 1px;
}

.hero-desc {
  position: relative;
  margin: 5px 0 0;
  font-size: 13px;
  opacity: 0.9;
}

/* 新建对话 */
.new-chat {
  width: calc(100% - 28px);
  margin: 0 14px;
  display: flex;
  align-items: center;
  gap: 11px;
  border: none;
  background: #fff;
  border-radius: var(--r-md);
  box-shadow: var(--shadow-1);
  padding: 13px 15px;
  text-align: left;
  cursor: pointer;
}

.new-ico {
  width: 38px;
  height: 38px;
  border-radius: 13px;
  background: var(--brand-grad);
  color: #fff;
  font-size: 19px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 6px 12px rgba(0, 180, 134, 0.25);
}

.new-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.new-main b {
  font-size: 16px;
  color: var(--ink);
}

.new-main i {
  font-style: normal;
  font-size: 12px;
  color: var(--muted);
}

.new-arrow {
  color: #c3ccd6;
  flex-shrink: 0;
}

/* 能力卡 */
.cap-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 9px;
  margin: 14px;
}

.cap-card {
  background: #fff;
  border-radius: var(--r-md);
  box-shadow: var(--shadow-1);
  padding: 14px 8px;
  text-align: center;
  cursor: pointer;
}

.cap-icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  color: #fff;
  font-size: 21px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 12px rgba(28, 43, 58, 0.14);
}

.cap-label {
  margin: 9px 0 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--ink);
}

.cap-desc {
  margin: 3px 0 0;
  font-size: 11px;
  color: var(--muted);
}

/* 快捷提问 */
.ask-card {
  padding: 4px 16px;
}

.ask-row {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 11px;
  border: none;
  background: none;
  padding: 13px 0;
  text-align: left;
  cursor: pointer;
}

.ask-row + .ask-row {
  border-top: 1px solid var(--line);
}

.ask-icon {
  width: 30px;
  height: 30px;
  border-radius: 10px;
  background: var(--sun-soft);
  color: var(--sun);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  flex-shrink: 0;
}

.ask-text {
  flex: 1;
  font-size: 15px;
  color: var(--ink-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ask-arrow {
  color: #c3ccd6;
  flex-shrink: 0;
}

/* 最近对话 */
.sess-card {
  padding: 4px 16px;
}

.sess-row {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 13px 0;
  cursor: pointer;
}

.sess-row + .sess-row {
  border-top: 1px solid var(--line);
}

.sess-icon {
  width: 30px;
  height: 30px;
  border-radius: 10px;
  background: var(--sun-soft);
  color: var(--sun);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  flex-shrink: 0;
}

.sess-main {
  flex: 1;
  min-width: 0;
}

.sess-name {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sess-preview {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sess-del {
  border: none;
  background: none;
  color: #c3ccd6;
  font-size: 17px;
  padding: 6px;
  flex-shrink: 0;
  cursor: pointer;
}

/* 提示 */
.notice {
  display: flex;
  gap: 7px;
  margin: 16px 14px 20px;
  padding: 12px 14px;
  background: rgba(255, 159, 46, 0.09);
  border-radius: var(--r-sm);
  color: #9c6b1a;
  font-size: 13px;
  line-height: 1.65;
}

.notice .van-icon {
  flex-shrink: 0;
  font-size: 16px;
  margin-top: 2px;
}
</style>
