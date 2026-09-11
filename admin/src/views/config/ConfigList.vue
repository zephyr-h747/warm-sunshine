<template>
  <div class="page">
    <!-- 页面头 -->
    <div class="page-header">
      <div class="ph-icon"><el-icon><Setting /></el-icon></div>
      <div class="ph-main">
        <p class="ph-title">系统配置</p>
        <p class="ph-desc">共 {{ list.length }} 项参数 · 修改后即时生效</p>
      </div>
    </div>

    <div class="table-card">
      <h3 class="section-title">配置列表</h3>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="configKey" label="配置项" min-width="200" />
        <el-table-column prop="configValue" label="当前值" min-width="220" show-overflow-tooltip />
        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" min-width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 编辑配置 -->
    <el-dialog v-model="editDialog.visible" title="编辑配置" width="480px">
      <el-form label-width="80px">
        <el-form-item label="配置项">
          <el-tag>{{ editDialog.row && editDialog.row.configKey }}</el-tag>
        </el-form-item>
        <el-form-item label="说明">
          {{ editDialog.row && editDialog.row.description }}
        </el-form-item>
        <el-form-item label="配置值">
          <el-input v-if="editDialog.type === 'number'" v-model.number="editDialog.value" type="number" />
          <el-switch v-else-if="editDialog.type === 'boolean'" v-model="editDialog.boolValue"
            active-text="开启" inactive-text="关闭" />
          <el-input v-else v-model="editDialog.value" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="editDialog.loading" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as configApi from '../../api/config'

const loading = ref(false)
const list = ref([])

async function loadData() {
  loading.value = true
  try {
    list.value = await configApi.listConfigs()
  } finally {
    loading.value = false
  }
}

// ---- 编辑 ----
const editDialog = reactive({ visible: false, loading: false, row: null, value: '', boolValue: false, type: 'text' })

function openEdit(row) {
  editDialog.row = row
  // 纯数字或 true/false 的值用对应控件编辑
  if (row.configValue === 'true' || row.configValue === 'false') {
    editDialog.type = 'boolean'
    editDialog.boolValue = row.configValue === 'true'
  } else if (/^\d+$/.test(row.configValue)) {
    editDialog.type = 'number'
    editDialog.value = row.configValue
  } else {
    editDialog.type = 'text'
    editDialog.value = row.configValue
  }
  editDialog.visible = true
}

async function submitEdit() {
  const value =
    editDialog.type === 'boolean' ? String(editDialog.boolValue) : String(editDialog.value)
  editDialog.loading = true
  try {
    await configApi.updateConfig(editDialog.row.configKey, value)
    ElMessage.success('配置已保存')
    editDialog.visible = false
    loadData()
  } finally {
    editDialog.loading = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.section-title {
  margin-bottom: 12px;
  color: #303133;
}
</style>
