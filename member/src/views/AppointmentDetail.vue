<template>
  <div class="shell book-shell">
    <div class="page-body">
      <van-nav-bar title="套餐详情" left-arrow @click-left="$router.back()" />

      <div v-if="pkg" class="book-body">
        <!-- 套餐头卡 -->
        <div class="hero fade-up">
          <div class="hero-deco deco-1"></div>
          <div class="hero-deco deco-2"></div>
          <p class="hero-name">{{ pkg.name }}</p>
          <div class="hero-price">
            <em>{{ pkg.price }}</em>
            <span>积分 / 次</span>
          </div>
          <p class="hero-desc">{{ pkg.description }}</p>
        </div>

        <!-- 项目明细 -->
        <div class="app-card items-card fade-up d1">
          <p class="card-title">体检项目</p>
          <div class="item-grid">
            <span v-for="(item, i) in pkg.items" :key="i" class="item-cell">
              <van-icon name="checked" />
              {{ item }}
            </span>
          </div>
          <p v-if="pkg.suitablePeople" class="suit">
            <van-icon name="friends-o" /> 适用人群：{{ pkg.suitablePeople }}
          </p>
        </div>

        <!-- 第一步：日期 -->
        <div class="app-card step-card fade-up d2">
          <p class="card-title"><i class="step-no">1</i>选择体检日期</p>
          <button class="date-cell press" type="button" @click="showCalendar = true">
            <van-icon name="calendar-o" class="date-ico" />
            <span :class="{ ph: !selectedDate }">{{ selectedDate || '请选择日期' }}</span>
            <van-icon name="arrow" class="date-arrow" />
          </button>
          <van-calendar
            v-model:show="showCalendar"
            teleport="body"
            :min-date="minDate"
            :max-date="maxDate"
            :show-confirm="true"
            @confirm="onPickDate"
          />
        </div>

        <!-- 第二步：时段 -->
        <div v-if="selectedDate" class="app-card step-card fade-up d3">
          <p class="card-title"><i class="step-no">2</i>选择时段</p>
          <div v-if="slots.length" class="slot-grid">
            <button
              v-for="slot in slots"
              :key="slot.id"
              type="button"
              class="slot press"
              :class="{ on: chosen?.id === slot.id, full: slot.remaining <= 0 }"
              :disabled="slot.remaining <= 0"
              @click="onChoose(slot)"
            >
              <span class="slot-time">{{ slot.timeRange }}</span>
              <span class="slot-remain">{{ slot.remaining > 0 ? `余 ${slot.remaining}` : '已满' }}</span>
            </button>
          </div>
          <van-empty v-else description="该日暂无可用时段" image-size="72" />
        </div>

        <!-- 确认 -->
        <div class="confirm-bar">
          <div class="confirm-info">
            <template v-if="chosen">
              <p class="ci-main">{{ selectedDate }} {{ chosen.timeRange }}</p>
              <p class="ci-sub">将扣除 {{ pkg.price }} 积分</p>
            </template>
            <template v-else>
              <p class="ci-main ph">请先选择日期与时段</p>
              <p class="ci-sub">支持随时取消并退还积分</p>
            </template>
          </div>
          <van-button round class="confirm-btn" :disabled="!chosen" :loading="submitting" @click="onConfirm">
            确认预约
          </van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast } from 'vant'
import { getPackage, listSlots, createAppointment } from '../api/appointment'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pkg = ref(null)
const minDate = new Date()
const maxDate = new Date(Date.now() + 30 * 24 * 3600 * 1000)
const showCalendar = ref(false)
const selectedDate = ref('')
const slots = ref([])
const chosen = ref(null)
const submitting = ref(false)

function fmtDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

async function onPickDate(value) {
  selectedDate.value = fmtDate(value)
  showCalendar.value = false
  chosen.value = null
  try {
    const data = await listSlots({ packageId: pkg.value.id, date: selectedDate.value })
    slots.value = data || []
  } catch (e) { /* 统一提示 */ }
}

function onChoose(slot) {
  if (slot.remaining <= 0) return
  chosen.value = slot
}

