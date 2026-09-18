<template>
  <el-container class="app-layout">
    <!-- 侧边栏 -->
    <el-aside :width="sidebarWidth" class="sidebar">
      <Sidebar :collapsed="sidebarCollapsed" />
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 顶栏 -->
      <el-header class="topbar" height="56px">
        <TopBar 
          :collapsed="sidebarCollapsed"
          @toggle-sidebar="toggleSidebar"
        />
      </el-header>
      
      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import Sidebar from '@/components/layout/Sidebar.vue'
import TopBar from '@/components/layout/TopBar.vue'

const sidebarCollapsed = ref(false)

const sidebarWidth = computed(() => {
  return sidebarCollapsed.value ? '64px' : '240px'
})

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}
</script>

<style lang="scss" scoped>
.app-layout {
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  background: var(--color-bg-sidebar);
  border-right: 1px solid var(--color-border-light);
  transition: width var(--transition-normal);
  overflow: hidden;
}

.main-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.topbar {
  background: var(--color-bg-sidebar);
  border-bottom: 1px solid var(--color-border-light);
  padding: 0;
  display: flex;
  align-items: center;
}

.main-content {
  flex: 1;
  overflow: auto;
  padding: var(--spacing-lg);
  background: var(--color-bg-page);
}

// 页面切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
