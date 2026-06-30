<script setup lang="ts">
import axios from 'axios'
import { reactive, ref, watch } from 'vue'

import {
  updatePotentialCustomer,
  type Gender,
  type PotentialCustomerDetailResponse,
} from '@/api/potential'

const props = defineProps<{
  customer: PotentialCustomerDetailResponse
}>()

const emit = defineEmits<{
  close: []
  updated: [customer: PotentialCustomerDetailResponse]
}>()

const isEditing = ref(false)
const isSaving = ref(false)
const errorMessage = ref('')

const form = reactive({
  name: '',
  gender: 'FEMALE' as Gender,
  birthDate: '',
})

const resetForm = () => {
  form.name = props.customer.name
  form.gender = props.customer.gender
  form.birthDate = props.customer.birthDate
}

watch(
  () => props.customer,
  () => {
    resetForm()
    isEditing.value = false
    errorMessage.value = ''
  },
  { immediate: true },
)

const getErrorMessage = (
  error: unknown,
  fallbackMessage = '잠재고객 수정에 실패했습니다.',
) => {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? fallbackMessage
  }

  return fallbackMessage
}

const handleEdit = () => {
  isEditing.value = true
  errorMessage.value = ''
}

const handleCancelEdit = () => {
  resetForm()
  isEditing.value = false
  errorMessage.value = ''
}

