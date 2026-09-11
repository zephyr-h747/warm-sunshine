<template>
  <div class="page">
    <div class="page-header">
      <div class="ph-icon"><el-icon><Flag /></el-icon></div>
      <div class="ph-main">
        <div class="ph-title-row">
          <p class="ph-title">{{ activity.title || '活动详情' }}</p>
          <el-tag :type="tagType(activity.status)" round>{{ statusText(activity.status) }}</el-tag>
        </div>
        <p class="ph-desc">已报名 {{ activity.currentParticipants ?? 0 }} / {{ activity.maxParticipants ?? 0 }} 人 · 剩余 {{ (activity.maxParticipants ?? 0) - (activity.currentParticipants ?? 0) }} 个名额</p>
      </div>
      <el-button :icon="ArrowLeft" round @click="goBack">返回</el-button>
    </div>

    <div v-loading="loading">
      <!-- 活动信息 -->
      <div class="table-card">
        <h3 class="section-title">活动信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="活动 ID">{{ activity.id }}</el-descriptions-item>
          <el-descriptions-item label="最大名额">{{ activity.maxParticipants }}</el-descriptions-item>
          <el-descriptions-item label="已报名">{{ activity.currentParticipants }}</el-descriptions-item>
          <el-descriptions-item label="剩余名额">{{ activity.maxParticipants - activity.currentParticipants }}</el-descriptions-item>
          <el-descriptions-item label="报名时间">
            {{ activity.registrationStart || '-' }} ~ {{ activity.registrationEnd || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="活动时间">
            {{ activity.activityStart || '-' }} ~ {{ activity.activityEnd || '-' }}
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="activity.content" class="content">
          <h4>活动内容</h4>
          <p class="pre">{{ activity.content }}</p>
        </div>
      </div>

      <!-- 报名列表 -->
      <div class="table-card section">
        <h3 class="section-title">报名列表（{{ regTotal }} 人）</h3>
        <el-table :data="registrations" stripe>
          <el-table-column prop="id" label="报名ID" width="90" />
          <el-table-column prop="userId" label="会员ID" width="90" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column prop="checkInStatus" label="签到状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.checkInStatus === 'CHECKED_IN' ? 'success' : 'info'">
                {{ row.checkInStatus === 'CHECKED_IN' ? '已签到' : '未签到' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="报名时间" min-width="170" />
        </el-table>

        <div class="pager">
          <el-pagination
            v-model:current-page="regQuery.pageNum"
            v-model:page-size="regQuery.pageSize"
            :total="regTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="loadRegistrations"
            @size-change="loadRegistrations"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import * as activityApi from '../../api/activity'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const activity = ref({})
const registrations = ref([])
const regTotal = ref(0)
const regQuery = { pageNum: 1, pageSize: 10 }

const statusMap = { DRAFT: '草稿', REGISTRATING: '报名中', IN_PROGRESS: '进行中', ENDED: '已结束' }
const tagMap = { DRAFT: 'info', REGISTRATING: 'success', IN_PROGRESS: 'warning', ENDED: '' }

function statusText(s) {
  return statusMap[s] || s || '-'
}
function tagType(s) {
  return tagMap[s] || 'info'
}

function goBack() {
  router.back()
}

async function loadActivity() {
  loading.value = true
  try {
    activity.value = await activityApi.getActivity(route.params.id)
  } finally {
    loading.value = false
  }
}

async function loadRegistrations() {
  const data = await activityApi.listRegistrations(route.params.id, { ...regQuery })
  registrations.value = data.list
  regTotal.value = data.total
}

onMounted(() => {
  loadActivity()
  loadRegistrations()
})
</script>

<style scoped>
.ph-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.section {
  margin-top: 16px;
}

.section-title {
  margin-bottom: 12px;
  color: #303133;
}

.content {
  margin-top: 16px;
}

.content h4 {
  margin-bottom: 8px;
  color: #303133;
}

.pre {
  white-space: pre-wrap;
  color: #606266;
}
</style>
