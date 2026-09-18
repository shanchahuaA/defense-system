<template>
  <div class="my-students-page">
    <div class="page-header">
      <h1>我指导的学生</h1>
    </div>

    <!-- 学生列表 -->
    <el-card>
      <el-table :data="students" v-loading="loading" style="width: 100%" @row-dblclick="handleView">
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
        <el-table-column prop="reviewerName" label="评阅人" width="100" />
        <el-table-column prop="groupName" label="答辩小组" width="120" />
        <el-table-column label="指导成绩" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.advisorScore" class="score">{{ row.advisorScore }}</span>
            <el-button v-else type="primary" link size="small" @click="handleScore(row, 'advisor')">
              填写
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="评阅成绩" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.reviewerScore" class="score">{{ row.reviewerScore }}</span>
            <el-tag v-else type="info" size="small">待评阅</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleSelectReviewer(row)">选评阅人</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && students.length === 0" description="暂无指导的学生" />
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑学生信息" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="学生姓名">
          <el-input :value="currentStudent?.name" disabled />
        </el-form-item>
        <el-form-item label="论文/设计题目">
          <el-input v-model="editForm.thesisTitle" placeholder="请输入题目" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input
            v-model="editForm.thesisAbstract"
            type="textarea"
            :rows="5"
            placeholder="请输入论文/设计摘要"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 评分弹窗 -->
    <el-dialog v-model="scoreVisible" title="填写指导教师成绩" width="400px">
      <el-form :model="scoreForm" label-width="100px">
        <el-form-item label="学生">
          <el-text>{{ currentStudent?.name }}</el-text>
        </el-form-item>
        <el-form-item label="指导成绩">
          <el-input-number v-model="scoreForm.score" :min="0" :max="100" style="width: 100%" />
          <div class="form-tip">请填写 0-100 分的整数成绩</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveScore" :loading="saving">提交</el-button>
      </template>
    </el-dialog>

    <!-- 选择评阅人弹窗 -->
    <el-dialog v-model="reviewerVisible" title="选择评阅人" width="500px">
      <el-form :model="reviewerForm" label-width="100px">
        <el-form-item label="学生">
          <el-text>{{ currentStudent?.name }}</el-text>
        </el-form-item>
        <el-form-item label="当前评阅人">
          <el-text v-if="currentStudent?.reviewerName">{{ currentStudent.reviewerName }}</el-text>
          <el-text v-else type="info">未分配</el-text>
        </el-form-item>
        <el-form-item label="选择评阅人">
          <el-select v-model="reviewerForm.reviewerId" placeholder="请选择评阅人" style="width: 100%">
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="teacher.name"
              :value="teacher.id"
            />
          </el-select>
          <div class="form-tip">评阅人需从本系教师中选择</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewerVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveReviewer" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyStudents, updateStudent, assignStudentReviewer } from '@/api/student'
import { getTeachers } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const students = ref([])
const teachers = ref([])

// 编辑相关
const editVisible = ref(false)
const currentStudent = ref(null)
const editForm = ref({
  thesisTitle: '',
  thesisAbstract: ''
})

// 评分相关
const scoreVisible = ref(false)
const scoreForm = ref({
  score: 0
})

// 评阅人相关
const reviewerVisible = ref(false)
const reviewerForm = ref({
  reviewerId: null
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

// 加载教师列表（用于选择评阅人）
async function loadTeachers() {
  try {
    const departmentId = userStore.userInfo?.departmentId
    if (departmentId) {
      const res = await getTeachers(departmentId)
      // 过滤掉自己
      teachers.value = (res.data || []).filter(t => t.id !== userStore.userInfo?.userId)
    }
  } catch (error) {
    console.error('加载教师列表失败:', error)
  }
}

// 查看详情
function handleView(row) {
  handleEdit(row)
}

// 编辑学生
function handleEdit(row) {
  currentStudent.value = row
  editForm.value = {
    thesisTitle: row.thesisTitle || '',
    thesisAbstract: row.thesisAbstract || ''
  }
  editVisible.value = true
}

// 保存编辑
async function handleSaveEdit() {
  if (!currentStudent.value) return
  
  saving.value = true
  try {
    await updateStudent(currentStudent.value.id, {
      ...currentStudent.value,
      thesisTitle: editForm.value.thesisTitle,
      thesisAbstract: editForm.value.thesisAbstract
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    loadStudents()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 打开评分弹窗
function handleScore(row) {
  currentStudent.value = row
  scoreForm.value.score = row.advisorScore || 0
  scoreVisible.value = true
}

// 保存评分
async function handleSaveScore() {
  if (!currentStudent.value) return
  
  saving.value = true
  try {
    await updateStudent(currentStudent.value.id, {
      ...currentStudent.value,
      advisorScore: scoreForm.value.score
    })
    ElMessage.success('成绩保存成功')
    scoreVisible.value = false
    loadStudents()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 选择评阅人
function handleSelectReviewer(row) {
  currentStudent.value = row
  reviewerForm.value.reviewerId = row.reviewerId
  reviewerVisible.value = true
}

// 保存评阅人
async function handleSaveReviewer() {
  if (!currentStudent.value || !reviewerForm.value.reviewerId) {
    ElMessage.warning('请选择评阅人')
    return
  }
  
  saving.value = true
  try {
    await assignStudentReviewer(currentStudent.value.id, reviewerForm.value.reviewerId)
    ElMessage.success('评阅人分配成功')
    reviewerVisible.value = false
    loadStudents()
  } catch (error) {
    ElMessage.error('分配失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadStudents()
  loadTeachers()
})
</script>

<style lang="scss" scoped>
.my-students-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  
  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
}

.score {
  font-weight: 600;
  color: var(--el-color-primary);
}

.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
</style>
