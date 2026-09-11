<template>
  <div class="page">
    <!-- 页面头 -->
    <div class="page-header">
      <div class="ph-icon"><el-icon><Bell /></el-icon></div>
      <div class="ph-main">
        <p class="ph-title">消息通知</p>
        <p class="ph-desc">共 {{ total }} 条记录 · 支持单发、批量推送与短信同步</p>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-select v-model="query.type" placeholder="消息类型" clearable style="width: 150px">
        <el-option label="系统通知" value="SYSTEM" />
        <el-option label="预约提醒" value="APPOINTMENT" />
        <el-option label="活动通知" value="ACTIVITY" />
        <el-option label="健康提醒" value="HEALTH_REMINDER" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      <el-button type="primary" :icon="Promotion" @click="openPush">推送消息</el-button>
    </div>

    <!-- 消息表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">{{ typeText(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="发送时间" min-width="170" />
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

    <!-- 推送消息 -->
    <el-dialog v-model="pushDialog.visible" title="推送消息" width="540px">
      <el-tabs v-model="pushDialog.mode">
        <el-tab-pane label="单个会员" name="single">
          <el-form label-width="90px">
            <el-form-item label="会员ID">
              <el-input-number v-model="pushDialog.userId" :min="1" />
              <div class="form-tip">会员 ID 可在「会员管理」中查看</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="批量推送" name="batch">
          <el-form label-width="90px">
            <el-form-item label="会员ID列表">
              <el-select v-model="pushDialog.userIds" multiple filterable allow-create default-first-option
                style="width: 100%" placeholder="输入会员 ID 后回车，可添加多个" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <el-form label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="pushDialog.title" maxlength="100" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="pushDialog.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="pushDialog.type" style="width: 200px">
            <el-option label="系统通知" value="SYSTEM" />
            <el-option label="预约提醒" value="APPOINTMENT" />
            <el-option label="活动通知" value="ACTIVITY" />
            <el-option label="健康提醒" value="HEALTH_REMINDER" />
          </el-select>
        </el-form-item>
        <el-form-item label="同时发短信">
          <el-switch v-model="pushDialog.sms" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pushDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="pushDialog.loading" @click="submitPush">推送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Promotion } from '@element-plus/icons-vue'
import * as messageApi from '../../api/message'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ type: '', pageNum: 1, pageSize: 10 })

const typeMap = {
  SYSTEM: '系统通知',
  APPOINTMENT: '预约提醒',
  ACTIVITY: '活动通知',
  HEALTH_REMINDER: '健康提醒'
}

function typeText(t) {
  return typeMap[t] || t || '-'
}

async function loadData() {
  loading.value = true
  try {
    const data = await messageApi.listMessages({ ...query })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.type = ''
  query.pageNum = 1
  loadData()
}

// ---- 推送 ----
const pushDialog = reactive({
  visible: false,
  loading: false,
  mode: 'single',
  userId: null,
  userIds: [],
  title: '',
  content: '',
  type: 'SYSTEM',
  sms: false
})

function openPush() {
  pushDialog.mode = 'single'
  pushDialog.userId = null
  pushDialog.userIds = []
  pushDialog.title = ''
  pushDialog.content = ''
  pushDialog.type = 'SYSTEM'
  pushDialog.sms = false
  pushDialog.visible = true
}

async function submitPush() {
  if (!pushDialog.title || !pushDialog.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  if (pushDialog.mode === 'single' && !pushDialog.userId) {
    ElMessage.warning('请输入会员 ID')
    return
  }
  if (pushDialog.mode === 'batch' && !pushDialog.userIds.length) {
    ElMessage.warning('请至少输入一个会员 ID')
    return
  }
  pushDialog.loading = true
  try {
    if (pushDialog.mode === 'single') {
      await messageApi.pushMessage({
        userId: pushDialog.userId,
        title: pushDialog.title,
        content: pushDialog.content,
        type: pushDialog.type,
        sms: pushDialog.sms
      })
      ElMessage.success('推送成功')
    } else {
      // 输入框中得到的是字符串，转数字
      const ids = pushDialog.userIds.map((v) => Number(v))
      const count = await messageApi.pushBatchMessage({
        userIds: ids,
        title: pushDialog.title,
        content: pushDialog.content,
        type: pushDialog.type,
        sms: pushDialog.sms
      })
      ElMessage.success('已向 ' + count + ' 位会员推送')
    }
    pushDialog.visible = false
    loadData()
  } finally {
    pushDialog.loading = false
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
