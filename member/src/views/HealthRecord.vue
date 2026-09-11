<template>
  <div class="shell">
    <div class="page-body">
      <!-- 头部 -->
      <header class="sub-hero">
        <div class="sub-hero-deco"></div>
        <button class="back-btn press" type="button" @click="$router.back()">
          <van-icon name="arrow-left" />
        </button>
        <div class="sub-hero-text">
          <h2>健康记录</h2>
          <p>每天一分钟，健康看得见</p>
        </div>
        <button class="trend-btn press" type="button" @click="$router.push('/health/trend')">
          <van-icon name="chart-trending-o" />
          趋势
        </button>
      </header>

      <!-- 录入表单 -->
      <div class="app-card form-card fade-up">
        <div class="metric-grid">
          <div class="metric" :class="{ filled: form.systolic }">
            <label>收缩压 <em>mmHg</em></label>
            <input v-model="form.systolic" type="number" inputmode="numeric" placeholder="如 120" />
            <span class="ref">参考 90-140</span>
          </div>
          <div class="metric" :class="{ filled: form.diastolic }">
            <label>舒张压 <em>mmHg</em></label>
            <input v-model="form.diastolic" type="number" inputmode="numeric" placeholder="如 80" />
            <span class="ref">参考 60-90</span>
          </div>
          <div class="metric" :class="{ filled: form.bloodSugar }">
            <label>血糖 <em>mmol/L</em></label>
            <input v-model="form.bloodSugar" type="number" inputmode="decimal" placeholder="如 5.6" />
            <span class="ref">参考 3.9-6.1</span>
          </div>
          <div class="metric" :class="{ filled: form.heartRate }">
            <label>心率 <em>次/分</em></label>
            <input v-model="form.heartRate" type="number" inputmode="numeric" placeholder="如 72" />
            <span class="ref">参考 60-100</span>
          </div>
          <div class="metric" :class="{ filled: form.weight }">
            <label>体重 <em>kg</em></label>
            <input v-model="form.weight" type="number" inputmode="decimal" placeholder="如 65.5" />
            <span class="ref">自动算 BMI</span>
          </div>
        </div>

        <van-field
          v-model="form.memo"
          class="memo-field"
          type="textarea"
          rows="2"
          maxlength="500"
          show-word-limit
          placeholder="今日备注（选填）：睡眠、饮食、心情…"
        />

        <van-button round block class="save-btn" :loading="saving" @click="onSave">
          保存今日记录
        </van-button>
      </div>

      <!-- 历史记录 -->
      <div class="sec-head">
        <div class="sec-title">最近记录</div>
      </div>

      <div v-if="records.length" class="record-list">
        <div v-for="r in records" :key="r.id" class="app-card record-card">
          <div class="record-head">
            <span class="record-date">{{ fmtFriendly(r.recordedAt) }}</span>
            <span v-if="r.bmi" class="record-bmi">BMI {{ r.bmi }}</span>
          </div>
          <div class="record-pills">
            <span
              v-if="r.systolic || r.diastolic"
              class="pill"
              :class="bpStatus(r) === 'ok' ? 'ok' : 'warn'"
            >血压 {{ r.systolic ?? '-' }}/{{ r.diastolic ?? '-' }}</span>
            <span v-if="r.bloodSugar" class="pill" :class="st('bloodSugar', r.bloodSugar)">
              血糖 {{ r.bloodSugar }}
            </span>
            <span v-if="r.heartRate" class="pill" :class="st('heartRate', r.heartRate)">
              心率 {{ r.heartRate }}
            </span>
            <span v-if="r.weight" class="pill">体重 {{ r.weight }}kg</span>
          </div>
          <p v-if="r.memo" class="record-memo">{{ r.memo }}</p>
        </div>
        <div v-if="hasMore" class="load-more" @click="loadMore">加载更多</div>
      </div>

      <van-empty v-else description="还没有记录，先录入一条吧" />
    </div>

    <TabBar />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { showSuccessToast } from 'vant'
import TabBar from '../components/TabBar.vue'
import { addHealthRecord, listHealthRecords } from '../api/health'
import { vitalStatus, fmtFriendly } from '../utils/format'

const saving = ref(false)
const records = ref([])
const pageNum = ref(1)
const hasMore = ref(false)

const form = reactive({
  systolic: '',
  diastolic: '',
  bloodSugar: '',
  heartRate: '',
  weight: '',
  memo: ''
})

function st(key, val) {
  return vitalStatus(key, val) === 'ok' ? 'ok' : 'warn'
}

function bpStatus(r) {
  const s1 = vitalStatus('systolic', r.systolic)
  const s2 = vitalStatus('diastolic', r.diastolic)
  return s1 === 'ok' && s2 === 'ok' ? 'ok' : 'warn'
}

