import request from '../utils/request'

// 社区活动
export function listActivities(params) {
  return request.get('/member/activity/list', { params })
}

export function getActivity(id) {
  return request.get(`/member/activity/${id}`)
}

export function registerActivity(id) {
  return request.post(`/member/activity/${id}/register`)
}

export function checkinActivity(id) {
  return request.post(`/member/activity/${id}/checkin`)
}

export function listMyActivities(params) {
  return request.get('/member/activity/mine', { params })
}

export function getCheckinStatus(id) {
  return request.get(`/member/activity/${id}/checkin-status`)
}
