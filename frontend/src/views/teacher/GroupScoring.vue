<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getMyGroups } from '@/api/group'
import { getGroupStudents } from '@/api/student'
import { getScoreConfigsByType } from '@/api/scoreConfig'
import { saveGroupScore, getTeacherStudentScore, getTeacherScoresByYear } from '@/api/groupScore'
import { getCurrentYear } from '@/api/year'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)

// 当前年份
const currentYear = ref(null)

// 教师所在分组
const groups = ref([])
const selectedGroupId = ref(null)

// 学生列表
const students = ref([])

// 评分配置
const scoreConfigs = ref([])

// 当前评分的学生
const currentStudent = ref(null)
const dialogVisible = ref(false)

// 评分表单
const scoreForm = ref({
  item1Score: 0,
  item2Score: 0,
  item3Score: 0,
  item4Score: 0,
  item5Score: 0,
  item6Score: 0
})

// 自动分配用的总分输入
const inputTotalScore = ref(0)

// 加载当前年份
async function loadCurrentYear() {
  try {
    const res = await getCurrentYear()
    currentYear.value = res.data
  } catch (error) {
    console.error('获取当前年份失败:', error)
  }
}

// 加载教师所在的分组
async function loadGroups() {
  try {
    const res = await getMyGroups(currentYear.value?.id)
    groups.value = res.data?.list || res.data || []
    if (groups.value.length > 0) {
      selectedGroupId.value = groups.value[0].id
      loadStudents()
    }
  } catch (error) {
    console.error('获取分组失败:', error)
  }
}

