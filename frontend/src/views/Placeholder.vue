<template>
  <div class="placeholder-page">
    <el-result icon="info" :title="title" sub-title="功能开发中，敬请期待...">
      <template #extra>
        <el-button type="primary" @click="$router.back()">返回上页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta?.title || '功能开发中')
</script>

<style scoped>
.placeholder-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
