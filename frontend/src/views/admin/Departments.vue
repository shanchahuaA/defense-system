<template>
  <div class="department-page">
    <div class="page-header">
      <h1>院系管理</h1>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增院系
      </el-button>
    </div>
    
    <!-- 搜索筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="院系名称">
          <el-input v-model="keyword" placeholder="输入名称搜索" style="width: 200px" clearable @clear="keyword = ''" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 院系列表 -->
    <el-card>
      <el-table :data="filteredDepartments" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="院系名称" min-width="180" />
        <el-table-column prop="deanName" label="系主任" width="120" />
        <el-table-column prop="adminName" label="管理员" width="120">
          <template #default="{ row }">
            {{ row.adminName || '未分配' }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleSignature(row)">签名</el-button>
            <el-popconfirm 
              title="确定删除该院系吗？" 
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
      :title="isEdit ? '编辑院系' : '新增院系'"
      width="600px"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
      >
        <el-form-item label="院系名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入院系名称" />
        </el-form-item>
        <el-form-item label="系主任" prop="deanName">
          <el-input v-model="form.deanName" placeholder="请输入系主任姓名" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="2"
            placeholder="请输入院系描述" 
          />
        </el-form-item>
        
        <!-- 管理员设置区域（仅编辑时显示） -->
        <el-divider v-if="isEdit" content-position="left">院系管理员</el-divider>
        <template v-if="isEdit">
          <el-form-item label="当前管理员">
            <span>{{ currentDept?.adminName || '未分配' }}</span>
          </el-form-item>
          <el-form-item label="设置方式">
            <el-radio-group v-model="adminMode">
              <el-radio value="keep">保持不变</el-radio>
              <el-radio value="select">选择已有教师</el-radio>
              <el-radio value="create">创建新管理员</el-radio>
              <el-radio value="none">清除管理员</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="adminMode === 'select'" label="选择教师">
            <el-select v-model="selectedAdminId" placeholder="选择教师" filterable style="width: 100%">
              <el-option
                v-for="teacher in deptTeachers"
                :key="teacher.id"
                :label="teacher.name + ' (' + teacher.username + ')'"
                :value="teacher.id"
              />
            </el-select>
          </el-form-item>
          <template v-if="adminMode === 'create'">
            <el-form-item label="账号">
              <el-input v-model="newAdmin.username" placeholder="登录账号" />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="newAdmin.name" placeholder="管理员用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="newAdmin.password" type="password" placeholder="登录密码" show-password />
            </el-form-item>
          </template>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 签名上传弹窗 -->
    <el-dialog v-model="signatureDialogVisible" title="上传系主任签名" width="400px">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept="image/*"
        :on-change="handleFileChange"
        drag
      >
        <el-icon class="el-icon--upload"><Upload /></el-icon>
        <div class="el-upload__text">将签名图片拖到此处，或<em>点击上传</em></div>
      </el-upload>
      <div v-if="currentDept?.deanSignature" class="current-signature">
        <p>当前签名：</p>
        <img :src="currentDept.deanSignature" alt="签名" style="max-width: 200px" />
      </div>
      <template #footer>
        <el-button @click="signatureDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUploadSignature" :loading="uploading">
          上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 设置管理员弹窗 -->
    <el-dialog v-model="adminDialogVisible" title="设置院系管理员" width="500px">
      <el-form label-width="100px">
        <el-form-item label="院系">
          <span>{{ currentDept?.name }}</span>
        </el-form-item>
        <el-form-item label="当前管理员">
          <span>{{ currentDept?.adminName || '未分配' }}</span>
        </el-form-item>
        <el-form-item label="选择管理员">
          <el-select v-model="selectedAdminId" placeholder="选择教师作为管理员" filterable style="width: 100%">
            <el-option label="无（清除管理员）" :value="null" />
            <el-option
              v-for="teacher in deptTeachers"
              :key="teacher.id"
              :label="teacher.name + ' (' + teacher.username + ')'"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adminDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAdmin" :loading="settingAdmin">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import { 
  getDepartments, 
  createDepartment, 
  updateDepartment, 
  deleteDepartment,
  uploadDeanSignature,
  setDepartmentAdmin
} from '@/api/department'
import { getTeachers } from '@/api/user'

const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const departments = ref([])
const dialogVisible = ref(false)
const signatureDialogVisible = ref(false)
const isEdit = ref(false)
const currentDept = ref(null)
const selectedFile = ref(null)
const selectedAdminId = ref(null)
const deptTeachers = ref([])
const formRef = ref(null)
const uploadRef = ref(null)
const keyword = ref('')
const adminMode = ref('keep')
const newAdmin = ref({ username: '', name: '', password: '' })

