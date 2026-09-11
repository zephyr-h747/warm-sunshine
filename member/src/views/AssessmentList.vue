<template>
  <div class="shell asm-shell">
    <div class="page-body">
      <van-nav-bar title="健康评测" left-arrow @click-left="$router.back()" />

      <!-- 头部引导 -->
      <div class="hero fade-up">
        <div class="hero-deco deco-1"></div>
        <div class="hero-deco deco-2"></div>
        <div class="hero-inner">
          <div class="hero-icon"><van-icon name="notes-o" /></div>
          <div>
            <p class="hero-title">健康自测量表</p>
            <p class="hero-desc">几分钟了解身体状况，完成可获积分</p>
          </div>
        </div>
      </div>

      <!-- 可参与问卷 -->
      <template v-if="list.length">
        <div class="sec-head fade-up d1">
          <div class="sec-title">可参与的评测</div>
          <span class="sec-more">{{ list.length }} 份量表</span>
        </div>
        <div
          v-for="(q, i) in list"
          :key="q.id"
          class="app-card qa-card press fade-up"
          :class="'d' + Math.min(i + 1, 5)"
          @click="$router.push(`/assessment/${q.id}`)"
        >
          <div class="qa-icon"><van-icon name="records" /></div>
          <div class="qa-body">
            <p class="qa-title">{{ q.title }}</p>
            <p class="qa-desc">{{ q.description || '参与评测，获取个性化健康建议' }}</p>
          </div>
          <van-icon name="arrow" class="qa-arrow" />
        </div>
      </template>
      <van-empty v-else description="暂无已发布问卷" class="fade-up d1" />

      <!-- 历史结果 -->
      <template v-if="historyList.length">
        <div class="sec-head fade-up">
          <div class="sec-title">我的评测记录</div>
        </div>
        <div class="app-card hist-card fade-up">
          <div
            v-for="h in historyList"
            :key="h.id"
            class="hist-row press"
            @click="$router.push(`/assessment/result/${h.id}`)"
          >
            <div class="hist-score" :class="scoreGrade(h.aiScore)">
              {{ h.aiScore }}<i>分</i>
            </div>
            <div class="hist-body">
              <p class="hist-title">{{ h.questionnaireTitle }}</p>
              <p class="hist-time">{{ fmtFriendly(h.createTime) }}</p>
            </div>
            <van-icon name="arrow" class="qa-arrow" />
          </div>
        </div>
      </template>
    </div>
    <TabBar />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import TabBar from '../components/TabBar.vue'
import { listQuestionnaires, history } from '../api/assessment'
import { fmtFriendly } from '../utils/format'

const list = ref([])
const historyList = ref([])

function scoreGrade(s) {
  if (s >= 85) return 'good'
  if (s >= 60) return 'mid'
  return 'bad'
}

onMounted(async () => {
  try {
    const data = await listQuestionnaires({ pageNum: 1, pageSize: 20 })
    list.value = data?.list || []
  } catch (e) { /* 忽略 */ }
  try {
    const h = await history({ pageNum: 1, pageSize: 10 })
    historyList.value = h?.list || []
  } catch (e) { /* 忽略 */ }
})
</script>

<style scoped>
.asm-shell {
  background:
    radial-gradient(90% 22% at 50% 0%, rgba(124, 92, 255, 0.08), transparent 70%),
    var(--bg);
}

/* 头部引导 */
.hero {
  position: relative;
  margin: 8px 14px 6px;
  border-radius: var(--r-lg);
  background: linear-gradient(140deg, #6d4df6 0%, #7c5cff 45%, #a78bfa 100%);
  padding: 18px 16px;
  overflow: hidden;
  box-shadow: 0 12px 28px rgba(124, 92, 255, 0.28);
}

.hero-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}

.deco-1 { width: 120px; height: 120px; top: -50px; right: -30px; }
.deco-2 { width: 60px; height: 60px; bottom: -20px; left: 30%; opacity: 0.7; }

.hero-inner {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  color: #fff;
}

.hero-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
}

.hero-title {
  margin: 0;
  font-size: 19px;
  font-weight: 800;
  letter-spacing: 1px;
}

.hero-desc {
  margin: 5px 0 0;
  font-size: 13px;
  opacity: 0.85;
}

/* 问卷卡 */
.qa-card {
  display: flex;
  align-items: center;
  gap: 13px;
  cursor: pointer;
}

.qa-icon {
  width: 48px;
  height: 48px;
  border-radius: 15px;
  background: var(--violet-grad);
  color: #fff;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 6px 14px rgba(124, 92, 255, 0.3);
}

.qa-body {
  flex: 1;
  min-width: 0;
}

.qa-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.qa-desc {
  margin: 5px 0 0;
  font-size: 13px;
  color: var(--muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.qa-arrow {
  color: #c3ccd6;
  flex-shrink: 0;
}

/* 历史记录 */
.hist-card {
  padding: 4px 16px;
}

.hist-row {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 13px 0;
  cursor: pointer;
}

.hist-row + .hist-row {
  border-top: 1px solid var(--line);
}

.hist-score {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 1px;
  font-size: 20px;
  font-weight: 800;
  color: #fff;
  flex-shrink: 0;
}

.hist-score i {
  font-size: 11px;
  font-style: normal;
  font-weight: 600;
}

.hist-score.good { background: var(--brand-grad); }
.hist-score.mid { background: var(--sun-grad); }
.hist-score.bad { background: var(--rose-grad); }

.hist-body {
  flex: 1;
  min-width: 0;
}

.hist-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hist-time {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--muted);
}
</style>
