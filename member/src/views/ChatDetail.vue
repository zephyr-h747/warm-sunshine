<template>
  <div class="chat-page">
    <van-nav-bar title="AI 健康助手" left-arrow @click-left="$router.back()" />

    <div ref="scrollRef" class="chat-body">
      <!-- 欢迎语 -->
      <div class="welcome fade-up">
        <div class="bot-avatar"><van-icon name="chat-o" /></div>
        <p>您好，我是福琛，您的专属健康助手</p>
      </div>

      <div
        v-for="(msg, i) in messages"
        :key="i"
        class="msg-row fade-up"
        :class="msg.role"
      >
        <div v-if="msg.role === 'assistant'" class="avatar ai"><van-icon name="chat-o" /></div>
        <div class="bubble">
          <span
            v-if="msg.role === 'assistant' && i === messages.length - 1 && typing && !msg.content"
            class="dots"
          >
            <i></i><i></i><i></i>
          </span>
          <template v-else>
            <span v-if="msg.role === 'assistant' && i === messages.length - 1 && typing" class="typing-cursor">▍</span>
            {{ msg.content }}
          </template>
        </div>
        <div v-if="msg.role === 'user'" class="avatar me">我</div>
      </div>

      <!-- 快捷提问（仅欢迎语时展示） -->
      <div v-if="messages.length <= 1" class="quick-asks fade-up d1">
        <button v-for="q in quickAsks" :key="q" type="button" class="qa-chip press" @click="sendQuick(q)">
          {{ q }}
        </button>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="chat-input">
      <div class="input-box">
        <van-field
          v-model="input"
          placeholder="输入您想咨询的健康问题…"
          :border="false"
          autosize
          rows="1"
          type="textarea"
          class="input-field"
          @keyup.enter="onSend"
        />
      </div>
      <button class="send-btn press" type="button" :disabled="!input.trim() || typing" @click="onSend">
        <van-icon name="arrow-up" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getAccessToken } from '../utils/auth'
import { chatStreamUrl, createSession, getHistory } from '../api/chat'

const route = useRoute()
const messages = ref([])
const input = ref('')
const typing = ref(false)
const scrollRef = ref(null)
// 会话 ID：进入真实会话（/chat/{id}）时由后端历史确定；新对话在首次发送时创建
const sessionId = ref(null)

const quickAsks = ['血压偏高要注意什么？', '老年人适合哪些运动？', '晚上睡不好怎么办？']

function scrollBottom() {
  nextTick(() => {
    if (scrollRef.value) {
      scrollRef.value.scrollTop = scrollRef.value.scrollHeight
    }
  })
}

/** 首次发送时创建会话，会话名取首条消息前 20 字 */
async function ensureSession(firstMessage) {
  if (sessionId.value) return sessionId.value
  const s = await createSession(String(firstMessage).slice(0, 20))
  sessionId.value = s.id
  return sessionId.value
}

/** 加载已有会话的历史消息 */
async function loadHistory(id) {
  try {
    const page = await getHistory(id, { pageNum: 1, pageSize: 50 })
    messages.value = (page.list || []).map((m) => ({ role: m.role, content: m.message }))
    scrollBottom()
  } catch (e) {
    // 历史加载失败不阻断继续对话
  }
}

async function onSend() {
  const text = input.value.trim()
  if (!text || typing.value) return
  input.value = ''
  await doSend(text)
}

async function sendQuick(q) {
  if (typing.value) return
  await doSend(q)
}

