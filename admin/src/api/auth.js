import request from '../utils/request'

// 认证接口（与会员端共用 /api/auth）
export function login(data) {
  return request.post('/auth/login', data)
}

export function refresh(refreshToken) {
  return request.post('/auth/refresh', { refreshToken })
}

export function logout(refreshToken) {
  return request.post('/auth/logout', { refreshToken })
}
