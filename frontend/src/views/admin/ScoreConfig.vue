<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getScoreConfigsByType, saveScoreConfigsByType } from '@/api/scoreConfig'

const loading = ref(false)
const saving = ref(false)
const activeTab = ref('PAPER')
const configs = ref([])

// 加载配置
async function loadConfigs() {
  loading.value = true
  try {
    const res = await getScoreConfigsByType(activeTab.value)
    configs.value = res.data || []
  } catch (error) {
    console.error('加载配置失败:', error)
  } finally {
    loading.value = false
  }
}

// 切换Tab
function handleTabChange() {
  loadConfigs()
}

// 权重合计
const totalWeight = computed(() => {
  return configs.value.reduce((sum, item) => {
    return sum + (parseFloat(item.weight) || 0)
  }, 0)
})

// 满分合计
const totalMaxScore = computed(() => {
  return configs.value.reduce((sum, item) => {
    return sum + (parseInt(item.maxScore) || 0)
  }, 0)
})

// 新增评分项
function handleAdd() {
  configs.value.push({
    itemIndex: configs.value.length + 1,
    itemName: '',
    itemDescription: '',
    weight: 0,
    maxScore: 0
  })
}

// 删除评分项
function handleDelete(index) {
  configs.value.splice(index, 1)
  // 重新排序
  configs.value.forEach((item, i) => {
    item.itemIndex = i + 1
  })
}

// 保存配置
async function handleSave() {
  // 校验权重
  if (Math.abs(totalWeight.value - 1) > 0.001) {
    ElMessage.warning(`权重合计必须为1.00，当前为${totalWeight.value.toFixed(2)}`)
    return
  }
  
  // 校验满分
  if (totalMaxScore.value !== 100) {
    ElMessage.warning(`满分合计必须为100，当前为${totalMaxScore.value}`)
    return
  }
  
  // 校验名称
  for (const config of configs.value) {
    if (!config.itemName) {
      ElMessage.warning('评分项名称不能为空')
      return
    }
  }
  
  saving.value = true
  try {
    await saveScoreConfigsByType(activeTab.value, configs.value)
    ElMessage.success('保存成功')
    loadConfigs()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 重置配置
async function handleReset() {
  await ElMessageBox.confirm('确定要重置为默认配置吗？', '提示', {
    type: 'warning'
  })
  
  if (activeTab.value === 'PAPER') {
    configs.value = [
      { itemIndex: 1, itemName: '论文质量', itemDescription: '', weight: 0.40, maxScore: 40 },
      { itemIndex: 2, itemName: '答辩的自述报告', itemDescription: '', weight: 0.30, maxScore: 30 },
      { itemIndex: 3, itemName: '回答问题的情况', itemDescription: '', weight: 0.30, maxScore: 30 }
    ]
  } else {
    configs.value = [
      { itemIndex: 1, itemName: '设计质量1', itemDescription: '', weight: 0.15, maxScore: 15 },
      { itemIndex: 2, itemName: '设计质量2', itemDescription: '', weight: 0.15, maxScore: 15 },
      { itemIndex: 3, itemName: '设计质量3', itemDescription: '', weight: 0.15, maxScore: 15 },
      { itemIndex: 4, itemName: '答辩的自述报告', itemDescription: '', weight: 0.25, maxScore: 25 },
      { itemIndex: 5, itemName: '回答问题情况1', itemDescription: '', weight: 0.15, maxScore: 15 },
      { itemIndex: 6, itemName: '回答问题情况2', itemDescription: '', weight: 0.15, maxScore: 15 }
    ]
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<template>
  <div class="score-config-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评分配置</span>
          <div class="actions">
            <el-button @click="handleReset">重置默认</el-button>
            <el-button type="primary" @click="handleSave" :loading="saving">
              保存配置
            </el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="论文类型" name="PAPER" />
        <el-tab-pane label="设计类型" name="DESIGN" />
      </el-tabs>

      <el-table :data="configs" v-loading="loading" border style="width: 100%">
        <el-table-column prop="itemIndex" label="序号" width="70" align="center" />
        <el-table-column label="评分项名称" width="180">
          <template #default="{ row }">
            <el-input v-model="row.itemName" placeholder="请输入名称" />
          </template>
        </el-table-column>
        <el-table-column label="描述" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.itemDescription" placeholder="请输入描述" />
          </template>
        </el-table-column>
        <el-table-column label="权重" width="180">
          <template #default="{ row }">
            <el-input-number 
              v-model="row.weight" 
              :min="0" 
              :max="1" 
              :step="0.05" 
              :precision="2"
              :controls="true"
              size="default"
            />
          </template>
        </el-table-column>
        <el-table-column label="满分" width="180">
          <template #default="{ row }">
            <el-input-number 
              v-model="row.maxScore" 
              :min="0" 
              :max="100"
              :controls="true"
              size="default"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="70" align="center">
          <template #default="{ $index }">
            <el-button type="danger" link @click="handleDelete($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <div class="summary">
          <span>权重合计: 
            <span :class="{ 'text-danger': Math.abs(totalWeight - 1) > 0.001 }">
              {{ totalWeight.toFixed(2) }}
            </span>
          </span>
          <span style="margin-left: 20px">满分合计: 
            <span :class="{ 'text-danger': totalMaxScore !== 100 }">
              {{ totalMaxScore }}
            </span>
          </span>
        </div>
        <el-button type="primary" plain @click="handleAdd">+ 添加评分项</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.score-config-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-footer {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary {
  color: #666;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
}
</style>
