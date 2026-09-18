<template>
  <div class="dashboard">
    <h1 class="page-title">仪表盘</h1>
    
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <el-card v-for="stat in stats" :key="stat.title" class="stat-card">
        <div class="stat-icon" :style="{ background: stat.color }">
          <el-icon :size="24" color="white">
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-title">{{ stat.title }}</div>
        </div>
      </el-card>
    </div>
    
    <!-- 欢迎信息 -->
    <el-card class="welcome-card">
      <h2>欢迎，{{ userStore.userInfo?.name }}</h2>
      <p>您当前的角色是：<el-tag type="primary">{{ roleLabel }}</el-tag></p>
      <p>当前答辩年份：<strong>{{ userStore.currentYearName }}</strong></p>
    </el-card>
    
    <!-- 快捷操作 -->
    <el-card class="quick-actions">
      <template #header>
        <span>快捷操作</span>
      </template>
      <div class="action-buttons">
        <template v-if="userStore.isSuperAdmin">
          <el-button type="primary" @click="$router.push('/departments')">
            <el-icon><OfficeBuilding /></el-icon>
            管理院系
          </el-button>
          <el-button type="primary" @click="$router.push('/config/scoring')">
            <el-icon><Setting /></el-icon>
            配置评分指标
          </el-button>
        </template>
        
        <template v-if="userStore.isDeptAdmin">
          <el-button type="primary" @click="$router.push('/students')">
            <el-icon><User /></el-icon>
            管理学生
          </el-button>
          <el-button type="primary" @click="$router.push('/groups')">
            <el-icon><Grid /></el-icon>
            答辩分组
          </el-button>
        </template>
        
        <template v-if="userStore.isTeacher">
          <el-button type="primary" @click="$router.push('/scoring/group')">
            <el-icon><Edit /></el-icon>
            开始评分
          </el-button>
          <el-button type="primary" @click="$router.push('/my-students')">
            <el-icon><Reading /></el-icon>
            我的学生
          </el-button>
        </template>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { 
  OfficeBuilding, User, Grid, Edit, Reading, Setting,
  UserFilled, Document, DataAnalysis 
} from '@element-plus/icons-vue'

const userStore = useUserStore()

const roleLabel = computed(() => {
  const roleMap = {
    'SUPER_ADMIN': '超级管理员',
    'DEPT_ADMIN': '院系管理员',
    'TEACHER': userStore.isGroupLeader ? '答辩组长' : '教师'
  }
  return roleMap[userStore.role] || '未知'
})

// 模拟统计数据（后续从API获取）
const stats = computed(() => {
  if (userStore.isSuperAdmin) {
    return [
      { title: '院系总数', value: 5, icon: OfficeBuilding, color: '#4F46E5' },
      { title: '教师总数', value: 120, icon: UserFilled, color: '#10B981' },
      { title: '学生总数', value: 800, icon: User, color: '#F59E0B' },
      { title: '答辩小组', value: 32, icon: Grid, color: '#EF4444' }
    ]
  } else if (userStore.isDeptAdmin) {
    return [
      { title: '本院教师', value: 45, icon: UserFilled, color: '#4F46E5' },
      { title: '本院学生', value: 200, icon: User, color: '#10B981' },
      { title: '答辩小组', value: 8, icon: Grid, color: '#F59E0B' },
      { title: '已分组学生', value: 180, icon: DataAnalysis, color: '#EF4444' }
    ]
  } else {
    return [
      { title: '我指导的学生', value: 5, icon: Reading, color: '#4F46E5' },
      { title: '待评分学生', value: 12, icon: Edit, color: '#10B981' },
      { title: '已提交评分', value: 8, icon: Document, color: '#F59E0B' },
      { title: '待导出文档', value: 3, icon: DataAnalysis, color: '#EF4444' }
    ]
  }
})
</script>

<style lang="scss" scoped>
.dashboard {
  max-width: 1200px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 24px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  
  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-card {
  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
  }
  
  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: var(--radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .stat-content {
    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: var(--color-text-primary);
      line-height: 1.2;
    }
    
    .stat-title {
      font-size: 14px;
      color: var(--color-text-secondary);
      margin-top: 4px;
    }
  }
}

.welcome-card {
  margin-bottom: 24px;
  
  h2 {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 12px;
    color: var(--color-text-primary);
  }
  
  p {
    color: var(--color-text-secondary);
    margin-bottom: 8px;
    
    strong {
      color: var(--color-primary);
    }
  }
}

.quick-actions {
  .action-buttons {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }
}
</style>
