<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import {
  resolveSalesCustomerStageCode,
  resolveSalesCustomerStageName,
  type SalesCustomer,
} from '@/api/sales'
import SalesReportSendButton from '@/components/sales/SalesReportSendButton.vue'
import SalesWebformSendButton from '@/components/sales/SalesWebformSendButton.vue'

const props = defineProps<{
  customers: SalesCustomer[]
  selectedReportCustomerIds?: number[]
}>()

const emit = defineEmits<{
  'update:selectedReportCustomerIds': [customerIds: number[]]
  'send-result': [message: string, type: 'success' | 'error']
}>()

const SENDABLE_REPORT_STATUS_CODES = new Set(['01', '02', '03'])
const selectAllCheckbox = ref<HTMLInputElement | null>(null)

// 리포트 ID가 있고 발송 버튼 노출 조건을 만족하는 고객만 체크박스 선택 대상입니다.
const selectableCustomerIds = computed(() => props.customers.map((customer) => customer.customerId))

// 백엔드 상태 코드와 canSendReport 플래그를 함께 보고 발송/재발송 버튼 노출 여부를 결정합니다.
const canShowSendButton = (customer: SalesCustomer) =>
  !customer.graduated &&
  customer.hasReport &&
  Boolean(customer.reportStatusCode) &&
  SENDABLE_REPORT_STATUS_CODES.has(customer.reportStatusCode!) &&
  (customer.canSendReport || customer.reportStatusCode === '02')

const webformStatusName = (customer: SalesCustomer) =>
  customer.webformStatusName ?? '-'

const reportStatusName = (customer: SalesCustomer) => {
  if (customer.graduated) return '졸업'
  if (!customer.reportUrl) return '미생성'
  return customer.reportStatusName || '-'
}

const canOpenCustomerDetail = (customer: SalesCustomer) =>
  customer.parentId != null

const isAllSelected = computed(
  () =>
    selectableCustomerIds.value.length > 0 &&
    selectableCustomerIds.value.every((customerId) =>
      props.selectedReportCustomerIds?.includes(customerId),
    ),
)
const isPartiallySelected = computed(
  () => Boolean(props.selectedReportCustomerIds?.length) && !isAllSelected.value,
)

// 헤더 체크박스는 현재 화면에서 선택 가능한 리포트만 대상으로 전체 선택을 토글합니다.
const toggleAllCustomers = () => {
  emit('update:selectedReportCustomerIds', isAllSelected.value ? [] : selectableCustomerIds.value)
}

// 개별 행 체크박스 선택 상태를 부모의 selectedReportIds v-model로 되돌립니다.
const toggleReport = (customer: SalesCustomer) => {
  const selectedIds = props.selectedReportCustomerIds ?? []
  const nextIds = selectedIds.includes(customer.customerId)
    ? selectedIds.filter((selectedCustomerId) => selectedCustomerId !== customer.customerId)
    : [...selectedIds, customer.customerId]

  emit('update:selectedReportCustomerIds', nextIds)
}

const emitSendResult = (message: string, type: 'success' | 'error') => {
  emit('send-result', message, type)
}

// 페이지/검색 조건 변경으로 화면에서 사라진 리포트는 선택 목록에서도 제거합니다.
watch(
  () => props.customers,
  (customers) => {
    const visibleCustomerIds = new Set(customers.map((customer) => customer.customerId))
    const selectedIds = props.selectedReportCustomerIds ?? []
    const nextIds = selectedIds.filter((customerId) => visibleCustomerIds.has(customerId))

    if (nextIds.length !== selectedIds.length) {
      emit('update:selectedReportCustomerIds', nextIds)
    }
  },
)

// HTML 체크박스의 indeterminate 상태는 DOM 프로퍼티라 watch로 직접 동기화합니다.
watch(
  isPartiallySelected,
  (isIndeterminate) => {
    if (selectAllCheckbox.value) {
      selectAllCheckbox.value.indeterminate = isIndeterminate
    }
  },
  { immediate: true },
)

