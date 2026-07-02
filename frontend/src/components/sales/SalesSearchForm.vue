<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, reactive, ref, watch } from 'vue'

import type { SalesSearchFilters } from '@/api/sales'

const props = defineProps<{
  filters?: SalesSearchFilters
}>()

const emit = defineEmits<{
  search: [filters: SalesSearchFilters]
}>()

const initialForm = () => ({
  customerName: '',
  age: '',
  gender: '' as '' | 'Male' | 'Female',
  customerStageCode: '01' as '' | '01' | '02',
  consultStatusCodes: [] as string[],
  contractStatusCodes: [] as string[],
  hasReport: false,
  hasThreeStep: false,
})
type SalesSearchFormState = ReturnType<typeof initialForm>
type SalesSearchFormFilters = Partial<SalesSearchFormState>

// 화면 입력값은 문자열/배열 중심으로 관리하고, submit 시 API 검색 조건으로 변환
const form = reactive(initialForm())
const customerNameSpacingError = ref('')
let customerNameSpacingErrorTimer: ReturnType<typeof setTimeout> | undefined

watch(
  () => props.filters,
  (filters) => {
    if (!filters) return

    form.consultStatusCodes = filters.consultStatusCode
      ? [...filters.consultStatusCode]
      : []

    form.contractStatusCodes = filters.contractStatusCode
      ? [...filters.contractStatusCode]
      : []
  },
  {
    immediate: true,
    deep: true,
  },
)

// 코드 값으로 내려가는 필터를 사용자가 읽을 수 있는 칩 문구로 바꾸기 위한 라벨 맵
const consultStatusLabels: Record<string, string> = {
  '01': '미상담',
  '02': '상담중',
}
const contractStatusLabels: Record<string, string> = {
  '01': '설계중',
  '02': '설계완료',
  '03': '청약중',
  '04': '청약완료',
  '06': '수납완료',
}
const quickFilters: { label: string; description: string; filters: SalesSearchFormFilters }[] = [
  {
    label: '첫 연락 대상',
    description: '리포트는 준비됐지만 아직 상담을 시작하지 않은 잠재고객입니다.',
    filters: { customerStageCode: '01', consultStatusCodes: ['01'], hasReport: true },
  },
  {
    label: '상담 지속관리',
    description: '상담이 진행 중이며 리포트를 바탕으로 추가 안내가 필요한 잠재고객입니다.',
    filters: { customerStageCode: '01', consultStatusCodes: ['02'], hasReport: true },
  },
  {
    label: '우선 상담 대상',
    description: '아직 상담 전이지만 3-Step 평가 대상으로 우선 확인이 필요한 잠재고객입니다.',
    filters: { customerStageCode: '01', consultStatusCodes: ['01'], hasThreeStep: true },
  },
  {
    label: '설계 진행건',
    description: '설계중 또는 설계완료 상태로 다음 계약 단계 확인이 필요한 통합고객입니다.',
    filters: { customerStageCode: '02', contractStatusCodes: ['01', '02'] },
  },
  {
    label: '청약 진행건',
    description: '청약중 또는 청약완료 상태로 마무리 진행 상황을 확인할 통합고객입니다.',
    filters: { customerStageCode: '02', contractStatusCodes: ['03', '04'] },
  },
  {
    label: '수납 완료건',
    description: '수납이 완료됐고 리포트가 있어 후속 안내와 제안이 가능한 통합고객입니다.',
    filters: { customerStageCode: '02', contractStatusCodes: ['06'], hasReport: true },
  },
]
const appliedFilterChips = computed(() => {
  const chips: { key: string; label: string; remove: () => void }[] = []

  if (form.customerName) {
    chips.push({ key: 'customerName', label: `고객 ${form.customerName}`, remove: () => { form.customerName = '' } })
  }
  if (form.age !== '') {
    chips.push({ key: 'age', label: `나이 ${form.age}`, remove: () => { form.age = '' } })
  }
  if (form.gender) {
    chips.push({ key: 'gender', label: form.gender === 'Male' ? '남' : '여', remove: () => { form.gender = '' } })
  }
  if (form.customerStageCode) {
    chips.push({ key: 'customerStageCode', label: form.customerStageCode === '01' ? '잠재' : '통합', remove: () => { form.customerStageCode = '' } })
  }
  form.consultStatusCodes.forEach((code) => {
    chips.push({ key: `consult-${code}`, label: `상담 ${consultStatusLabels[code] ?? code}`, remove: () => { form.consultStatusCodes = form.consultStatusCodes.filter((item) => item !== code) } })
  })
  form.contractStatusCodes.forEach((code) => {
    chips.push({ key: `contract-${code}`, label: `계약 ${contractStatusLabels[code] ?? code}`, remove: () => { form.contractStatusCodes = form.contractStatusCodes.filter((item) => item !== code) } })
  })
  if (form.hasReport) {
    chips.push({ key: 'hasReport', label: '리포트', remove: () => { form.hasReport = false } })
  }
  if (form.hasThreeStep) {
    chips.push({ key: 'hasThreeStep', label: '3-Step', remove: () => { form.hasThreeStep = false } })
  }

  return chips
})

