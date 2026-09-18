<template>
  <div class="final-scoring-page">
    <div class="page-header">
      <h1>大组答辩评分</h1>
      <el-text type="info">对每个小组第一名进行评分，满分100分</el-text>
    </div>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>大组答辩学生</span>
          <el-tag type="info" size="small" v-if="currentYear">{{ currentYear.name }}</el-tag>
        </div>
      </template>

      <el-table :data="students" v-loading="loading" style="width: 100%">
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="thesisTitle" label="论文/设计题目" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.thesisType === 'PAPER' ? 'primary' : 'success'" size="small">
              {{ row.thesisType === 'PAPER' ? '论文' : '设计' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="groupName" label="所属小组" width="120" />
        <el-table-column label="小组成绩" width="100" align="center">
          <template #default="{ row }">
            <span class="score">{{ row.groupAvgScore ? row.groupAvgScore.toFixed(1) : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="大组成绩" width="100" align="center">
          <template #default="{ row }">
            <span class="score final" v-if="row.finalScore">{{ row.finalScore.toFixed(1) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="我的评分" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.myScore" type="success">{{ row.myScore }}分</el-tag>
            <el-tag v-else type="info">未评分</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleScore(row)">
              {{ row.myScore ? '修改' : '评分' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && students.length === 0" description="暂无需要大组答辩的学生" />
    </el-card>

    <!-- 提示说明 -->
    <el-card class="tips-card">
      <template #header>
        <span>评分说明</span>
      </template>
      <ul class="tips-list">
        <li><strong>参与学生：</strong>每个答辩小组的第一名自动进入大组答辩</li>
        <li><strong>调节系数：</strong>大组成绩 ÷ 小组成绩 = 调节系数（精确到3位小数）</li>
        <li><strong>最终成绩：</strong>小组内每位学生的最终答辩成绩 = 小组成绩 × 调节系数</li>
        <li><strong>评分范围：</strong>0-100分的整数</li>
      </ul>
    </el-card>

    <!-- 评分弹窗 -->
    <el-dialog v-model="dialogVisible" title="大组答辩评分" width="450px">
      <div v-if="currentStudent" class="student-info">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentStudent.studentName }}</el-descriptions-item>
          <el-descriptions-item label="题目">{{ currentStudent.thesisTitle }}</el-descriptions-item>
          <el-descriptions-item label="小组成绩">
            {{ currentStudent.groupAvgScore ? currentStudent.groupAvgScore.toFixed(1) : '-' }} 分
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-divider />

      <el-form :model="scoreForm" label-width="80px">
        <el-form-item label="评分">
          <el-input-number
            v-model="scoreForm.totalScore"
            :min="0"
            :max="100"
            :step="1"
            size="large"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交评分</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFinalDefenseStudents, saveFinalScore, getMyFinalScore } from '@/api/finalScore'
import { getCurrentYear } from '@/api/year'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const students = ref([])
const currentYear = ref(null)
const dialogVisible = ref(false)
const currentStudent = ref(null)

const scoreForm = ref({
  totalScore: 0
})

// 加载当前年份
async function loadCurrentYear() {
  try {
    const res = await getCurrentYear()
    currentYear.value = res.data
  } catch (error) {
    console.error('获取当前年份失败:', error)
  }
}

// 加载学生列表
async function loadStudents() {
  loading.value = true
  try {
    const res = await getFinalDefenseStudents(currentYear.value?.id)
    const list = res.data || []
    
    // 加载每个学生的当前教师评分
    for (const student of list) {
      try {
        const scoreRes = await getMyFinalScore(student.studentId, currentYear.value?.id)
        if (scoreRes.data) {
          student.myScore = scoreRes.data.totalScore
        }
      } catch (e) {
        // 忽略
      }
    }
    
    students.value = list
  } catch (error) {
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

// 打开评分弹窗
async function handleScore(student) {
  currentStudent.value = student
  scoreForm.value.totalScore = student.myScore || 0
  dialogVisible.value = true
}

// 提交评分
async function handleSubmit() {
  if (scoreForm.value.totalScore < 0 || scoreForm.value.totalScore > 100) {
    ElMessage.warning('请输入0-100分的成绩')
    return
  }

  submitting.value = true
  try {
    await saveFinalScore({
      studentId: currentStudent.value.studentId,
      yearId: currentYear.value?.id,
      totalScore: scoreForm.value.totalScore
    })
    ElMessage.success('评分提交成功')
    dialogVisible.value = false
    loadStudents()
  } catch (error) {
    ElMessage.error('评分提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadCurrentYear()
  await loadStudents()
})
</script>

<style lang="scss" scoped>
.final-scoring-page {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.score {
  font-weight: 600;
  color: var(--el-color-primary);
  
  &.final {
    color: var(--el-color-success);
  }
}

.tips-card {
  margin-top: 20px;
  
  .tips-list {
    margin: 0;
    padding-left: 20px;
    
    li {
      line-height: 2;
      color: #606266;
    }
  }
}

.student-info {
  margin-bottom: 16px;
}
</style>
