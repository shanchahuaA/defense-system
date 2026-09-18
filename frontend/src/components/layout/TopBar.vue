<template>
  <div class="topbar-container">
    <!-- 左侧：折叠按钮 -->
    <div class="topbar-left">
      <el-button 
        text 
        @click="$emit('toggle-sidebar')"
        class="collapse-btn"
      >
        <el-icon :size="20">
          <Expand v-if="collapsed" />
          <Fold v-else />
        </el-icon>
      </el-button>
      
      <!-- 面包屑 -->
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 右侧：年份选择器、主题切换、用户信息 -->
    <div class="topbar-right">
      <!-- 年份选择器 -->
      <el-select
        v-model="selectedYear"
        placeholder="选择答辩年份"
        size="default"
        class="year-select"
        @change="handleYearChange"
      >
        <el-option
          v-for="year in years"
          :key="year.id"
          :label="year.name"
          :value="year.id"
        />
      </el-select>
      
      <!-- 主题切换 -->
      <el-button text class="theme-btn" @click="themeStore.toggleTheme">
        <el-icon :size="18">
          <Sunny v-if="themeStore.isDark" />
          <Moon v-else />
        </el-icon>
      </el-button>
      
      <!-- 用户下拉菜单 -->
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" class="user-avatar">
            {{ userStore.userInfo?.name?.charAt(0) || 'U' }}
          </el-avatar>
          <span class="user-name">{{ userStore.userInfo?.name }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item disabled>
              <el-tag size="small" type="primary">{{ roleLabel }}</el-tag>
            </el-dropdown-item>
            <el-dropdown-item divided command="password">
              <el-icon><Lock /></el-icon>
              修改密码
            </el-dropdown-item>
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="6-20位新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Expand, Fold, Sunny, Moon, ArrowDown, Lock, SwitchButton
} from '@element-plus/icons-vue'
import { changePassword } from '@/api/auth'

defineProps({
  collapsed: Boolean
})

defineEmits(['toggle-sidebar'])

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const selectedYear = ref(userStore.currentYearId)

// 模拟年份数据（后续从API获取）
const years = ref([
  { id: 1, name: '2025年夏答辩季' }
])

const currentTitle = computed(() => route.meta?.title)

const roleLabel = computed(() => {
  const roleMap = {
    'SUPER_ADMIN': '超级管理员',
    'DEPT_ADMIN': '院系管理员',
    'TEACHER': userStore.isGroupLeader ? '答辩组长' : '教师'
  }
  return roleMap[userStore.role] || '未知'
})

function handleYearChange(yearId) {
  const year = years.value.find(y => y.id === yearId)
  if (year) {
    userStore.setCurrentYear(yearId, year.name)
    ElMessage.success(`已切换到：${year.name}`)
    // 刷新当前页面数据
    router.go(0)
  }
}

function handleCommand(command) {
  switch (command) {
    case 'password':
      openPasswordDialog()
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout()
        router.push('/login')
        ElMessage.success('已退出登录')
      }).catch(() => {})
      break
  }
}

// 修改密码相关
const passwordDialogVisible = ref(false)
const changingPassword = ref(false)
const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

function openPasswordDialog() {
  passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  passwordDialogVisible.value = true
}

async function handleChangePassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  changingPassword.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordDialogVisible.value = false
    userStore.logout()
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    changingPassword.value = false
  }
}
</script>

<style lang="scss" scoped>
.topbar-container {
  width: 100%;
  height: 100%;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .collapse-btn {
    padding: 8px;
  }
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .year-select {
    width: 160px;
  }
  
  .theme-btn {
    padding: 8px;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: var(--radius-md);
    transition: background-color var(--transition-fast);
    
    &:hover {
      background-color: var(--color-bg-hover);
    }
    
    .user-avatar {
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
      color: white;
      font-weight: 600;
    }
    
    .user-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--color-text-primary);
    }
  }
}
</style>