const hasAnyWhitespace = (value: string) => /\s/.test(value)
const isEnglishNameInput = (value: string) => /^[A-Za-z\s]*$/.test(value)
const customerNameError = computed(() => customerNameSpacingError.value)
const normalizeCustomerNameInput = () => {
  if (!hasAnyWhitespace(form.customerName) || isEnglishNameInput(form.customerName)) return

  form.customerName = form.customerName.replace(/\s+/g, '')
  customerNameSpacingError.value = '공백은 입력할 수 없습니다.'

  if (customerNameSpacingErrorTimer) clearTimeout(customerNameSpacingErrorTimer)
  customerNameSpacingErrorTimer = setTimeout(() => {
    customerNameSpacingError.value = ''
    customerNameSpacingErrorTimer = undefined
  }, 2_000)
}
const submit = async () => {
  await nextTick()

  if (customerNameError.value) return

  emit('search', {
    customerName: form.customerName || undefined,
    age: form.age === '' ? undefined : Number(form.age),
    gender: form.gender || undefined,
    customerStageCode: form.customerStageCode || undefined,
    consultStatusCode: form.consultStatusCodes.length ? [...form.consultStatusCodes] : undefined,
    contractStatusCode: form.contractStatusCodes.length ? [...form.contractStatusCodes] : undefined,
    hasReport: form.hasReport || undefined,
    hasThreeStep: form.hasThreeStep || undefined,
  })
}

const reset = () => {
  Object.assign(form, initialForm())
  submit()
}

// 프리셋 적용 시 기존 입력값은 지우고 해당 프리셋 조건만 남김
const applyQuickFilter = (filters: SalesSearchFormFilters) => {
  Object.assign(form, initialForm(), filters)
  submit()
}

// 칩 삭제 후 즉시 검색을 다시 실행해 화면과 필터 상태를 맞춤
const removeFilter = (remove: () => void) => {
  remove()
  submit()
}

onBeforeUnmount(() => {
  if (customerNameSpacingErrorTimer) clearTimeout(customerNameSpacingErrorTimer)
})

</script>

