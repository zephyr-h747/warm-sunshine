import request from '../utils/request'

// 会员管理（/api/admin/members）
export function listMembers(params) {
  return request.get('/admin/members', { params })
}

export function getMember(id) {
  return request.get('/admin/members/' + id)
}

export function updateMemberStatus(id, status) {
  return request.put('/admin/members/' + id + '/status', { status })
}

export function updateMemberLevel(id, memberLevel) {
  return request.put('/admin/members/' + id + '/level', { memberLevel })
}

export function updateMemberPoints(id, delta, reason) {
  return request.put('/admin/members/' + id + '/points', { delta, reason })
}

export function resetMemberPassword(id, newPassword) {
  return request.post('/admin/members/' + id + '/reset-password', { newPassword })
}
