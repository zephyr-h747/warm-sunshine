import request from '../utils/request'

// 健康评测管理（/api/admin/assessment）

// ---- 问卷 ----
export function listQuestionnaires(params) {
  return request.get('/admin/assessment/questionnaires', { params })
}

export function getQuestionnaire(id) {
  return request.get('/admin/assessment/questionnaires/' + id)
}

export function createQuestionnaire(data) {
  return request.post('/admin/assessment/questionnaires', data)
}

export function updateQuestionnaire(id, data) {
  return request.put('/admin/assessment/questionnaires/' + id, data)
}

export function deleteQuestionnaire(id) {
  return request.delete('/admin/assessment/questionnaires/' + id)
}

export function publishQuestionnaire(id) {
  return request.put('/admin/assessment/questionnaires/' + id + '/publish')
}

export function unpublishQuestionnaire(id) {
  return request.put('/admin/assessment/questionnaires/' + id + '/unpublish')
}

// ---- 题目 ----
export function createQuestion(data) {
  return request.post('/admin/assessment/questions', data)
}

export function updateQuestion(id, data) {
  return request.put('/admin/assessment/questions/' + id, data)
}

export function deleteQuestion(id) {
  return request.delete('/admin/assessment/questions/' + id)
}
