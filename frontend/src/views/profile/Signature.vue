<template>
  <div class="signature-page">
    <div class="page-header">
      <h1>个人签名</h1>
    </div>

    <el-card class="signature-card">
      <template #header>
        <div class="card-header">
          <span>签名图片</span>
          <el-text type="info" size="small">用于生成答辩成绩表等文档</el-text>
        </div>
      </template>

      <!-- 当前签名预览 -->
      <div class="signature-preview" v-if="currentSignature">
        <p class="label">当前签名：</p>
        <div class="preview-box">
          <img :src="signatureUrl" alt="当前签名" @error="handleImageError" />
        </div>
      </div>
      <el-empty v-else description="暂未上传签名" :image-size="80" />

      <el-divider />

      <!-- 上传区域 -->
      <div class="upload-section">
        <p class="label">上传新签名：</p>
        <el-upload
          ref="uploadRef"
          class="signature-uploader"
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleFileChange"
          accept="image/png,image/jpeg,image/jpg"
          drag
        >
          <div v-if="previewUrl" class="upload-preview">
            <img :src="previewUrl" alt="预览" />
            <div class="preview-mask">
              <el-icon :size="20"><Refresh /></el-icon>
              <span>重新选择</span>
            </div>
          </div>
          <div v-else class="upload-placeholder">
            <el-icon :size="40"><Upload /></el-icon>
            <div class="el-upload__text">
              拖拽图片到此处，或 <em>点击上传</em>
            </div>
            <div class="el-upload__tip">
              支持 PNG、JPG 格式，建议使用白底的手写签名图片
            </div>
          </div>
        </el-upload>

        <div class="upload-actions" v-if="selectedFile">
          <el-button @click="handleClear">取消</el-button>
          <el-button type="primary" @click="handleUpload" :loading="uploading">
            <el-icon><Upload /></el-icon>
            确认上传
          </el-button>
        </div>
      </div>

      <!-- 使用说明 -->
      <el-alert
        type="info"
        :closable="false"
        class="tips-alert"
      >
        <template #title>
          <strong>签名图片要求</strong>
        </template>
        <ul class="tips-list">
          <li>请使用白色背景的手写签名扫描件或照片</li>
          <li>签名应清晰可辨，建议分辨率不低于 300x100 像素</li>
          <li>文件大小不超过 2MB</li>
          <li>上传后签名将用于答辩成绩表、评分表等文档的自动填充</li>
        </ul>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Refresh } from '@element-plus/icons-vue'
import { uploadTeacherSignature } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const uploadRef = ref(null)
const uploading = ref(false)
const selectedFile = ref(null)
const previewUrl = ref('')
const currentSignature = ref('')

// 签名图片URL（需要带token访问）
const signatureUrl = computed(() => {
  if (!currentSignature.value) return ''
  // 如果是相对路径，加上baseURL
  if (currentSignature.value.startsWith('/')) {
    return import.meta.env.VITE_API_BASE_URL + currentSignature.value
  }
  return currentSignature.value
})

// 加载当前签名
function loadCurrentSignature() {
  // 从用户信息中获取签名路径（如果有）
  const signature = userStore.userInfo?.signature
  if (signature) {
    currentSignature.value = signature
  }
}

// 处理文件选择
function handleFileChange(file) {
  // 验证文件类型
  const allowedTypes = ['image/png', 'image/jpeg', 'image/jpg']
  if (!allowedTypes.includes(file.raw.type)) {
    ElMessage.error('只支持 PNG、JPG 格式的图片')
    return
  }
  
  // 验证文件大小（2MB）
  if (file.raw.size > 2 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 2MB')
    return
  }
  
  selectedFile.value = file.raw
  previewUrl.value = URL.createObjectURL(file.raw)
}

// 清除选择
function handleClear() {
  selectedFile.value = null
  previewUrl.value = ''
  uploadRef.value?.clearFiles()
}

// 上传签名
async function handleUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择签名图片')
    return
  }
  
  uploading.value = true
  try {
    const userId = userStore.userInfo?.userId
    if (!userId) {
      ElMessage.error('用户信息异常，请重新登录')
      return
    }
    
    const res = await uploadTeacherSignature(userId, selectedFile.value)
    currentSignature.value = res.data
    
    // 更新本地存储和 store 中的用户信息
    const newUserInfo = { ...userStore.userInfo, signature: res.data }
    userStore.userInfo = newUserInfo
    localStorage.setItem('userInfo', JSON.stringify(newUserInfo))
    
    ElMessage.success('签名上传成功')
    handleClear()
  } catch (error) {
    ElMessage.error(error.message || '上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

// 图片加载失败处理
function handleImageError() {
  currentSignature.value = ''
}

onMounted(() => {
  loadCurrentSignature()
})
</script>

<style lang="scss" scoped>
.signature-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
  
  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
}

.signature-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.signature-preview {
  .preview-box {
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    padding: 16px;
    background: #fafafa;
    display: inline-block;
    
    img {
      max-width: 300px;
      max-height: 100px;
      display: block;
    }
  }
}

.upload-section {
  margin-top: 20px;
}

.signature-uploader {
  :deep(.el-upload) {
    width: 100%;
  }
  
  :deep(.el-upload-dragger) {
    width: 100%;
    height: auto;
    padding: 30px 20px;
  }
}

.upload-placeholder {
  text-align: center;
  
  .el-icon {
    color: #909399;
  }
  
  .el-upload__text {
    margin-top: 12px;
    color: #606266;
    
    em {
      color: var(--el-color-primary);
      font-style: normal;
    }
  }
  
  .el-upload__tip {
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
  }
}

.upload-preview {
  position: relative;
  
  img {
    max-width: 300px;
    max-height: 100px;
    display: block;
    margin: 0 auto;
  }
  
  .preview-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: white;
    opacity: 0;
    transition: opacity 0.3s;
    border-radius: 4px;
    
    span {
      margin-top: 4px;
      font-size: 12px;
    }
  }
  
  &:hover .preview-mask {
    opacity: 1;
  }
}

.upload-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

.tips-alert {
  margin-top: 24px;
  
  .tips-list {
    margin: 8px 0 0;
    padding-left: 20px;
    
    li {
      line-height: 1.8;
      color: #606266;
    }
  }
}
</style>
