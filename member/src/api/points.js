import request from '../utils/request'

// 积分流水（/api/member/points）
export function listPointTransactions(params) {
  return request.get('/member/points/transactions', { params })
}
