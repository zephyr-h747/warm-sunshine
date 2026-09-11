<template>
  <div class="page">
    <!-- 页面头 -->
    <div class="page-header">
      <div class="ph-icon"><el-icon><Calendar /></el-icon></div>
      <div class="ph-main">
        <p class="ph-title">预约记录</p>
        <p class="ph-desc">共 {{ total }} 条预约 · 确认后可上传 PDF 体检报告</p>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="预约状态" clearable style="width: 140px">
        <el-option label="待确认" value="PENDING" />
        <el-option label="已确认" value="CONFIRMED" />
        <el-option label="已取消" value="CANCELED" />
        <el-option label="已完成" value="COMPLETED" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
        start-placeholder="体检日期起" end-placeholder="体检日期止" style="width: 260px" />
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <!-- 预约表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="预约ID" width="80" />
        <el-table-column prop="userPhone" label="会员手机号" width="130" />
        <el-table-column prop="userName" label="会员姓名" width="100" />
        <el-table-column prop="packageName" label="套餐" min-width="150" />
        <el-table-column prop="appointDate" label="体检日期" width="120" />
        <el-table-column prop="timeRange" label="时段" width="130" />
        <el-table-column prop="price" label="价格(积分)" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button link type="primary" @click="handleConfirm(row)">确认</el-button>
              <el-button link type="danger" @click="handleCancel(row)">取消</el-button>
            </template>
            <template v-if="row.status === 'CONFIRMED' || row.status === 'COMPLETED'">
              <el-button link type="primary" @click="openUpload(row)">
                {{ row.reportUrl ? '重新上传' : '上传报告' }}
              </el-button>
              <el-button v-if="row.reportUrl" link type="success" @click="viewReport(row)">查看</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </div>

    <!-- 上传报告 -->
    <el-dialog v-model="uploadDialog.visible" title="上传体检报告" width="440px">
      <p class="form-tip">
        为预约 #{{ uploadDialog.row && uploadDialog.row.id }}（{{ uploadDialog.row && uploadDialog.row.userName }}）上传 PDF 报告，
        上传后会员可在会员端查看。
      </p>
      <el-upload
        drag
        accept=".pdf"
        :auto-upload="false"
        :limit="1"
        :on-change="onFileChange"
        :on-remove="() => (uploadDialog.file = null)"
      >
        <el-icon :size="40"><UploadFilled /></el-icon>
        <div>将 PDF 文件拖到此处，或点击选择</div>
      </el-upload>
      <template #footer>
        <el-button @click="uploadDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="uploadDialog.loading" :disabled="!uploadDialog.file" @click="submitUpload">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, UploadFilled } from '@element-plus/icons-vue'
import * as appointmentApi from '../../api/appointment'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const dateRange = ref([])
const query = reactive({ status: '', start: '', end: '', pageNum: 1, pageSize: 10 })

const statusMap = { PENDING: '待确认', CONFIRMED: '已确认', CANCELED: '已取消', COMPLETED: '已完成' }
const tagMap = { PENDING: 'warning', CONFIRMED: 'success', CANCELED: 'info', COMPLETED: '' }

function statusText(s) {
  return statusMap[s] || s
}
function tagType(s) {
  return tagMap[s] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    query.start = dateRange.value && dateRange.value[0] ? dateRange.value[0] : ''
    query.end = dateRange.value && dateRange.value[1] ? dateRange.value[1] : ''
    const data = await appointmentApi.listAppointments({ ...query })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.status = ''
  dateRange.value = []
  query.pageNum = 1
  loadData()
}

// ---- 确认/取消预约 ----
async function handleConfirm(row) {
  await appointmentApi.updateAppointmentStatus(row.id, 'CONFIRMED')
  ElMessage.success('预约已确认')
  loadData()
}

async function handleCancel(row) {
  await ElMessageBox.confirm('确定取消预约 #' + row.id + ' 吗？', '提示', { type: 'warning' })
  await appointmentApi.updateAppointmentStatus(row.id, 'CANCELED')
  ElMessage.success('预约已取消')
  loadData()
}

// ---- 上传报告 ----
const uploadDialog = reactive({ visible: false, loading: false, row: null, file: null })

function openUpload(row) {
  uploadDialog.row = row
  uploadDialog.file = null
  uploadDialog.visible = true
}

function onFileChange(file) {
  uploadDialog.file = file.raw
}

async function submitUpload() {
  uploadDialog.loading = true
  try {
    await appointmentApi.uploadReport(uploadDialog.row.id, uploadDialog.file)
    ElMessage.success('报告已上传')
    uploadDialog.visible = false
    loadData()
  } finally {
    uploadDialog.loading = false
  }
}

function viewReport(row) {
  if (row.reportUrl) {
    window.open(row.reportUrl, '_blank')
  }
}

onMounted(loadData)
</script>

<style scoped>
.form-tip {
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
}
</style>
