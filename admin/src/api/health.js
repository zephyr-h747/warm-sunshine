import request from '../utils/request'

// 管理端健康档案（/api/admin/health-record）
export function listHealthRecords(memberId, params) {
  return request.get(`/admin/health-record/${memberId}`, { params })
}

export function getHealthTrend(memberId, indicator) {
  return request.get(`/admin/health-record/${memberId}/trend`, {
    params: indicator ? { indicator } : {}
  })
}
