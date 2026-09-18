<template>
  <div class="teacher-page">
    <div class="page-header">
      <h1>教师管理</h1>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增教师
      </el-button>
    </div>
    
    <!-- 搜索筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="院系">
          <el-select v-model="filters.departmentId" placeholder="选择院系" clearable @change="loadTeachers" style="width: 220px">
            <el-option label="全部" :value="null" />
            <el-option 
              v-for="dept in departments" 
              :key="dept.id" 
              :label="dept.name" 
              :value="dept.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工号/姓名">
          <el-input v-model="filters.keyword" placeholder="输入工号或姓名" style="width: 150px" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTeachers">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 教师列表 -->
    <el-card>
      <el-table :data="filteredTeachers" v-loading="loading" style="width: 100%" @row-dblclick="handleView">
        <el-table-column prop="teacherNo" label="工号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="departmentName" label="院系" min-width="150" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleResetPassword(row)">重置密码</el-button>
            <el-popconfirm 
              title="确定删除该教师吗？" 
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
    
    <!-- 新增/编辑弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑教师' : '新增教师'"
      width="550px"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="80px"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工号" prop="teacherNo">
              <el-input v-model="form.teacherNo" placeholder="请输入工号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="账号" prop="username">
              <el-input v-model="form.username" placeholder="请输入账号" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="!isEdit">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" placeholder="默认123456" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="院系" prop="departmentId">
          <el-select v-model="form.departmentId" placeholder="选择院系" style="width: 100%">
            <el-option 
              v-for="dept in departments" 
              :key="dept.id" 
              :label="dept.name" 
              :value="dept.id" 
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="教师详情" width="550px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工号">{{ currentDetail?.teacherNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentDetail?.name }}</el-descriptions-item>
        <el-descriptions-item label="院系">{{ currentDetail?.departmentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentDetail?.enabled ? 'success' : 'danger'" size="small">
            {{ currentDetail?.enabled ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="账号" :span="2">
          <el-text type="primary" style="font-weight: bold;">{{ currentDetail?.username }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="密码提示" :span="2">
          <el-text type="warning">初始密码：123456（如忘记请点击下方“重置密码”）</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="电话">{{ currentDetail?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentDetail?.email || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="warning" @click="handleResetPassword(currentDetail)">重置密码</el-button>
        <el-button type="primary" @click="handleEdit(currentDetail); detailVisible = false">编辑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getTeachers, createTeacher, updateTeacher, deleteUser, resetUserPassword, DEFAULT_INITIAL_CODE } from '@/api/user'
import { getDepartments } from '@/api/department'

const loading = ref(false)
const submitting = ref(false)
const teachers = ref([])
const departments = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentTeacher = ref(null)
const formRef = ref(null)
const detailVisible = ref(false)
const currentDetail = ref(null)

const filters = ref({
  departmentId: null,
  keyword: ''
})

// 按工号/姓名筛选
const filteredTeachers = computed(() => {
  if (!filters.value.keyword) return teachers.value
  const kw = filters.value.keyword.toLowerCase()
  return teachers.value.filter(t => 
    (t.teacherNo && t.teacherNo.toLowerCase().includes(kw)) ||
    (t.name && t.name.toLowerCase().includes(kw))
  )
})

const form = ref({
  teacherNo: '',
  name: '',
  username: '',
  password: '',
  departmentId: null,
  phone: '',
  email: ''
})

const rules = {
  teacherNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择院系', trigger: 'change' }]
}

// 加载院系列表
async function loadDepartments() {
  try {
    const res = await getDepartments()
    departments.value = res.data || []
    // 默认选择第一个院系
    if (departments.value.length > 0 && !filters.value.departmentId) {
      filters.value.departmentId = departments.value[0].id
    }
  } catch (error) {
    console.error('加载院系失败:', error)
  }
}

// 加载教师列表
async function loadTeachers() {
  loading.value = true
  try {
    const res = await getTeachers(filters.value.departmentId)
    teachers.value = res.data || []
  } catch (error) {
    console.error('加载教师列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 查看教师详情
function handleView(row) {
  currentDetail.value = row
  detailVisible.value = true
}

// 新增教师
function handleAdd() {
  isEdit.value = false
  form.value = {
    teacherNo: '',
    name: '',
    username: '',
    password: DEFAULT_INITIAL_CODE,
    departmentId: filters.value.departmentId,
    phone: '',
    email: ''
  }
  dialogVisible.value = true
}

// 编辑教师
function handleEdit(row) {
  isEdit.value = true
  currentTeacher.value = row
  form.value = {
    teacherNo: row.teacherNo || '',
    name: row.name,
    username: row.username,
    password: '',
    departmentId: row.departmentId,
    phone: row.phone || '',
    email: row.email || ''
  }
  dialogVisible.value = true
}

// 提交表单
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateTeacher(currentTeacher.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createTeacher(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadTeachers()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除教师
async function handleDelete(id) {
  try {
    await deleteUser(id)
    ElMessage.success('删除成功')
    loadTeachers()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}

// 重置密码
async function handleResetPassword(row) {
  try {
    await ElMessageBox.confirm(`确定将 ${row.name} 的密码重置为 123456 吗？`, '重置密码')
    await resetUserPassword(row.id)
    ElMessage.success('密码已重置为 123456')
  } catch (error) {
    if (error === 'cancel') return
    ElMessage.error(error.message || '重置失败')
  }
}

onMounted(async () => {
  await loadDepartments()
  if (filters.value.departmentId) {
    loadTeachers()
  }
})
</script>

<style lang="scss" scoped>
.teacher-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h1 {
      font-size: 22px;
      font-weight: 600;
      color: var(--color-text-primary);
      margin: 0;
    }
  }
  
  .filter-card {
    margin-bottom: 16px;
    
    :deep(.el-card__body) {
      padding: 16px 16px 0;
    }
  }
}
</style>
