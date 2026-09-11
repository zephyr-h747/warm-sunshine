<template>
  <div class="page">
    <!-- 页面头 -->
    <div class="page-header">
      <div class="ph-icon"><el-icon><User /></el-icon></div>
      <div class="ph-main">
        <p class="ph-title">会员管理</p>
        <p class="ph-desc">共 {{ total }} 位会员 · 点击姓名查看健康档案与趋势</p>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input v-model="query.phone" placeholder="手机号" clearable style="width: 180px" @keyup.enter="loadData" />
      <el-input v-model="query.realName" placeholder="姓名" clearable style="width: 160px" @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px">
        <el-option label="正常" value="ENABLED" />
        <el-option label="已禁用" value="DISABLED" />
      </el-select>
      <el-select v-model="query.memberLevel" placeholder="会员等级" clearable style="width: 130px">
        <el-option v-for="lv in levels" :key="lv" :label="levelText(lv)" :value="lv" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <!-- 会员表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="会员" width="190">
          <template #default="{ row }">
            <div class="member-cell">
              <span class="member-avatar" :class="'lv-' + (row.memberLevel || 'NORMAL')">
                {{ avatarChar(row) }}
              </span>
              <div class="member-info">
                <el-link type="primary" :underline="false" class="member-name" @click="goDetail(row.id)">
                  {{ row.realName || '未填写' }}
                </el-link>
                <span class="member-phone">{{ row.phone }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="70">
          <template #default="{ row }">{{ genderText(row.gender) }}</template>
        </el-table-column>
        <el-table-column prop="memberLevel" label="会员等级" width="100">
          <template #default="{ row }">
            <span class="level-badge" :class="'lb-' + (row.memberLevel || 'NORMAL')">
              {{ levelText(row.memberLevel) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="100">
          <template #default="{ row }">
            <span class="points-num">{{ row.points ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <span class="status-dot" :class="row.status === 'ENABLED' ? 'ok' : 'off'">
              {{ row.status === 'ENABLED' ? '正常' : '已禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="170" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.id)">详情</el-button>
            <el-button link :type="row.status === 'ENABLED' ? 'danger' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="primary" @click="openLevelDialog(row)">调级</el-button>
            <el-button link type="warning" @click="openPointsDialog(row)">调积分</el-button>
            <el-button link type="info" @click="openResetDialog(row)">重置密码</el-button>
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

    <!-- 调整等级 -->
    <el-dialog v-model="levelDialog.visible" title="调整会员等级" width="360px">
      <el-form label-width="80px">
        <el-form-item label="会员">
          {{ levelDialog.member && levelDialog.member.realName }}（{{ levelDialog.member && levelDialog.member.phone }}）
        </el-form-item>
        <el-form-item label="当前等级">
          <el-tag>{{ levelText(levelDialog.member && levelDialog.member.memberLevel) }}</el-tag>
        </el-form-item>
        <el-form-item label="新等级">
          <el-select v-model="levelDialog.memberLevel" style="width: 100%">
            <el-option v-for="lv in levels" :key="lv" :label="levelText(lv)" :value="lv" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="levelDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="levelDialog.loading" @click="submitLevel">确定</el-button>
      </template>
    </el-dialog>

    <!-- 调整积分 -->
    <el-dialog v-model="pointsDialog.visible" title="调整积分" width="420px">
      <el-form label-width="80px">
        <el-form-item label="会员">
          {{ pointsDialog.member && pointsDialog.member.realName }}（当前 {{ pointsDialog.member && pointsDialog.member.points }} 分）
        </el-form-item>
        <el-form-item label="变动值">
          <el-input-number v-model="pointsDialog.delta" :step="10" />
          <div class="form-tip">正数加分，负数扣分</div>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="pointsDialog.reason" type="textarea" :rows="2" placeholder="调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pointsDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="pointsDialog.loading" @click="submitPoints">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码 -->
    <el-dialog v-model="resetDialog.visible" title="重置会员密码" width="420px">
      <el-form label-width="80px">
        <el-form-item label="会员">
          {{ resetDialog.member && resetDialog.member.realName }}（{{ resetDialog.member && resetDialog.member.phone }}）
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="resetDialog.newPassword" type="password" show-password placeholder="8-20 位，含大小写字母和数字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="resetDialog.loading" @click="submitReset">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import * as memberApi from '../../api/member'

const router = useRouter()

const levels = ['NORMAL', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND']
const levelMap = { NORMAL: '普通', SILVER: '银卡', GOLD: '金卡', PLATINUM: '铂金', DIAMOND: '钻石' }
const levelTagMap = { NORMAL: 'info', SILVER: '', GOLD: 'warning', PLATINUM: 'success', DIAMOND: 'danger' }

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ phone: '', realName: '', status: '', memberLevel: '', pageNum: 1, pageSize: 10 })

function levelText(lv) {
  return levelMap[lv] || lv || '-'
}
function levelTag(lv) {
  return levelTagMap[lv] || 'info'
}
function genderText(g) {
  if (g === 'MALE') return '男'
  if (g === 'FEMALE') return '女'
  return '-'
}

function avatarChar(row) {
  return (row.realName || row.phone || '?').charAt(0)
}

async function loadData() {
  loading.value = true
  try {
    const data = await memberApi.listMembers({ ...query })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.phone = ''
  query.realName = ''
  query.status = ''
  query.memberLevel = ''
  query.pageNum = 1
  loadData()
}

function goDetail(id) {
  router.push('/member/' + id)
}

// ---- 状态启停 ----
async function toggleStatus(row) {
  const target = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const action = target === 'DISABLED' ? '禁用' : '启用'
  await ElMessageBox.confirm('确定' + action + '会员「' + (row.realName || row.phone) + '」吗？', '提示', { type: 'warning' })
  await memberApi.updateMemberStatus(row.id, target)
  ElMessage.success(action + '成功')
  loadData()
}

// ---- 调整等级 ----
const levelDialog = reactive({ visible: false, loading: false, member: null, memberLevel: 'NORMAL' })

function openLevelDialog(row) {
  levelDialog.member = row
  levelDialog.memberLevel = row.memberLevel
  levelDialog.visible = true
}

async function submitLevel() {
  levelDialog.loading = true
  try {
    await memberApi.updateMemberLevel(levelDialog.member.id, levelDialog.memberLevel)
    ElMessage.success('等级已调整')
    levelDialog.visible = false
    loadData()
  } finally {
    levelDialog.loading = false
  }
}

// ---- 调整积分 ----
const pointsDialog = reactive({ visible: false, loading: false, member: null, delta: 10, reason: '' })

function openPointsDialog(row) {
  pointsDialog.member = row
  pointsDialog.delta = 10
  pointsDialog.reason = ''
  pointsDialog.visible = true
}

async function submitPoints() {
  if (!pointsDialog.reason) {
    ElMessage.warning('请填写调整原因')
    return
  }
  pointsDialog.loading = true
  try {
    await memberApi.updateMemberPoints(pointsDialog.member.id, pointsDialog.delta, pointsDialog.reason)
    ElMessage.success('积分已调整')
    pointsDialog.visible = false
    loadData()
  } finally {
    pointsDialog.loading = false
  }
}

// ---- 重置密码 ----
const resetDialog = reactive({ visible: false, loading: false, member: null, newPassword: '' })

function openResetDialog(row) {
  resetDialog.member = row
  resetDialog.newPassword = ''
  resetDialog.visible = true
}

async function submitReset() {
  if (!resetDialog.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  resetDialog.loading = true
  try {
    await memberApi.resetMemberPassword(resetDialog.member.id, resetDialog.newPassword)
    ElMessage.success('密码已重置')
    resetDialog.visible = false
  } finally {
    resetDialog.loading = false
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

/* 会员单元格：头像 + 姓名/手机号 */
.member-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  color: #fff;
  font-size: 16px;
  font-weight: 800;
  flex-shrink: 0;
}

/* 等级对应头像渐变 */
.member-avatar.lv-NORMAL { background: linear-gradient(135deg, #8d9aa9, #b3c0cc); }
.member-avatar.lv-SILVER { background: linear-gradient(135deg, #6b87a8, #93b3d1); }
.member-avatar.lv-GOLD { background: linear-gradient(135deg, #e6a23c, #ffc35b); }
.member-avatar.lv-PLATINUM { background: linear-gradient(135deg, #0e9f78, #13c291); }
.member-avatar.lv-DIAMOND { background: linear-gradient(135deg, #7c5cff, #a78bfa); }

.member-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.member-name {
  font-size: 14px;
  font-weight: 700;
  line-height: 1.3;
}

.member-phone {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.4;
}

/* 等级徽章 */
.level-badge {
  display: inline-block;
  padding: 3px 11px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
}

.level-badge.lb-NORMAL { background: linear-gradient(135deg, #8d9aa9, #b3c0cc); }
.level-badge.lb-SILVER { background: linear-gradient(135deg, #6b87a8, #93b3d1); }
.level-badge.lb-GOLD { background: linear-gradient(135deg, #e6a23c, #ffc35b); }
.level-badge.lb-PLATINUM { background: linear-gradient(135deg, #0e9f78, #13c291); }
.level-badge.lb-DIAMOND { background: linear-gradient(135deg, #7c5cff, #a78bfa); }

/* 积分 */
.points-num {
  font-size: 14.5px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  background: linear-gradient(135deg, #0e9f78, #13c291);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 状态圆点 */
.status-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
}

.status-dot::before {
  content: '';
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.status-dot.ok {
  color: #0e9f78;
}

.status-dot.ok::before {
  background: #13c291;
  box-shadow: 0 0 6px rgba(19, 194, 145, 0.7);
}

.status-dot.off {
  color: #e6543a;
}

.status-dot.off::before {
  background: #e6543a;
}
</style>