async function doSend(text) {
  messages.value.push({ role: 'user', content: text })
  const bot = { role: 'assistant', content: '' }
  messages.value.push(bot)
  typing.value = true
  scrollBottom()

  try {
    const sid = await ensureSession(text)
    const resp = await fetch(chatStreamUrl(), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${getAccessToken()}`
      },
      body: JSON.stringify({ sessionId: sid, message: text })
    })
    if (!resp.ok || !resp.body) {
      // 读取后端统一 JSON 错误（401 未登录 / 403 无权限 / 404 会话不存在等）
      let msg = 'AI 服务暂时不可用，请稍后重试'
      try {
        const err = await resp.json()
        if (err && err.message) msg = err.message
      } catch (e) {
        /* 非 JSON 响应，使用默认提示 */
      }
      throw new Error(msg)
    }
    const reader = resp.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    for (;;) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      // SSE 事件以空行分隔；同一事件内多行 data: 按 \n 连接
      const events = buffer.split('\n\n')
      buffer = events.pop() || ''
      for (const evt of events) {
        const data = evt
          .split('\n')
          .filter((l) => l.startsWith('data:'))
          .map((l) => l.slice(5).replace(/^ /, ''))
          .join('\n')
        if (!data || data === '[DONE]') continue
        bot.content += data
        scrollBottom()
      }
    }
    if (!bot.content) {
      bot.content = 'AI 没有返回内容，请稍后重试。'
    }
  } catch (e) {
    bot.content = e.message || 'AI 服务暂时不可用，请稍后重试'
  } finally {
    typing.value = false
    scrollBottom()
  }
}

onMounted(() => {
  const param = String(route.params.sessionId || '')
  if (/^\d+$/.test(param)) {
    sessionId.value = Number(param)
    loadHistory(sessionId.value)
  }
  const q = route.query.q
  if (q) {
    doSend(String(q))
  }
})
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  background:
    radial-gradient(80% 20% at 50% 0%, rgba(255, 159, 46, 0.08), transparent 70%),
    var(--bg);
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 14px 16px;
  box-sizing: border-box;
}

/* 欢迎语 */
.welcome {
  text-align: center;
  margin: 14px 0 18px;
}

.bot-avatar {
  width: 52px;
  height: 52px;
  margin: 0 auto;
  border-radius: 50%;
  background: var(--sun-grad);
  color: #fff;
  font-size: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 18px rgba(255, 159, 46, 0.35);
}

.welcome p {
  margin: 9px 0 0;
  font-size: 13px;
  color: var(--muted);
}

/* 消息 */
.msg-row {
  display: flex;
  margin-bottom: 14px;
  gap: 9px;
}

.msg-row.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.avatar.ai {
  background: var(--sun-grad);
  font-size: 16px;
  box-shadow: 0 4px 10px rgba(255, 159, 46, 0.3);
}

.avatar.me {
  background: var(--brand-grad);
  box-shadow: 0 4px 10px rgba(0, 180, 134, 0.3);
}

.bubble {
  max-width: 74%;
  padding: 11px 14px;
  border-radius: 4px 16px 16px 16px;
  font-size: 16px;
  line-height: 1.65;
  word-break: break-word;
  background: #fff;
  color: var(--ink);
  box-shadow: var(--shadow-1);
}

.msg-row.user .bubble {
  border-radius: 16px 4px 16px 16px;
  background: var(--brand-grad);
  color: #fff;
}

/* 等待动画 */
.dots {
  display: inline-flex;
  gap: 4px;
  padding: 4px 2px;
}

.dots i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #c3ccd6;
  animation: bounce 1.2s ease-in-out infinite;
}

.dots i:nth-child(2) { animation-delay: 0.15s; }
.dots i:nth-child(3) { animation-delay: 0.3s; }

@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.5; }
  30% { transform: translateY(-5px); opacity: 1; }
}

.typing-cursor {
  animation: blink 1s step-start infinite;
  color: var(--brand);
}

@keyframes blink {
  50% { opacity: 0; }
}

/* 快捷提问 */
.quick-asks {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 6px;
}

.qa-chip {
  border: 1.5px solid rgba(255, 159, 46, 0.35);
  background: rgba(255, 159, 46, 0.07);
  color: #d0761a;
  font-size: 14px;
  padding: 8px 14px;
  border-radius: 999px;
  cursor: pointer;
}

/* 输入区 */
.chat-input {
  display: flex;
  align-items: flex-end;
  gap: 9px;
  padding: 10px 12px calc(10px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(16px);
  border-top: 1px solid var(--line);
}

.input-box {
  flex: 1;
  background: var(--bg);
  border-radius: 14px;
  overflow: hidden;
}

.input-field {
  padding: 4px 6px;
  font-size: 16px;
}

.send-btn {
  width: 42px;
  height: 42px;
  border: none;
  border-radius: 50%;
  background: var(--brand-grad);
  color: #fff;
  font-size: 19px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;
  box-shadow: 0 6px 14px rgba(0, 180, 134, 0.32);
}

.send-btn:disabled {
  background: #c9d3dd;
  box-shadow: none;
}
</style>
