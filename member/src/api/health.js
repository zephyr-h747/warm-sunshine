import request from '../utils/request'

// 健康记录
export function addHealthRecord(data) {
  return request.post('/member/health', data)
}

export function listHealthRecords(params) {
  return request.get('/member/health/list', { params })
}

export function getHealthRecord(id) {
  return request.get(`/member/health/${id}`)
}

export function getHealthTrend(params) {
  return request.get('/member/health/trend', { params })
}
