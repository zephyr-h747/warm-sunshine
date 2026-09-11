import request from '../utils/request'

// 健康评测
export function listQuestionnaires(params) {
  return request.get('/member/assessment/list', { params })
}

export function getQuestionnaire(id) {
  return request.get(`/member/assessment/${id}`)
}

export function submitAssessment(data) {
  return request.post('/member/assessment/submit', data)
}

export function history(params) {
  return request.get('/member/assessment/history', { params })
}

export function getResult(id) {
  return request.get(`/member/assessment/${id}/detail`)
}