async function onConfirm() {
  if (!chosen.value) return
  // 关键操作二次确认
  try {
    await showConfirmDialog({
      title: '确认预约',
      message: `确认预约 ${selectedDate.value} ${chosen.value.timeRange} 的「${pkg.value.name}」，将扣除 ${pkg.value.price} 积分？`,
      confirmButtonText: '确认预约'
    })
  } catch (e) {
    return // 用户取消
  }
  submitting.value = true
  try {
    await createAppointment({ slotId: chosen.value.id, packageId: pkg.value.id })
    showSuccessToast('预约成功')
    if (userStore.userInfo?.points != null) {
      userStore.userInfo.points = userStore.userInfo.points - pkg.value.price
    }
    router.replace('/appointment/mine')
  } catch (e) {
    // 统一提示（如积分不足/名额已满）
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    pkg.value = await getPackage(route.params.id)
  } catch (e) { /* 统一提示 */ }
})
</script>

<style scoped>
.book-shell {
  background:
    radial-gradient(90% 22% at 50% 0%, rgba(63, 124, 255, 0.09), transparent 70%),
    var(--bg);
}

.book-body {
  padding-bottom: 100px;
}

/* 头卡 */
.hero {
  position: relative;
  margin: 8px 14px;
  border-radius: var(--r-lg);
  background: linear-gradient(140deg, #2f6cf6 0%, #3f7cff 50%, #6fa5ff 100%);
  padding: 20px 18px;
  overflow: hidden;
  color: #fff;
  box-shadow: 0 12px 28px rgba(63, 124, 255, 0.3);
}

.hero-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}

.deco-1 { width: 140px; height: 140px; top: -60px; right: -40px; }
.deco-2 { width: 60px; height: 60px; bottom: -24px; left: 20%; opacity: 0.7; }

.hero-name {
  position: relative;
  margin: 0;
  font-size: 21px;
  font-weight: 800;
  letter-spacing: 1px;
}

.hero-price {
  position: relative;
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-top: 8px;
}

.hero-price em {
  font-size: 34px;
  font-weight: 800;
  font-style: normal;
}

.hero-price span {
  font-size: 13px;
  opacity: 0.8;
}

.hero-desc {
  position: relative;
  margin: 8px 0 0;
  font-size: 14px;
  line-height: 1.6;
  opacity: 0.92;
}

/* 卡片标题 */
.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 700;
  color: var(--ink);
}

.step-no {
  width: 22px;
  height: 22px;
  border-radius: 8px;
  background: var(--sky-grad);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  font-style: normal;
}

/* 项目 */
.item-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.item-cell {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: var(--ink-2);
  background: var(--bg);
  padding: 8px 13px;
  border-radius: 11px;
}

.item-cell .van-icon {
  color: var(--brand);
  font-size: 15px;
}

.suit {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 12px 0 0;
  font-size: 13px;
  color: var(--muted);
}

.suit .van-icon {
  font-size: 15px;
}

/* 日期 */
.date-cell {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1.5px solid var(--line);
  border-radius: 13px;
  background: #fff;
  padding: 13px 14px;
  font-size: 16px;
  color: var(--ink);
  font-weight: 600;
  cursor: pointer;
}

.date-cell .ph {
  color: var(--muted);
  font-weight: 400;
}

.date-ico {
  color: var(--sky);
  font-size: 19px;
}

.date-arrow {
  margin-left: auto;
  color: #c3ccd6;
}

/* 时段 */
.slot-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 9px;
}

.slot {
  border: 1.5px solid var(--line);
  background: #fff;
  border-radius: 13px;
  padding: 11px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.slot.on {
  border-color: var(--sky);
  background: rgba(63, 124, 255, 0.08);
  box-shadow: 0 6px 14px rgba(63, 124, 255, 0.2);
}

.slot.full {
  opacity: 0.45;
  cursor: not-allowed;
}

.slot-time {
  font-size: 16px;
  font-weight: 700;
  color: var(--ink);
}

.slot.on .slot-time {
  color: var(--sky);
}

.slot-remain {
  font-size: 12px;
  color: var(--muted);
}

.slot.on .slot-remain {
  color: var(--sky);
  font-weight: 600;
}

/* 底部确认条 */
.confirm-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  max-width: 480px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(16px);
  border-top: 1px solid var(--line);
  z-index: 10;
}

.ci-main {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
}

.ci-main.ph {
  color: var(--muted);
  font-weight: 400;
}

.ci-sub {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--muted);
}

.confirm-btn {
  flex-shrink: 0;
  border: none;
  background: var(--sky-grad);
  color: #fff;
  font-weight: 700;
  padding: 0 26px;
  box-shadow: 0 8px 18px rgba(63, 124, 255, 0.32);
}

.confirm-btn.van-button--disabled {
  opacity: 0.5;
  box-shadow: none;
}
</style>
