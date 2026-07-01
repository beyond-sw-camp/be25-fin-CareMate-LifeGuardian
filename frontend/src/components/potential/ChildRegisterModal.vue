<script setup lang="ts">
import axios from 'axios'
import { reactive, ref } from 'vue'

import {
  createPotentialCustomer,
  type Gender,
  type ParentCustomerSearchResponse,
  type PotentialCustomerCreateResponse,
} from '@/api/potential'

const props = defineProps<{
  parent: ParentCustomerSearchResponse
}>()

const emit = defineEmits<{
  close: []
  registered: [customer: PotentialCustomerCreateResponse]
  back: []
}>()

const form = reactive({
  name: '',
  gender: 'FEMALE' as Gender,
  birthDate: '',
})

const isRegistering = ref(false)
const errorMessage = ref('')

const getErrorMessage = (
  error: unknown,
  fallbackMessage = '잠재고객 등록에 실패했습니다.',
) => {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? fallbackMessage
  }

  return fallbackMessage
}

const handleRegister = async () => {
  errorMessage.value = ''

  if (!form.name || !form.gender || !form.birthDate) {
    errorMessage.value = '자녀 정보를 모두 입력해주세요.'
    return
  }

  isRegistering.value = true

  try {
    const result = await createPotentialCustomer({
      parentCustomerId: props.parent.integratedCustomerId,
      relationshipCode: props.parent.relationshipCode,
      name: form.name,
      gender: form.gender,
      birthDate: form.birthDate,
    })

    emit('registered', result)
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    isRegistering.value = false
  }
}
</script>

<template>
  <div class="modal-backdrop child-modal" role="presentation">
    <section
      class="modal-card child-modal__card"
      role="dialog"
      aria-modal="true"
      aria-labelledby="child-modal-title"
    >
      <header class="child-modal__header">
        <div>
          <p class="child-modal__eyebrow">잠재고객 등록</p>
          <h3 id="child-modal-title">자녀 정보 입력</h3>
        </div>

        <button
          class="child-modal__close"
          type="button"
          aria-label="닫기"
          @click="emit('close')"
        >
          ×
        </button>
      </header>

      <div class="child-modal__body">
        <div class="child-modal__parent-box">
          <strong>확인된 부모 정보</strong>
          <dl>
            <div>
              <dt>이름</dt>
              <dd>{{ parent.name }}</dd>
            </div>
            <div>
              <dt>관계</dt>
              <dd>{{ parent.relationshipName }}</dd>
            </div>
            <div>
              <dt>생년월일</dt>
              <dd>{{ parent.birthDate }}</dd>
            </div>
            <div>
              <dt>연락처</dt>
              <dd>{{ parent.phone }}</dd>
            </div>
          </dl>
        </div>

        <label class="child-modal__field">
          <span>자녀 이름</span>
          <input v-model.trim="form.name" type="text" placeholder="자녀 이름 입력" />
        </label>

        <label class="child-modal__field">
          <span>성별</span>
          <select v-model="form.gender">
            <option value="FEMALE">여성</option>
            <option value="MALE">남성</option>
          </select>
        </label>

        <label class="child-modal__field child-modal__field--full">
          <span>자녀 생년월일</span>
          <input v-model="form.birthDate" type="date" />
        </label>

        <p v-if="errorMessage" class="child-modal__message child-modal__message--error">
          {{ errorMessage }}
        </p>
      </div>

      <footer class="child-modal__footer">
        <button class="button button-secondary" type="button" @click="emit('back')">
          이전
        </button>
        <button
          class="button button-primary"
          type="button"
          :disabled="isRegistering"
          @click="handleRegister"
        >
          {{ isRegistering ? '등록 중' : '등록' }}
        </button>
      </footer>
    </section>
  </div>
</template>

<style scoped>
.child-modal {
  z-index: 30;
}

.child-modal__card {
  display: flex;
  width: min(500px, 100%);
  max-height: calc(100vh - 48px);
  overflow: hidden;
  border: 1px solid #e4e9f2;
  border-radius: 18px;
  background: #ffffff;
  flex-direction: column;
}

.child-modal__header {
  position: relative;
  flex: 0 0 auto;
  border-bottom: 1px solid #e8edf5;
  background:
    radial-gradient(circle at 90% 10%, color-mix(in srgb, var(--color-primary) 14%, transparent), transparent 40%),
    linear-gradient(135deg, color-mix(in srgb, var(--color-primary) 7%, white) 0%, #ffffff 70%);
  padding: 22px 60px 20px 26px;
}

.child-modal__eyebrow {
  margin: 0 0 4px;
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 900;
}

.child-modal__header h3 {
  margin: 0;
  color: var(--color-text);
  font-size: 18px;
  font-weight: 900;
}

.child-modal__close {
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

.child-modal__close:hover {
  background: #f3f5f8;
  color: #263142;
}

.child-modal__body {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 13px 12px;
  overflow-y: auto;
  padding: 22px 26px;
}

.child-modal__parent-box,
.child-modal__field--full,
.child-modal__message {
  grid-column: 1 / -1;
}

.child-modal__parent-box {
  border: 1px solid #e3e8f0;
  border-radius: 10px;
  background: #fbfcfe;
  padding: 12px;
}

.child-modal__parent-box strong {
  display: block;
  margin-bottom: 9px;
  color: #263142;
  font-size: 12px;
  font-weight: 900;
}

.child-modal__parent-box dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 7px 12px;
  margin: 0;
}

.child-modal__parent-box div {
  display: grid;
  grid-template-columns: 62px 1fr;
  gap: 6px;
  font-size: 11px;
}

.child-modal__parent-box dt {
  color: #7f8999;
  font-weight: 800;
}

.child-modal__parent-box dd {
  margin: 0;
  color: #172033;
  font-weight: 700;
}

.child-modal__field {
  display: grid;
  gap: 6px;
}

.child-modal__field span {
  color: #394456;
  font-size: 11px;
  font-weight: 800;
}

.child-modal__field input,
.child-modal__field select {
  width: 100%;
  height: 34px;
  border: 1px solid #d7dde7;
  border-radius: 7px;
  background: #ffffff;
  color: #172033;
  padding: 0 10px;
  font-size: 12px;
}

.child-modal__field input:focus,
.child-modal__field select:focus {
  border-color: var(--color-primary);
  outline: none;
}

.child-modal__message {
  margin: 0;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 11px;
  font-weight: 700;
}

.child-modal__message--error {
  background: #fff0f1;
  color: #c43e4b;
}

.child-modal__footer {
  display: flex;
  flex: 0 0 auto;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid #edf0f5;
  background: #fbfcfe;
  padding: 14px 20px 17px;
}

.child-modal__footer .button {
  min-width: 60px;
  min-height: 30px;
  border-radius: 8px;
  font-size: 14px;
}
</style>
