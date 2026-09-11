<template>
  <div class="page">
    <div class="page-header">
      <div class="ph-icon"><el-icon><DocumentChecked /></el-icon></div>
      <div class="ph-main">
        <div class="ph-title-row">
          <p class="ph-title">{{ questionnaire.title || '问卷详情' }}</p>
          <el-tag :type="questionnaire.status === 'PUBLISHED' ? 'success' : 'info'" round>
            {{ questionnaire.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </el-tag>
        </div>
        <p class="ph-desc">共 {{ questions.length }} 道题目 · 单选 / 多选 / 文本</p>
      </div>
      <el-button :icon="ArrowLeft" round @click="goBack">返回</el-button>
    </div>

    <div class="table-card">
      <p v-if="questionnaire.description" class="desc">{{ questionnaire.description }}</p>

      <div class="q-header">
        <h3 class="section-title">题目列表（{{ questions.length }} 题）</h3>
        <el-button type="primary" size="small" :icon="Plus" @click="openEdit(null)">添加题目</el-button>
      </div>

      <el-table :data="questions" v-loading="loading" stripe>
        <el-table-column prop="sortOrder" label="序号" width="70" sortable />
        <el-table-column prop="content" label="题干" min-width="240" />
        <el-table-column prop="type" label="题型" width="90">
          <template #default="{ row }">{{ typeText(row.type) }}</template>
        </el-table-column>
        <el-table-column label="选项" min-width="220">
          <template #default="{ row }">
            <span v-if="row.type === 'TEXT'">-</span>
            <span v-else>{{ (row.options || []).join(' / ') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑题目 -->
    <el-dialog v-model="editDialog.visible" :title="editDialog.form.id ? '编辑题目' : '添加题目'" width="560px">
      <el-form ref="formRef" :model="editDialog.form" :rules="rules" label-width="80px">
        <el-form-item label="题干" prop="content">
          <el-input v-model="editDialog.form.content" maxlength="200" />
        </el-form-item>
        <el-form-item label="题型">
          <el-radio-group v-model="editDialog.form.type">
            <el-radio value="SINGLE">单选</el-radio>
            <el-radio value="MULTIPLE">多选</el-radio>
            <el-radio value="TEXT">文本</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="editDialog.form.type !== 'TEXT'" label="选项">
          <el-select v-model="editDialog.form.options" multiple filterable allow-create default-first-option
            style="width: 100%" placeholder="输入选项内容后回车，如：是" />
          <div class="form-tip">至少提供 2 个选项</div>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editDialog.form.sortOrder" :min="1" :max="999" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import * as assessmentApi from '../../api/assessment'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const questionnaire = ref({})
const questions = computed(() => questionnaire.value.questions || [])

const typeMap = { SINGLE: '单选', MULTIPLE: '多选', TEXT: '文本' }

function typeText(t) {
  return typeMap[t] || t
}

function goBack() {
  router.back()
}

async function loadData() {
  loading.value = true
  try {
    questionnaire.value = await assessmentApi.getQuestionnaire(route.params.id)
  } finally {
    loading.value = false
  }
}

// ---- 添加/编辑题目 ----
const formRef = ref()
const rules = {
  content: [{ required: true, message: '请输入题干', trigger: 'blur' }]
}

const editDialog = reactive({
  visible: false,
  loading: false,
  form: { id: null, content: '', type: 'SINGLE', options: [], sortOrder: 1 }
})

function openEdit(row) {
  editDialog.form = row
    ? { id: row.id, content: row.content, type: row.type, options: [...(row.options || [])], sortOrder: row.sortOrder }
    : { id: null, content: '', type: 'SINGLE', options: [], sortOrder: questions.value.length + 1 }
  editDialog.visible = true
}

async function submitEdit() {
  await formRef.value.validate()
  if (editDialog.form.type !== 'TEXT' && editDialog.form.options.length < 2) {
    ElMessage.warning('选择题至少需要 2 个选项')
    return
  }
  editDialog.loading = true
  try {
    const payload = { ...editDialog.form, questionnaireId: Number(route.params.id) }
    if (payload.type === 'TEXT') {
      payload.options = []
    }
    if (payload.id) {
      await assessmentApi.updateQuestion(payload.id, payload)
      ElMessage.success('题目已更新')
    } else {
      await assessmentApi.createQuestion(payload)
      ElMessage.success('题目已添加')
    }
    editDialog.visible = false
    loadData()
  } finally {
    editDialog.loading = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该题目吗？', '提示', { type: 'warning' })
  await assessmentApi.deleteQuestion(row.id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.ph-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.desc {
  margin-bottom: 12px;
  color: #606266;
}

.q-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-title {
  color: #303133;
}

.form-tip {
  width: 100%;
  font-size: 12px;
  color: #909399;
}
</style>
