<template>
  <div class="shell mine-shell">
    <div class="page-body">
      <van-nav-bar title="我的预约" left-arrow @click-left="$router.back()" />

      <!-- 状态筛选 -->
      <div class="chips fade-up">
        <button
          v-for="f in filters"
          :key="f.key"
          class="chip press"
          :class="{ on: filter === f.key }"
          type="button"
          @click="filter = f.key"
        >
          {{ f.label }}
          <i v-if="f.key && counts[f.key]" class="chip-n">{{ counts[f.key] }}</i>
        </button>
      </div>

      <template v-if="shown.length">
        <div
          v-for="(a, i) in shown"
          :key="a.id"
          class="app-card apt-card fade-up"
          :class="'d' + Math.min(i + 1, 5)"
        >
          <div class="apt-head">
            <div class="apt-icon"><van-icon name="calendar-o" /></div>
            <div class="apt-info">
              <p class="apt-name">{{ a.packageName }}</p>
              <p class="apt-time">{{ a.appointDate }} {{ a.timeRange }}</p>
            </div>
            <span class="apt-status" :style="statusStyle(a.status)">{{ statusText(a.status) }}</span>
          </div>

          <div class="apt-meta">
            <span class="meta-item"><van-icon name="gold-coin-o" /> {{ a.price }} 积分</span>
            <span v-if="a.reportUrl" class="meta-item report"><van-icon name="notes-o" /> 报告已生成</span>
          </div>

          <div class="apt-actions">
            <van-button
              v-if="canCancel(a.status)"
              size="small"
              round
              plain
              class="btn-cancel"
              @click="onCancel(a)"
            >
              取消预约
            </van-button>
            <van-button
              v-if="a.status === 'COMPLETED'"
              size="small"
              round
              class="btn-report"
              @click="onReport(a)"
            >
              查看报告
            </van-button>
          </div>
        </div>
      </template>
      <van-empty v-else description="暂无预约记录" class="fade-up d1" />

      <div class="go-pkg press fade-up" @click="$router.push('/appointment')">
        <van-icon name="plus" /> 预约新的体检
      </div>
    </div>
    <TabBar />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { showConfirmDialog, showSuccessToast, showToast } from 'vant'
import TabBar from '../components/TabBar.vue'
import { listMyAppointments, cancelAppointment, getReportUrl } from '../api/appointment'
import { useUserStore } from '../stores/user'
import { APPT_STATUS } from '../utils/format'

const list = ref([])
const filter = ref('')
const userStore = useUserStore()

const filters = [
  { key: '', label: '全部' },
  { key: 'PENDING', label: '待确认' },
  { key: 'CONFIRMED', label: '已确认' },
  { key: 'COMPLETED', label: '已完成' },
  { key: 'CANCELED', label: '已取消' }
]

const counts = computed(() => {
  const c = {}
  list.value.forEach((a) => {
    c[a.status] = (c[a.status] || 0) + 1
  })
  return c
})

const shown = computed(() => (filter.value ? list.value.filter((a) => a.status === filter.value) : list.value))

function statusText(s) {
  return APPT_STATUS[s]?.text || s
}

function statusStyle(s) {
  const st = APPT_STATUS[s]
  return st ? { color: st.color, background: st.bg } : {}
}

function canCancel(s) {
  return s === 'PENDING' || s === 'CONFIRMED'
}

async function load() {
  try {
    const data = await listMyAppointments({ pageNum: 1, pageSize: 20 })
    list.value = data?.list || []
  } catch (e) { /* 忽略 */ }
}

async function onCancel(a) {
  try {
    await showConfirmDialog({
      title: '取消预约',
      message: `确定取消「${a.packageName}」的预约吗？取消后将退还 ${a.price} 积分。`,
      confirmButtonText: '确定取消',
      confirmButtonColor: '#ff5b4d'
    })
  } catch (e) {
    return
  }
  try {
    await cancelAppointment(a.id)
    showSuccessToast('已取消，积分已退还')
    if (userStore.userInfo?.points != null) {
      userStore.userInfo.points = userStore.userInfo.points + (a.price || 0)
    }
    load()
  } catch (e) { /* 统一提示 */ }
}

async function onReport(a) {
  try {
    const data = await getReportUrl(a.id)
    if (data?.url) {
      // 报告下载链接带 5 分钟签名，直接打开
      window.open(data.url, '_blank')
    }
  } catch (e) {
    showToast('报告尚未生成')
  }
}

onMounted(load)
</script>

<style scoped>
.mine-shell {
  background:
    radial-gradient(90% 22% at 50% 0%, rgba(63, 124, 255, 0.08), transparent 70%),
    var(--bg);
}

/* 筛选 */
.chips {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 10px 14px 6px;
  scrollbar-width: none;
}

.chips::-webkit-scrollbar {
  display: none;
}

.chip {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 1.5px solid var(--line);
  background: #fff;
  color: var(--ink-2);
  font-size: 14px;
  font-weight: 600;
  padding: 7px 15px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.chip.on {
  border-color: transparent;
  background: var(--sky-grad);
  color: #fff;
  box-shadow: 0 6px 14px rgba(63, 124, 255, 0.32);
}

.chip-n {
  font-size: 11px;
  font-style: normal;
  background: rgba(255, 255, 255, 0.28);
  border-radius: 999px;
  padding: 0 6px;
}

.chip:not(.on) .chip-n {
  background: var(--bg);
  color: var(--muted);
}

/* 预约卡 */
.apt-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.apt-icon {
  width: 46px;
  height: 46px;
  border-radius: 15px;
  background: rgba(63, 124, 255, 0.1);
  color: var(--sky);
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.apt-info {
  flex: 1;
  min-width: 0;
}

.apt-name {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.apt-time {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--muted);
}

.apt-status {
  font-size: 12px;
  font-weight: 700;
  padding: 4px 11px;
  border-radius: 999px;
  flex-shrink: 0;
}

/* 元信息 */
.apt-meta {
  display: flex;
  gap: 14px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed var(--line);
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--ink-2);
}

.meta-item .van-icon {
  color: #ff9f2e;
}

.meta-item.report .van-icon {
  color: var(--brand);
}

/* 操作 */
.apt-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

.btn-cancel {
  color: var(--danger);
  border-color: rgba(255, 91, 77, 0.4);
  padding: 0 18px;
}

.btn-report {
  border: none;
  background: var(--sky-grad);
  color: #fff;
  font-weight: 600;
  padding: 0 18px;
}

/* 底部入口 */
.go-pkg {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 6px 14px 20px;
  padding: 13px;
  border: 1.5px dashed #b9c8dd;
  border-radius: var(--r-md);
  color: var(--sky);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}
</style>
