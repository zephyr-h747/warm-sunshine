import { createRouter, createWebHistory } from 'vue-router'
import { getAccessToken } from '../utils/auth'
import { useUserStore } from '../stores/user'

// 无需认证的页面
const publicPages = ['/login', '/register', '/forgot-password']

const routes = [
  { path: '/', component: () => import('../views/Home.vue'), meta: { title: '首页' } },
  { path: '/login', component: () => import('../views/Login.vue'), meta: { title: '登录' } },
  { path: '/register', component: () => import('../views/Register.vue'), meta: { title: '注册' } },
  { path: '/forgot-password', component: () => import('../views/ForgotPassword.vue'), meta: { title: '找回密码' } },
  { path: '/health', component: () => import('../views/HealthRecord.vue'), meta: { title: '健康记录' } },
  { path: '/health/trend', component: () => import('../views/HealthTrend.vue'), meta: { title: '健康趋势' } },
  { path: '/assessment', component: () => import('../views/AssessmentList.vue'), meta: { title: '健康评测' } },
  { path: '/assessment/:id', component: () => import('../views/AssessmentDetail.vue'), meta: { title: '健康答题' } },
  { path: '/assessment/result/:id', component: () => import('../views/AssessmentResult.vue'), meta: { title: '评测结果' } },
  { path: '/chat', component: () => import('../views/ChatList.vue'), meta: { title: 'AI 助手' } },
  { path: '/chat/:sessionId', component: () => import('../views/ChatDetail.vue'), meta: { title: 'AI 对话' } },
  { path: '/appointment', component: () => import('../views/AppointmentList.vue'), meta: { title: '体检预约' } },
  { path: '/appointment/:id', component: () => import('../views/AppointmentDetail.vue'), meta: { title: '套餐详情' } },
  { path: '/appointment/mine', component: () => import('../views/AppointmentMine.vue'), meta: { title: '我的预约' } },
  { path: '/activity', component: () => import('../views/ActivityList.vue'), meta: { title: '社区活动' } },
  { path: '/activity/:id', component: () => import('../views/ActivityDetail.vue'), meta: { title: '活动详情' } },
  { path: '/message', component: () => import('../views/MessageList.vue'), meta: { title: '消息通知' } },
  { path: '/profile', component: () => import('../views/Profile.vue'), meta: { title: '个人中心' } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳登录页
router.beforeEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 智慧养老社区` : '智慧养老社区'
  if (publicPages.includes(to.path)) {
    return true
  }
  if (!getAccessToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

// 启动时若已登录则拉取用户信息
router.beforeEach(async (to) => {
  if (publicPages.includes(to.path) || !getAccessToken()) {
    return true
  }
  const userStore = useUserStore()
  if (!userStore.userInfo) {
    try {
      await userStore.refreshUserInfo()
    } catch (e) {
      // 拉取失败（token 失效等）由 request 统一处理
    }
  }
  return true
})

export default router
