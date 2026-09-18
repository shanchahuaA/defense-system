<template>
  <div class="ai-config-page">
    <div class="page-header">
      <h1>AI 配置</h1>
    </div>

    <el-card v-loading="loading">
      <el-form :model="form" label-width="140px" class="config-form">
        <!-- API Key 配置 -->
        <el-divider content-position="left">
          <el-icon><Key /></el-icon>
          API 配置
        </el-divider>
        
        <el-form-item label="通义千问 API Key">
          <div class="api-key-row">
            <el-input
              v-model="form.apiKey"
              :type="showApiKey ? 'text' : 'password'"
              placeholder="请输入通义千问 (DashScope) API Key"
              style="flex: 1"
            >
              <template #suffix>
                <el-icon class="cursor-pointer" @click="showApiKey = !showApiKey">
                  <View v-if="!showApiKey" />
                  <Hide v-else />
                </el-icon>
              </template>
            </el-input>
            <el-button type="primary" plain @click="handleTestConnection" :loading="testing">
              测试连接
            </el-button>
          </div>
          <div class="form-tip">
            <el-link type="primary" href="https://bailian.console.aliyun.com/?apiKey=1" target="_blank">
              前往阿里云百炼获取 API Key
            </el-link>
          </div>
        </el-form-item>

        <!-- 论文评语模板 -->
        <el-divider content-position="left">
          <el-icon><Document /></el-icon>
          论文评语提示词模板
        </el-divider>

        <el-form-item label="论文评语模板">
          <el-input
            v-model="form.paperPrompt"
            type="textarea"
            :rows="8"
            placeholder="请输入论文评语的提示词模板..."
          />
          <div class="form-tip">
            可用变量：<code>{studentName}</code> 学生姓名、<code>{thesisTitle}</code> 论文题目、<code>{scores}</code> 成绩信息
          </div>
        </el-form-item>

        <!-- 设计评语模板 -->
        <el-divider content-position="left">
          <el-icon><Cpu /></el-icon>
          设计评语提示词模板
        </el-divider>

        <el-form-item label="设计评语模板">
          <el-input
            v-model="form.designPrompt"
            type="textarea"
            :rows="8"
            placeholder="请输入设计评语的提示词模板..."
          />
          <div class="form-tip">
            可用变量：<code>{studentName}</code> 学生姓名、<code>{thesisTitle}</code> 设计题目、<code>{scores}</code> 成绩信息
          </div>
        </el-form-item>

        <!-- 保存按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving" size="large">
            <el-icon><Check /></el-icon>
            保存配置
          </el-button>
          <el-button @click="handleReset" size="large">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 使用说明 -->
    <el-card class="tips-card">
      <template #header>
        <span>使用说明</span>
      </template>
      <ul class="tips-list">
        <li><strong>API Key：</strong>本系统使用通义千问 (Qwen) 大模型API生成答辩评语，请先获取并配置 API Key</li>
        <li><strong>提示词模板：</strong>系统会将模板中的变量替换为实际数据后发送给 AI 模型</li>
        <li><strong>评语生成：</strong>答辩组长可在"AI评语生成"页面为学生批量生成评语</li>
        <li><strong>注意事项：</strong>生成的评语可手动编辑，最终评语以人工确认后的版本为准</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Key, View, Hide, Document, Cpu, Check, Refresh } from '@element-plus/icons-vue'
import { getAiConfig, saveAiConfig, testAiConnection } from '@/api/ai'

const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const showApiKey = ref(false)

const form = ref({
  apiKey: '',
  paperPrompt: '',
  designPrompt: ''
})

const originalForm = ref({})

// 加载配置
async function loadConfig() {
  loading.value = true
  try {
    const res = await getAiConfig()
    form.value = {
      apiKey: res.data?.apiKey || '',
      paperPrompt: res.data?.paperPrompt || '',
      designPrompt: res.data?.designPrompt || ''
    }
    originalForm.value = { ...form.value }
  } catch (error) {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

// 保存配置
async function handleSave() {
  if (!form.value.apiKey) {
    ElMessage.warning('请填写 API Key')
    return
  }
  
  saving.value = true
  try {
    await saveAiConfig(form.value)
    originalForm.value = { ...form.value }
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 测试连接
async function handleTestConnection() {
  if (!form.value.apiKey) {
    ElMessage.warning('请先填写 API Key')
    return
  }
  
  testing.value = true
  try {
    const res = await testAiConnection({ apiKey: form.value.apiKey })
    if (res.data && res.data.includes('成功')) {
      ElMessage.success(res.data)
    } else {
      ElMessage.warning(res.data || '连接测试失败')
    }
  } catch (error) {
    ElMessage.error('连接测试失败：' + (error.message || '未知错误'))
  } finally {
    testing.value = false
  }
}

// 重置
function handleReset() {
  form.value = { ...originalForm.value }
  ElMessage.info('已重置为上次保存的配置')
}

onMounted(() => {
  loadConfig()
})
</script>

<style lang="scss" scoped>
.ai-config-page {
  padding: 20px;
  max-width: 900px;
}

.page-header {
  margin-bottom: 20px;
  
  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
}

.config-form {
  max-width: 700px;
}

.api-key-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  
  code {
    background: #f5f7fa;
    padding: 2px 6px;
    border-radius: 3px;
    color: #409eff;
    margin: 0 2px;
  }
}

.cursor-pointer {
  cursor: pointer;
}

.tips-card {
  margin-top: 20px;
  max-width: 900px;
  
  .tips-list {
    margin: 0;
    padding-left: 20px;
    
    li {
      line-height: 2;
      color: #606266;
    }
  }
}

:deep(.el-divider__text) {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}
</style>
