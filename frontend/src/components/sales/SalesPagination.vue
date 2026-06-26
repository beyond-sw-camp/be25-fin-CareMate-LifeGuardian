<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps<{
  currentPage: number
  totalPages: number
}>()

const emit = defineEmits<{
  change: [page: number]
}>()

// 현재 페이지를 중심으로 최대 5개의 페이지 번호만 노출.
const pages = computed(() => {
  const start = Math.max(1, Math.min(props.currentPage - 2, props.totalPages - 4))
  const end = Math.min(props.totalPages, start + 4)

  return Array.from({ length: Math.max(0, end - start + 1) }, (_, index) => start + index)
})

// 범위를 벗어나거나 현재와 같은 페이지는 재조회 X.
const move = (page: number) => {
  if (page >= 1 && page <= props.totalPages && page !== props.currentPage) {
    emit('change', page)
  }
}
</script>

<template>
  <nav v-if="totalPages > 0" class="sales-pagination" aria-label="페이지 이동">
    <button type="button" :disabled="currentPage === 1" @click="move(1)">&lt;&lt;</button>
    <button type="button" :disabled="currentPage === 1" @click="move(currentPage - 1)">&lt;</button>
    <button
      v-for="page in pages"
      :key="page"
      :class="{ 'is-active': page === currentPage }"
      type="button"
      @click="move(page)"
    >
      {{ page }}
    </button>
    <button type="button" :disabled="currentPage === totalPages" @click="move(currentPage + 1)">&gt;</button>
    <button type="button" :disabled="currentPage === totalPages" @click="move(totalPages)">&gt;&gt;</button>
  </nav>
</template>

<style scoped>
.sales-pagination {
  display: flex;
  justify-content: center;
  gap: 4px;
  margin-top: 8px;
}

.sales-pagination button {
  min-width: 22px;
  height: 22px;
  border: 1px solid transparent;
  border-radius: 5px;
  background: transparent;
  color: #5f6c7d;
  padding: 0 5px;
  font-size: 10px;
  font-weight: 700;
}

.sales-pagination button.is-active {
  border-color: #c7d5f8;
  background: #eef3ff;
  color: #4055d4;
}

.sales-pagination button:disabled {
  cursor: default;
  opacity: 0.35;
}
</style>
