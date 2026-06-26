<script setup lang="ts">
import { ref, watch } from 'vue'
import { getSalesUserList, transferCustomers, retireSalesUser } from '@/api/members'
import type { SalesUserSummary } from '@/api/members'

const props = defineProps<{
  isOpen: boolean
  fromUserId: number
  fromUserName: string
  remainingCount: number
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success', transferredCount: number): void
}>()

const activeUsers = ref<SalesUserSummary[]>([])
const selectedToUserId = ref<number | null>(null)
const isLoadingUsers = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')

const loadActiveUsers = async () => {
  isLoadingUsers.value = true
  errorMessage.value = ''
  try {
    const data = await getSalesUserList({ statusCode: '01', page: 1, size: 100 })
    // 본인을 제외한 활성 사원 목록 필터링
    activeUsers.value = (data.content || []).filter(u => u.id !== props.fromUserId)
  } catch (error) {
    console.error('Failed to load active sales users:', error)
    errorMessage.value = '이관 대상 영업사원 목록을 불러오지 못했습니다.'
  } finally {
    isLoadingUsers.value = false
  }
}

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    selectedToUserId.value = null
    errorMessage.value = ''
    loadActiveUsers()
  }
})

const handleTransfer = async () => {
  if (!selectedToUserId.value) return
  isSubmitting.value = true
  errorMessage.value = ''

  try {
    // 1. 고객 이관 처리
    const transferResult = await transferCustomers(props.fromUserId, selectedToUserId.value)
    
    // 2. 최종 퇴사 처리 및 토큰 파기
    await retireSalesUser(props.fromUserId)

    const totalTransferred = (transferResult.transferredPotentialCount || 0) + (transferResult.transferredIntegratedCount || 0)
    emit('success', totalTransferred)
  } catch (error: any) {
    console.error('Failed to transfer and retire sales user:', error)
    errorMessage.value = error.response?.data?.message ?? '고객 이관 및 퇴사 처리 중 오류가 발생했습니다.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div v-if="isOpen" class="modal-backdrop" @click="$emit('close')">
    <div class="modal-card" @click.stop>
      <div class="modal-header">
        <div class="modal-title-wrapper">
          <span class="warning-icon">⚠️</span>
          <h2 class="modal-title">퇴사 처리 검증</h2>
        </div>
        <button class="close-button" type="button" @click="$emit('close')" :disabled="isSubmitting">
          &times;
        </button>
      </div>

      <div class="modal-body">
        <!-- 잔여 고객 알림 박스 (붉은색) -->
        <div class="alert-box">
          <div class="alert-msg-title">❌ 잔여 고객이 존재하여 퇴사 처리가 불가합니다</div>
          <div class="alert-msg-desc">현재 {{ remainingCount }}명의 고객이 배정되어 있습니다</div>
        </div>

        <div class="transfer-form">
          <label class="form-label">
            <span class="badge-number">3</span>
            <span>이관 대상자 선택</span>
          </label>
          
          <div class="select-wrapper">
            <select v-model="selectedToUserId" class="input select-input" :disabled="isLoadingUsers || isSubmitting">
              <option :value="null" disabled>이관받을 활성 영업사원을 선택하세요</option>
              <option v-for="user in activeUsers" :key="user.id" :value="user.id">
                {{ user.name }} ({{ user.employeeId }}) - 현재 {{ user.customerCount }}명
              </option>
            </select>
            <span v-if="isLoadingUsers" class="select-loading">로딩 중...</span>
          </div>

          <!-- 이관 대상자 선택 완료 표시 (초록색) -->
          <div v-if="selectedToUserId" class="selection-complete">
            ✅ 이관 대상자 선택 완료
          </div>

          <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
        </div>

        <!-- 실행 내역 안내 -->
        <div class="execution-list-section">
          <div class="execution-list-title">퇴사 처리 시 실행 내역:</div>
          <ul class="execution-list">
            <li>활성 JWT 토큰 Redis 블랙리스트 등재</li>
            <li>개인정보(PII) 별도 암호화 테이블로 격리</li>
            <li>시스템 접속 권한 즉시 차단</li>
            <li>3년 후 자동 파기 스케줄 등록</li>
          </ul>
        </div>
      </div>

      <div class="modal-footer">
        <button class="button button-secondary cancel-btn" type="button" @click="$emit('close')" :disabled="isSubmitting">
          취소
        </button>
        <button 
          class="button button-danger submit-btn" 
          type="button" 
          :disabled="!selectedToUserId || isSubmitting"
          @click="handleTransfer"
        >
          {{ isSubmitting ? '처리 중...' : '퇴사 및 토큰 파기' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 14px;
  border-bottom: 1px solid var(--color-border);
}

.modal-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.warning-icon {
  font-size: 18px;
}

.modal-title {
  margin: 0;
  font-size: 17px;
  font-weight: 900;
  color: var(--color-text);
  letter-spacing: -0.03em;
}

.close-button {
  background: none;
  border: none;
  font-size: 24px;
  line-height: 1;
  color: var(--color-text-muted);
  cursor: pointer;
}

.close-button:hover {
  color: var(--color-text);
}

.modal-body {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 붉은색 잔여 고객 알림 박스 */
.alert-box {
  background: #fff5f5;
  border: 1px solid #ffc1c1;
  border-radius: var(--radius-sm);
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.alert-msg-title {
  color: #e53e3e;
  font-size: 13px;
  font-weight: 800;
}

.alert-msg-desc {
  color: #e53e3e;
  font-size: 12px;
  font-weight: 700;
  padding-left: 20px;
}

.transfer-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 800;
  color: var(--color-text);
}

/* 숫자 3 레드 서클 뱃지 */
.badge-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  background-color: #ef4444;
  color: #ffffff;
  border-radius: 50%;
  font-size: 10px;
  font-weight: 800;
}

.select-wrapper {
  position: relative;
  width: 100%;
}

.select-input {
  width: 100%;
  font-size: 13px;
}

.select-loading {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 11px;
  color: var(--color-text-muted);
}

.selection-complete {
  color: #10b981;
  font-size: 12px;
  font-weight: 800;
  margin-top: 4px;
}

.error-text {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--color-danger);
  font-weight: 700;
}

/* 실행 내역 섹션 */
.execution-list-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.execution-list-title {
  font-size: 12px;
  font-weight: 800;
  color: #4a5568;
}

.execution-list {
  margin: 0;
  padding-left: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.execution-list li {
  font-size: 12px;
  line-height: 1.4;
  color: #718096;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 24px 20px;
  border-top: 1px solid var(--color-border);
}

.cancel-btn {
  height: 38px;
  padding: 0 16px;
  font-size: 13px;
}

.submit-btn {
  height: 38px;
  padding: 0 16px;
  font-size: 13px;
  background: var(--color-danger);
  color: #ffffff;
}

.submit-btn:hover:not(:disabled) {
  background: #dc2626;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
