<template>
  <div class="shell trend-shell">
    <div class="page-body">
      <van-nav-bar title="健康趋势" left-arrow @click-left="$router.back()" />

      <!-- 指标胶囊 -->
      <div class="chips thin-scroll fade-up">
        <button
          v-for="(t, i) in trends"
          :key="t.indicator"
          class="chip press"
          :class="{ on: i === activeIdx }"
          type="button"
          @click="activeIdx = i"
        >
          {{ t.indicatorName }}
        </button>
      </div>

      <!-- 概览卡 -->
      <div v-if="current" class="app-card overview fade-up d1">
        <div class="ov-head">
          <div>
            <p class="ov-name">{{ current.indicatorName }}</p>
            <p class="ov-sub">近 6 个月变化</p>
          </div>
          <span class="ov-count">{{ current.records?.length || 0 }} 条记录</span>
        </div>
        <div class="ov-stats">
          <div class="ov-item">
            <p class="ov-val">{{ fmt(current.avgValue) }}</p>
            <p class="ov-lab">平均值</p>
          </div>
          <div class="ov-item hi">
            <p class="ov-val">{{ fmt(current.maxValue) }}</p>
            <p class="ov-lab">最高</p>
          </div>
          <div class="ov-item lo">
            <p class="ov-val">{{ fmt(current.minValue) }}</p>
            <p class="ov-lab">最低</p>
          </div>
        </div>
      </div>

      <!-- 图表卡 -->
      <div class="app-card chart-card fade-up d2">
        <div ref="chartRef" class="chart"></div>
        <div v-if="range" class="range-tip">
          <i class="range-swatch"></i>正常参考区间 {{ range.min }} ~ {{ range.max }} {{ range.unit }}
        </div>
        <van-empty
          v-if="!current || !current.records || !current.records.length"
          class="chart-empty"
          image-size="88"
          description="该指标暂无数据，去记录一次吧"
        />
      </div>

      <!-- 最近记录 -->
      <div v-if="recentPoints.length" class="app-card points-card fade-up d3">
        <p class="pc-title">最近记录</p>
        <div v-for="p in recentPoints" :key="p.date" class="pc-row">
          <span class="pc-date">{{ p.date.slice(5) }}</span>
          <span class="pc-bar-wrap"><i class="pc-bar" :style="barStyle(p)"></i></span>
          <span class="pc-val">{{ p.value }}</span>
        </div>
      </div>
    </div>
    <TabBar />
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import TabBar from '../components/TabBar.vue'
import { getHealthTrend } from '../api/health'
import { VITAL_RANGES } from '../utils/format'

const trends = ref([])
const activeIdx = ref(0)
const chartRef = ref(null)
let chart = null

const current = computed(() => trends.value[activeIdx.value])

/* 当前指标的正常区间（indicator 为大写下划线，VITAL_RANGES 为小写） */
const range = computed(() => {
  const t = current.value
  if (!t) return null
  const r = VITAL_RANGES[t.indicator?.toLowerCase()]
  return r && r.max > 0 ? r : null
})

const recentPoints = computed(() => {
  const recs = current.value?.records || []
  return recs.slice(-6).reverse()
})

function fmt(v) {
  return v == null ? '--' : v
}

/* 迷你条形图：以区间或数据极值为标尺 */
function barStyle(p) {
  const recs = current.value?.records || []
  const vals = recs.map((r) => Number(r.value) || 0)
  const lo = range.value ? range.value.min : Math.min(...vals)
  const hi = range.value ? range.value.max : Math.max(...vals)
  const span = hi - lo || 1
  const pct = Math.max(8, Math.min(100, ((Number(p.value) - lo) / span) * 100))
  return { width: pct + '%' }
}

function renderChart() {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  const t = current.value
  const recs = (t && t.records) || []
  const r = range.value
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
      grid: { left: 44, right: 18, top: 26, bottom: 30 },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: recs.map((x) => x.date.slice(5)),
        axisLine: { lineStyle: { color: '#eef2f6' } },
        axisTick: { show: false },
        axisLabel: { fontSize: 11, color: '#93a0af' }
      },
      yAxis: {
        type: 'value',
        scale: true,
        axisLabel: { fontSize: 11, color: '#93a0af' },
        splitLine: { lineStyle: { color: '#f4f6f9' } }
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
          markArea:
            r
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
  try {
    const data = await getHealthTrend()
    trends.value = data || []
  } catch (e) {
    /* 统一提示 */
  }
  await nextTick()
  renderChart()
})

onUnmounted(() => {
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<style scoped>
.trend-shell {
  background:
    radial-gradient(90% 24% at 50% 0%, rgba(0, 180, 134, 0.07), transparent 70%),
    var(--bg);
}

/* 指标胶囊 */
.chips {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 10px 14px 4px;
  scrollbar-width: none;
}

.chips::-webkit-scrollbar {
  display: none;
}

.chip {
  flex-shrink: 0;
  border: 1.5px solid var(--line);
  background: #fff;
  color: var(--ink-2);
  font-size: 14px;
  font-weight: 600;
  padding: 7px 16px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.chip.on {
  border-color: transparent;
  background: var(--brand-grad);
  color: #fff;
  box-shadow: 0 6px 14px rgba(0, 180, 134, 0.32);
}

/* 概览卡 */
.ov-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.ov-name {
  margin: 0;
  font-size: 19px;
  font-weight: 800;
  color: var(--ink);
}

.ov-sub {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--muted);
}

.ov-count {
  font-size: 12px;
  color: var(--brand-deep);
  background: var(--brand-soft);
  padding: 3px 10px;
  border-radius: 999px;
  white-space: nowrap;
}

.ov-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  margin-top: 14px;
  background: var(--bg);
  border-radius: 14px;
  padding: 12px 0;
}

.ov-item {
  text-align: center;
  position: relative;
}

.ov-item + .ov-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 15%;
  height: 70%;
  width: 1px;
  background: #e4e9ef;
}

.ov-val {
  margin: 0;
  font-size: 21px;
  font-weight: 800;
  color: var(--ink);
}

.ov-item.hi .ov-val {
  color: #ff7a00;
}

.ov-item.lo .ov-val {
  color: var(--sky);
}

.ov-lab {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--muted);
}

/* 图表卡 */
.chart-card {
  position: relative;
  padding-bottom: 8px;
}

.chart {
  width: 100%;
  height: 250px;
}

.range-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  color: var(--muted);
}

.range-swatch {
  width: 14px;
  height: 8px;
  border-radius: 3px;
  background: rgba(63, 124, 255, 0.15);
  border: 1px solid rgba(63, 124, 255, 0.25);
}

.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: var(--r-lg);
}

/* 最近记录 */
.pc-title {
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
}

.pc-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
}

.pc-date {
  width: 48px;
  font-size: 13px;
  color: var(--muted);
  flex-shrink: 0;
}

.pc-bar-wrap {
  flex: 1;
  height: 8px;
  background: var(--bg);
  border-radius: 999px;
  overflow: hidden;
}

.pc-bar {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: var(--brand-grad);
  transition: width 0.6s ease;
}

.pc-val {
  width: 44px;
  text-align: right;
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-2);
  flex-shrink: 0;
}
</style>
