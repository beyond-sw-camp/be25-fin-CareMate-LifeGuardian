<script setup lang="ts">
import { ref, watch } from 'vue'
import type { DashboardSalesUser } from '@/api/admin'

const props = defineProps<{
  salesUsers: DashboardSalesUser[]
}>()

const emit = defineEmits<{
  (e: 'search', keyword: string): void
  (e: 'togglePin', userId: number, currentPinned: boolean): void
  (e: 'selectUser', userId: number): void
}>()

const keywordInput = ref('')
let debounceTimer: ReturnType<typeof setTimeout> | null = null

watch(keywordInput, (newVal) => {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    emit('search', newVal)
  }, 300)
})
</script>

<template>
  <div class="card perf-card">
    <div class="card-header perf-header">
      <div>
        <h3 class="section-title">영업사원 당월 성과</h3>
        <p class="section-desc">지점원 실적 현황 및 핀 고정 관리</p>
      </div>
    </div>

    <!-- 검색 바 -->
    <div class="perf-search">
      <div class="search-input-wrapper">
        <input
          v-model="keywordInput"
          class="input search-input"
          placeholder="사원명으로 검색..."
          type="text"
        />
        <span class="search-icon">🔍</span>
      </div>
    </div>

    <div class="card-body perf-body">
      <div class="table-container">
        <table class="data-table perf-table">
          <thead>
            <tr>
              <th width="48" class="text-center">고정</th>
              <th width="60">순위</th>
              <th>이름</th>
              <th class="text-right">실적</th>
              <th class="text-right" width="100">목표 대비</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="salesUsers.length === 0">
              <td colspan="5" class="text-center empty-row">
                검색 결과가 없습니다.
              </td>
            </tr>
            <tr
              v-for="user in salesUsers"
              :key="user.userId"
              class="clickable-row"
              @click="$emit('selectUser', user.userId)"
            >
              <!-- 핀 고정 버튼 -->
              <td class="text-center" @click.stop>
                <button
                  class="pin-button"
                  :class="{ 'pin-button--active': user.isPinned }"
                  type="button"
                  :aria-label="user.isPinned ? '핀 고정 해제' : '핀 고정'"
                  @click="$emit('togglePin', user.userId, user.isPinned)"
                >
                  <!-- Pin Icon -->
                  <svg
                    v-if="user.isPinned"
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                    width="16"
                    height="16"
                    class="icon-pinned"
                  >
                    <path d="M16 12V4h1V2H7v2h1v8l-2 2v2h5.2v6h1.6v-6H18v-2l-2-2z"/>
                  </svg>
                  <svg
                    v-else
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    width="16"
                    height="16"
                    class="icon-unpinned"
                  >
                    <path d="M16 12V4h1V2H7v2h1v8l-2 2v2h5.2v6h1.6v-6H18v-2l-2-2z"/>
                  </svg>
                </button>
              </td>

              <!-- 순위 -->
              <td class="font-bold text-slate-500">
                {{ user.rank }}위
              </td>

              <!-- 이름 -->
              <td class="font-bold text-slate-800">
                {{ user.employeeName }}
              </td>

              <!-- 당월 건수 -->
              <td class="text-right font-bold text-slate-800">
                {{ user.thisMonthCount }}건
              </td>

              <!-- 목표 대비 차이 -->
              <td class="text-right font-bold">
                <span
                  :class="{
                    'diff-up': user.targetDifference > 0,
                    'diff-down': user.targetDifference < 0,
                    'diff-zero': user.targetDifference === 0
                  }"
                >
                  {{ user.targetDifference > 0 ? '+' : '' }}{{ user.targetDifference }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.perf-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.perf-header {
  padding-bottom: 12px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
  color: var(--color-text);
}

.section-desc {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-muted);
}

.perf-search {
  padding: 0 20px 12px;
}

.search-input-wrapper {
  position: relative;
  width: 100%;
}

.search-input {
  padding-left: 36px;
  font-size: 13px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 14px;
  pointer-events: none;
  color: var(--color-text-subtle);
}

.perf-body {
  flex: 1;
  padding: 0 20px 20px;
  overflow: hidden;
}

.table-container {
  overflow-y: auto;
  max-height: 480px;
  border-radius: var(--radius-sm);
}

.perf-table {
  width: 100%;
  border: none;
}

.perf-table th {
  position: sticky;
  top: 0;
  z-index: 2;
  font-size: 11px;
}

.clickable-row {
  cursor: pointer;
  transition: background-color 100ms ease;
}

.clickable-row:hover {
  background-color: var(--color-surface-muted);
}

.clickable-row:hover td {
  background-color: var(--color-surface-muted) !important;
}

.pin-button {
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  border-radius: 4px;
  color: var(--color-text-subtle);
  transition: color 120ms ease, background-color 120ms ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.pin-button:hover {
  background-color: var(--color-border);
  color: var(--color-text);
}

.pin-button--active {
  color: #f59e0b; /* Gold/Amber color for pinned */
}

.pin-button--active:hover {
  color: #d97706;
}

.text-center {
  text-align: center;
}

.text-right {
  text-align: right;
}

.font-bold {
  font-weight: 700;
}

.empty-row {
  padding: 32px 0;
  color: var(--color-text-subtle);
  font-weight: 700;
}

.diff-up {
  color: #ef4444; /* red-500 */
}

.diff-down {
  color: #3b82f6; /* blue-500 */
}

.diff-zero {
  color: var(--color-text-muted);
}
</style>