async function load() {
  try {
    const data = await listHealthRecords({ pageNum: pageNum.value, pageSize: 10 })
    const list = data?.list || []
    records.value = pageNum.value === 1 ? list : records.value.concat(list)
    hasMore.value = data?.pages ? pageNum.value < data.pages : false
  } catch (e) { /* 忽略 */ }
}

function loadMore() {
  pageNum.value++
  load()
}

async function onSave() {
  const payload = {}
  let filled = 0
  for (const key of ['systolic', 'diastolic', 'heartRate']) {
    if (form[key] !== '') {
      payload[key] = Number(form[key])
      filled++
    }
  }
  for (const key of ['bloodSugar', 'weight']) {
    if (form[key] !== '') {
      payload[key] = Number(form[key])
      filled++
    }
  }
  if (!filled) {
    showSuccessToast('请至少填写一项指标')
    return
  }
  if (form.memo) payload.memo = form.memo

  saving.value = true
  try {
    await addHealthRecord(payload)
    showSuccessToast('记录成功，继续保持')
    form.systolic = ''
    form.diastolic = ''
    form.bloodSugar = ''
    form.heartRate = ''
    form.weight = ''
    form.memo = ''
    pageNum.value = 1
    load()
  } catch (e) {
    /* 范围校验错误由拦截器统一提示 */
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
/* 头部 */
.sub-hero {
  position: relative;
  background: linear-gradient(160deg, #00a07a 0%, #00b486 45%, #2ecf9f 100%);
  border-radius: 0 0 26px 26px;
  padding: 24px 16px 30px;
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
  color: #fff;
}

.sub-hero-deco {
  position: absolute;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  top: -60px;
  right: -30px;
}

.back-btn {
  position: relative;
  z-index: 1;
  border: none;
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
}

.sub-hero-text {
  position: relative;
  z-index: 1;
  flex: 1;
}

.sub-hero-text h2 {
  margin: 0;
  font-size: 21px;
  font-weight: 800;
}

.sub-hero-text p {
  margin: 4px 0 0;
  font-size: 13px;
  opacity: 0.85;
}

.trend-btn {
  position: relative;
  z-index: 1;
  border: none;
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  padding: 8px 14px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  flex-shrink: 0;
}

/* 录入表单 */
.form-card {
  margin-top: -14px;
  position: relative;
  z-index: 2;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.metric {
  background: var(--bg);
  border: 1.5px solid transparent;
  border-radius: 14px;
  padding: 10px 12px;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.metric.filled {
  border-color: rgba(0, 180, 134, 0.5);
  background: rgba(0, 180, 134, 0.05);
}

.metric label {
  display: flex;
  align-items: baseline;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-2);
}

.metric label em {
  font-style: normal;
  font-size: 11px;
  color: var(--muted);
}

.metric input {
  width: 100%;
  border: none;
  background: none;
  outline: none;
  font-size: 22px;
  font-weight: 800;
  color: var(--ink);
  padding: 6px 0 2px;
  font-family: inherit;
}

.metric input::placeholder {
  font-size: 14px;
  font-weight: 400;
  color: #c3cbd4;
}

.ref {
  font-size: 11px;
  color: var(--muted);
}

.memo-field {
  margin-top: 10px;
  background: var(--bg);
  border-radius: 14px;
  padding: 4px 0;
}

.memo-field :deep(.van-cell::after) {
  display: none;
}

.save-btn {
  margin-top: 14px;
  background: var(--brand-grad);
  border: none;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  box-shadow: 0 8px 20px rgba(0, 180, 134, 0.3);
}

/* 记录列表 */
.record-list {
  padding-bottom: 4px;
}

.record-card {
  padding: 14px 16px;
}

.record-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-date {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-2);
}

.record-bmi {
  font-size: 12px;
  color: var(--sky);
  background: rgba(63, 124, 255, 0.1);
  padding: 2px 8px;
  border-radius: 999px;
}

.record-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.pill {
  font-size: 13px;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: 999px;
  background: var(--bg);
  color: var(--ink-2);
}

.pill.ok {
  color: var(--brand-deep);
  background: var(--brand-soft);
}

.pill.warn {
  color: #ff7a00;
  background: var(--sun-soft);
}

.record-memo {
  margin: 10px 0 0;
  font-size: 13px;
  color: var(--muted);
  line-height: 1.6;
}

.load-more {
  text-align: center;
  color: var(--brand-deep);
  font-size: 14px;
  font-weight: 600;
  padding: 10px 0 14px;
  cursor: pointer;
}
</style>
