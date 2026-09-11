import request from '../utils/request'

// 社区活动管理（/api/admin/activity）
export function listActivities(params) {
  return request.get('/admin/activity', { params })
}

export function getActivity(id) {
  return request.get('/admin/activity/' + id)
}

export function createActivity(data) {
  return request.post('/admin/activity', data)
}

export function updateActivity(id, data) {
  return request.put('/admin/activity/' + id, data)
}

export function deleteActivity(id) {
  return request.delete('/admin/activity/' + id)
}

// 报名列表
export function listRegistrations(id, params) {
  return request.get('/admin/activity/' + id + '/registrations', { params })
}
