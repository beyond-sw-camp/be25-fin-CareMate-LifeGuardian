<script setup lang="ts">
import axios from 'axios'
import { reactive, ref } from 'vue'

import {
  searchParentCustomer,
  type ParentCustomerSearchRequest,
  type ParentCustomerSearchResponse,
  type RelationshipCode,
} from '@/api/potential'

const emit = defineEmits<{
  close: []
  next: [parent: ParentCustomerSearchResponse]
}>()

const form = reactive<ParentCustomerSearchRequest>({
  name: '',
  birthDate: '',
  relationshipCode: '01',
  phone: '',
  rrn: '',
  address: '',
})

const searchedParent = ref<ParentCustomerSearchResponse | null>(null)
const isSearching = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const getErrorMessage = (
  error: unknown,
  fallbackMessage = '부모 통합고객 조회에 실패했습니다.',
) => {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? fallbackMessage
  }

  return fallbackMessage
}

const handleSearchParent = async () => {
  errorMessage.value = ''
  successMessage.value = ''
  searchedParent.value = null

  if (
    !form.name ||
    !form.birthDate ||
    !form.relationshipCode ||
    !form.phone ||
    !form.rrn ||
    !form.address
  ) {
    errorMessage.value = '부모 정보를 모두 입력해주세요.'
    return
  }

  isSearching.value = true

  try {
    const result = await searchParentCustomer({ ...form })

    searchedParent.value = result
    successMessage.value = '부모 통합고객 정보가 확인되었습니다.'
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    isSearching.value = false
  }
}

const handleRelationshipChange = (event: Event) => {
  form.relationshipCode = (event.target as HTMLSelectElement).value as RelationshipCode
  searchedParent.value = null
  successMessage.value = ''
}

const handleNext = () => {
  if (!searchedParent.value) {
    errorMessage.value = '먼저 부모 통합고객 조회를 완료해주세요.'
    return
  }

  emit('next', searchedParent.value)
}
</script>

<template>
  <div class="modal-backdrop parent-modal" role="presentation">
    <section
      class="modal-card parent-modal__card"
      role="dialog"
      aria-modal="true"
      aria-labelledby="parent-modal-title"
    >
      <header class="parent-modal__header">
        <div>
          <p class="parent-modal__eyebrow">잠재고객 등록</p>
          <h3 id="parent-modal-title">부모 정보 조회</h3>
        </div>

        <button
          class="parent-modal__close"
          type="button"
          aria-label="닫기"
          @click="emit('close')"
        >
          x
        </button>
      </header>

      <div class="parent-modal__body">
        <label class="parent-modal__field">
          <span>부모 이름</span>
          <input v-model.trim="form.name" type="text" placeholder="이름 입력" />
        </label>

        <label class="parent-modal__field">
          <span>부모 생년월일</span>
          <input v-model="form.birthDate" type="date" />
        </label>

        <label class="parent-modal__field">
          <span>관계</span>
          <select :value="form.relationshipCode" @change="handleRelationshipChange">
            <option value="01">부</option>
            <option value="02">모</option>
          </select>
        </label>

        <label class="parent-modal__field">
          <span>부모 연락처</span>
          <input v-model.trim="form.phone" type="text" placeholder="010-0000-0000" />
        </label>

        <label class="parent-modal__field">
          <span>주민등록번호</span>
          <input v-model.trim="form.rrn" type="text" placeholder="830411-1******" />
        </label>

        <label class="parent-modal__field parent-modal__field--full">
          <span>주소</span>
          <input v-model.trim="form.address" type="text" placeholder="주소 입력" />
        </label>

        <p v-if="errorMessage" class="parent-modal__message parent-modal__message--error">
          {{ errorMessage }}
        </p>

        <p v-if="successMessage" class="parent-modal__message parent-modal__message--success">
          {{ successMessage }}
        </p>

        <div v-if="searchedParent" class="parent-modal__result">
          <strong>조회된 부모 정보</strong>
          <dl>
            <div>
              <dt>이름</dt>
              <dd>{{ searchedParent.name }}</dd>
            </div>
            <div>
              <dt>생년월일</dt>
              <dd>{{ searchedParent.birthDate }}</dd>
            </div>
            <div>
              <dt>관계</dt>
              <dd>{{ searchedParent.relationshipName }}</dd>
            </div>
            <div>
              <dt>연락처</dt>
              <dd>{{ searchedParent.phone }}</dd>
            </div>
          </dl>
        </div>
      </div>

      <footer class="parent-modal__footer">
        <button
          class="button button-secondary"
          type="button"
          :disabled="isSearching"
          @click="handleSearchParent"
        >
          {{ isSearching ? '조회 중' : '조회' }}
        </button>
        <button
          class="button button-primary"
          type="button"
          :disabled="!searchedParent"
          @click="handleNext"
        >
          다음
        </button>
      </footer>
    </section>
  </div>
