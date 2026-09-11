import request from '../utils/request'

// 消息通知管理（/api/admin/message）
export function listMessages(params) {
  return request.get('/admin/message', { params })
}

// 单人推送
export function pushMessage(data) {
  return request.post('/admin/message/push', data)
}

// 批量推送，返回发送条数
export function pushBatchMessage(data) {
  return request.post('/admin/message/push-batch', data)
}
