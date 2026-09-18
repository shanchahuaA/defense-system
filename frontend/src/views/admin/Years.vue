<template>
  <div class="year-page">
    <div class="page-header">
      <h1>年份管理</h1>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增年份
      </el-button>
    </div>
    
    <!-- 年份列表 -->
    <el-card>
      <el-table :data="years" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="年份名称" min-width="200" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.isCurrent ? 'success' : 'info'">
              {{ row.isCurrent ? '当前年份' : '历史年份' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="答辩成绩表日期" width="130">
          <template #default="{ row }">
            {{ row.scoreDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="成绩评定表日期" width="130">
          <template #default="{ row }">
            {{ row.evaluationDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button 
              v-if="!row.isCurrent" 
              type="success" 
              link 
              @click="handleSetCurrent(row)"
            >
              设为当前
            </el-button>
            <el-popconfirm 
              v-if="!row.isCurrent"
              title="确定删除该年份吗？" 
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
      :title="isEdit ? '编辑年份' : '新增年份'"
      width="450px"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="90px"
      >
        <el-form-item label="年份名称" prop="name">
          <el-input v-model="form.name" placeholder="如：2025年夏答辩季" />
        </el-form-item>
        <el-form-item label="设为当前">
          <el-switch v-model="form.isCurrent" />
        </el-form-item>
        <el-form-item label="答辩成绩表日期">
          <el-date-picker
            v-model="form.scoreDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="成绩评定表日期">
          <el-date-picker
            v-model="form.evaluationDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getYears, createYear, updateYear, deleteYear, setCurrentYear } from '@/api/year'

const loading = ref(false)
const submitting = ref(false)
const years = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentYearItem = ref(null)
const formRef = ref(null)

const form = ref({
  name: '',
  isCurrent: false,
  scoreDate: null,
  evaluationDate: null
})

const rules = {
  name: [{ required: true, message: '请输入年份名称', trigger: 'blur' }]
}

// 加载年份列表
async function loadYears() {
  loading.value = true
  try {
    const res = await getYears()
    years.value = res.data || []
  } catch (error) {
    console.error('加载年份列表失败:', error)
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

// 新增年份
function handleAdd() {
  isEdit.value = false
  form.value = { name: '', isCurrent: false, scoreDate: null, evaluationDate: null }
  dialogVisible.value = true
}

// 编辑年份
function handleEdit(row) {
  isEdit.value = true
  currentYearItem.value = row
  form.value = {
    name: row.name,
    isCurrent: row.isCurrent,
    scoreDate: row.scoreDate || null,
    evaluationDate: row.evaluationDate || null
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
      await updateYear(currentYearItem.value.id, form.value.name, form.value.isCurrent, form.value.scoreDate, form.value.evaluationDate)
      ElMessage.success('更新成功')
    } else {
      await createYear(form.value.name, form.value.isCurrent, form.value.scoreDate, form.value.evaluationDate)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadYears()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除年份
async function handleDelete(id) {
  try {
    await deleteYear(id)
    ElMessage.success('删除成功')
    loadYears()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}

// 设为当前年份
async function handleSetCurrent(row) {
  try {
    await setCurrentYear(row.id)
    ElMessage.success(`已将 ${row.name} 设为当前年份`)
    loadYears()
  } catch (error) {
    ElMessage.error(error.message || '设置失败')
  }
}

onMounted(() => {
  loadYears()
})
</script>

<style lang="scss" scoped>
.year-page {
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
}
</style>
