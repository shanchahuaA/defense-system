<template>
  <div class="sidebar-container">
    <!-- Logo区域 -->
    <div class="logo-area" :class="{ collapsed }">
      <el-icon :size="28" color="var(--color-primary)">
        <School />
      </el-icon>
      <span v-if="!collapsed" class="logo-text">答辩管理系统</span>
    </div>
    
    <!-- 菜单 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      :collapse-transition="false"
      class="sidebar-menu"
      router
    >
      <!-- 仪表盘 - 所有角色 -->
      <el-menu-item index="/dashboard">
        <el-icon><Odometer /></el-icon>
        <template #title>仪表盘</template>
      </el-menu-item>
      
      <!-- 超级管理员菜单 -->
      <template v-if="userStore.isSuperAdmin">
        <el-sub-menu index="dept-manage">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>院系管理</span>
          </template>
          <el-menu-item index="/departments">院系列表</el-menu-item>
          <el-menu-item index="/admins">管理员列表</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="data-manage">
          <template #title>
            <el-icon><User /></el-icon>
            <span>数据管理</span>
          </template>
          <el-menu-item index="/teachers">教师管理</el-menu-item>
          <el-menu-item index="/students">学生管理</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="config">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </template>
          <el-menu-item index="/config/scoring">评分指标</el-menu-item>
          <el-menu-item index="/config/templates">模板管理</el-menu-item>
          <el-menu-item index="/config/ai">AI配置</el-menu-item>
          <el-menu-item index="/config/years">年份管理</el-menu-item>
        </el-sub-menu>
      </template>
      
      <!-- 院系管理员菜单 -->
      <template v-if="userStore.isDeptAdmin">
        <el-menu-item index="/students">
          <el-icon><User /></el-icon>
          <template #title>学生管理</template>
        </el-menu-item>
        
        <el-menu-item index="/teachers">
          <el-icon><Avatar /></el-icon>
          <template #title>教师管理</template>
        </el-menu-item>
        
        <el-menu-item index="/groups">
          <el-icon><Grid /></el-icon>
          <template #title>答辩分组</template>
        </el-menu-item>
      </template>
      
      <!-- 教师菜单 -->
      <template v-if="userStore.isTeacher">
        <el-sub-menu index="scoring">
          <template #title>
            <el-icon><Edit /></el-icon>
            <span>我的评分</span>
          </template>
          <el-menu-item index="/scoring/group">小组答辩评分</el-menu-item>
          <el-menu-item index="/scoring/final">大组答辩评分</el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/my-students">
          <el-icon><Reading /></el-icon>
          <template #title>我指导的学生</template>
        </el-menu-item>
        
        <!-- 组长特有菜单 -->
        <template v-if="userStore.isGroupLeader">
          <el-sub-menu index="leader">
            <template #title>
              <el-icon><Star /></el-icon>
              <span>组长功能</span>
            </template>
            <el-menu-item index="/ai-comments">AI评语生成</el-menu-item>
            <el-menu-item index="/statistics">统分表</el-menu-item>
          </el-sub-menu>
        </template>
        
        <el-menu-item index="/defense-record">
          <el-icon><Document /></el-icon>
          <template #title>答辩记录</template>
        </el-menu-item>
        
        <el-menu-item index="/documents">
          <el-icon><Download /></el-icon>
          <template #title>文档导出</template>
        </el-menu-item>
        
        <el-menu-item index="/profile/signature">
          <el-icon><EditPen /></el-icon>
          <template #title>个人签名</template>
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  School, Odometer, OfficeBuilding, User, Avatar, Grid,
  Edit, Reading, Star, Document, Download, EditPen, Setting
} from '@element-plus/icons-vue'

defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.logo-area {
  height: 56px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid var(--color-border-light);
  
  &.collapsed {
    justify-content: center;
    padding: 0;
  }
  
  .logo-text {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text-primary);
    white-space: nowrap;
  }
}

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  
  &:not(.el-menu--collapse) {
    width: 100%;
  }
}

// 菜单项激活样式优化
:deep(.el-menu-item.is-active) {
  background-color: var(--color-primary) !important;
  color: white !important;
}

:deep(.el-sub-menu__title:hover),
:deep(.el-menu-item:hover) {
  background-color: var(--color-bg-hover) !important;
}
</style>
