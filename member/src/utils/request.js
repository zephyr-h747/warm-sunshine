import axios from 'axios'
import { showToast } from 'vant'
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from './auth'
import router from '../router'

// axios 实例：baseURL /api，由 Vite 代理到后端 8080
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：附加 Bearer Token
request.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 刷新令牌的共享 Promise（防止并发 401 触发多次刷新）
let refreshing = null

async function doRefresh() {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    throw new Error('no refresh token')
  }
  const { data } = await axios.post('/api/auth/refresh', { refreshToken })
  if (data.code !== 200) {
    throw new Error(data.message || '刷新失败')
  }
  setTokens(data.data.accessToken)
  return data.data.accessToken
}

// 响应拦截：
//  - HTTP 200 + code 200 → 直接返回 data
//  - HTTP 401 → 用 refreshToken 无感刷新并重放原请求；刷新失败跳登录
//  - 其余错误 → Vant Toast 提示
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res && res.code === 200) {
      return res.data
    }
    const msg = (res && res.message) || '请求失败'
    showToast(msg)
    return Promise.reject(new Error(msg))
  },
  async (error) => {
    const { response, config } = error
    if (response && response.status === 401 && config && !config._retried) {
      // 本地已无刷新令牌（如退出后的竞态请求）：静默清理，无需提示
      if (!getRefreshToken()) {
        clearTokens()
        return Promise.reject(error)
      }
      config._retried = true
      try {
        if (!refreshing) {
          refreshing = doRefresh().finally(() => {
            refreshing = null
          })
        }
        await refreshing
        config.headers.Authorization = `Bearer ${getAccessToken()}`
        return request(config)
      } catch (e) {
        clearTokens()
        if (router.currentRoute.value.path !== '/login') {
          router.replace('/login')
        }
        showToast('登录已过期，请重新登录')
        return Promise.reject(e)
      }
    }
    if (response && response.status === 403 && !(response.data && response.data.message)) {
      // 角色权限拦截（如管理员 token 访问会员接口）：清理本地凭证，引导重新登录
      clearTokens()
      if (router.currentRoute.value.path !== '/login') {
        router.replace('/login')
      }
      showToast('当前账号无会员权限，请使用会员账号登录')
      return Promise.reject(error)
    }
    if (response && response.data && response.data.message) {
      showToast(response.data.message)
    } else {
      showToast('网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
