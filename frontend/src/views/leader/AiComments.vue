<template>
  <div class="ai-comments-page">
    <div class="page-header">
      <h1>AI评语生成</h1>
      <el-button type="primary" @click="handleBatchGenerate" :loading="batchGenerating" :disabled="selectedStudents.length === 0">
        批量生成 ({{ selectedStudents.length }})
      </el-button>
    </div>

    <!-- 学生列表 -->
    <el-card>
      <el-table
        ref="tableRef"
        :data="students"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="thesisTitle" label="论文题目" min-width="200" show-overflow-tooltip />
        <el-table-column label="论文类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.thesisType === 'PAPER' ? 'primary' : 'success'" size="small">
              {{ row.thesisType === 'PAPER' ? '论文' : '设计' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成绩" width="140">
          <template #default="{ row }">
            <span v-if="row.groupScore">小组: {{ row.groupScore }}</span>
            <span v-if="row.finalScore"> / 大组: {{ row.finalScore }}</span>
            <span v-if="!row.groupScore && !row.finalScore" class="text-muted">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="评语状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.aiComment ? 'success' : 'info'" size="small">
              {{ row.aiComment ? '已生成' : '待生成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleGenerate(row)" :loading="generatingId === row.id">
              {{ row.aiComment ? '重新生成' : '生成' }}
            </el-button>
            <el-button type="success" link @click="handleView(row)" :disabled="!row.aiComment">
              查看/编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 评语编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="AI评语" width="600px">
      <div v-if="currentStudent">
        <el-descriptions :column="2" border class="mb-4">
          <el-descriptions-item label="学生">{{ currentStudent.name }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="论文题目" :span="2">{{ currentStudent.thesisTitle }}</el-descriptions-item>
        </el-descriptions>
        <el-form-item label="评语内容">
          <el-input
            v-model="editComment"
            type="textarea"
            :rows="8"
            placeholder="AI生成的评语将显示在这里，您可以手动编辑"
          />
        </el-form-item>
      </div>
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
import { getMyGroupStudents } from '@/api/group'
import { generateComment, batchGenerateComments, saveComment } from '@/api/ai'

const loading = ref(false)
const students = ref([])
const selectedStudents = ref([])
const generatingId = ref(null)
const batchGenerating = ref(false)
const dialogVisible = ref(false)
const currentStudent = ref(null)
const editComment = ref('')
const saving = ref(false)

// 加载学生列表
async function loadStudents() {
  loading.value = true
  try {
    const res = await getMyGroupStudents()
    students.value = res.data || []
  } catch (error) {
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

// 选择变化
function handleSelectionChange(selection) {
  selectedStudents.value = selection
}

// 单个生成
async function handleGenerate(row) {
  generatingId.value = row.id
  try {
    const res = await generateComment(row.id)
    row.aiComment = res.data
    ElMessage.success('评语生成成功')
  } catch (error) {
    ElMessage.error(error.message || '生成失败')
  } finally {
    generatingId.value = null
  }
}

// 批量生成
async function handleBatchGenerate() {
  if (selectedStudents.value.length === 0) return

  batchGenerating.value = true
  try {
    const ids = selectedStudents.value.map(s => s.id)
    const res = await batchGenerateComments(ids)
    
    // 更新本地数据
    const results = res.data || {}
    students.value.forEach(s => {
      if (results[s.id] && !results[s.id].startsWith('生成失败')) {
        s.aiComment = results[s.id]
      }
    })

    const successCount = Object.values(results).filter(v => !v.startsWith('生成失败')).length
    ElMessage.success(`批量生成完成，成功 ${successCount}/${ids.length}`)
  } catch (error) {
    ElMessage.error('批量生成失败')
  } finally {
    batchGenerating.value = false
  }
}

// 查看/编辑
function handleView(row) {
  currentStudent.value = row
  editComment.value = row.aiComment || ''
  dialogVisible.value = true
}

// 保存评语
async function handleSave() {
  if (!currentStudent.value) return

  saving.value = true
  try {
    await saveComment(currentStudent.value.id, editComment.value)
    currentStudent.value.aiComment = editComment.value
    ElMessage.success('保存成功')
    dialogVisible.value = false
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

<style scoped>
.ai-comments-page {
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

.text-muted {
  color: #909399;
}

.mb-4 {
  margin-bottom: 16px;
}
</style>
