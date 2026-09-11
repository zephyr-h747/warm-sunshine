<template>
  <div class="aurora result-page">
    <van-nav-bar title="评测结果" left-arrow @click-left="$router.back()" />

    <div v-if="result" class="result-body">
      <!-- 圆环 -->
      <div class="ring-wrap fade-up">
        <svg class="ring" viewBox="0 0 200 200">
          <circle class="ring-bg" cx="100" cy="100" r="86" />
          <circle
            class="ring-fg"
            :class="grade"
            cx="100"
            cy="100"
            r="86"
            :stroke-dasharray="CIRC"
            :stroke-dashoffset="CIRC - (CIRC * shown) / 100"
          />
        </svg>
        <div class="ring-center">
          <p class="score">{{ shown }}</p>
          <p class="score-unit">健康分</p>
        </div>
      </div>

      <div class="grade-chip fade-up d1" :class="grade">{{ gradeText }}</div>
      <p class="qa-title fade-up d1">{{ result.questionnaireTitle }}</p>

      <!-- AI 建议 -->
      <div class="app-card sug-card fade-up d2">
        <div class="sug-head">
          <span class="sug-icon"><van-icon name="bulb-o" /></span>
          <span>AI 健康建议</span>
        </div>
        <p class="sug-text">{{ result.aiSuggestion || '暂无建议' }}</p>
      </div>

      <p class="time fade-up d3">完成于 {{ fmtDateTime(result.createTime) }}</p>

      <div class="btns fade-up d3">
        <van-button round block class="btn-main" @click="$router.replace('/assessment')">
          返回评测列表
        </van-button>
        <van-button round block plain class="btn-sub" type="primary" @click="$router.push('/health/trend')">
          查看健康趋势
        </van-button>
      </div>
    </div>

    <van-empty v-else description="结果不存在" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getResult } from '../api/assessment'
import { animateNumber, fmtDateTime } from '../utils/format'

const CIRC = 2 * Math.PI * 86

const route = useRoute()
const result = ref(null)
const shown = ref(0)

const grade = computed(() => {
  const s = result.value?.aiScore ?? 0
  if (s >= 85) return 'good'
  if (s >= 60) return 'mid'
  return 'bad'
})

const gradeText = computed(() => {
  return { good: '状态优秀，请继续保持', mid: '状态尚可，仍有提升空间', bad: '建议多加关注，及时调整' }[grade.value]
})

onMounted(async () => {
  try {
    result.value = await getResult(route.params.id)
    const target = result.value?.aiScore ?? 0
    animateNumber((v) => (shown.value = v), target, 1200)
  } catch (e) {
    /* 统一提示 */
  }
})
</script>

<style scoped>
.result-page {
  overflow-y: auto;
}

.result-body {
  padding: 8px 20px 40px;
  text-align: center;
}

/* 圆环 */
.ring-wrap {
  position: relative;
  width: 190px;
  margin: 14px auto 0;
}

.ring {
  width: 100%;
  transform: rotate(-90deg);
}

.ring-bg {
  fill: none;
  stroke: rgba(28, 43, 58, 0.07);
  stroke-width: 13;
}

.ring-fg {
  fill: none;
  stroke-width: 13;
  stroke-linecap: round;
  transition: stroke-dashoffset 1.2s cubic-bezier(0.22, 0.8, 0.36, 1);
}

.ring-fg.good { stroke: #00b486; }
.ring-fg.mid { stroke: #ff9f2e; }
.ring-fg.bad { stroke: #ff5b7f; }

.ring-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.score {
  margin: 0;
  font-size: 52px;
  font-weight: 800;
  line-height: 1;
  color: var(--ink);
}

.score-unit {
  margin: 8px 0 0;
  font-size: 14px;
  color: var(--muted);
}

/* 等级 */
.grade-chip {
  display: inline-block;
  margin-top: 14px;
  font-size: 15px;
  font-weight: 700;
  padding: 7px 18px;
  border-radius: 999px;
  color: #fff;
}

.grade-chip.good { background: var(--brand-grad); box-shadow: 0 8px 18px rgba(0, 180, 134, 0.32); }
.grade-chip.mid { background: var(--sun-grad); box-shadow: 0 8px 18px rgba(255, 159, 46, 0.32); }
.grade-chip.bad { background: var(--rose-grad); box-shadow: 0 8px 18px rgba(255, 91, 127, 0.32); }

.qa-title {
  margin: 12px 0 0;
  font-size: 20px;
  font-weight: 800;
  color: var(--ink);
}

/* 建议 */
.sug-card {
  margin-top: 18px;
  text-align: left;
}

.sug-head {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
}

.sug-icon {
  width: 30px;
  height: 30px;
  border-radius: 10px;
  background: var(--sun-soft);
  color: var(--sun);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.sug-text {
  margin: 12px 0 0;
  font-size: 16px;
  line-height: 1.8;
  color: var(--ink-2);
  white-space: pre-wrap;
}

.time {
  margin: 14px 0 0;
  font-size: 13px;
  color: var(--muted);
}

.btns {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 22px;
}

.btn-main {
  border: none;
  background: var(--brand-grad);
  color: #fff;
  font-weight: 700;
  box-shadow: 0 10px 24px rgba(0, 180, 134, 0.32);
}

.btn-sub {
  border-color: var(--brand);
}
</style>