// 고객 성별은 API/목 데이터 표기가 섞일 수 있어 대표 표기로 정규화합니다.
const genderLabel = (gender: string) => {
  if (gender === 'MALE' || gender === 'Male' || gender === 'M' || gender === '남') return '남'
  if (gender === 'FEMALE' || gender === 'Female' || gender === 'F' || gender === '여') return '여'
  return gender
}

// 계약 상태 코드를 배지 색상 클래스에 매핑합니다.
const contractClass = (statusCode?: string) => {
  const classMap: Record<string, string> = {
    '01': 'warning',
    '02': 'blue',
    '03': 'success',
    '04': 'danger',
    '06': 'muted',
  }

  return statusCode ? classMap[statusCode] ?? 'muted' : 'danger-soft'
}

const contractStatusLabel = (customer: SalesCustomer) => {
  const labelMap: Record<string, string> = {
    '01': '설계중',
    '02': '설계완료',
    '03': '청약중',
    '04': '청약완료',
    '06': '수납완료',
  }

  return customer.contractStatusName || (customer.contractStatusCode ? labelMap[customer.contractStatusCode] : undefined) || '-'
}

// 보험 나이 변경 D-Day는 서버 값이 있으면 우선 사용하고, 없으면 날짜로 계산합니다.
const calculateAgeShiftDDay = (customer: SalesCustomer) => {
  if (typeof customer.ageIncreaseDDay === 'number') return customer.ageIncreaseDDay
  if (!customer.insuranceAgeShiftDate) return undefined

  const [year, month, day] = customer.insuranceAgeShiftDate.split('-').map(Number)
  if (!year || !month || !day) return undefined

  const today = new Date()
  const targetTime = Date.UTC(year, month - 1, day)
  const todayTime = Date.UTC(today.getFullYear(), today.getMonth(), today.getDate())

  return Math.round((targetTime - todayTime) / 86_400_000)
}

// 나이 변경이 30일 이내인 고객 행은 우선 관리 대상으로 강조합니다.
const ageShiftRowClass = (customer: SalesCustomer) => {
  const dDay = calculateAgeShiftDDay(customer)
  if (dDay === undefined || dDay < 0 || dDay > 30) return undefined
  return dDay <= 7 ? 'sales-table__row--age-shift-near' : 'sales-table__row--age-shift-warning'
}

// 행 안에 표시할 보험 나이 변경 안내 문구를 만듭니다.
const ageShiftGuide = (customer: SalesCustomer) => {
  const dDay = calculateAgeShiftDDay(customer)
  if (dDay === undefined || dDay < 0 || dDay > 30) return undefined
  if (dDay === 0) return '보험 나이 변경 D-Day'
  return dDay <= 7 ? `보험 나이 변경 임박 D-${dDay}` : `보험 나이 변경 예정 D-${dDay}`
}

const stepClass = (sortRank: number) => (sortRank === 1 ? 'danger' : 'warning')
</script>

