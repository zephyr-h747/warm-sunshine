// 通用格式化与展示辅助

/** 会员等级文案 */
export const LEVEL_NAMES = {
  NORMAL: '普通会员',
  SILVER: '白银会员',
  GOLD: '黄金会员',
  PLATINUM: '铂金会员',
  DIAMOND: '钻石会员'
}

/** 会员等级图标（Vant 图标名） */
export const LEVEL_ICONS = {
  NORMAL: 'user-o',
  SILVER: 'star-o',
  GOLD: 'gold-coin-o',
  PLATINUM: 'diamond-o',
  DIAMOND: 'diamond'
}

/** 预约状态 */
export const APPT_STATUS = {
  PENDING: { text: '待确认', color: '#ff9f2e', bg: 'rgba(255,159,46,.12)' },
  CONFIRMED: { text: '已确认', color: '#00b486', bg: 'rgba(0,180,134,.12)' },
  CANCELED: { text: '已取消', color: '#93a0af', bg: 'rgba(147,160,175,.14)' },
  COMPLETED: { text: '已完成', color: '#3f7cff', bg: 'rgba(63,124,255,.12)' }
}

/** 活动状态 */
export const ACT_STATUS = {
  REGISTRATING: { text: '报名中', color: '#ff9f2e', bg: 'rgba(255,159,46,.12)' },
  IN_PROGRESS: { text: '进行中', color: '#00b486', bg: 'rgba(0,180,134,.12)' },
  ENDED: { text: '已结束', color: '#93a0af', bg: 'rgba(147,160,175,.14)' },
  DRAFT: { text: '筹备中', color: '#93a0af', bg: 'rgba(147,160,175,.14)' },
  CANCELED: { text: '已取消', color: '#93a0af', bg: 'rgba(147,160,175,.14)' }
}

/** 消息类型 */
export const MSG_TYPES = {
  APPOINTMENT: { text: '预约提醒', icon: 'calendar-o', color: '#3f7cff', bg: 'rgba(63,124,255,.12)' },
  ACTIVITY: { text: '社区活动', icon: 'flag-o', color: '#7c5cff', bg: 'rgba(124,92,255,.12)' },
  SYSTEM: { text: '系统通知', icon: 'setting-o', color: '#4f5f6f', bg: 'rgba(79,95,111,.1)' },
  HEALTH_REMINDER: { text: '健康提醒', icon: 'heart', color: '#ff5b7f', bg: 'rgba(255,91,127,.12)' }
}

/** 指标健康范围（与后端推送阈值一致，仅用于前端着色） */
export const VITAL_RANGES = {
  systolic: { name: '收缩压', unit: 'mmHg', min: 90, max: 140 },
  diastolic: { name: '舒张压', unit: 'mmHg', min: 60, max: 90 },
  bloodSugar: { name: '血糖', unit: 'mmol/L', min: 3.9, max: 6.1 },
  heartRate: { name: '心率', unit: '次/分', min: 60, max: 100 },
  weight: { name: '体重', unit: 'kg', min: 0, max: 0 },
  bmi: { name: 'BMI', unit: '', min: 18.5, max: 24 }
}

/** 判断指标是否在正常范围 */
export function vitalStatus(key, value) {
  if (value == null || value === '') return 'none'
  const r = VITAL_RANGES[key]
  if (!r || (r.min === 0 && r.max === 0)) return 'ok'
  const v = Number(value)
  return v >= r.min && v <= r.max ? 'ok' : 'warn'
}

/** 时间格式化：2026-08-19 14:30 */
export function fmtDateTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

/** 日期格式化：2026-08-19 */
export function fmtDate(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 10)
}

/** 友好日期：今天 14:30 / 昨天 / 08-17 */
export function fmtFriendly(t) {
  if (!t) return ''
  const s = String(t).replace('T', ' ')
  const d = s.slice(0, 10)
  const time = s.slice(11, 16)
  const now = new Date()
  const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  const yest = new Date(now.getTime() - 86400000)
  const yesterday = `${yest.getFullYear()}-${String(yest.getMonth() + 1).padStart(2, '0')}-${String(yest.getDate()).padStart(2, '0')}`
  if (d === today) return `今天 ${time}`
  if (d === yesterday) return `昨天 ${time}`
  return `${d.slice(5)} ${time}`
}

/** 根据当前时间返回问候语 */
export function greeting() {
  const h = new Date().getHours()
  if (h >= 5 && h < 11) return '早上好'
  if (h >= 11 && h < 13) return '中午好'
  if (h >= 13 && h < 18) return '下午好'
  return '晚上好'
}

/** 日期栏文案：8月19日 · 星期三 */
export function dateLine() {
  const now = new Date()
  const weeks = ['日', '一', '二', '三', '四', '五', '六']
  return `${now.getMonth() + 1}月${now.getDate()}日 · 星期${weeks[now.getDay()]}`
}

/** 数字滚动动画（ease-out） */
export function animateNumber(update, target, duration = 900) {
  const start = performance.now()
  function tick(now) {
    const p = Math.min((now - start) / duration, 1)
    const eased = 1 - Math.pow(1 - p, 3)
    update(Math.round(target * eased))
    if (p < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

/** 手机号脱敏：138****8000 */
export function maskPhone(phone) {
  if (!phone || phone.length < 11) return phone || ''
  return phone.slice(0, 3) + '****' + phone.slice(7)
}
