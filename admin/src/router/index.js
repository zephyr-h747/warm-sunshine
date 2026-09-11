import { createRouter, createWebHistory } from 'vue-router'
import { getAccessToken } from '../utils/auth'

// 使用后台布局的页面（需登录）
const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { title: '登录' } },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'member', component: () => import('../views/member/MemberList.vue'), meta: { title: '会员管理' } },
      { path: 'member/:id', component: () => import('../views/member/MemberDetail.vue'), meta: { title: '会员详情' } },
      { path: 'appointment/package', component: () => import('../views/appointment/PackageList.vue'), meta: { title: '体检套餐' } },
      { path: 'appointment/list', component: () => import('../views/appointment/AppointmentList.vue'), meta: { title: '预约记录' } },
      { path: 'assessment', component: () => import('../views/assessment/QuestionnaireList.vue'), meta: { title: '健康评测' } },
      { path: 'assessment/:id', component: () => import('../views/assessment/QuestionnaireDetail.vue'), meta: { title: '问卷详情' } },
      { path: 'activity', component: () => import('../views/activity/ActivityList.vue'), meta: { title: '社区活动' } },
      { path: 'activity/:id', component: () => import('../views/activity/ActivityDetail.vue'), meta: { title: '活动详情' } },
      { path: 'message', component: () => import('../views/message/MessageList.vue'), meta: { title: '消息通知' } },
      { path: 'config', component: () => import('../views/config/ConfigList.vue'), meta: { title: '系统配置' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳登录页
router.beforeEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 智慧养老社区管理后台` : '智慧养老社区管理后台'
  if (to.path === '/login') {
    return true
  }
  if (!getAccessToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
