<script setup lang="ts">
withDefaults(defineProps<{
  icon?: string
  text?: string
}>(), {
  icon: 'ShoppingBag',
  text: '暂无数据',
})
</script>

<template>
  <div class="empty-state">
    <el-icon :size="64" class="empty-icon"><component :is="icon" /></el-icon>
    <p class="empty-text">{{ text }}</p>
    <slot />
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-icon {
  color: #c7d2fe;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 15px;
  color: var(--shop-text-secondary);
  margin-bottom: 16px;
}
</style>