<template>
  <div class="sales-table">
    <table>
      <thead>
        <tr>
          <th>
            <input
              ref="selectAllCheckbox"
              type="checkbox"
              :checked="isAllSelected"
              :disabled="selectableCustomerIds.length === 0"
              aria-label="현재 목록의 고객 전체 선택"
              @change="toggleAllCustomers"
            />
          </th>
          <th>고객명</th>
          <th>성별</th>
          <th>만 나이</th>
          <th>생년월일</th>
          <th>3step</th>
          <th>고객 단계</th>
          <th>계약 현황</th>
          <th>보험명</th>
          <th>피보험자</th>
          <th>웹폼 회수일</th>
          <th>웹폼 발송상태</th>
          <th>리포트 발송상태</th>
          <th>웹폼 발송</th>
          <th>리포트 발송</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="customer in customers"
          :key="customer.customerId"
          :class="ageShiftRowClass(customer)"
        >
          <td>
            <input
              type="checkbox"
              :checked="Boolean(selectedReportCustomerIds?.includes(customer.customerId))"
              :aria-label="`${customer.customerName} 선택`"
              @change="toggleReport(customer)"
            />
          </td>
          <td>
            <RouterLink
              v-if="canOpenCustomerDetail(customer)"
              class="customer-name"
              :class="{
                'customer-name--link': true,
                'customer-name--age-shift': ageShiftGuide(customer),
              }"
              :tabindex="ageShiftGuide(customer) ? 0 : undefined"
              :to="{
                name: 'user-detail',
                params: { customerId: customer.customerId },
                query: {
                  conversionStatusCode: resolveSalesCustomerStageCode(customer),
                },
              }"
            >
              {{ customer.customerName }}
              <span
                v-if="ageShiftGuide(customer)"
                class="customer-name__tooltip"
                role="tooltip"
              >
                {{ ageShiftGuide(customer) }}
              </span>
            </RouterLink>
            <span
              v-else
              class="customer-name customer-name--disabled"
              :class="{ 'customer-name--age-shift': ageShiftGuide(customer) }"
              :tabindex="ageShiftGuide(customer) ? 0 : undefined"
            >
              {{ customer.customerName }}
              <span
                v-if="ageShiftGuide(customer)"
                class="customer-name__tooltip"
                role="tooltip"
              >
                {{ ageShiftGuide(customer) }}
              </span>
            </span>
          </td>
          <td>{{ genderLabel(customer.gender) }}</td>
          <td>{{ customer.age }}</td>
          <td>{{ customer.birthDate }}</td>
          <td>
            <span
              v-if="customer.threeStepCode || customer.sortRank === 1"
              class="step-dot"
              :class="`step-dot--${stepClass(customer.sortRank)}`"
              :title="customer.threeStepName"
            ></span>
            <span v-else>-</span>
          </td>
          <td>{{ resolveSalesCustomerStageName(customer) }}</td>
          <td>
            <span class="contract-badge" :class="`contract-badge--${contractClass(customer.contractStatusCode)}`">
              {{ contractStatusLabel(customer) }}
            </span>
          </td>
          <td>{{ customer.insuranceName }}</td>
          <td>{{ customer.insuredName }}</td>
          <td>{{ customer.webformReceivedAt }}</td>
          <td class="report-status">
            <span class="report-status__badge">{{ webformStatusName(customer) }}</span>
          </td>
          <td class="report-status">
            <span
              class="report-status__badge"
              :class="{ 'report-status__badge--graduated': customer.graduated }"
            >
              {{ reportStatusName(customer) }}
            </span>
          </td>
          <td>
            <div class="sales-table__actions">
              <SalesWebformSendButton
                :customer="customer"
                @send-result="emitSendResult"
              />
            </div>
          </td>
          <td>
            <div class="sales-table__actions">
              <SalesReportSendButton
                :customer="customer"
                @send-result="emitSendResult"
              />
            </div>
          </td>
        </tr>
        <tr v-if="customers.length === 0">
          <td class="sales-table__empty" colspan="15">조회된 영업현황이 없습니다.</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.sales-table {
  overflow-x: auto;
  overflow-y: visible;
  border: 1px solid #e1e7f0;
  border-radius: 6px;
}

.sales-table table {
  width: 100%;
  min-width: 980px;
  table-layout: fixed;
  border-collapse: collapse;
}

.sales-table th,
.sales-table td {
  height: 32px;
  border-bottom: 1px solid #edf1f6;
  padding: 0 8px;
  text-align: center;
  font-size: 11px;
  white-space: nowrap;
}

.sales-table th {
  height: 30px;
  background: #f6f8fb;
  color: #4c586b;
  font-weight: 800;
}

.sales-table tr:last-child td {
  border-bottom: 0;
}

.sales-table tbody tr:hover {
  background: #fbfcff;
}

.sales-table .sales-table__empty {
  height: 80px;
  color: var(--color-text-muted);
}

.sales-table th:first-child,
.sales-table td:first-child {
  width: 31px;
}

.sales-table th:nth-last-child(2),
.sales-table td:nth-last-child(2),
.sales-table th:last-child,
.sales-table td:last-child {
  width: 78px;
}

.sales-table input[type='checkbox'] {
  width: 12px;
  height: 12px;
  margin: 0;
  accent-color: var(--color-primary);
}

