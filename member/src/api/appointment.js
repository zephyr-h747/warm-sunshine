import request from '../utils/request'

// 体检预约
export function listPackages(params) {
  return request.get('/member/appointment/packages', { params })
}

export function getPackage(id) {
  return request.get(`/member/appointment/packages/${id}`)
}

export function listSlots(params) {
  return request.get('/member/appointment/slots', { params })
}

export function createAppointment(data) {
  return request.post('/member/appointment', data)
}

export function cancelAppointment(id) {
  return request.post(`/member/appointment/${id}/cancel`)
}

export function listMyAppointments(params) {
  return request.get('/member/appointment/mine', { params })
}

export function getReportUrl(id) {
  return request.get(`/member/appointment/${id}/report`)
}
