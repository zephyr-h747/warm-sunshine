<template>
  <div class="page">
    <!-- 页面头 -->
    <div class="page-header">
      <div class="ph-icon"><el-icon><ShoppingCart /></el-icon></div>
      <div class="ph-main">
        <p class="ph-title">体检套餐</p>
        <p class="ph-desc">共 {{ total }} 个套餐 · 积分兑换制，可生成可约时段</p>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openEdit(null)">新增套餐</el-button>
      <el-button :icon="Timer" @click="slotDialog.visible = true">生成可约时段</el-button>
    </div>

    <!-- 套餐表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="套餐名称" min-width="160" />
        <el-table-column prop="price" label="价格(积分)" width="100" />
        <el-table-column prop="suitablePeople" label="适用人群" width="120" />
        <el-table-column label="项目" min-width="220">
          <template #default="{ row }">
            <span>{{ (row.items || []).join('、') || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ row.status === 'ENABLED' ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
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

    <!-- 新增/编辑套餐 -->
    <el-dialog v-model="editDialog.visible" :title="editDialog.form.id ? '编辑套餐' : '新增套餐'" width="560px">
      <el-form ref="formRef" :model="editDialog.form" :rules="rules" label-width="90px">
        <el-form-item label="套餐名称" prop="name">
          <el-input v-model="editDialog.form.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="价格(积分)" prop="price">
          <el-input-number v-model="editDialog.form.price" :min="0" :max="999999" />
        </el-form-item>
        <el-form-item label="适用人群">
          <el-input v-model="editDialog.form.suitablePeople" placeholder="如：60 岁以上老年人" />
        </el-form-item>
        <el-form-item label="检查项目">
          <el-select v-model="editDialog.form.items" multiple filterable allow-create default-first-option
            style="width: 100%" placeholder="输入后回车添加，如：血常规" />
        </el-form-item>
        <el-form-item label="套餐说明">
          <el-input v-model="editDialog.form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editDialog.form.status" active-value="ENABLED" inactive-value="DISABLED"
            active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="editDialog.loading" @click="submitEdit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 生成可约时段 -->
    <el-dialog v-model="slotDialog.visible" title="生成可约时段" width="520px">
      <el-form label-width="90px">
        <el-form-item label="选择套餐">
          <el-select v-model="slotDialog.packageId" style="width: 100%" placeholder="选择套餐">
            <el-option v-for="p in packages" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="slotDialog.range" type="daterange" value-format="YYYY-MM-DD"
            start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="slotDialog.timeRanges" multiple filterable allow-create default-first-option
            style="width: 100%" placeholder="输入如 09:00-10:00 后回车" />
          <div class="form-tip">可输入自定义时段，回车添加</div>
        </el-form-item>
        <el-form-item label="每时段名额">
          <el-input-number v-model="slotDialog.maxCount" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="slotDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="slotDialog.loading" @click="submitSlots">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Timer } from '@element-plus/icons-vue'
import * as appointmentApi from '../../api/appointment'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const packages = ref([])
const query = reactive({ pageNum: 1, pageSize: 10 })

const formRef = ref()
const rules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

// 兼容后端返回的 items 为 JSON 字符串的情况，统一转为数组，避免渲染报错
function normalizeItems(items) {
  if (Array.isArray(items)) return items
  if (typeof items === 'string' && items) {
    try {
      const parsed = JSON.parse(items)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return []
    }
  }
  return []
}

async function loadData() {
  loading.value = true
  try {
    const data = await appointmentApi.listPackages({ ...query })
    list.value = (data.list || []).map((p) => ({ ...p, items: normalizeItems(p.items) }))
    total.value = data.total
    // 时段生成弹窗的套餐下拉数据（最多取 100 条）
    const all = await appointmentApi.listPackages({ pageNum: 1, pageSize: 100 })
    packages.value = (all.list || []).map((p) => ({ ...p, items: normalizeItems(p.items) }))
  } finally {
    loading.value = false
  }
}

// ---- 新增/编辑 ----
const editDialog = reactive({
  visible: false,
  loading: false,
  form: { id: null, name: '', price: 0, suitablePeople: '', items: [], description: '', status: 'ENABLED' }
})

function openEdit(row) {
  if (row) {
    editDialog.form = { ...row, items: [...(row.items || [])] }
  } else {
    editDialog.form = { id: null, name: '', price: 0, suitablePeople: '', items: [], description: '', status: 'ENABLED' }
  }
  editDialog.visible = true
}

async function submitEdit() {
  await formRef.value.validate()
  editDialog.loading = true
  try {
    if (editDialog.form.id) {
      await appointmentApi.updatePackage(editDialog.form.id, editDialog.form)
      ElMessage.success('套餐已更新')
    } else {
      await appointmentApi.createPackage(editDialog.form)
      ElMessage.success('套餐已创建')
    }
    editDialog.visible = false
    loadData()
  } finally {
    editDialog.loading = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除套餐「' + row.name + '」吗？', '提示', { type: 'warning' })
  await appointmentApi.deletePackage(row.id)
  ElMessage.success('已删除')
  loadData()
}

// ---- 生成时段 ----
const slotDialog = reactive({
  visible: false,
  loading: false,
  packageId: null,
  range: [],
  timeRanges: [],
  maxCount: 5
})

async function submitSlots() {
  if (!slotDialog.packageId) {
    ElMessage.warning('请选择套餐')
    return
  }
  if (!slotDialog.range || slotDialog.range.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  if (!slotDialog.timeRanges.length) {
    ElMessage.warning('请至少添加一个时段')
    return
  }
  slotDialog.loading = true
  try {
    const count = await appointmentApi.generateSlots({
      packageId: slotDialog.packageId,
      startDate: slotDialog.range[0],
      endDate: slotDialog.range[1],
      timeRanges: slotDialog.timeRanges,
      maxCount: slotDialog.maxCount
    })
    ElMessage.success('已生成 ' + count + ' 个可约时段')
    slotDialog.visible = false
  } finally {
    slotDialog.loading = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.form-tip {
  width: 100%;
  font-size: 12px;
  color: #909399;
}
</style>
