<template>
  <div class="documents-page">
    <div class="page-header">
      <h1>文档导出</h1>
      <el-text type="info">导出答辩成绩表、评分过程表等文档</el-text>
    </div>

    <!-- 选择分组 -->
    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="答辩小组">
          <el-select v-model="selectedGroupId" placeholder="选择分组" @change="loadStudents" style="width: 220px">
            <el-option
              v-for="group in groups"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleBatchExport" :loading="exporting" :disabled="selectedStudents.length === 0" v-if="false">
            <el-icon><Download /></el-icon>
            批量导出 ({{ selectedStudents.length }})
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 学生列表 -->
    <el-card>
      <el-table
        :data="students"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" />
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
        <el-table-column label="小组成绩" width="100" align="center">
          <template #default="{ row }">
            {{ row.groupAvgScore ? row.groupAvgScore.toFixed(1) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="(cmd) => handleExport(row, cmd)">
              <el-button type="primary" link>
                导出文档
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="defense_score">答辩成绩表</el-dropdown-item>
                  <el-dropdown-item command="score_process">答辩成绩无评语过程表</el-dropdown-item>
                  <el-dropdown-item command="evaluation">成绩评定表</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && students.length === 0" description="请选择答辩小组" />
    </el-card>

    <!-- 导出说明 -->
    <el-card class="tips-card">
      <template #header>
        <span>文档说明</span>
      </template>
      <ul class="tips-list">
        <li><strong>答辩成绩表：</strong>包含学生信息、各项评分、评语和评委签名</li>
        <li><strong>答辩成绩无评语过程表：</strong>仅包含评分信息，不含评语</li>
        <li><strong>成绩评定表：</strong>包含指导成绩、评阅成绩、答辩成绩和总成绩</li>
        <li><strong>批量导出：</strong>选中多个学生后可打包下载为 ZIP 文件</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, ArrowDown } from '@element-plus/icons-vue'
import { getMyGroups } from '@/api/group'
import { getGroupStudents } from '@/api/student'
import { useUserStore } from '@/stores/user'
import { getCurrentYear } from '@/api/year'

const userStore = useUserStore()
const loading = ref(false)
const exporting = ref(false)
const groups = ref([])
const students = ref([])
const selectedGroupId = ref(null)
const selectedStudents = ref([])
const currentYear = ref(null)

// 加载当前年份
async function loadCurrentYear() {
  try {
    const res = await getCurrentYear()
    currentYear.value = res.data
  } catch (error) {
    console.error('获取当前年份失败:', error)
  }
}

// 加载分组
async function loadGroups() {
  try {
    const res = await getMyGroups(currentYear.value?.id)
    groups.value = res.data?.list || res.data || []
    if (groups.value.length > 0) {
      selectedGroupId.value = groups.value[0].id
      loadStudents()
    }
  } catch (error) {
    ElMessage.error('加载分组失败')
  }
}

// 加载学生
async function loadStudents() {
  if (!selectedGroupId.value) return
  
  loading.value = true
  try {
    const res = await getGroupStudents(selectedGroupId.value)
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

// 导出单个文档
async function handleExport(student, docType) {
  ElMessage.info(`正在导出 ${student.name} 的${getDocTypeName(docType)}...`)
  
  // TODO: 调用后端导出接口下载文件
  // 这里需要后端实现文档生成API
  setTimeout(() => {
    ElMessage.warning('文档生成功能需要配置模板后使用')
  }, 1000)
}

// 批量导出
async function handleBatchExport() {
  if (selectedStudents.value.length === 0) return
  
  exporting.value = true
  try {
    ElMessage.info('正在打包文档...')
    
    // TODO: 调用后端批量导出接口
    setTimeout(() => {
      ElMessage.warning('批量导出功能需要配置模板后使用')
      exporting.value = false
    }, 1000)
  } catch (error) {
    ElMessage.error('导出失败')
    exporting.value = false
  }
}

// 获取文档类型名称
function getDocTypeName(docType) {
  const names = {
    defense_score: '答辩成绩表',
    score_process: '答辩成绩无评语过程表',
    evaluation: '成绩评定表'
  }
  return names[docType] || docType
}

onMounted(async () => {
  await loadCurrentYear()
  await loadGroups()
})
</script>

<style lang="scss" scoped>
.documents-page {
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

.filter-card {
  margin-bottom: 16px;
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
</style>