.sales-table tbody tr.sales-table__row--age-shift-warning .customer-name--age-shift {
  text-decoration-color: rgb(251 146 60 / 42%);
}

.sales-table tbody tr.sales-table__row--age-shift-near .customer-name--age-shift {
  text-decoration-color: rgb(250 204 21 / 48%);
}

.sales-table tbody tr.sales-table__row--age-shift-warning .customer-name--age-shift,
.sales-table tbody tr.sales-table__row--age-shift-near .customer-name--age-shift {
  text-decoration-line: underline;
  text-decoration-skip-ink: none;
  text-decoration-thickness: 0.62em;
  text-underline-offset: -0.3em;
}

.customer-name {
  position: relative;
  display: inline-block;
  color: inherit;
  font-weight: 700;
}

.customer-name--link {
  text-decoration: none;
}

.customer-name--link:hover {
  color: var(--color-primary);
}

.customer-name--disabled {
  color: #98a2b3;
}

.customer-name--age-shift {
  outline: none;
}

.customer-name__tooltip {
  position: absolute;
  left: 50%;
  bottom: calc(100% + 7px);
  z-index: 10;
  width: max-content;
  max-width: 160px;
  visibility: hidden;
  border-radius: 6px;
  background: #273043;
  color: #ffffff;
  opacity: 0;
  padding: 5px 8px;
  font-size: 10px;
  font-weight: 600;
  line-height: 1.3;
  pointer-events: none;
  transform: translate(-50%, 3px);
  transition:
    opacity 120ms ease,
    transform 120ms ease,
    visibility 120ms ease;
}

.customer-name__tooltip::after {
  position: absolute;
  top: 100%;
  left: 50%;
  border: 4px solid transparent;
  border-top-color: #273043;
  content: '';
  transform: translateX(-50%);
}

.customer-name--age-shift:hover .customer-name__tooltip,
.customer-name--age-shift:focus .customer-name__tooltip {
  visibility: visible;
  opacity: 1;
  transform: translate(-50%, 0);
}

.step-dot {
  display: inline-block;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  box-shadow: inset 0 -1px 2px rgb(0 0 0 / 20%);
}

.step-dot--danger {
  background: #e22b2f;
}

.step-dot--warning {
  background: #ffd319;
}

.contract-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 46px;
  height: 20px;
  border-radius: 5px;
  padding: 0 8px;
  font-size: 10px;
  font-weight: 800;
}

.contract-badge--danger-soft {
  background: #f5e8ea;
  color: #9f4b58;
}

.contract-badge--muted {
  background: #edf1f5;
  color: #657386;
}

.contract-badge--warning {
  background: #fff4d7;
  color: #8a6412;
}

.contract-badge--danger {
  background: #ffe4e6;
  color: #b23b49;
}

.contract-badge--success {
  background: #ddf7e7;
  color: #24723b;
}

.contract-badge--blue {
  background: #e3efff;
  color: #285fba;
}

.report-button {
  min-width: 54px;
  height: 24px;
  border: 0;
  border-radius: 5px;
  background: var(--color-primary);
  color: #ffffff;
  padding: 0 10px;
  font-size: 10px;
  font-weight: 800;
}

.sales-table__actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.report-button--webform {
  min-width: 54px;
}

.report-button--resend {
  border: 1px solid var(--color-primary);
  background: color-mix(in srgb, var(--color-primary) 8%, white);
  color: var(--color-primary);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--color-primary) 10%, transparent);
}

.report-button--resend:hover:not(:disabled) {
  background: color-mix(in srgb, var(--color-primary) 14%, white);
  color: var(--color-primary);
}

.report-button:hover:not(:disabled) {
  background: color-mix(in srgb, var(--color-primary) 84%, black);
}

.report-status {
  font-weight: 400;
}

.report-status__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 44px;
  height: 20px;
  border-radius: 999px;
  background: #f1f4f8;
  color: #5f6b7a;
  padding: 0 8px;
  font-size: 10px;
  font-weight: 800;
}

.report-status__badge--graduated {
  background: #fff4d7;
  color: #b45309;
  font-weight: 800;
}
</style>
