<template>
  <div class="statistics-page">
    <div class="page-header">
      <h1>统分表</h1>
      <el-button type="primary" @click="exportToExcel" :disabled="students.length === 0">
        <el-icon><Download /></el-icon>
        导出Excel
      </el-button>
    </div>

    <!-- 统分表 -->
    <el-card>
      <el-table :data="students" v-loading="loading" border style="width: 100%">
        <el-table-column prop="studentNo" label="学号" width="120" fixed />
        <el-table-column prop="name" label="姓名" width="100" fixed />
        <el-table-column prop="thesisTitle" label="论文题目" min-width="200" show-overflow-tooltip />
        <el-table-column label="论文类型" width="80">
          <template #default="{ row }">
            {{ row.thesisType === 'PAPER' ? '论文' : '设计' }}
          </template>
        </el-table-column>
        <el-table-column prop="advisorScore" label="指导成绩" width="90" align="center" />
        <el-table-column prop="reviewerScore" label="评阅成绩" width="90" align="center" />
        <el-table-column prop="groupAvgScore" label="答辩得分" width="90" align="center" />
        <el-table-column label="调节系数" width="90" align="center">
          <template #default="{ row }">
            <span class="adjustment-factor">{{ row.adjustmentFactor ? row.adjustmentFactor.toFixed(3) : '1.000' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最终答辩成绩" width="110" align="center">
          <template #default="{ row }">
            <span class="final-defense-score">{{ calculateDefenseScore(row) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="综合成绩" width="100" align="center">
          <template #default="{ row }">
            <span class="final-score">{{ calculateFinalScore(row) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getGradeType(calculateFinalScore(row))">
              {{ getGrade(calculateFinalScore(row)) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="AI评语" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.aiComment" type="success" size="small">已生成</el-tag>
            <el-tag v-else type="info" size="small">待生成</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 统计摘要 -->
      <div class="statistics-summary" v-if="students.length > 0">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="学生总数">{{ students.length }}</el-descriptions-item>
          <el-descriptions-item label="优秀">{{ gradeCount.excellent }}</el-descriptions-item>
          <el-descriptions-item label="良好">{{ gradeCount.good }}</el-descriptions-item>
          <el-descriptions-item label="中等">{{ gradeCount.medium }}</el-descriptions-item>
          <el-descriptions-item label="及格">{{ gradeCount.pass }}</el-descriptions-item>
          <el-descriptions-item label="不及格">{{ gradeCount.fail }}</el-descriptions-item>
          <el-descriptions-item label="平均分">{{ avgScore }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { getMyGroupStudents } from '@/api/group'

const loading = ref(false)
const students = ref([])

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

// 计算最终答辩成绩（答辩得分 × 调节系数）
function calculateDefenseScore(row) {
  const group = row.groupAvgScore || 0
  const factor = row.adjustmentFactor || 1
  
  if (!group) return null
  
  const score = group * factor
  return Math.round(score * 10) / 10
}

// 计算综合成绩（指导30% + 评阅30% + 最终答辩40%）
function calculateFinalScore(row) {
  const advisor = row.advisorScore || 0
  const reviewer = row.reviewerScore || 0
  const defenseScore = calculateDefenseScore(row) || 0
  
  if (!advisor && !reviewer && !defenseScore) return null
  
  const score = advisor * 0.3 + reviewer * 0.3 + defenseScore * 0.4
  return Math.round(score * 10) / 10
}

// 获取等级
function getGrade(score) {
  if (!score) return '-'
  if (score >= 90) return '优秀'
  if (score >= 80) return '良好'
  if (score >= 70) return '中等'
  if (score >= 60) return '及格'
  return '不及格'
}

// 获取等级对应的标签类型
function getGradeType(score) {
  if (!score) return 'info'
  if (score >= 90) return 'success'
  if (score >= 80) return 'primary'
  if (score >= 70) return 'warning'
  if (score >= 60) return ''
  return 'danger'
}

// 统计各等级人数
const gradeCount = computed(() => {
  const count = { excellent: 0, good: 0, medium: 0, pass: 0, fail: 0 }
  students.value.forEach(s => {
    const score = calculateFinalScore(s)
    if (!score) return
    if (score >= 90) count.excellent++
    else if (score >= 80) count.good++
    else if (score >= 70) count.medium++
    else if (score >= 60) count.pass++
    else count.fail++
  })
  return count
})

// 平均分
const avgScore = computed(() => {
  const validStudents = students.value.filter(s => calculateFinalScore(s) !== null)
  if (validStudents.length === 0) return '-'
  const sum = validStudents.reduce((acc, s) => acc + calculateFinalScore(s), 0)
  return (sum / validStudents.length).toFixed(1)
})

// 导出Excel
function exportToExcel() {
  const headers = ['学号', '姓名', '论文题目', '论文类型', '指导成绩', '评阅成绩', '答辩得分', '调节系数', '最终答辩成绩', '综合成绩', '等级']
  const rows = students.value.map(s => [
    s.studentNo,
    s.name,
    s.thesisTitle,
    s.thesisType === 'PAPER' ? '论文' : '设计',
    s.advisorScore || '',
    s.reviewerScore || '',
    s.groupAvgScore || '',
    s.adjustmentFactor ? s.adjustmentFactor.toFixed(3) : '1.000',
    calculateDefenseScore(s) || '',
    calculateFinalScore(s) || '',
    getGrade(calculateFinalScore(s))
  ])

  // 创建CSV内容
  const csvContent = [headers.join(','), ...rows.map(r => r.map(cell => `"${cell}"`).join(','))].join('\n')
  
  // 创建并下载文件
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `统分表_${new Date().toLocaleDateString()}.csv`
  link.click()
  
  ElMessage.success('导出成功')
}

onMounted(() => {
  loadStudents()
})
</script>

<style scoped>
.statistics-page {
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

.final-score {
  font-weight: 600;
  color: var(--el-color-primary);
}

.statistics-summary {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color);
}
</style>