// 按名称筛选
const filteredDepartments = computed(() => {
  if (!keyword.value) return departments.value
  return departments.value.filter(dept => 
    dept.name.toLowerCase().includes(keyword.value.toLowerCase())
  )
})

const form = ref({
  name: '',
  deanName: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入院系名称', trigger: 'blur' }]
}

// 搜索
function handleSearch() {
  // 前端筛选，computed自动处理
}

// 加载院系列表
async function loadDepartments() {
  loading.value = true
  try {
    const res = await getDepartments()
    departments.value = res.data || []
  } catch (error) {
    console.error('加载院系列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 新增院系
function handleAdd() {
  isEdit.value = false
  form.value = { name: '', deanName: '', description: '' }
  dialogVisible.value = true
}

// 编辑院系
async function handleEdit(row) {
  isEdit.value = true
  currentDept.value = row
  form.value = {
    name: row.name,
    deanName: row.deanName || '',
    description: row.description || ''
  }
  adminMode.value = 'keep'
  selectedAdminId.value = row.adminId || null
  newAdmin.value = { username: '', name: '', password: '' }
  
  // 加载该院系的教师列表
  try {
    const res = await getTeachers({ departmentId: row.id })
    deptTeachers.value = res.data?.list || res.data || []
  } catch (error) {
    console.error('加载教师列表失败:', error)
    deptTeachers.value = []
  }
  
  dialogVisible.value = true
}

// 提交表单
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  // 创建新管理员时校验
  if (isEdit.value && adminMode.value === 'create') {
    if (!newAdmin.value.username || !newAdmin.value.name || !newAdmin.value.password) {
      ElMessage.warning('请填写完整的管理员信息')
      return
    }
  }
  
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateDepartment(currentDept.value.id, form.value)
      
      // 处理管理员设置
      if (adminMode.value === 'select' && selectedAdminId.value) {
        await setDepartmentAdmin(currentDept.value.id, selectedAdminId.value)
      } else if (adminMode.value === 'create') {
        // 创建新管理员并设置
        const { createDeptAdmin } = await import('@/api/department')
        await createDeptAdmin(currentDept.value.id, newAdmin.value)
      } else if (adminMode.value === 'none') {
        await setDepartmentAdmin(currentDept.value.id, null)
      }
      
      ElMessage.success('更新成功')
    } else {
      await createDepartment(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadDepartments()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除院系
async function handleDelete(id) {
  try {
    await deleteDepartment(id)
    ElMessage.success('删除成功')
    loadDepartments()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}

// 打开签名上传
function handleSignature(row) {
  currentDept.value = row
  selectedFile.value = null
  signatureDialogVisible.value = true
}

// 文件选择
function handleFileChange(file) {
  selectedFile.value = file.raw
}

// 上传签名
async function handleUploadSignature() {
  if (!selectedFile.value) {
    ElMessage.warning('请选择签名图片')
    return
  }
  
  uploading.value = true
  try {
    await uploadDeanSignature(currentDept.value.id, selectedFile.value)
    ElMessage.success('上传成功')
    signatureDialogVisible.value = false
    loadDepartments()
  } catch (error) {
    ElMessage.error(error.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

// 打开设置管理员弹窗
async function handleSetAdmin(row) {
  currentDept.value = row
  selectedAdminId.value = row.adminId || null
  
  // 加载该院系的教师列表
  try {
    const res = await getTeachers({ departmentId: row.id })
    deptTeachers.value = res.data?.list || res.data || []
  } catch (error) {
    console.error('加载教师列表失败:', error)
    deptTeachers.value = []
  }
  
  adminDialogVisible.value = true
}

// 保存管理员设置
async function handleSaveAdmin() {
  settingAdmin.value = true
  try {
    await setDepartmentAdmin(currentDept.value.id, selectedAdminId.value)
    ElMessage.success('设置成功')
    adminDialogVisible.value = false
    loadDepartments()
  } catch (error) {
    ElMessage.error(error.message || '设置失败')
  } finally {
    settingAdmin.value = false
  }
}

onMounted(() => {
  loadDepartments()
})
</script>

<style lang="scss" scoped>
.department-page {
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
  
  .current-signature {
    margin-top: 16px;
    padding: 12px;
    background: var(--color-bg-secondary);
    border-radius: var(--radius-md);
    
    p {
      margin: 0 0 8px 0;
      font-size: 14px;
      color: var(--color-text-secondary);
    }
  }
}
</style>
