import request from '../utils/request'

// 体检预约管理（/api/admin/appointment）

// ---- 套餐 ----
export function listPackages(params) {
  return request.get('/admin/appointment/packages', { params })
}

export function createPackage(data) {
  return request.post('/admin/appointment/packages', data)
}

export function updatePackage(id, data) {
  return request.put('/admin/appointment/packages/' + id, data)
}

export function deletePackage(id) {
  return request.delete('/admin/appointment/packages/' + id)
}

// ---- 可约时段 ----
export function generateSlots(data) {
  return request.post('/admin/appointment/slots/generate', data)
}

// ---- 预约记录 ----
export function listAppointments(params) {
  return request.get('/admin/appointment/list', { params })
}

// 处理预约状态（CONFIRMED 确认 / CANCELED 取消）
export function updateAppointmentStatus(id, status) {
  return request.put('/admin/appointment/' + id + '/status', null, { params: { status } })
}

// 上传统一体检报告（multipart），返回报告访问 URL
export function uploadReport(id, file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/admin/appointment/' + id + '/report', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
