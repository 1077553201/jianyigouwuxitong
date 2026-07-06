<script setup lang="ts">
const props = defineProps<{
  currentPage: number
  pageSize: number
  total: number
}>()

const emit = defineEmits<{
  'update:currentPage': [page: number]
  change: [page: number]
}>()

function handleChange(page: number) {
  emit('update:currentPage', page)
  emit('change', page)
}
</script>

<template>
  <div class="pagination-wrap" v-if="total > pageSize">
    <el-pagination
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="handleChange"
    />
  </div>
</template>

<style scoped>
.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}
</style>