<template>
  <form class="card sales-search" @submit.prevent="submit">
    <div class="sales-search__header">
      <h3 class="sales-section-title">검색조건</h3>
      <div class="sales-search__quick" aria-label="빠른 필터">
        <button
          v-for="quickFilter in quickFilters"
          :key="quickFilter.label"
          class="sales-search__quick-button"
          type="button"
          :aria-label="`${quickFilter.label}: ${quickFilter.description}`"
          :title="quickFilter.description"
          @click="applyQuickFilter(quickFilter.filters)"
        >
          {{ quickFilter.label }}
        </button>
      </div>
    </div>

    <div class="sales-search__row sales-search__row--top">
      <label class="sales-search__field" :class="{ 'sales-search__field--invalid': customerNameError }">
        <span>고객 이름</span>
        <input
          v-model="form.customerName"
          class="sales-search__input"
          :class="{ 'sales-search__input--invalid': customerNameError }"
          :aria-invalid="Boolean(customerNameError)"
          aria-describedby="sales-customer-name-error"
          placeholder="이름을 입력하세요."
          @input="normalizeCustomerNameInput"
        />
        <p
          v-if="customerNameError"
          id="sales-customer-name-error"
          class="sales-search__field-error"
          role="alert"
        >
          {{ customerNameError }}
        </p>
      </label>

      <label class="sales-search__field sales-search__field--age">
        <span>나이</span>
        <input v-model="form.age" class="sales-search__input" min="0" type="number" placeholder="나이를 입력하세요." />
      </label>

      <fieldset class="sales-search__gender">
        <legend>성별</legend>
        <label><input v-model="form.gender" type="radio" value="Male" @change="submit" /> 남</label>
        <label><input v-model="form.gender" type="radio" value="Female" @change="submit" /> 여</label>
      </fieldset>

      <fieldset class="sales-search__stage">
        <legend>고객 유형</legend>
        <label><input v-model="form.customerStageCode" type="radio" value="" @change="submit" /> 전체</label>
        <label><input v-model="form.customerStageCode" type="radio" value="01" @change="submit" /> 잠재</label>
        <label><input v-model="form.customerStageCode" type="radio" value="02" @change="submit" /> 통합</label>
      </fieldset>
    </div>

    <div class="sales-search__row sales-search__row--filters">
      <span class="sales-search__label">상담 현황</span>
      <div class="sales-search__options">
        <label class="sales-search__toggle">
          <input v-model="form.consultStatusCodes" type="checkbox" value="01" @change="submit" />
          <span>미상담</span>
        </label>
        <label class="sales-search__toggle">
          <input v-model="form.consultStatusCodes" type="checkbox" value="02" @change="submit" />
          <span>상담중</span>
        </label>
      </div>

      <span class="sales-search__label sales-search__label--contract">계약 현황</span>
      <div class="sales-search__options">
        <label class="sales-search__toggle">
          <input v-model="form.contractStatusCodes" type="checkbox" value="01" @change="submit" />
          <span>설계중</span>
        </label>
        <label class="sales-search__toggle">
          <input v-model="form.contractStatusCodes" type="checkbox" value="02" @change="submit" />
          <span>설계완료</span>
        </label>
        <label class="sales-search__toggle">
          <input v-model="form.contractStatusCodes" type="checkbox" value="03" @change="submit" />
          <span>청약중</span>
        </label>
        <label class="sales-search__toggle">
          <input v-model="form.contractStatusCodes" type="checkbox" value="04" @change="submit" />
          <span>청약완료</span>
        </label>
        <label class="sales-search__toggle">
          <input v-model="form.contractStatusCodes" type="checkbox" value="06" @change="submit" />
          <span>수납완료</span>
        </label>
        <label class="sales-search__toggle">
          <input v-model="form.hasReport" type="checkbox" @change="submit" />
          <span>리포트</span>
        </label>
        <label class="sales-search__toggle">
          <input v-model="form.hasThreeStep" type="checkbox" @change="submit" />
          <span>3-Step</span>
        </label>
      </div>

      <div class="sales-search__actions">
        <button class="button button-primary sales-search__button" type="submit">조회</button>
        <button class="button button-primary sales-search__button" type="button" @click="reset">초기화</button>
      </div>
    </div>

    <div v-if="appliedFilterChips.length" class="sales-search__applied" aria-label="적용된 필터">
      <span class="sales-search__applied-label">적용 필터</span>
      <button
        v-for="chip in appliedFilterChips"
        :key="chip.key"
        class="sales-search__chip"
        type="button"
        @click="removeFilter(chip.remove)"
      >
        {{ chip.label }}
        <span aria-hidden="true">x</span>
      </button>
    </div>
  </form>
</template>

<style scoped>
.sales-search {
  margin-bottom: 7px;
  border: 1px solid #e3e8f0;
  box-shadow: none;
  padding: 10px 14px 10px;
}

.sales-search__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 5px;
}

.sales-section-title {
  margin: 0;
  color: #263142;
  font-size: 14px;
  font-weight: 900;
  letter-spacing: 0;
}

.sales-search__quick {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 5px;
}

.sales-search__quick-button {
  height: 21px;
  border: 1px solid #dfe5ee;
  border-radius: 5px;
  background: #f7f9fc;
  color: #4f5d70;
  padding: 0 8px;
  font-size: 10px;
  font-weight: 800;
}

.sales-search__quick-button:hover {
  border-color: color-mix(in srgb, var(--color-primary) 34%, white);
  background: color-mix(in srgb, var(--color-primary) 10%, white);
  color: var(--color-primary);
}

.sales-search__row {
  display: grid;
  align-items: center;
  min-height: 24px;
  color: #394252;
  font-size: 11px;
  font-weight: 700;
}

.sales-search__row--top {
  grid-template-columns: minmax(210px, 260px) minmax(150px, 180px) minmax(130px, 160px) minmax(190px, 240px);
  column-gap: 16px;
  margin-bottom: 10px;
}

.sales-search__input {
  width: 100%;
  height: 23px;
  border: 1px solid #d9e0ea;
  border-radius: 5px;
  background: #ffffff;
  padding: 0 8px;
  color: var(--color-text);
  font-size: 11px;
  outline: none;
}

.sales-search__input:focus {
  border-color: var(--color-primary);
  background: #ffffff;
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-primary) 14%, transparent);
}

.sales-search__input--invalid,
.sales-search__input--invalid:focus {
  border-color: #d85a65;
  box-shadow: 0 0 0 3px rgb(216 90 101 / 14%);
}

.sales-search__input::placeholder {
  color: #a6afbd;
}

