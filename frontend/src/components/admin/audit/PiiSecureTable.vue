<script setup lang="ts">
import type { PiiSecureSummary } from '@/api/members'

const props = defineProps<{
  piiList: PiiSecureSummary[]
  totalElements: number
  totalPages: number
  currentPage: number
  isLoading: boolean
}>()

const emit = defineEmits<{
  (e: 'pageChange', page: number): void
}>()

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
  <div class="card pii-card">
    <div class="card-header pii-header">
      <h3 class="section-title">퇴사자 PII(개인식별정보) 분리 보관 현황</h3>
      <p class="section-desc">근로기준법 및 개인정보보호법에 의거한 퇴사 후 3년 보존 기간 경과 확인용 목록</p>
    </div>

    <div class="card-body pii-body">
      <div v-if="isLoading" class="table-loading-state">
        <div class="spinner"></div>
        <p>PII 격리 데이터를 불러오는 중...</p>
      </div>

      <div v-else class="table-container">
        <table class="data-table pii-table">
          <thead>
            <tr>
              <th>사번</th>
              <th>퇴사 처리일</th>
              <th>파기 예정일</th>
              <th class="text-right">남은 파기 일수</th>
              <th>보관 상태</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="piiList.length === 0">
              <td colspan="5" class="text-center empty-text">보관 중인 PII 데이터가 존재하지 않습니다.</td>
            </tr>
            <tr v-for="item in piiList" :key="item.employeeId">
              <td class="font-bold text-slate-800 font-mono">{{ item.employeeId }}</td>
              <td class="text-slate-600 font-mono">{{ item.retiredAt.split(' ')[0] }}</td>
              <td class="text-slate-600 font-mono">{{ item.purgedAt.split(' ')[0] }}</td>
              <td class="text-right font-bold font-mono" :class="item.remainingDays <= 100 ? 'text-rose-500' : 'text-slate-600'">
                {{ item.remainingDays }}일 남음
              </td>
              <td>
                <span class="badge badge-active pii-status-badge">
                  {{ item.statusName }}
                </span>
              </td>
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
.pii-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.pii-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 18px 20px;
}

.section-title {
  margin: 0;
  font-size: 15px;
  font-weight: 900;
  color: var(--color-text);
}

.section-desc {
  margin: 0;
  font-size: 11.5px;
  color: var(--color-text-muted);
}

.pii-body {
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

.pii-table {
  width: 100%;
  border-collapse: collapse;
  border: none;
}

.pii-table th {
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
  text-align: center;
}

.pii-status-badge {
  background-color: #f1f5f9;
  color: #334155;
  border: 1px solid #cbd5e1;
}

.text-rose-500 {
  color: #f43f5e !important;
  animation: pulse-red 2s infinite;
}

@keyframes pulse-red {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
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