// 加载分组内学生
async function loadStudents() {
  if (!selectedGroupId.value) return
  loading.value = true
  try {
    const res = await getGroupStudents(selectedGroupId.value)
    const studentList = res.data?.list || res.data || []
    
    // 获取当前教师已有的评分
    const teacherScoresRes = await getTeacherScoresByYear(userStore.userInfo?.userId, currentYear.value?.id)
    const teacherScores = teacherScoresRes.data || []
    
    // 合并评分到学生列表
    students.value = studentList.map(student => {
      const scoreRecord = teacherScores.find(s => s.studentId === student.id)
      return {
        ...student,
        myScore: scoreRecord ? scoreRecord.totalScore : null
      }
    })
  } catch (error) {
    console.error('获取学生列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载评分配置
async function loadScoreConfigs(thesisType) {
  try {
    const res = await getScoreConfigsByType(thesisType)
    scoreConfigs.value = res.data || []
  } catch (error) {
    console.error('获取评分配置失败:', error)
  }
}

// 打开评分弹窗
async function handleScore(student) {
  currentStudent.value = student
  
  // 加载对应类型的评分配置
  await loadScoreConfigs(student.thesisType || 'PAPER')
  
  // 检查是否已有评分
  try {
    const res = await getTeacherStudentScore(
      userStore.userInfo?.userId,
      student.id,
      currentYear.value?.id
    )
    if (res.data) {
      scoreForm.value = {
        item1Score: res.data.item1Score || 0,
        item2Score: res.data.item2Score || 0,
        item3Score: res.data.item3Score || 0,
        item4Score: res.data.item4Score || 0,
        item5Score: res.data.item5Score || 0,
        item6Score: res.data.item6Score || 0
      }
    } else {
      // 重置为0
      scoreForm.value = {
        item1Score: 0,
        item2Score: 0,
        item3Score: 0,
        item4Score: 0,
        item5Score: 0,
        item6Score: 0
      }
    }
  } catch (error) {
    console.error('获取已有评分失败:', error)
  }
  
  dialogVisible.value = true
}

// 计算总分
const totalScore = computed(() => {
  return (scoreForm.value.item1Score || 0) +
    (scoreForm.value.item2Score || 0) +
    (scoreForm.value.item3Score || 0) +
    (scoreForm.value.item4Score || 0) +
    (scoreForm.value.item5Score || 0) +
    (scoreForm.value.item6Score || 0)
})

// 提交评分
async function handleSubmit() {
  saving.value = true
  try {
    const score = {
      studentId: currentStudent.value.id,
      teacherId: userStore.userInfo?.userId,
      yearId: currentYear.value?.id,
      ...scoreForm.value,
      totalScore: totalScore.value
    }
    await saveGroupScore(score)
    ElMessage.success('评分提交成功')
    dialogVisible.value = false
    loadStudents() // 重新加载学生列表以更新显示分数
  } catch (error) {
    ElMessage.error('评分提交失败')
  } finally {
    saving.value = false
  }
}

// 获取评分项对应的表单字段
function getScoreField(index) {
  const fields = ['item1Score', 'item2Score', 'item3Score', 'item4Score', 'item5Score', 'item6Score']
  return fields[index] || 'item1Score'
}

// 自动分配小项成绩
function autoDistributeScore() {
  const total = inputTotalScore.value
  if (!total || total <= 0 || total > 100) {
    ElMessage.warning('请输入0-100之间的总分')
    return
  }
  
  if (scoreConfigs.value.length === 0) {
    ElMessage.warning('暂无评分配置')
    return
  }
  
  // 计算总权重
  const totalWeight = scoreConfigs.value.reduce((sum, config) => sum + (config.weight || 0), 0)
  if (totalWeight === 0) {
    ElMessage.warning('评分配置权重总和为0')
    return
  }
  
  // 按权重比例分配
  let distributedTotal = 0
  const scores = []
  
  scoreConfigs.value.forEach((config, index) => {
    const weight = config.weight || 0
    const maxScore = config.maxScore || (weight * 100)
    
    // 按权重计算理论分数
    let score = Math.round((total * weight / totalWeight) * 10) / 10
    
    // 确保不超过最大分
    score = Math.min(score, maxScore)
    
    // 设计类型是整数
    score = Math.round(score)
    
    scores.push(score)
    distributedTotal += score
  })
  
  // 微调确保总分一致
  const diff = Math.round(total) - distributedTotal
  if (diff !== 0 && scores.length > 0) {
    // 找到最大的项进行调整
    const maxIndex = scores.indexOf(Math.max(...scores))
    const config = scoreConfigs.value[maxIndex]
    const maxAllowed = config?.maxScore || 100
    const newScore = Math.min(Math.max(0, scores[maxIndex] + diff), maxAllowed)
    scores[maxIndex] = newScore
  }
  
  // 应用到表单
  scores.forEach((score, index) => {
    const field = getScoreField(index)
    scoreForm.value[field] = score
  })
  
  ElMessage.success('已自动分配各项成绩')
}

onMounted(async () => {
  await loadCurrentYear()
  await loadGroups()
})
</script>

<template>
  <div class="group-scoring-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>小组答辩评分</span>
        </div>
      </template>

      <!-- 选择分组 -->
      <el-form inline class="filter-form">
        <el-form-item label="答辩小组">
          <el-select v-model="selectedGroupId" placeholder="选择分组" @change="loadStudents" style="min-width: 200px">
            <el-option
              v-for="group in groups"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <span v-if="currentYear">当前年份: {{ currentYear.name }}</span>
        </el-form-item>
      </el-form>

      <!-- 学生列表 -->
      <el-table :data="students" v-loading="loading" border style="width: 100%">
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="thesisTitle" label="论文题目" min-width="200" show-overflow-tooltip />
        <el-table-column prop="thesisType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.thesisType === 'PAPER' ? 'primary' : 'success'" size="small">
              {{ row.thesisType === 'PAPER' ? '论文' : '设计' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="100">
          <template #default="{ row }">
            <span v-if="row.myScore !== null" class="scored-text">{{ row.myScore }} 分</span>
            <span v-else class="pending-text">未评分</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleScore(row)">评分</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 评分弹窗 -->
    <el-dialog v-model="dialogVisible" title="答辩评分" width="600px">
      <div class="student-info" v-if="currentStudent">
        <p><strong>学号:</strong> {{ currentStudent.studentNo }}</p>
        <p><strong>姓名:</strong> {{ currentStudent.name }}</p>
        <p><strong>论文题目:</strong> {{ currentStudent.thesisTitle }}</p>
      </div>

      <el-divider />

      <el-form label-width="150px">
        <el-form-item
          v-for="(config, index) in scoreConfigs"
          :key="config.id || index"
          :label="config.itemName"
        >
          <div class="score-input-row">
            <el-input-number
              v-model="scoreForm[getScoreField(index)]"
              :min="0"
              :max="config.maxScore"
              :step="1"
            />
            <span class="max-score">/ {{ config.maxScore }}</span>
          </div>
          <div class="score-desc" v-if="config.itemDescription">
            {{ config.itemDescription }}
          </div>
        </el-form-item>

        <el-divider />

        <!-- 自动分配功能 -->
        <el-form-item label="填写总分自动分配">
          <div class="auto-distribute-row">
            <el-input-number
              v-model="inputTotalScore"
              :min="0"
              :max="100"
              :step="1"
              placeholder="输入总分"
              style="width: 120px"
            />
            <el-button type="success" @click="autoDistributeScore">
              自动分配
            </el-button>
            <span class="tip-text">输入总分后点击自动分配，系统将按权重比例生成各项成绩</span>
          </div>
        </el-form-item>

        <el-divider />

        <el-form-item label="合计">
          <span class="total-score">{{ totalScore }} 分</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">提交评分</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.group-scoring-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 16px;
}

.student-info p {
  margin: 8px 0;
}

.score-input-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.max-score {
  color: #909399;
}

.score-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.total-score {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.auto-distribute-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.tip-text {
  font-size: 12px;
  color: #909399;
}
.scored-text {
  color: var(--el-color-success);
  font-weight: bold;
}

.pending-text {
  color: var(--el-color-info);
  font-style: italic;
}
</style>
