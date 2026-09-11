<template>
  <div class="shell quiz-shell">
    <div class="page-body">
      <van-nav-bar title="健康答题" left-arrow @click-left="$router.back()" />

      <template v-if="qa">
        <!-- 进度条 -->
        <div class="prog-wrap fade-up">
          <div class="prog-info">
            <span class="prog-title">{{ qa.title }}</span>
            <span class="prog-count">已答 <b>{{ answered }}</b> / {{ total }}</span>
          </div>
          <div class="prog-track"><i class="prog-bar" :style="{ width: progress + '%' }"></i></div>
        </div>

        <div v-if="qa.description" class="desc-banner fade-up">{{ qa.description }}</div>

        <!-- 题目 -->
        <div
          v-for="(q, idx) in qa.questions"
          :key="q.id"
          class="app-card q-card fade-up"
          :class="'d' + Math.min(idx + 1, 5)"
        >
          <div class="q-head">
            <span class="q-no">{{ idx + 1 }}</span>
            <p class="q-title">{{ q.content }}</p>
            <span class="q-type">{{ typeLabel(q.type) }}</span>
          </div>

          <!-- 单选 -->
          <div v-if="q.type === 'SINGLE'" class="opts">
            <button
              v-for="opt in q.options"
              :key="opt"
              type="button"
              class="opt press"
              :class="{ on: answers[q.id] === opt }"
              @click="answers[q.id] = opt"
            >
              <i class="opt-check"></i>
              <span>{{ opt }}</span>
            </button>
          </div>

          <!-- 多选 -->
          <div v-else-if="q.type === 'MULTIPLE'" class="opts">
            <button
              v-for="opt in q.options"
              :key="opt"
              type="button"
              class="opt press square"
              :class="{ on: (answers[q.id] || []).includes(opt) }"
              @click="toggleMulti(q.id, opt)"
            >
              <i class="opt-check"></i>
              <span>{{ opt }}</span>
            </button>
          </div>

          <!-- 文本 -->
          <van-field
            v-else
            v-model="answers[q.id]"
            type="textarea"
            rows="2"
            autosize
            placeholder="请输入您的回答"
            class="opt-text"
          />
        </div>

        <!-- 提交 -->
        <div class="submit-bar fade-up">
          <van-button round block class="submit-btn" :loading="submitting" @click="onSubmit">
            提交评测 · 预得 <em>20</em> 积分
          </van-button>
        </div>
      </template>

      <van-empty v-else-if="loaded" description="问卷不存在或未发布" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccessToast, showToast } from 'vant'
import { getQuestionnaire, submitAssessment } from '../api/assessment'

const route = useRoute()
const router = useRouter()
const qa = ref(null)
const loaded = ref(false)
const submitting = ref(false)
const answers = reactive({})

const total = computed(() => qa.value?.questions?.length || 0)

const answered = computed(() => {
  const qs = qa.value?.questions || []
  return qs.filter((q) => {
    const v = answers[q.id]
    return Array.isArray(v) ? v.length > 0 : v != null && v !== ''
  }).length
})

const progress = computed(() => (total.value ? Math.round((answered.value / total.value) * 100) : 0))

function typeLabel(t) {
  return t === 'SINGLE' ? '单选' : t === 'MULTIPLE' ? '多选' : '问答'
}

function toggleMulti(qid, opt) {
  if (!answers[qid]) answers[qid] = []
  const arr = answers[qid]
  const i = arr.indexOf(opt)
  if (i >= 0) arr.splice(i, 1)
  else arr.push(opt)
}

async function onSubmit() {
  const questions = qa.value.questions
  if (!questions.every((q) => (answers[q.id] ?? '') !== '' && (!(answers[q.id] instanceof Array) || answers[q.id].length))) {
    showToast('请完成所有题目')
    return
  }
  const answerItems = questions.map((q) => {
    let value = answers[q.id]
    if (Array.isArray(value)) value = value.join('、')
    return { qid: q.id, type: q.type, value: value == null ? '' : value }
  })
  submitting.value = true
  try {
    const result = await submitAssessment({ questionnaireId: qa.value.id, answers: answerItems })
    showSuccessToast('评测完成')
    router.replace(`/assessment/result/${result.id}`)
  } catch (e) {
    /* 统一提示 */
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const data = await getQuestionnaire(route.params.id)
    qa.value = data
  } catch (e) {
    /* 未发布/不存在 */
  } finally {
    loaded.value = true
  }
})
</script>

<style scoped>
.quiz-shell {
  background:
    radial-gradient(90% 20% at 50% 0%, rgba(124, 92, 255, 0.07), transparent 70%),
    var(--bg);
}

/* 进度 */
.prog-wrap {
  margin: 8px 14px 12px;
}

.prog-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.prog-title {
  font-size: 17px;
  font-weight: 800;
  color: var(--ink);
}

.prog-count {
  font-size: 13px;
  color: var(--muted);
}

.prog-count b {
  color: var(--violet);
  font-size: 15px;
}

.prog-track {
  height: 8px;
  background: #e8ecf2;
  border-radius: 999px;
  overflow: hidden;
}

.prog-bar {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: var(--violet-grad);
  transition: width 0.4s ease;
}

/* 说明横幅 */
.desc-banner {
  margin: 0 14px 12px;
  padding: 11px 14px;
  background: rgba(124, 92, 255, 0.08);
  border-left: 3px solid var(--violet);
  border-radius: 0 12px 12px 0;
  font-size: 14px;
  color: var(--ink-2);
  line-height: 1.6;
}

/* 题卡 */
.q-head {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.q-no {
  width: 26px;
  height: 26px;
  border-radius: 9px;
  background: var(--violet-grad);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 1px;
}

.q-title {
  flex: 1;
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--ink);
  line-height: 1.55;
}

.q-type {
  font-size: 11px;
  color: var(--violet);
  background: rgba(124, 92, 255, 0.1);
  padding: 2px 8px;
  border-radius: 999px;
  flex-shrink: 0;
  margin-top: 3px;
}

/* 选项 */
.opts {
  display: flex;
  flex-direction: column;
  gap: 9px;
  margin-top: 13px;
}

.opt {
  display: flex;
  align-items: center;
  gap: 11px;
  text-align: left;
  border: 1.5px solid var(--line);
  background: #fff;
  border-radius: 13px;
  padding: 12px 14px;
  font-size: 16px;
  color: var(--ink-2);
  cursor: pointer;
  transition: all 0.2s ease;
}

.opt.on {
  border-color: var(--brand);
  background: var(--brand-soft);
  color: var(--brand-deep);
  font-weight: 600;
}

.opt-check {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1.5px solid #cfd8e0;
  flex-shrink: 0;
  position: relative;
  transition: all 0.2s ease;
}

.opt.square .opt-check {
  border-radius: 6px;
}

.opt.on .opt-check {
  border-color: var(--brand);
  background: var(--brand-grad);
}

.opt.on .opt-check::after {
  content: '';
  position: absolute;
  left: 6px;
  top: 3px;
  width: 4px;
  height: 9px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

/* 提交 */
.submit-bar {
  padding: 18px 16px 30px;
}

.submit-btn {
  border: none;
  background: var(--violet-grad);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  box-shadow: 0 10px 24px rgba(124, 92, 255, 0.35);
}

.submit-btn em {
  font-style: normal;
  color: #ffe08a;
}

.opt-text {
  margin-top: 12px;
  background: var(--bg);
  border-radius: 12px;
  padding: 4px;
}
</style>
