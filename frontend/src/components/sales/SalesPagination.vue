<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
const props = defineProps<{
  currentPage: number
  totalPages: number
}>()

const emit = defineEmits<{
  change: [page: number]
}>()

const isCompact = ref(false)

const updateCompactMode = () => {
  isCompact.value = window.innerWidth <= 760
}

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

onMounted(() => {
  updateCompactMode()
  window.addEventListener('resize', updateCompactMode)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateCompactMode)
})
</script>

<template>
  <nav v-if="totalPages > 0" class="sales-pagination" aria-label="페이지 이동">
    <button type="button" :disabled="currentPage === 1" @click="move(1)">&lt;&lt;</button>
    <button type="button" :disabled="currentPage === 1" @click="move(currentPage - 1)">&lt;</button>
    <template v-if="!isCompact">
      <button
        v-for="page in pages"
        :key="page"
        :class="{ 'is-active': page === currentPage }"
        type="button"
        @click="move(page)"
      >
        {{ page }}
      </button>
    </template>
    <span
      v-else
      class="sales-pagination__compact"
    >
      {{ currentPage }} / {{ totalPages }}
    </span>
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
  white-space: nowrap;
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
  flex-shrink: 0;
}

.sales-pagination button.is-active {
  border-color: color-mix(in srgb, var(--color-primary) 34%, white);
  background: color-mix(in srgb, var(--color-primary) 10%, white);
  color: var(--color-primary);
}

.sales-pagination button:disabled {
  cursor: default;
  opacity: 0.35;
}

.sales-pagination__compact {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 44px;
  height: 22px;
  color: #5f6c7d;
  font-size: 10px;
  font-weight: 700;
}
</style>