.sales-search__field-error {
  grid-column: 2;
  margin: -3px 0 0;
  color: #d85a65;
  font-size: 10px;
  font-weight: 800;
  line-height: 1.2;
}

.sales-search__gender,
.sales-search__stage {
  display: flex;
  align-items: center;
  gap: 6px;
  border: 0;
  margin: 0;
  padding: 0;
  white-space: nowrap;
}

.sales-search__gender legend,
.sales-search__stage legend {
  float: left;
  margin-right: 8px;
  font-weight: 700;
  white-space: nowrap;
}

.sales-search__label {
  white-space: nowrap;
}

.sales-search__label--contract {
  margin-left: 26px;
}

.sales-search__row--filters {
  grid-template-columns: max-content auto max-content minmax(0, 1fr) auto;
  column-gap: 12px;
  border-top: 1px solid #edf1f6;
  padding-top: 10px;
}

.sales-search__applied {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 5px;
  border-top: 1px solid #edf1f6;
  padding-top: 5px;
}

.sales-search__applied-label {
  margin-right: 3px;
  color: #7c8797;
  font-size: 10px;
  font-weight: 800;
}

.sales-search__chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 20px;
  border: 1px solid color-mix(in srgb, var(--color-primary) 26%, white);
  border-radius: 999px;
  background: color-mix(in srgb, var(--color-primary) 8%, white);
  color: var(--color-primary);
  padding: 0 8px;
  font-size: 10px;
  font-weight: 800;
}

.sales-search__chip span {
  color: #7d8bb2;
  font-size: 11px;
  line-height: 1;
}

.sales-search label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.sales-search .sales-search__field {
  display: grid;
  grid-template-columns: 50px minmax(0, 1fr);
  align-items: center;
  gap: 6px;
  white-space: normal;
}

.sales-search .sales-search__field--invalid {
  align-items: start;
}

.sales-search__field--age {
  margin-left: 0;
}

.sales-search__options {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 5px 6px;
  min-width: 0;
}

.sales-search input[type='radio'] {
  width: 13px;
  height: 13px;
  flex: 0 0 13px;
  appearance: none;
  border: 1px solid #cfd8e5;
  border-radius: 50%;
  background: #ffffff;
  margin: 0;
  box-shadow: inset 0 0 0 3px #ffffff;
  transition:
    border-color 120ms ease,
    background-color 120ms ease,
    box-shadow 120ms ease;
}

.sales-search input[type='radio']:checked {
  border-color: #f6a76b;
  background: #f37021;
  box-shadow: inset 0 0 0 3px #ffffff;
}

.sales-search input[type='radio']:focus-visible {
  outline: 2px solid rgb(243 112 33 / 18%);
  outline-offset: 2px;
}

.sales-search__gender label,
.sales-search__stage label {
  height: 21px;
  border-radius: 999px;
  padding: 0 3px;
  color: #4f5d70;
  line-height: 1;
}

.sales-search__toggle {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.sales-search__toggle input {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  opacity: 0;
  pointer-events: none;
}

.sales-search__toggle span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 22px;
  border: 1px solid #d6dee9;
  border-radius: 999px;
  background: #fbfcfe;
  color: #526173;
  padding: 0 9px;
  font-size: 10px;
  font-weight: 850;
  line-height: 1;
  transition:
    border-color 120ms ease,
    background-color 120ms ease,
    color 120ms ease,
    box-shadow 120ms ease;
}

.sales-search__toggle span::before {
  width: 0;
  overflow: hidden;
  color: inherit;
  content: '✓';
  font-size: 10px;
  font-weight: 900;
  transition:
    width 120ms ease,
    margin-right 120ms ease;
}

.sales-search__toggle input:checked + span {
  border-color: #f6b37d;
  background: #fff3ea;
  color: #d95f16;
  box-shadow: inset 0 0 0 1px rgb(243 112 33 / 8%);
}

.sales-search__toggle input:checked + span::before {
  width: 10px;
  margin-right: 3px;
}

.sales-search__toggle input:focus-visible + span {
  outline: 2px solid rgb(243 112 33 / 18%);
  outline-offset: 2px;
}

.sales-search__actions {
  display: flex;
  gap: 6px;
  margin-left: auto;
}

.sales-search__button {
  min-width: 52px;
  min-height: 23px;
  border-radius: 5px;
  padding: 0 10px;
  font-size: 11px;
}

@media (max-width: 1100px) {
  .sales-search__header {
    align-items: flex-start;
    flex-direction: column;
  }

  .sales-search__quick {
    justify-content: flex-start;
  }

  .sales-search__row--top,
  .sales-search__row--filters {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .sales-search__actions {
    margin-left: 0;
  }
}
</style>


