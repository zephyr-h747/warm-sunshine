<template>
  <div class="shell act-shell">
    <div class="page-body">
      <van-nav-bar title="社区活动" left-arrow @click-left="$router.back()" />

      <!-- 状态胶囊 -->
      <div class="chips fade-up">
        <button
          v-for="f in filters"
          :key="f.key"
          class="chip press"
          :class="{ on: status === f.key }"
          type="button"
          @click="switchTab(f.key)"
        >
          {{ f.label }}
        </button>
      </div>

      <template v-if="list.length">
        <div
          v-for="(act, i) in list"
          :key="act.id"
          class="app-card act-card press fade-up"
          :class="'d' + Math.min(i + 1, 5)"
          @click="$router.push(`/activity/${act.id}`)"
        >
          <div class="act-cover" :class="'cover-' + (act.id % 4)">
            <van-icon name="flag-o" />
            <span class="act-tag">{{ statusText(act.status) }}</span>
          </div>
          <div class="act-body">
            <p class="act-title">{{ act.title }}</p>
            <div class="act-meta">
              <van-icon name="clock-o" />
              <span>{{ fmtDate(act.activityStart) }}</span>
              <span v-if="act.registered" class="mine-chip">
                {{ act.checkInStatus === 'CHECKED_IN' ? '已签到' : '已报名' }}
              </span>
            </div>
            <div class="act-cap">
              <div class="cap-track">
                <i
                  class="cap-bar"
                  :class="{ hot: filledRate(act) >= 0.8 }"
                  :style="{ width: filledRate(act) * 100 + '%' }"
                ></i>
              </div>
              <span class="cap-text">余 {{ act.remaining }} 名额</span>
            </div>
          </div>
        </div>
      </template>
      <van-empty v-else description="暂无活动" class="fade-up d1" />
    </div>
    <TabBar />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import TabBar from '../components/TabBar.vue'
import { listActivities } from '../api/activity'
import { ACT_STATUS, fmtDate } from '../utils/format'

const filters = [
  { key: 'REGISTRATING', label: '报名中' },
  { key: 'IN_PROGRESS', label: '进行中' },
  { key: '', label: '全部' }
]

const status = ref('REGISTRATING')
const list = ref([])

function statusText(s) {
  return ACT_STATUS[s]?.text || s
}

function filledRate(act) {
  const max = act.maxParticipants || 1
  const used = max - (act.remaining ?? 0)
  return Math.max(0, Math.min(1, used / max))
}

function switchTab(key) {
  status.value = key
  load()
}

async function load() {
  try {
    const data = await listActivities({ status: status.value, pageNum: 1, pageSize: 20 })
    list.value = data?.list || []
  } catch (e) { /* 忽略 */ }
}

load()
</script>

<style scoped>
.act-shell {
  background:
    radial-gradient(90% 22% at 50% 0%, rgba(255, 159, 46, 0.08), transparent 70%),
    var(--bg);
}

/* 状态胶囊 */
.chips {
  display: flex;
  gap: 8px;
  padding: 10px 14px 6px;
}

.chip {
  flex: 1;
  border: 1.5px solid var(--line);
  background: #fff;
  color: var(--ink-2);
  font-size: 14px;
  font-weight: 600;
  padding: 8px 0;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.chip.on {
  border-color: transparent;
  background: var(--sun-grad);
  color: #fff;
  box-shadow: 0 6px 14px rgba(255, 159, 46, 0.32);
}

/* 活动卡 */
.act-card {
  display: flex;
  padding: 0;
  overflow: hidden;
  cursor: pointer;
}

.act-cover {
  width: 96px;
  flex-shrink: 0;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  color: rgba(255, 255, 255, 0.85);
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
  padding: 13px 14px;
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
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 8px;
  font-size: 12px;
  color: var(--muted);
}

/* 我的报名标记 */
.mine-chip {
  margin-left: auto;
  background: var(--brand-soft);
  color: var(--brand-deep);
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 999px;
}

/* 名额进度 */
.act-cap {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}

.cap-track {
  flex: 1;
  height: 6px;
  background: var(--bg);
  border-radius: 999px;
  overflow: hidden;
}

.cap-bar {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: var(--sun-grad);
}

.cap-bar.hot {
  background: linear-gradient(90deg, #ff5b7f, #ff8fa5);
}

.cap-text {
  font-size: 11px;
  color: var(--muted);
  white-space: nowrap;
}
</style>
