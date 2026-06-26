<script setup lang="ts">
import { ref, watch } from 'vue'
import type { AuditLogInfo } from '@/api/admin'

const props = defineProps<{
  logs: AuditLogInfo[]
  totalElements: number
  totalPages: number
  currentPage: number
  isLoading: boolean
}>()

const emit = defineEmits<{
  filterChange: [filters: { startDate: string; endDate: string; actionTypeCode: string }]
  pageChange: [page: number]
}>()

const getTodayString = () => new Date().toISOString().slice(0, 10)

// 로컬 필터 상태 (기본값 오늘)
const startDate = ref(getTodayString())
const endDate = ref(getTodayString())
const actionTypeCode = ref('')

const handleSearch = () => {
  emit('filterChange', {
    startDate: startDate.value,
    endDate: endDate.value,
    actionTypeCode: actionTypeCode.value
  })
}

// 필터 변경 감지 시 즉시 검색 트리거
watch([actionTypeCode], () => {
  handleSearch()
})

const handlePrevPage = () => {
  if (props.currentPage > 1) {
    emit('pageChange', props.currentPage - 1)
  }
}

const handleNextPage = () => {
  if (props.currentPage < props.totalPages) {
    emit('pageChange', props.currentPage + 1)
  }
}
</script>

<template>
  <div class="card audit-card">
    <div class="card-header audit-header">
      <h3 class="section-title">생애주기 리포트 열람 및 중요 행위 감사 로그</h3>
      
      <!-- 상단 감사 로그 검색 필터 -->
      <div class="audit-filters">
        <div class="filter-item">
          <label class="filter-label">기간:</label>
          <div class="date-range-inputs">
            <input v-model="startDate" type="date" class="input filter-input-date" :disabled="isLoading" />
            <span class="range-tilde">~</span>
            <input v-model="endDate" type="date" class="input filter-input-date" :disabled="isLoading" />
          </div>
        </div>
        
        <div class="filter-item">
          <label class="filter-label">액션 분류:</label>
          <select v-model="actionTypeCode" class="input filter-select" :disabled="isLoading">
            <option value="">전체 액션</option>
            <option value="01">로그인 (LOGIN)</option>
            <option value="02">민감정보조회 (PII_VIEW)</option>
            <option value="03">다운로드 (DOWNLOAD)</option>
          </select>
        </div>

        <button class="button button-primary search-btn" type="button" @click="handleSearch" :disabled="isLoading">
          조회
        </button>
      </div>
    </div>

    <div class="card-body audit-body">
      <div v-if="isLoading" class="table-loading-state">
        <div class="spinner"></div>
        <p>감사 로그 데이터를 불러오는 중...</p>
      </div>

      <div v-else class="table-container">
        <table class="data-table audit-table">
          <thead>
            <tr>
              <th>발생 일시</th>
              <th>수행 사원</th>
              <th>보안 액션</th>
              <th>대상 고객</th>
              <th>접속 IP</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="logs.length === 0">
              <td colspan="5" class="text-center empty-text">조회 기간 내 발생한 감사 로그가 없습니다.</td>
            </tr>
            <tr v-for="log in logs" :key="log.auditId">
              <td class="text-slate-500 font-bold font-mono">{{ log.createdAt }}</td>
              <td>
                <div class="employee-cell">
                  <span class="employee-name font-bold">{{ log.employeeName || '시스템/알수없음' }}</span>
                  <span class="employee-id">사원 ID: {{ log.salesUserId }}</span>
                </div>
              </td>
              <td>
                <span class="action-badge" :class="`action-${log.actionName.includes('LOGIN') ? 'login' : log.actionName.includes('PII') ? 'view' : 'download'}`">
                  {{ log.actionName }}
                </span>
              </td>
              <td>
                <div v-if="log.customerName || log.customerFormattedId" class="customer-cell font-bold">
                  <span class="customer-name">{{ log.customerName || '-' }}</span>
                  <span class="customer-id font-mono">({{ log.customerFormattedId }})</span>
                </div>
                <span v-else class="text-slate-400 font-bold">-</span>
              </td>
              <td class="font-mono font-bold text-slate-600">{{ log.ipAddress }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 페이징 컨트롤 -->
      <div class="pagination">
        <button 
          class="button button-secondary pagination-btn" 
          :disabled="currentPage === 1 || isLoading"
          @click="handlePrevPage"
          type="button"
        >
          이전
        </button>
        <span class="pagination-info">
          {{ currentPage }} / {{ totalPages || 1 }} 페이지 (총 {{ totalElements }}건)
        </span>
        <button 
          class="button button-secondary pagination-btn" 
          :disabled="currentPage >= totalPages || isLoading"
          @click="handleNextPage"
          type="button"
        >
          다음
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.audit-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.audit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  padding: 18px 20px;
}

.section-title {
  margin: 0;
  font-size: 15px;
  font-weight: 900;
  color: var(--color-text);
  flex: 1;
}

.audit-filters {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 12px;
  font-weight: 800;
  color: var(--color-text-muted);
  white-space: nowrap;
}

.date-range-inputs {
  display: flex;
  align-items: center;
  gap: 4px;
}

.filter-input-date {
  width: 125px;
  height: 32px;
  font-size: 11.5px;
  padding: 0 8px;
}

.range-tilde {
  font-size: 12px;
  color: var(--color-text-subtle);
  font-weight: 800;
}

.filter-select {
  width: 150px;
  height: 32px;
  font-size: 11.5px;
  padding: 0 8px;
}

.search-btn {
  height: 32px;
  font-size: 12px;
  padding: 0 14px;
}

.audit-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0 20px 20px;
}

.table-container {
  flex: 1;
  overflow-y: auto;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
}

.audit-table {
  width: 100%;
  border-collapse: collapse;
  border: none;
}

.audit-table th {
  position: sticky;
  top: 0;
  z-index: 10;
}

.table-loading-state {
  display: grid;
  place-items: center;
  padding: 60px 0;
  color: var(--color-text-muted);
  font-weight: 700;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-text {
  padding: 40px 0;
  color: var(--color-text-subtle);
  font-weight: 700;
}

.employee-cell, .customer-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.employee-name {
  color: var(--color-text);
  font-size: 13px;
}

.employee-id {
  font-size: 11px;
  color: var(--color-text-muted);
}

.customer-name {
  color: var(--color-text);
  font-size: 13px;
}

.customer-id {
  font-size: 11px;
  color: var(--color-text-subtle);
}

.action-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 800;
}

.action-login {
  background-color: #eff6ff;
  color: #2563eb;
}

.action-view {
  background-color: #fff7ed;
  color: #ea580c;
}

.action-download {
  background-color: #f0fdf4;
  color: #16a34a;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}

.pagination-btn {
  height: 30px;
  padding: 0 12px;
  font-size: 11px;
}

.pagination-info {
  font-size: 11px;
  color: var(--color-text-muted);
  font-weight: 700;
}
</style>