</template>

<style scoped>
.parent-modal {
  z-index: 30;
}

.parent-modal__card {
  display: flex;
  width: min(520px, 100%);
  max-height: calc(100vh - 48px);
  overflow: hidden;
  border: 1px solid #e4e9f2;
  border-radius: 18px;
  background: #ffffff;
  flex-direction: column;
}

.parent-modal__header {
  position: relative;
  flex: 0 0 auto;
  border-bottom: 1px solid #e8edf5;
  background:
    radial-gradient(circle at 90% 10%, rgb(56 163 255 / 13%), transparent 40%),
    linear-gradient(135deg, #f7faff 0%, #ffffff 70%);
  padding: 22px 60px 20px 26px;
}

.parent-modal__eyebrow {
  margin: 0 0 4px;
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 900;
}

.parent-modal__header h3 {
  margin: 0;
  color: var(--color-text);
  font-size: 18px;
  font-weight: 900;
}

.parent-modal__close {
  position: absolute;
  top: 18px;
  right: 18px;
  display: grid;
  width: 28px;
  height: 28px;
  place-items: center;
  border: 1px solid #e0e6ef;
  border-radius: 9px;
  background: #ffffff;
  color: #667085;
  font-size: 18px;
  line-height: 1;
}

.parent-modal__body {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 13px 12px;
  overflow-y: auto;
  padding: 22px 26px;
}

.parent-modal__field {
  display: grid;
  gap: 6px;
}

.parent-modal__field--full,
.parent-modal__message,
.parent-modal__result {
  grid-column: 1 / -1;
}

.parent-modal__field span {
  color: #394456;
  font-size: 11px;
  font-weight: 800;
}

.parent-modal__field input,
.parent-modal__field select {
  width: 100%;
  height: 34px;
  border: 1px solid #d7dde7;
  border-radius: 7px;
  background: #ffffff;
  color: #172033;
  padding: 0 10px;
  font-size: 12px;
}

.parent-modal__field input:focus,
.parent-modal__field select:focus {
  border-color: var(--color-primary);
  outline: none;
}

.parent-modal__message {
  margin: 0;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 11px;
  font-weight: 700;
}

.parent-modal__message--error {
  background: #fff0f1;
  color: #c43e4b;
}

.parent-modal__message--success {
  background: #eaf8ee;
  color: #24723b;
}

.parent-modal__result {
  border: 1px solid #e3e8f0;
  border-radius: 10px;
  background: #fbfcfe;
  padding: 12px;
}

.parent-modal__result strong {
  display: block;
  margin-bottom: 9px;
  color: #263142;
  font-size: 12px;
  font-weight: 900;
}

.parent-modal__result dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 7px 12px;
  margin: 0;
}

.parent-modal__result div {
  display: grid;
  grid-template-columns: 62px 1fr;
  gap: 6px;
  font-size: 11px;
}

.parent-modal__result dt {
  color: #7f8999;
  font-weight: 800;
}

.parent-modal__result dd {
  margin: 0;
  color: #172033;
  font-weight: 700;
}

.parent-modal__footer {
  display: flex;
  flex: 0 0 auto;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid #edf0f5;
  background: #fbfcfe;
  padding: 14px 20px 17px;
}

.parent-modal__footer .button {
  min-width: 60px;
  min-height: 30px;
  border-radius: 8px;
  font-size: 14px;
}
</style>