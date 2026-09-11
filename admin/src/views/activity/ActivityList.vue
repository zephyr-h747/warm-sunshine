<template>
  <div class="page">
    <!-- 页面头 -->
    <div class="page-header">
      <div class="ph-icon"><el-icon><Flag /></el-icon></div>
      <div class="ph-main">
        <p class="ph-title">社区活动</p>
        <p class="ph-desc">共 {{ total }} 场活动 · 签到可获积分奖励</p>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="活动状态" clearable style="width: 140px">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="报名中" value="REGISTRATING" />
        <el-option label="进行中" value="IN_PROGRESS" />
        <el-option label="已结束" value="ENDED" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      <el-button type="primary" :icon="Plus" @click="openEdit(null)">新建活动</el-button>
    </div>

    <!-- 活动表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="活动标题" min-width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="goDetail(row.id)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="activityStart" label="活动开始" min-width="160" />
        <el-table-column prop="activityEnd" label="活动结束" min-width="160" />
        <el-table-column label="报名/名额" width="110">
          <template #default="{ row }">{{ row.currentParticipants }}/{{ row.maxParticipants }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.id)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新建/编辑活动 -->
    <el-dialog v-model="editDialog.visible" :title="editDialog.form.id ? '编辑活动' : '新建活动'" width="620px">
      <el-form ref="formRef" :model="editDialog.form" :rules="rules" label-width="90px">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="editDialog.form.title" maxlength="100" />
        </el-form-item>
        <el-form-item label="报名时间">
          <el-date-picker v-model="regRange" type="datetimerange" value-format="YYYY-MM-DDTHH:mm:ss"
            start-placeholder="报名开始" end-placeholder="报名结束" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动时间" prop="activityStart">
          <el-date-picker v-model="actRange" type="datetimerange" value-format="YYYY-MM-DDTHH:mm:ss"
            start-placeholder="活动开始" end-placeholder="活动结束" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最大名额" prop="maxParticipants">
          <el-input-number v-model="editDialog.form.maxParticipants" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="活动内容">
          <el-input v-model="editDialog.form.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editDialog.form.status" style="width: 200px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="报名中" value="REGISTRATING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已结束" value="ENDED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="editDialog.loading" @click="submitEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import * as activityApi from '../../api/activity'

const router = useRouter()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ status: '', pageNum: 1, pageSize: 10 })

const statusMap = { DRAFT: '草稿', REGISTRATING: '报名中', IN_PROGRESS: '进行中', ENDED: '已结束' }
const tagMap = { DRAFT: 'info', REGISTRATING: 'success', IN_PROGRESS: 'warning', ENDED: '' }

function statusText(s) {
  return statusMap[s] || s
}
function tagType(s) {
  return tagMap[s] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const data = await activityApi.listActivities({ ...query })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.status = ''
  query.pageNum = 1
  loadData()
}

function goDetail(id) {
  router.push('/activity/' + id)
}

// ---- 新建/编辑 ----
const formRef = ref()
const rules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  maxParticipants: [{ required: true, message: '请输入最大名额', trigger: 'blur' }]
}

const regRange = ref([])
const actRange = ref([])

const editDialog = reactive({
  visible: false,
  loading: false,
  form: { id: null, title: '', content: '', maxParticipants: 50, status: 'DRAFT' }
})

function openEdit(row) {
  if (row) {
    editDialog.form = {
      id: row.id,
      title: row.title,
      content: row.content,
      maxParticipants: row.maxParticipants,
      status: row.status
    }
    regRange.value = row.registrationStart && row.registrationEnd
      ? [row.registrationStart, row.registrationEnd]
      : []
    actRange.value = row.activityStart && row.activityEnd ? [row.activityStart, row.activityEnd] : []
  } else {
    editDialog.form = { id: null, title: '', content: '', maxParticipants: 50, status: 'DRAFT' }
    regRange.value = []
    actRange.value = []
  }
  editDialog.visible = true
}

async function submitEdit() {
  await formRef.value.validate()
  if (!actRange.value || actRange.value.length !== 2) {
    ElMessage.warning('请选择活动时间')
    return
  }
  editDialog.loading = true
  try {
    const payload = {
      ...editDialog.form,
      registrationStart: regRange.value && regRange.value[0] ? regRange.value[0] : null,
      registrationEnd: regRange.value && regRange.value[1] ? regRange.value[1] : null,
      activityStart: actRange.value[0],
      activityEnd: actRange.value[1]
    }
    if (payload.id) {
      await activityApi.updateActivity(payload.id, payload)
      ElMessage.success('活动已更新')
    } else {
      await activityApi.createActivity(payload)
      ElMessage.success('活动已创建')
    }
    editDialog.visible = false
    loadData()
  } finally {
    editDialog.loading = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除活动「' + row.title + '」吗？', '提示', { type: 'warning' })
  await activityApi.deleteActivity(row.id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>
