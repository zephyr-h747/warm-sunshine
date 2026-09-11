import request from '../utils/request'

// 系统配置（/api/admin/config）
export function listConfigs() {
  return request.get('/admin/config')
}

export function getConfig(key) {
  return request.get('/admin/config/' + key)
}

export function updateConfig(key, value) {
  return request.put('/admin/config/' + key, { value })
}
