<template>
  <div class="page detail-page">
    <!-- 顶部返回 -->
    <div class="toolbar topbar">
      <el-button :icon="ArrowLeft" circle @click="goBack" />
      <span class="title">会员详情</span>
      <span class="title-sub">Member Profile</span>
    </div>

    <div v-loading="loading" class="detail-body">
      <!-- 会员档案头卡 -->
      <div class="profile-hero">
        <div class="hero-deco deco-1"></div>
        <div class="hero-deco deco-2"></div>
        <div class="hero-inner">
          <div class="avatar">{{ avatarChar }}</div>
          <div class="hero-main">
            <div class="hero-name-row">
              <h2 class="hero-name">{{ member.realName || member.phone }}</h2>
              <el-tag :type="member.status === 'ENABLED' ? 'success' : 'danger'" effect="dark" round>
                {{ member.status === 'ENABLED' ? '正常' : '已禁用' }}
              </el-tag>
              <el-tag :class="'level-' + (member.memberLevel || 'NORMAL')" effect="dark" round>
                {{ levelText }}
              </el-tag>
            </div>
            <p class="hero-meta">
              <el-icon><Iphone /></el-icon>
              {{ member.phone }}
              <el-divider direction="vertical" />
              {{ genderText }}
              <template v-if="member.birthDate">
                <el-divider direction="vertical" />
                {{ member.birthDate }}
              </template>
            </p>
          </div>
          <div class="hero-points">
            <div class="hp-item">
              <p class="hp-num">{{ member.points ?? 0 }}</p>
              <p class="hp-label">积分</p>
            </div>
            <i class="hp-divider"></i>
            <div class="hp-item">
              <p class="hp-num">{{ (member.recentHealthRecords || []).length }}</p>
              <p class="hp-label">健康记录</p>
            </div>
            <i class="hp-divider"></i>
            <div class="hp-item">
              <p class="hp-num">{{ (member.appointments || []).length }}</p>
              <p class="hp-label">体检预约</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 健康趋势 -->
      <div class="trend-card">
        <div class="section-head">
          <h3 class="section-title">健康趋势</h3>
          <span class="section-sub">近 6 个月关键指标走势</span>
        </div>
        <div v-if="trends.length" class="indicator-chips">
          <button
            v-for="(t, i) in trends"
            :key="t.indicator"
            class="ind-chip"
            :class="{ on: i === activeIdx }"
            type="button"
            @click="activeIdx = i"
          >
            {{ t.indicatorName }}
          </button>
        </div>
        <div v-if="trends.length" class="trend-grid">
          <div class="trend-chart-wrap">
            <div ref="chartRef" class="trend-chart"></div>
            <div v-if="currentRange" class="range-tip">
              <i class="range-swatch"></i>正常参考区间 {{ currentRange.min }} ~ {{ currentRange.max }} {{ currentRange.unit }}
            </div>
          </div>
          <div v-if="current" class="trend-stats">
            <div class="ts-item">
              <p class="ts-val">{{ fmt(current.avgValue) }}</p>
              <p class="ts-lab">平均值</p>
            </div>
            <div class="ts-item hi">
              <p class="ts-val">{{ fmt(current.maxValue) }}</p>
              <p class="ts-lab">最高</p>
            </div>
            <div class="ts-item lo">
              <p class="ts-val">{{ fmt(current.minValue) }}</p>
              <p class="ts-lab">最低</p>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无健康趋势数据" :image-size="70" />
      </div>

      <!-- 最近健康记录 -->
      <div class="table-card section">
        <div class="section-head">
          <h3 class="section-title">最近健康记录</h3>
        </div>
        <el-table :data="member.recentHealthRecords || []" stripe>
          <el-table-column prop="recordedAt" label="记录时间" min-width="170">
            <template #default="{ row }">{{ fmtTime(row.recordedAt) }}</template>
          </el-table-column>
          <el-table-column label="血压" width="120">
            <template #default="{ row }">
              <span :class="bpClass(row)">{{ bpText(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="bloodSugar" label="血糖" width="90">
            <template #default="{ row }">
              <span :class="vitalClass('bloodSugar', row.bloodSugar)">{{ row.bloodSugar ?? '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="heartRate" label="心率" width="90">
            <template #default="{ row }">
              <span :class="vitalClass('heartRate', row.heartRate)">{{ row.heartRate ?? '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="weight" label="体重" width="90" />
          <el-table-column prop="bmi" label="BMI" width="90" />
          <el-table-column prop="memo" label="备注" min-width="140" show-overflow-tooltip />
        </el-table>
      </div>

      <!-- 体检预约 -->
      <div class="table-card section">
        <div class="section-head">
          <h3 class="section-title">体检预约</h3>
        </div>
        <el-table :data="member.appointments || []" stripe>
          <el-table-column prop="id" label="预约ID" width="80" />
          <el-table-column prop="packageName" label="套餐" min-width="140" />
          <el-table-column prop="appointDate" label="体检日期" width="120" />
          <el-table-column prop="timeRange" label="时段" width="130" />
          <el-table-column prop="price" label="价格" width="90" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="apptTagType(row.status)" round>{{ apptText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="170">
            <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Iphone } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import * as memberApi from '../../api/member'
import { getHealthTrend } from '../../api/health'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const member = ref({})
const trends = ref([])
const activeIdx = ref(0)
const chartRef = ref(null)
let chart = null

const levelMap = { NORMAL: '普通', SILVER: '银卡', GOLD: '金卡', PLATINUM: '铂金', DIAMOND: '钻石' }
const apptMap = { PENDING: '待确认', CONFIRMED: '已确认', CANCELED: '已取消', COMPLETED: '已完成' }
const apptTagMap = { PENDING: 'warning', CONFIRMED: 'success', CANCELED: 'info', COMPLETED: '' }

/* 指标正常区间（key 与后端 indicator 小写一致） */
const VITAL_RANGES = {
  systolic: { unit: 'mmHg', min: 90, max: 140 },
  diastolic: { unit: 'mmHg', min: 60, max: 90 },
  blood_sugar: { unit: 'mmol/L', min: 3.9, max: 6.1 },
  heart_rate: { unit: '次/分', min: 60, max: 100 },
  weight: { unit: 'kg', min: 0, max: 0 },
  bmi: { unit: '', min: 18.5, max: 24 }
}

const genderText = computed(() => {
  if (member.value.gender === 'MALE') return '男'
  if (member.value.gender === 'FEMALE') return '女'
  return '性别未填'
})
const levelText = computed(() => levelMap[member.value.memberLevel] || member.value.memberLevel || '普通')
const avatarChar = computed(() => (member.value.realName || member.value.phone || '?').charAt(0))

const current = computed(() => trends.value[activeIdx.value])
const currentRange = computed(() => {
  const t = current.value
  if (!t) return null
  const r = VITAL_RANGES[t.indicator?.toLowerCase()]
  return r && r.max > 0 ? r : null
})

function fmt(v) {
  return v == null ? '--' : v
}

function fmtTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : '-'
}

function bpText(row) {
  return row.systolic && row.diastolic ? row.systolic + '/' + row.diastolic : '-'
}

function bpClass(row) {
  if (!row.systolic || !row.diastolic) return ''
  const ok = row.systolic >= 90 && row.systolic <= 140 && row.diastolic >= 60 && row.diastolic <= 90
  return ok ? 'vital-ok' : 'vital-warn'
}

function vitalClass(key, value) {
  if (value == null || value === '') return ''
  const r = VITAL_RANGES[key]
  if (!r || (r.min === 0 && r.max === 0)) return 'vital-ok'
  return Number(value) >= r.min && Number(value) <= r.max ? 'vital-ok' : 'vital-warn'
}

function apptText(s) {
  return apptMap[s] || s
}
function apptTagType(s) {
  return apptTagMap[s] || 'info'
}

function goBack() {
  router.back()
}

function renderChart() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const t = current.value
  const recs = (t && t.records) || []
  const r = currentRange.value
  chart.setOption(
    {
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(28,43,58,.92)',
        borderWidth: 0,
        textStyle: { color: '#fff', fontSize: 13 },
        formatter: (params) => {
          const p = params[0]
          return `${p.axisValue}<br/><b style="color:#3ad6a8">${p.value}</b> ${r ? r.unit : ''}`
        }
      },
      grid: { left: 48, right: 20, top: 28, bottom: 30 },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: recs.map((x) => x.date.slice(5)),
        axisLine: { lineStyle: { color: '#e6ebf1' } },
        axisTick: { show: false },
        axisLabel: { fontSize: 11, color: '#93a0af' }
      },
      yAxis: {
        type: 'value',
        scale: true,
        axisLabel: { fontSize: 11, color: '#93a0af' },
        splitLine: { lineStyle: { color: '#f2f5f9' } }
      },
      series: [
        {
          name: t ? t.indicatorName : '',
          type: 'line',
          smooth: 0.35,
          symbol: 'circle',
          symbolSize: 7,
          showSymbol: recs.length <= 14,
          data: recs.map((x) => x.value),
          lineStyle: { width: 3, color: '#00b486' },
          itemStyle: {
            color: '#fff',
            borderColor: '#00b486',
            borderWidth: 2,
            shadowColor: 'rgba(0,180,134,.3)',
            shadowBlur: 6
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(0,180,134,.28)' },
              { offset: 1, color: 'rgba(0,180,134,.02)' }
            ])
          },
          markArea: r
            ? {
                silent: true,
                itemStyle: { color: 'rgba(63,124,255,.06)' },
                label: { show: false },
                data: [[{ yAxis: r.min }, { yAxis: r.max }]]
              }
            : undefined
        }
      ]
    },
    true
  )
}

watch(activeIdx, async () => {
  await nextTick()
  renderChart()
})

onMounted(async () => {
  loading.value = true
  try {
    member.value = await memberApi.getMember(route.params.id)
  } finally {
    loading.value = false
  }
  try {
    trends.value = (await getHealthTrend(route.params.id)) || []
  } catch (e) {
    trends.value = []
  }
  await nextTick()
  renderChart()
  window.addEventListener('resize', resizeChart)
})

function resizeChart() {
  if (chart) chart.resize()
}

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<style scoped>
.detail-page {
  max-width: 1280px;
  margin: 0 auto;
}

.topbar {
  border-radius: 14px;
  border: 1px solid #edf1f6;
}

.title {
  font-size: 17px;
  font-weight: 700;
  color: #1c2b3a;
}

.title-sub {
  font-size: 12px;
  color: #9aa7b5;
  letter-spacing: 0.5px;
}

/* 档案头卡 */
.profile-hero {
  position: relative;
  overflow: hidden;
  border-radius: 18px;
  padding: 28px 30px;
  background: linear-gradient(135deg, #0b7a5e 0%, #0e9f78 45%, #13c291 100%);
  box-shadow: 0 16px 36px rgba(13, 160, 120, 0.28);
  color: #fff;
}

.hero-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}

.deco-1 {
  width: 240px;
  height: 240px;
  top: -110px;
  right: -60px;
}

.deco-2 {
  width: 110px;
  height: 110px;
  bottom: -46px;
  left: 32%;
  opacity: 0.6;
}

.hero-inner {
  position: relative;
  display: flex;
  align-items: center;
  gap: 22px;
  flex-wrap: wrap;
}

.avatar {
  width: 68px;
  height: 68px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.22);
  border: 1.5px solid rgba(255, 255, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 800;
  flex-shrink: 0;
}

.hero-main {
  flex: 1;
  min-width: 220px;
}

.hero-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-name {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  line-height: 1.2;
}

.hero-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 10px 0 0;
  font-size: 13.5px;
  opacity: 0.92;
}

.hero-points {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 14px 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(6px);
}

.hp-item {
  text-align: center;
  min-width: 56px;
}

.hp-num {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  line-height: 1.2;
}

.hp-label {
  margin: 3px 0 0;
  font-size: 12px;
  opacity: 0.85;
}

.hp-divider {
  width: 1px;
  height: 30px;
  background: rgba(255, 255, 255, 0.3);
}

/* 会员等级徽章配色 */
:deep(.level-NORMAL) {
  --el-tag-bg-color: rgba(255, 255, 255, 0.25);
  --el-tag-border-color: rgba(255, 255, 255, 0.4);
  --el-tag-text-color: #fff;
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
  color: #fff;
}

/* 趋势卡 */
.trend-card {
  margin-top: 16px;
  padding: 20px 22px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #edf1f6;
}

.section-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 14px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1c2b3a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 15px;
  border-radius: 2px;
  background: linear-gradient(180deg, #13c291, #0b7a5e);
}

.section-sub {
  font-size: 12px;
  color: #9aa7b5;
}

.indicator-chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.ind-chip {
  border: 1.5px solid #e6ebf1;
  background: #fff;
  color: #5b6b7c;
  font-size: 13px;
  font-weight: 600;
  padding: 6px 16px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ind-chip:hover {
  border-color: #13c291;
  color: #0e9f78;
}

.ind-chip.on {
  border-color: transparent;
  background: linear-gradient(135deg, #0e9f78, #13c291);
  color: #fff;
  box-shadow: 0 6px 14px rgba(14, 159, 120, 0.3);
}

.trend-grid {
  display: flex;
  gap: 18px;
  align-items: stretch;
}

.trend-chart-wrap {
  flex: 1;
  min-width: 0;
}

.trend-chart {
  width: 100%;
  height: 300px;
}

.range-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  font-size: 12px;
  color: #7d8b9a;
}

.range-swatch {
  width: 14px;
  height: 9px;
  border-radius: 3px;
  background: rgba(63, 124, 255, 0.18);
  border: 1px solid rgba(63, 124, 255, 0.35);
}

.trend-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
  justify-content: center;
  flex-shrink: 0;
}

.ts-item {
  min-width: 110px;
  padding: 14px 18px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f2faf7, #e8f6f1);
  border: 1px solid #d8efe5;
}

.ts-item.hi {
  background: linear-gradient(135deg, #fff4ec, #ffead9);
  border-color: #ffd9b8;
}

.ts-item.lo {
  background: linear-gradient(135deg, #eef4ff, #e2ecff);
  border-color: #c9dcff;
}

.ts-val {
  margin: 0;
  font-size: 21px;
  font-weight: 800;
  color: #1c2b3a;
}

.ts-item.hi .ts-val {
  color: #d97a2b;
}

.ts-item.lo .ts-val {
  color: #3f7cff;
}

.ts-lab {
  margin: 3px 0 0;
  font-size: 12px;
  color: #7d8b9a;
}

/* 表格区 */
.section {
  margin-top: 16px;
  border: 1px solid #edf1f6;
  border-radius: 14px;
}

.vital-ok {
  color: #0e9f78;
  font-weight: 600;
}

.vital-warn {
  color: #e6543a;
  font-weight: 700;
}

@media (max-width: 900px) {
  .trend-grid {
    flex-direction: column;
  }

  .trend-stats {
    flex-direction: row;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .hero-points {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
