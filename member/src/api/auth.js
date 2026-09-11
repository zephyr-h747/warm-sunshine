import request from '../utils/request'

// 认证接口
export function sendSmsCode(phone) {
  return request.post('/auth/sms-code', { phone })
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function login(data) {
  return request.post('/auth/login', data)
}

export function refresh(refreshToken) {
  return request.post('/auth/refresh', { refreshToken })
}

export function logout(refreshToken) {
  return request.post('/auth/logout', { refreshToken })
}

export function resetPassword(data) {
  return request.post('/auth/reset-password', data)
}
