<template>
  <div class="group-page">
    <div class="page-header">
      <h1>答辩分组管理</h1>
      <div class="header-actions">
        <el-button type="success" @click="handleStartUniversityDefense" :loading="startingDefense">
          <el-icon><Monitor /></el-icon>
          开启大组答辩
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增分组
        </el-button>
      </div>
    </div>
    
    <!-- 搜索筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="院系">
          <el-select v-model="filters.departmentId" placeholder="选择院系" @change="loadGroups" style="width: 220px">
            <el-option 
              v-for="dept in departments" 
              :key="dept.id" 
              :label="dept.name" 
              :value="dept.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadGroups">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 分组列表 -->
    <el-card>
      <el-table :data="groups" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="分组名称" width="150" />
        <el-table-column prop="thesisType" label="答辩类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.thesisType" :type="row.thesisType === 'PAPER' ? 'primary' : 'success'" size="small">
              {{ row.thesisType === 'PAPER' ? '论文' : '设计' }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="leaderName" label="组长" width="100" />
        <el-table-column label="组员" min-width="200">
          <template #default="{ row }">
            <el-tag 
              v-for="teacher in row.teachers" 
              :key="teacher.id" 
              size="small" 
              style="margin-right: 4px"
            >
              {{ teacher.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column prop="defenseTime" label="时间" width="150" />
        <el-table-column prop="studentCount" label="学生数" width="80" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm 
              title="确定删除该分组吗？" 
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
      :title="isEdit ? '编辑分组' : '新增分组'"
      width="550px"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="90px"
      >
        <el-form-item label="分组名称" prop="name">
          <el-input v-model="form.name" placeholder="如：计算机学院第一组" />
        </el-form-item>
        <el-form-item label="答辩类型" prop="thesisType">
          <el-select v-model="form.thesisType" placeholder="选择类型" style="width: 100%">
            <el-option label="论文答辩" value="PAPER" />
            <el-option label="设计答辩" value="DESIGN" />
          </el-select>
        </el-form-item>
        <el-form-item label="组长" prop="leaderId">
          <el-select v-model="form.leaderId" placeholder="选择组长" style="width: 100%">
            <el-option 
              v-for="t in teachers" 
              :key="t.id" 
              :label="t.name" 
              :value="t.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="组员教师">
          <el-select v-model="form.teacherIds" placeholder="选择组员" multiple style="width: 100%">
            <el-option 
              v-for="t in teachers" 
              :key="t.id" 
              :label="t.name" 
              :value="t.id" 
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="答辩地点">
              <el-input v-model="form.location" placeholder="如：教学楼301" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="答辩时间">
              <el-input v-model="form.defenseTime" placeholder="如：2025-06-15 上午" />
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
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Monitor } from '@element-plus/icons-vue'
import { getGroups, createGroup, updateGroup, deleteGroup, startUniversityDefense } from '@/api/group'
import { getDepartments } from '@/api/department'
import { getTeachers } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const startingDefense = ref(false)
const groups = ref([])
const departments = ref([])
const teachers = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentGroup = ref(null)
const formRef = ref(null)

const filters = ref({
  departmentId: null
})

const form = ref({
  name: '',
  thesisType: null,
  leaderId: null,
  teacherIds: [],
  location: '',
  defenseTime: ''
})

const rules = {
  name: [{ required: true, message: '请输入分组名称', trigger: 'blur' }]
}

// 加载院系列表
async function loadDepartments() {
  try {
    const res = await getDepartments()
    departments.value = res.data || []
    if (departments.value.length > 0) {
      filters.value.departmentId = departments.value[0].id
    }
  } catch (error) {
    console.error('加载院系失败:', error)
  }
}

// 加载教师列表
async function loadTeachers() {
  if (!filters.value.departmentId) return
  try {
    const res = await getTeachers(filters.value.departmentId)
    teachers.value = res.data || []
  } catch (error) {
    console.error('加载教师失败:', error)
  }
}

// 加载分组列表
async function loadGroups() {
  if (!filters.value.departmentId) return
  loading.value = true
  try {
    await loadTeachers()
    const res = await getGroups(filters.value.departmentId, userStore.selectedYearId)
    groups.value = res.data || []
  } catch (error) {
    console.error('加载分组列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 新增分组
function handleAdd() {
  isEdit.value = false
  form.value = {
    name: '',
    thesisType: null,
    leaderId: null,
    teacherIds: [],
    departmentId: filters.value.departmentId,
    location: '',
    defenseTime: ''
  }
  dialogVisible.value = true
}

// 编辑分组
function handleEdit(row) {
  isEdit.value = true
  currentGroup.value = row
  form.value = {
    name: row.name,
    thesisType: row.thesisType,
    leaderId: row.leaderId,
    teacherIds: (row.teachers || []).map(t => t.id),
    departmentId: row.departmentId,
    location: row.location || '',
    defenseTime: row.defenseTime || ''
  }
  dialogVisible.value = true
}

// 提交表单
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    const data = {
      ...form.value,
      departmentId: filters.value.departmentId
    }
    if (isEdit.value) {
      await updateGroup(currentGroup.value.id, data)
      ElMessage.success('更新成功')
    } else {
      await createGroup(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadGroups()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除分组
async function handleDelete(id) {
  try {
    await deleteGroup(id)
    ElMessage.success('删除成功')
    loadGroups()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}

// 开启大组答辩
async function handleStartUniversityDefense() {
  if (!filters.value.departmentId) return

  try {
    await ElMessageBox.confirm(
      '开启大组答辩将自动提取各小组第一名并组建“大组答辩”小组，是否继续？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    startingDefense.value = true
    await startUniversityDefense(filters.value.departmentId)
    ElMessage.success('大组答辩开启成功')
    loadGroups()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    startingDefense.value = false
  }
}

onMounted(async () => {
  await loadDepartments()
  if (filters.value.departmentId) {
    loadGroups()
  }
})
</script>

<style lang="scss" scoped>
.group-page {
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