const handleSave = async () => {
  errorMessage.value = ''

  if (!form.name || !form.gender || !form.birthDate) {
    errorMessage.value = '잠재고객 정보를 모두 입력해주세요.'
    return
  }

  isSaving.value = true

  try {
    const result = await updatePotentialCustomer(props.customer.potentialCustomerId, {
      name: form.name,
      gender: form.gender,
      birthDate: form.birthDate,
    })

    isEditing.value = false
    emit('updated', result)
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <div class="modal-backdrop potential-detail-modal" role="presentation">
    <section
      class="modal-card potential-detail-modal__card"
      role="dialog"
      aria-modal="true"
      aria-labelledby="potential-detail-modal-title"
    >
      <header class="potential-detail-modal__header">
        <div>
          <p class="potential-detail-modal__eyebrow">잠재고객 관리</p>
          <h3 id="potential-detail-modal-title">잠재고객 상세 정보</h3>
        </div>

        <button
          class="potential-detail-modal__close"
          type="button"
          aria-label="닫기"
          @click="emit('close')"
        >
          ×
        </button>
      </header>

      <div class="potential-detail-modal__body">
        <section class="potential-detail-modal__section">
          <strong>부모 정보</strong>

          <dl>
            <div>
              <dt>이름</dt>
              <dd>{{ customer.parentName }}</dd>
            </div>
            <div>
              <dt>관계</dt>
              <dd>{{ customer.relationshipName }}</dd>
            </div>
            <div>
              <dt>생년월일</dt>
              <dd>{{ customer.parentBirthDate }}</dd>
            </div>
            <div>
              <dt>연락처</dt>
              <dd>{{ customer.parentPhone }}</dd>
            </div>
            <div class="potential-detail-modal__full">
              <dt>주소</dt>
              <dd>{{ customer.parentAddress || '-' }}</dd>
            </div>
          </dl>
        </section>

        <section class="potential-detail-modal__section">
          <strong>잠재고객 정보</strong>

          <div class="potential-detail-modal__form">
            <label class="potential-detail-modal__field">
              <span>이름</span>
              <input
                v-model.trim="form.name"
                type="text"
                :disabled="!isEditing"
              />
            </label>

            <label class="potential-detail-modal__field">
              <span>성별</span>
              <select v-model="form.gender" :disabled="!isEditing">
                <option value="FEMALE">여성</option>
                <option value="MALE">남성</option>
              </select>
            </label>

            <label class="potential-detail-modal__field">
              <span>생년월일</span>
              <input
                v-model="form.birthDate"
                type="date"
                :disabled="!isEditing"
              />
            </label>

            <label class="potential-detail-modal__field">
              <span>만 나이</span>
              <input :value="customer.age" type="text" disabled />
            </label>

            <label class="potential-detail-modal__field">
              <span>상담 상태</span>
              <input :value="customer.consultStatusName" type="text" disabled />
            </label>

            <label class="potential-detail-modal__field">
              <span>전환 상태</span>
              <input :value="customer.conversionStatusName" type="text" disabled />
            </label>
          </div>
        </section>

        <p
          v-if="errorMessage"
          class="potential-detail-modal__message potential-detail-modal__message--error"
        >
          {{ errorMessage }}
        </p>
      </div>

      <footer class="potential-detail-modal__footer">
        <button
          v-if="!isEditing"
          class="button button-secondary"
          type="button"
          @click="emit('close')"
        >
          취소
        </button>

        <button
          v-if="!isEditing"
          class="button button-primary"
          type="button"
          @click="handleEdit"
        >
          수정
        </button>

        <button
          v-if="isEditing"
          class="button button-secondary"
          type="button"
          :disabled="isSaving"
          @click="handleCancelEdit"
        >
          취소
        </button>

        <button
          v-if="isEditing"
          class="button button-primary"
          type="button"
          :disabled="isSaving"
          @click="handleSave"
        >
          {{ isSaving ? '저장 중' : '저장' }}
        </button>
      </footer>
    </section>
  </div>
</template>

<style scoped>
.potential-detail-modal {
  z-index: 30;
}

.potential-detail-modal__card {
  display: flex;
  width: min(500px, 100%);
  max-height: calc(100vh - 48px);
  overflow: hidden;
  border: 1px solid #e4e9f2;
  border-radius: 18px;
  background: #ffffff;
  flex-direction: column;
}

.potential-detail-modal__header {
  position: relative;
  flex: 0 0 auto;
  border-bottom: 1px solid #e8edf5;
  background:
    radial-gradient(circle at 90% 10%, rgb(56 163 255 / 13%), transparent 40%),
    linear-gradient(135deg, #f7faff 0%, #ffffff 70%);
  padding: 22px 60px 20px 26px;
}

.potential-detail-modal__eyebrow {
  margin: 0 0 4px;
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 900;
}

.potential-detail-modal__header h3 {
  margin: 0;
  color: var(--color-text);
  font-size: 18px;
  font-weight: 900;
}

.potential-detail-modal__close {
  position: absolute;
  top: 20px;
  right: 22px;
  width: 30px;
  height: 30px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #7f8999;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
}

.potential-detail-modal__close:hover {
  background: #f3f5f8;
  color: #263142;
}

.potential-detail-modal__body {
  display: grid;
  gap: 14px;
  overflow-y: auto;
  padding: 22px 26px;
}

.potential-detail-modal__section {
  border: 1px solid #e3e8f0;
  border-radius: 10px;
  background: #fbfcfe;
  padding: 12px;
}

.potential-detail-modal__section strong {
  display: block;
  margin-bottom: 10px;
  color: #263142;
  font-size: 12px;
  font-weight: 900;
}

.potential-detail-modal__section dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 7px 12px;
  margin: 0;
}

.potential-detail-modal__section dl div {
  display: grid;
  grid-template-columns: 62px 1fr;
  gap: 6px;
  font-size: 11px;
}

.potential-detail-modal__full {
  grid-column: 1 / -1;
}

.potential-detail-modal__section dt {
  color: #7f8999;
  font-weight: 800;
}

.potential-detail-modal__section dd {
  margin: 0;
  color: #172033;
  font-weight: 700;
}

.potential-detail-modal__form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 13px 12px;
}

.potential-detail-modal__field {
  display: grid;
  gap: 6px;
}

.potential-detail-modal__field span {
  color: #394456;
  font-size: 11px;
  font-weight: 800;
}

.potential-detail-modal__field input,
.potential-detail-modal__field select {
  width: 100%;
  height: 34px;
  border: 1px solid #d7dde7;
  border-radius: 7px;
  background: #ffffff;
  color: #172033;
  padding: 0 10px;
  font-size: 12px;
}

.potential-detail-modal__field input:disabled,
.potential-detail-modal__field select:disabled {
  background: #f2f4f7;
  color: #667085;
  opacity: 1;
}

.potential-detail-modal__field input:focus,
.potential-detail-modal__field select:focus {
  border-color: var(--color-primary);
  outline: none;
}

.potential-detail-modal__message {
  margin: 0;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 11px;
  font-weight: 700;
}

.potential-detail-modal__message--error {
  background: #fff0f1;
  color: #c43e4b;
}

.potential-detail-modal__footer {
  display: flex;
  flex: 0 0 auto;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid #edf0f5;
  background: #fbfcfe;
  padding: 14px 20px 17px;
}

.potential-detail-modal__footer .button {
  min-width: 60px;
  min-height: 30px;
  border-radius: 8px;
  font-size: 14px;
}
</style>