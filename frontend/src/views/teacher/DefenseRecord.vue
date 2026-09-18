<template>
  <div class="defense-record-page">
    <div class="page-header">
      <h1>答辩记录</h1>
      <el-text type="info">填写指导学生的答辩情况记录</el-text>
    </div>

    <!-- 学生列表 -->
    <el-card>
      <el-table :data="students" v-loading="loading" style="width: 100%">
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="thesisTitle" label="论文/设计题目" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.thesisType === 'PAPER' ? 'primary' : 'success'" size="small">
              {{ row.thesisType === 'PAPER' ? '论文' : '设计' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="groupName" label="答辩小组" width="120" />
        <el-table-column label="答辩记录" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.defenseRecord" type="success" size="small">已填写</el-tag>
            <el-tag v-else type="info" size="small">待填写</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              {{ row.defenseRecord ? '编辑' : '填写' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && students.length === 0" description="暂无指导的学生" />
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="答辩记录" width="700px">
      <div v-if="currentStudent" class="student-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生">{{ currentStudent.name }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="题目" :span="2">{{ currentStudent.thesisTitle }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-divider />

      <el-form :model="form" label-width="120px">
        <el-form-item label="答辩日期">
          <el-date-picker
            v-model="form.defenseDate"
            type="date"
            placeholder="选择日期"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="答辩地点">
          <el-input v-model="form.defenseLocation" placeholder="如：教学楼301" />
        </el-form-item>
        <el-form-item label="答辩问题记录">
          <el-input
            v-model="form.defenseQuestions"
            type="textarea"
            :rows="4"
            placeholder="记录答辩过程中提出的问题..."
          />
        </el-form-item>
        <el-form-item label="答题情况">
          <el-input
            v-model="form.defenseAnswers"
            type="textarea"
            :rows="4"
            placeholder="记录学生的回答情况..."
          />
        </el-form-item>
        <el-form-item label="答辩评价">
          <el-input
            v-model="form.defenseEvaluation"
            type="textarea"
            :rows="3"
            placeholder="对答辩表现的整体评价..."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyStudents, updateStudent } from '@/api/student'

const loading = ref(false)
const saving = ref(false)
const students = ref([])
const dialogVisible = ref(false)
const currentStudent = ref(null)

const form = ref({
  defenseDate: null,
  defenseLocation: '',
  defenseQuestions: '',
  defenseAnswers: '',
  defenseEvaluation: ''
})

// 加载学生列表
async function loadStudents() {
  loading.value = true
  try {
    const res = await getMyStudents()
    students.value = res.data || []
  } catch (error) {
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

// 编辑答辩记录
function handleEdit(row) {
  currentStudent.value = row
  // 解析已有记录
  const record = row.defenseRecord ? JSON.parse(row.defenseRecord) : {}
  form.value = {
    defenseDate: row.defenseDate,
    defenseLocation: row.defenseLocation || '',
    defenseQuestions: record.questions || '',
    defenseAnswers: record.answers || '',
    defenseEvaluation: record.evaluation || ''
  }
  dialogVisible.value = true
}

// 保存答辩记录
async function handleSave() {
  if (!currentStudent.value) return
  
  saving.value = true
  try {
    const record = JSON.stringify({
      questions: form.value.defenseQuestions,
      answers: form.value.defenseAnswers,
      evaluation: form.value.defenseEvaluation
    })
    
    await updateStudent(currentStudent.value.id, {
      ...currentStudent.value,
      defenseDate: form.value.defenseDate,
      defenseLocation: form.value.defenseLocation,
      defenseRecord: record
    })
    
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadStudents()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadStudents()
})
</script>

<style lang="scss" scoped>
.defense-record-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  
  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px 0;
  }
}

.student-info {
  margin-bottom: 16px;
}
</style>
