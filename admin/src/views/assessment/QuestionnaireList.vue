<template>
  <div class="page">
    <!-- 页面头 -->
    <div class="page-header">
      <div class="ph-icon"><el-icon><DocumentChecked /></el-icon></div>
      <div class="ph-main">
        <p class="ph-title">健康评测</p>
        <p class="ph-desc">共 {{ total }} 份问卷 · 发布后会员端即可作答</p>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openEdit(null)">新建问卷</el-button>
    </div>

    <!-- 问卷表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="问卷标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="goDetail(row.id)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="240" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.id)">题目管理</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'DRAFT'" link type="success" @click="handlePublish(row)">发布</el-button>
            <el-button v-else link type="warning" @click="handleUnpublish(row)">下架</el-button>
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

    <!-- 新建/编辑问卷 -->
    <el-dialog v-model="editDialog.visible" :title="editDialog.form.id ? '编辑问卷' : '新建问卷'" width="520px">
      <el-form ref="formRef" :model="editDialog.form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editDialog.form.title" maxlength="100" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="editDialog.form.description" type="textarea" :rows="3" />
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
import { Plus } from '@element-plus/icons-vue'
import * as assessmentApi from '../../api/assessment'

const router = useRouter()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

const formRef = ref()
const rules = {
  title: [{ required: true, message: '请输入问卷标题', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const data = await assessmentApi.listQuestionnaires({ ...query })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push('/assessment/' + id)
}

// ---- 新建/编辑 ----
const editDialog = reactive({ visible: false, loading: false, form: { id: null, title: '', description: '' } })

function openEdit(row) {
  editDialog.form = row
    ? { id: row.id, title: row.title, description: row.description }
    : { id: null, title: '', description: '' }
  editDialog.visible = true
}

async function submitEdit() {
  await formRef.value.validate()
  editDialog.loading = true
  try {
    if (editDialog.form.id) {
      await assessmentApi.updateQuestionnaire(editDialog.form.id, editDialog.form)
      ElMessage.success('问卷已更新')
    } else {
      const created = await assessmentApi.createQuestionnaire(editDialog.form)
      ElMessage.success('问卷已创建')
      // 新建后直接跳转题目管理
      editDialog.visible = false
      goDetail(created.id)
      return
    }
    editDialog.visible = false
    loadData()
  } finally {
    editDialog.loading = false
  }
}

async function handlePublish(row) {
  await ElMessageBox.confirm('发布后会员端即可作答，确定发布吗？', '提示', { type: 'info' })
  await assessmentApi.publishQuestionnaire(row.id)
  ElMessage.success('已发布')
  loadData()
}

async function handleUnpublish(row) {
  await ElMessageBox.confirm('下架后会员端不再展示该问卷，确定下架吗？', '提示', { type: 'warning' })
  await assessmentApi.unpublishQuestionnaire(row.id)
  ElMessage.success('已下架')
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('删除问卷「' + row.title + '」将同时删除其题目，确定吗？', '提示', { type: 'warning' })
  await assessmentApi.deleteQuestionnaire(row.id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>
