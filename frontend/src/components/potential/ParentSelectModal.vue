<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'

import {
  getParentCustomers,
  type ParentCustomerSearchResponse,
} from '@/api/potential'

const emit = defineEmits<{
  close: []
  next: [parent: ParentCustomerSearchResponse]
}>()

const parents = ref<ParentCustomerSearchResponse[]>([])
const selectedParent = ref<ParentCustomerSearchResponse | null>(null)
const searchKeyword = ref('')
const isLoading = ref(false)
const errorMessage = ref('')

const filteredParents = computed(() => {
  const keyword = searchKeyword.value.trim()

  if (!keyword) {
    return parents.value
  }

  return parents.value.filter((parent) =>
    parent.name.includes(keyword),
  )
})

const getErrorMessage = (
  error: unknown,
  fallbackMessage = '부모 고객 목록을 불러오지 못했습니다.',
) => {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? fallbackMessage
  }

  return fallbackMessage
}

const loadParents = async () => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    parents.value = await getParentCustomers()
  } catch (error) {
    parents.value = []
    errorMessage.value = getErrorMessage(error)
  } finally {
    isLoading.value = false
  }
}

const selectParent = (parent: ParentCustomerSearchResponse) => {
  selectedParent.value = parent
}

const handleNext = () => {
  if (!selectedParent.value) return

  emit('next', selectedParent.value)
}

onMounted(() => {
  void loadParents()
})
</script>

<template>
  <div class="modal-backdrop parent-select-modal-wrap" role="presentation">
    <section 
        class="modal-card parent-select-modal"
        role="dialog"
        aria-modal="true"
    >
      <header class="parent-select-modal__header">
        <div>
          <h2>부모 고객 선택</h2>
          <p>자녀를 등록할 부모 고객을 선택해 주세요.</p>
        </div>

        <button
          class="parent-select-modal__close"
          type="button"
          aria-label="닫기"
          @click="emit('close')"
        >
          ×
        </button>
      </header>

      <div class="parent-select-modal__search">
        <label class="parent-select-modal__search-field">
          <span>부모명 검색</span>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="부모 고객명을 입력하세요."
          />
        </label>
      </div>

      <p
        v-if="errorMessage"
        class="parent-select-modal__message parent-select-modal__message--error"
      >
        {{ errorMessage }}
      </p>

      <p
        v-else-if="isLoading"
        class="parent-select-modal__message"
      >
        불러오는 중...
      </p>

      <div
        v-else
        class="parent-select-modal__list"
      >
        <button
          v-for="parent in filteredParents"
          :key="parent.integratedCustomerId"
          class="parent-select-modal__item"
          :class="{
            'parent-select-modal__item--selected':
              selectedParent?.integratedCustomerId === parent.integratedCustomerId,
          }"
          type="button"
          @click="selectParent(parent)"
        >
          <span class="parent-select-modal__radio"></span>

          <span class="parent-select-modal__info">
            <strong>{{ parent.name }}</strong>
            <span>
              {{ parent.relationshipName }}
              · {{ parent.birthDate }}
              · {{ parent.phone }}
            </span>
            <small>{{ parent.address || '-' }}</small>
          </span>
        </button>

        <p
          v-if="filteredParents.length === 0"
          class="parent-select-modal__empty"
        >
          조회된 부모 고객이 없습니다.
        </p>
      </div>

      <footer class="parent-select-modal__footer">
        <button
          class="parent-select-modal__button parent-select-modal__button--secondary"
          type="button"
          @click="emit('close')"
        >
          취소
        </button>

        <button
          class="parent-select-modal__button parent-select-modal__button--primary"
          type="button"
          :disabled="!selectedParent"
          @click="handleNext"
        >
          다음
        </button>
      </footer>
    </section>
  </div>
</template>

<style scoped>
.parent-select-modal-wrap {
    z-index: 30;
}

.parent-select-modal {
  display: flex;
  width: min(500px, 100%);
  max-height: calc(100vh - 48px);
  overflow: hidden;
  border: 1px solid #e4e9f2;
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 18px 60px rgb(15 23 42 / 24%);
  flex-direction: column;
}

