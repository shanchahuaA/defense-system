<template>
  <div class="templates-page">
    <div class="page-header">
      <h1>模板管理</h1>
      <el-text type="info">上传 Word 文档模板，用于生成各类答辩文档</el-text>
    </div>

    <el-card v-loading="loading">
      <el-table :data="templateList" style="width: 100%">
        <el-table-column prop="typeName" label="模板类型" width="200" />
        <el-table-column prop="templateName" label="模板名称" min-width="200" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.template" type="success">已上传</el-tag>
            <el-tag v-else type="info">未上传</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">
            {{ row.template?.updatedAt ? formatDate(row.template.updatedAt) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-upload
              :show-file-list="false"
              :before-upload="(file) => handleUpload(row, file)"
              accept=".docx"
              class="inline-upload"
            >
              <el-button type="primary" link>
                {{ row.template ? '重新上传' : '上传' }}
              </el-button>
            </el-upload>
            <el-button
              v-if="row.template"
              type="danger"
              link
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 说明 -->
    <el-card class="tips-card">
      <template #header>
        <span>模板说明</span>
      </template>
      <div class="tips-content">
        <p><strong>模板格式：</strong>请使用 .docx 格式的 Word 文档</p>
        <p><strong>占位符：</strong>在模板中使用大括号标记需要替换的内容，如：</p>
        <ul>
          <li><code v-pre>{{studentName}}</code> - 学生姓名</li>
          <li><code v-pre>{{studentNo}}</code> - 学号</li>
          <li><code v-pre>{{thesisTitle}}</code> - 论文/设计题目</li>
          <li><code v-pre>{{year}}</code>、<code v-pre>{{month}}</code>、<code v-pre>{{day}}</code> - 日期</li>
          <li><code v-pre>{{score1}}</code> ~ <code v-pre>{{score6}}</code> - 各项评分</li>
          <li><code v-pre>{{totalScore}}</code> - 总分</li>
          <li><code v-pre>{{comment}}</code> - 评语</li>
          <li><code v-pre>{{@signature}}</code> - 签名图片（图片占位符）</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllTemplates, uploadTemplate, deleteTemplate } from '@/api/template'

const loading = ref(false)
const templates = ref([])

// 模板类型定义
const templateTypes = [
  { type: 'PAPER_DEFENSE_SCORE', name: '论文答辩成绩表' },
  { type: 'DESIGN_DEFENSE_SCORE', name: '设计答辩成绩表' },
  { type: 'PAPER_EVALUATION', name: '论文成绩评定表' },
  { type: 'DESIGN_EVALUATION', name: '设计成绩评定表' },
  { type: 'GROUP_STATISTICS', name: '答辩小组统分表' },
  { type: 'PAPER_SCORE_PROCESS', name: '论文答辩成绩无评语过程表' },
  { type: 'DESIGN_SCORE_PROCESS', name: '设计答辩成绩无评语过程表' }
]

// 合并模板类型和已上传数据
const templateList = ref([])

// 加载模板
async function loadTemplates() {
  loading.value = true
  try {
    const res = await getAllTemplates()
    templates.value = res.data || []
    
    // 构建列表
    templateList.value = templateTypes.map(t => {
      const uploaded = templates.value.find(u => u.templateType === t.type)
      return {
        type: t.type,
        typeName: t.name,
        templateName: uploaded?.templateName || t.name,
        template: uploaded
      }
    })
  } catch (error) {
    ElMessage.error('加载模板列表失败')
  } finally {
    loading.value = false
  }
}

// 上传模板
async function handleUpload(row, file) {
  try {
    await uploadTemplate(row.type, row.typeName, file)
    ElMessage.success('上传成功')
    loadTemplates()
  } catch (error) {
    ElMessage.error('上传失败：' + (error.message || '未知错误'))
  }
  return false // 阻止默认上传
}

// 删除模板
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除"${row.typeName}"模板吗？`, '删除确认')
    await deleteTemplate(row.template.id)
    ElMessage.success('删除成功')
    loadTemplates()
  } catch (error) {
    if (error === 'cancel') return
    ElMessage.error('删除失败')
  }
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadTemplates()
})
</script>

<style lang="scss" scoped>
.templates-page {
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

.inline-upload {
  display: inline-block;
}

.tips-card {
  margin-top: 20px;
  
  .tips-content {
    p {
      margin: 8px 0;
    }
    
    ul {
      margin: 8px 0 0 0;
      padding-left: 24px;
      
      li {
        line-height: 1.8;
        
        code {
          background: #f5f7fa;
          padding: 2px 6px;
          border-radius: 3px;
          color: #409eff;
        }
      }
    }
  }
}
</style>
