import request from '../utils/request'

// 消息通知
export function listMessages(params) {
  return request.get('/member/message/list', { params })
}

export function getMessage(id) {
  return request.get(`/member/message/${id}`)
}

export function markAsRead(id) {
  return request.put(`/member/message/${id}/read`)
}

export function unreadCount() {
  return request.get('/member/message/unread-count')
}