.parent-select-modal__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  border-bottom: 1px solid #e8edf5;
  background:
    radial-gradient(circle at 90% 10%, color-mix(in srgb, var(--color-primary) 14%, transparent), transparent 40%),
    linear-gradient(135deg, color-mix(in srgb, var(--color-primary) 7%, white) 0%, #ffffff 70%);
  padding: 22px 60px 20px 26px;
  position: relative;
}

.parent-select-modal__header h2 {
  margin: 0;
  color: #172033;
  font-size: 20px;
  font-weight: 900;
}

.parent-select-modal__header p {
  margin: 6px 0 0;
  color: #7f8999;
  font-size: 12px;
  font-weight: 700;
}

.parent-select-modal__close {
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

.parent-select-modal__close:hover {
  background: #f3f5f8;
  color: #263142;
}

.parent-select-modal__search {
  padding: 16px 22px 10px;
}

.parent-select-modal__search-field {
  display: flex;
  align-items: center;
  gap: 10px;
}

.parent-select-modal__search-field span {
  color: #263142;
  font-size: 13px;
  font-weight: 800;
  white-space: nowrap;
}

.parent-select-modal__search-field input {
  flex: 1;
  height: 34px;
  border: 1px solid #d7dde7;
  border-radius: 6px;
  padding: 0 11px;
  color: #172033;
  font-size: 12px;
  outline: none;
}

.parent-select-modal__search-field input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-primary) 10%, transparent);
}

.parent-select-modal__message {
  display: flex;
  min-height: 220px;
  align-items: center;
  justify-content: center;
  margin: 0;
  color: #7f8999;
  font-size: 12px;
  font-weight: 700;
}

.parent-select-modal__message--error {
  color: #d85a65;
}

.parent-select-modal__list {
  max-height: 340px;
  overflow-y: auto;
  padding: 6px 22px 14px;
}

.parent-select-modal__item {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 12px;
  border: 1px solid #e1e7f0;
  border-radius: 8px;
  background: #ffffff;
  padding: 12px;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease, box-shadow 0.15s ease;
}

.parent-select-modal__item + .parent-select-modal__item {
  margin-top: 8px;
}

.parent-select-modal__item:hover {
  border-color: var(--color-primary);
  background: color-mix(in srgb, var(--color-primary) 4%, white);
}

.parent-select-modal__item--selected {
  border-color: var(--color-primary);
  background: color-mix(in srgb, var(--color-primary) 7%, white);
}

.parent-select-modal__item--selected:hover {
  border-color: var(--color-primary);
  background: color-mix(in srgb, var(--color-primary) 4%, white);
}

.parent-select-modal__radio {
  width: 14px;
  height: 14px;
  flex: 0 0 auto;
  border: 2px solid #c5cad3;
  border-radius: 50%;
}

.parent-select-modal__item--selected .parent-select-modal__radio {
  border: 4px solid var(--color-primary);
}

.parent-select-modal__info {
  display: grid;
  min-width: 0;
  gap: 4px;
}

.parent-select-modal__info strong {
  color: #172033;
  font-size: 14px;
  font-weight: 900;
}

.parent-select-modal__info span {
  color: #4c586b;
  font-size: 12px;
  font-weight: 700;
}

.parent-select-modal__info small {
  overflow: hidden;
  color: #8b95a6;
  font-size: 11px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.parent-select-modal__empty {
  display: flex;
  height: 160px;
  align-items: center;
  justify-content: center;
  margin: 0;
  color: #7f8999;
  font-size: 12px;
  font-weight: 700;
}

.parent-select-modal__footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  border-top: 1px solid #e5eaf2;
  padding: 14px 22px 18px;
}

.parent-select-modal__button {
  min-width: 60px;
  height: 30px;
  border-radius: 8px;
  padding: 0 16px;
  font-size: 14px;
  font-weight: 700;
}

.parent-select-modal__button--secondary {
  border: 1px solid #d7dde7;
  background: #ffffff;
  color: #4c586b;
}

.parent-select-modal__button--secondary:hover {
  background: #f8fafc;
}

.parent-select-modal__button--primary:hover:not(:disabled) {
  filter: brightness(0.95);
}

.parent-select-modal__button--primary {
  border: 1px solid var(--color-primary);
  background: var(--color-primary);
  color: #ffffff;
}

.parent-select-modal__button--primary:disabled {
  border-color: #c5cad3;
  background: #c5cad3;
  cursor: default;
}
</style>