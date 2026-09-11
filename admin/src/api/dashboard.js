import request from '../utils/request'

// 仪表盘
export function getStatistics() {
  return request.get('/admin/dashboard')
}
