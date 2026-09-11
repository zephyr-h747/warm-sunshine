import request from '../utils/request'

// AI 对话（P3 后端 /api/member/chat）
export function createSession(sessionName) {
  return request.post('/member/chat/session', { sessionName })
}

export function listSessions(params) {
  return request.get('/member/chat/sessions', { params })
}

export function deleteSession(id) {
  return request.delete(`/member/chat/session/${id}`)
}

export function getHistory(sessionId, params) {
  return request.get(`/member/chat/history/${sessionId}`, { params })
}

// 对话采用 SSE 流式，不走 axios；POST + JSON body
export function chatStreamUrl() {
  return '/api/member/chat/send'
}
