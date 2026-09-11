import request from '../utils/request'

// 个人中心
export function getProfile() {
  return request.get('/member/profile')
}

export function updateProfile(data) {
  return request.put('/member/profile', data)
}

export function changePassword(data) {
  return request.put('/member/profile/password', data)
}
