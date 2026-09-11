import { defineStore } from 'pinia'
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from '../utils/auth'
import * as authApi from '../api/auth'
import * as profileApi from '../api/profile'

// 用户状态：token 管理 + 用户信息
export const useUserStore = defineStore('user', {
  state: () => ({
    token: getAccessToken() || '',
    userInfo: null
  }),
  getters: {
    isLoggedIn: (s) => !!s.token
  },
  actions: {
    async login(payload) {
      const data = await authApi.login(payload)
      // 会员端仅允许 MEMBER 角色登录，管理员账号请使用管理后台
      if (data.role !== 'MEMBER') {
        throw new Error('该账号是管理员账号，请使用会员账号登录')
      }
      setTokens(data.accessToken, data.refreshToken)
      this.token = data.accessToken
      this.userInfo = {
        userId: data.userId,
        phone: data.phone,
        realName: data.realName,
        role: data.role,
        memberLevel: data.memberLevel,
        points: data.points,
        avatar: data.avatar
      }
      return data
    },

    async refreshUserInfo() {
      const data = await profileApi.getProfile()
      this.userInfo = { ...(this.userInfo || {}), ...data }
      return data
    },

    setTokens(accessToken, refreshToken) {
      setTokens(accessToken, refreshToken)
      this.token = accessToken || this.token
    },

    async logout() {
      const refreshToken = getRefreshToken()
      try {
        await authApi.logout(refreshToken)
      } catch (e) {
        // 忽略登出接口异常，本地照常清理
      }
      clearTokens()
      this.token = ''
      this.userInfo = null
    }
  }
})
