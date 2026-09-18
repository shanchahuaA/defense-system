<template>
  <div class="student-page">
    <div class="page-header">
      <h1>学生管理</h1>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增学生
      </el-button>
    </div>
    
    <!-- 搜索筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="院系">
          <el-select v-model="filters.departmentId" placeholder="选择院系" clearable @change="loadStudents" style="width: 220px">
            <el-option label="全部" :value="null" />
            <el-option 
              v-for="dept in departments" 
              :key="dept.id" 
              :label="dept.name" 
              :value="dept.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="论文类型">
          <el-select v-model="filters.thesisType" placeholder="全部" clearable @change="loadStudents" style="width: 120px">
            <el-option label="论文" value="PAPER" />
            <el-option label="设计" value="DESIGN" />
          </el-select>
        </el-form-item>
        <el-form-item label="学号/姓名">
          <el-input v-model="filters.keyword" placeholder="输入学号或姓名" style="width: 150px" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStudents">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 学生列表 -->
    <el-card>
      <el-table :data="filteredStudents" v-loading="loading" style="width: 100%" @row-dblclick="handleView">
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column label="班级专业" min-width="160">
          <template #default="{ row }">
            {{ formatClassMajor(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="departmentName" label="院系" min-width="140" show-overflow-tooltip />
        <el-table-column prop="thesisType" label="类型" width="70">
          <template #default="{ row }">
            <el-tag :type="row.thesisType === 'PAPER' ? 'primary' : 'success'" size="small">
              {{ row.thesisType === 'PAPER' ? '论文' : '设计' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="advisorName" label="指导教师" width="100" />
        <el-table-column prop="groupName" label="答辩小组" width="100" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm 
              title="确定删除该学生吗？" 
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadStudents"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑学生' : '新增学生'"
      width="650px"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="90px"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" placeholder="请输入学号" />
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
            <el-form-item label="班级" prop="className">
              <el-input-number v-model="form.className" :min="1" :max="99" placeholder="几班" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业" prop="major">
              <el-input v-model="form.major" placeholder="请输入专业" />
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
        <el-form-item label="论文题目" prop="thesisTitle">
          <el-input v-model="form.thesisTitle" placeholder="请输入论文题目" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="论文类型" prop="thesisType">
              <el-select v-model="form.thesisType" placeholder="选择类型" style="width: 100%">
                <el-option label="论文" value="PAPER" />
                <el-option label="设计" value="DESIGN" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="指导教师">
              <el-select v-model="form.advisorId" placeholder="选择指导教师" style="width: 100%" clearable>
                <el-option 
                  v-for="t in teachers" 
                  :key="t.id" 
                  :label="t.name" 
                  :value="t.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
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
    <el-dialog v-model="detailVisible" title="学生详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学号">{{ currentDetail?.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentDetail?.name }}</el-descriptions-item>
        <el-descriptions-item label="班级专业">{{ formatClassMajor(currentDetail || {}) }}</el-descriptions-item>
        <el-descriptions-item label="院系">{{ currentDetail?.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="论文类型">
          <el-tag :type="currentDetail?.thesisType === 'PAPER' ? 'primary' : 'success'" size="small">
            {{ currentDetail?.thesisType === 'PAPER' ? '论文' : '设计' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ currentDetail?.advisorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="论文题目" :span="2">{{ currentDetail?.thesisTitle || '-' }}</el-descriptions-item>
        <el-descriptions-item label="答辩小组">{{ currentDetail?.groupName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评阅人">{{ currentDetail?.reviewerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ currentDetail?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentDetail?.email || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEdit(currentDetail); detailVisible = false">编辑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getStudents, createStudent, updateStudent, deleteStudent } from '@/api/student'
import { getDepartments } from '@/api/department'
import { getTeachers } from '@/api/user'

const loading = ref(false)
const submitting = ref(false)
const students = ref([])
const departments = ref([])
const teachers = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentStudent = ref(null)
const formRef = ref(null)
const detailVisible = ref(false)
const currentDetail = ref(null)

const filters = ref({
  departmentId: null,
  thesisType: null,
  keyword: ''
})

// 按学号/姓名筛选
const filteredStudents = computed(() => {
  if (!filters.value.keyword) return students.value
  const kw = filters.value.keyword.toLowerCase()
  return students.value.filter(s => 
    (s.studentNo && s.studentNo.toLowerCase().includes(kw)) ||
    (s.name && s.name.toLowerCase().includes(kw))
  )
})

// 格式化班级专业显示：从学号提取年级，格式如"2023级计算机科学1班"
function formatClassMajor(row) {
  // 从学号第2-3位提取年份（如23 -> 2023）
  let grade = ''
  if (row.studentNo && row.studentNo.length >= 3) {
    const yearCode = row.studentNo.substring(1, 3)
    grade = '20' + yearCode + '级'
  }
  
  // 拼接：年级 + 专业 + 班级 + "班"
  const major = row.major || ''
  const classNum = row.className || ''
  
  if (grade || major || classNum) {
    return `${grade}${major}${classNum}班`
  }
  return ''
}

const pagination = ref({
  page: 1,
  size: 20,
  total: 0
})

const form = ref({
  studentNo: '',
  name: '',
  className: 1,
  major: '',
  departmentId: null,
  thesisTitle: '',
  thesisType: 'PAPER',
  advisorId: null,
  phone: '',
  email: ''
})

const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择院系', trigger: 'change' }],
  thesisType: [{ required: true, message: '请选择论文类型', trigger: 'change' }]
}

// 加载院系列表
async function loadDepartments() {
  try {
    const res = await getDepartments()
    departments.value = res.data || []
    if (departments.value.length > 0 && !filters.value.departmentId) {
      filters.value.departmentId = departments.value[0].id
    }
  } catch (error) {
    console.error('加载院系失败:', error)
  }
}

// 加载教师列表
async function loadTeachers() {
  if (!form.value.departmentId) return
  try {
    const res = await getTeachers(form.value.departmentId)
    teachers.value = res.data || []
  } catch (error) {
    console.error('加载教师失败:', error)
  }
}

// 加载学生列表
async function loadStudents() {
  loading.value = true
  try {
    const params = {
      departmentId: filters.value.departmentId,
      thesisType: filters.value.thesisType,
      page: pagination.value.page - 1,
      size: pagination.value.size
    }
    const res = await getStudents(params)
    students.value = res.data?.list || []
    pagination.value.total = res.data?.total || 0
  } catch (error) {
    console.error('加载学生列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 新增学生
function handleAdd() {
  isEdit.value = false
  form.value = {
    studentNo: '',
    name: '',
    className: 1,
    major: '',
    departmentId: filters.value.departmentId,
    thesisTitle: '',
    thesisType: 'PAPER',
    advisorId: null,
    phone: '',
    email: ''
  }
  dialogVisible.value = true
}

// 查看学生详情
function handleView(row) {
  currentDetail.value = row
  detailVisible.value = true
}

// 编辑学生
function handleEdit(row) {
  isEdit.value = true
  currentStudent.value = row
  form.value = {
    studentNo: row.studentNo,
    name: row.name,
    className: parseInt(row.className) || 1,
    major: row.major || '',
    departmentId: row.departmentId,
    thesisTitle: row.thesisTitle || '',
    thesisType: row.thesisType || 'PAPER',
    advisorId: row.advisorId,
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
      await updateStudent(currentStudent.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createStudent(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadStudents()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除学生
async function handleDelete(id) {
  try {
    await deleteStudent(id)
    ElMessage.success('删除成功')
    loadStudents()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}

// 监听院系变化加载教师
watch(() => form.value.departmentId, (val) => {
  if (val) loadTeachers()
})

onMounted(async () => {
  await loadDepartments()
  if (filters.value.departmentId) {
    loadStudents()
  }
})
</script>

<style lang="scss" scoped>
.student-page {
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
  
  .pagination-wrapper {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
