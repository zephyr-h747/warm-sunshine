import { defineStore } from 'pinia'
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from '../utils/auth'
import * as authApi from '../api/auth'

// 管理端用户状态：token + 管理员信息（来自登录返回）
export const useUserStore = defineStore('user', {
  state: () => ({
    token: getAccessToken() || '',
    adminInfo: null
  }),
  getters: {
    isLoggedIn: (s) => !!s.token
  },
  actions: {
    async login(payload) {
      const data = await authApi.login(payload)
      if (data.role !== 'ADMIN') {
        throw new Error('该账号不是管理员，请使用管理员账号登录')
      }
      setTokens(data.accessToken, data.refreshToken)
      this.token = data.accessToken
      this.adminInfo = {
        userId: data.userId,
        phone: data.phone,
        realName: data.realName,
        role: data.role
      }
      return data
    },

    setTokens(accessToken, refreshToken) {
      setTokens(accessToken, refreshToken)
      this.token = accessToken || this.token
    },

    async logout() {
      const refreshToken = getRefreshToken()
      try {
        if (refreshToken) {
          await authApi.logout(refreshToken)
        }
      } catch (e) {
        // 忽略登出接口异常，本地照常清理
      }
      clearTokens()
      this.token = ''
      this.adminInfo = null
    }
  }
})
