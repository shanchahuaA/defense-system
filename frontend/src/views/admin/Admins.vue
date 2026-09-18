<template>
  <div class="admin-page">
    <div class="page-header">
      <h1>管理员列表</h1>
      <p class="page-desc">显示所有院系管理员（包括直接创建的和由教师升级的）</p>
    </div>
    
    <!-- 管理员列表 -->
    <el-card>
      <el-table :data="admins" v-loading="loading" style="width: 100%" @row-dblclick="handleView">
        <el-table-column prop="username" label="账号" width="150" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="departmentName" label="所属院系" min-width="180" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="warning" link @click="handleResetPassword(row)">重置密码</el-button>
            <el-popconfirm 
              title="确定删除该管理员吗？" 
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="管理员详情" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="账号" :span="2">
          <el-text type="primary" style="font-weight: bold;">{{ currentDetail?.username }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentDetail?.name }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentDetail?.enabled ? 'success' : 'danger'" size="small">
            {{ currentDetail?.enabled ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="所属院系" :span="2">{{ currentDetail?.departmentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="密码提示" :span="2">
          <el-text type="warning">初始密码：123456（如忘记请点击下方"重置密码"）</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="电话">{{ currentDetail?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentDetail?.email || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="warning" @click="handleResetPassword(currentDetail)">重置密码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPureAdmins, deleteAdmin, resetUserPassword } from '@/api/user'

const loading = ref(false)
const admins = ref([])
const detailVisible = ref(false)
const currentDetail = ref(null)

// 加载管理员列表
async function loadAdmins() {
  loading.value = true
  try {
    const res = await getPureAdmins()
    admins.value = res.data || []
  } catch (error) {
    console.error('加载管理员列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 查看详情
function handleView(row) {
  currentDetail.value = row
  detailVisible.value = true
}

// 重置密码
async function handleResetPassword(row) {
  try {
    await ElMessageBox.confirm(
      `确定将 ${row.name} 的密码重置为 123456 吗？`,
      '重置密码',
      { type: 'warning' }
    )
    await resetUserPassword(row.id)
    ElMessage.success('密码已重置为 123456')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置密码失败')
    }
  }
}

// 删除管理员
async function handleDelete(id) {
  try {
    await deleteAdmin(id)
    ElMessage.success('删除成功')
    loadAdmins()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadAdmins()
})
</script>

<style scoped>
.admin-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
}

.page-desc {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.el-card {
  margin-bottom: 20px;
}
</style>
