<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import type { ContactCustomer } from '@/api/dashboard'

const props = defineProps<{
  customers: ContactCustomer[]
  selectedCustomerIds?: number[]
  sendingWebFormIds?: number[]
  sendingReportIds?: number[]
}>()

const emit = defineEmits<{
  'update:selectedCustomerIds': [customerIds: number[]]
  sendWebForm: [customer: ContactCustomer]
  sendReport: [customer: ContactCustomer]
  sendBulkWebForm: [] 
  sendBulkReport: [] 
}>()

const selectAllCheckbox = ref<HTMLInputElement | null>(null)

const customerIds = computed(() =>
  props.customers.map((customer) => customer.potentialCustomerId),
)

const isAllSelected = computed(
  () =>
    customerIds.value.length > 0 &&
    customerIds.value.every((customerId) =>
      props.selectedCustomerIds?.includes(customerId),
    ),
)

const isPartiallySelected = computed(
  () => Boolean(props.selectedCustomerIds?.length) && !isAllSelected.value,
)

const toggleAllCustomers = () => {
  emit('update:selectedCustomerIds', isAllSelected.value ? [] : customerIds.value)
}

const toggleCustomer = (customer: ContactCustomer) => {
  const selectedIds = props.selectedCustomerIds ?? []
  const customerId = customer.potentialCustomerId

  const nextIds = selectedIds.includes(customerId)
    ? selectedIds.filter((selectedCustomerId) => selectedCustomerId !== customerId)
    : [...selectedIds, customerId]

  emit('update:selectedCustomerIds', nextIds)
}

watch(
  () => props.customers,
  (customers) => {
    const visibleCustomerIds = new Set(
      customers.map((customer) => customer.potentialCustomerId),
    )

    const selectedIds = props.selectedCustomerIds ?? []
    const nextIds = selectedIds.filter((customerId) =>
      visibleCustomerIds.has(customerId),
    )

    if (nextIds.length !== selectedIds.length) {
      emit('update:selectedCustomerIds', nextIds)
    }
  },
)

watch(
  isPartiallySelected,
  (isIndeterminate) => {
    if (selectAllCheckbox.value) {
      selectAllCheckbox.value.indeterminate = isIndeterminate
    }
  },
  { immediate: true },
)

const genderLabel = (gender: string) => {
  if (gender === 'MALE') return '남'
  if (gender === 'FEMALE') return '여'
  return gender
}

const badgeClass = (badgeColor?: string) => {
  if (badgeColor === 'RED') return 'contact-table__badge--red'
  if (badgeColor === 'ORANGE') return 'contact-table__badge--orange'
  if (badgeColor === 'YELLOW') return 'contact-table__badge--yellow'
  return 'contact-table__badge--muted'
}

const contactReasonText = (customer: ContactCustomer) => {
  if (!customer.contactReason) return ''

  return customer.contactReason
    .split('|')
    .map((reason) => reason.trim())
    .filter(Boolean)
    .join(' · ')
}

const isWebFormSending = (customer: ContactCustomer) =>
  props.sendingWebFormIds?.includes(customer.potentialCustomerId) ?? false

const isReportSending = (customer: ContactCustomer) =>
  (props.sendingReportIds?.includes(customer.potentialCustomerId) ?? false)

const isWebFormSent = (customer: ContactCustomer) => {
  return customer.webFormStatusName === '발송완료'
}

const isReportSent = (customer: ContactCustomer) => {
  return (
    customer.reportSendStatusCode === '02' ||
    customer.reportSendStatusName === '발송완료' ||
    customer.reportSendStatusName === '발송성공'
  )
}

const webFormButtonLabel = (customer: ContactCustomer) => {
  if (isWebFormSending(customer)) return '발송 중'

  if (customer.webFormStatusName === '미발송') {
    return '발송'
  }

  if (customer.webFormStatusName === '회수/만료') {
    return '회수완료'
  }

  return customer.webFormStatusName || '발송'
}

const reportButtonLabel = (customer: ContactCustomer) => {
  if (isReportSending(customer)) return '발송 중'
  return customer.reportSendStatusName || '발송'
}
</script>

<template>
  <div class="contact-table-wrapper">
    <div class="contact-table-wrapper__actions">
      <button
        class="contact-table__bulk-button"
        type="button"
        @click="emit('sendBulkWebForm')"
      >
        웹폼 발송
      </button>

      <button
        class="contact-table__bulk-button"
        type="button"
        @click="emit('sendBulkReport')"
      >
        리포트 발송
      </button>
    </div>

    <div class="contact-table">
      <table>
        <thead>
          <tr>
            <th>
              <input
                ref="selectAllCheckbox"
                type="checkbox"
                :checked="isAllSelected"
                :disabled="customers.length === 0"
                aria-label="오늘 연락 고객 전체 선택"
                @change="toggleAllCustomers"
              />
            </th>
            <th>고객명</th>
            <th>성별</th>
            <th>만 나이</th>
            <th>생년월일</th>
            <th>상령일</th>
            <th>3step</th>
            <th>상담 여부</th>
            <th>웹폼 발송</th>
            <th>리포트 발송</th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="customer in customers"
            :key="customer.actionItemId"
            :class="{
              'contact-table__row--selected':
                selectedCustomerIds?.includes(customer.potentialCustomerId),
            }"
          >
            <td>
              <input
                type="checkbox"
                :checked="selectedCustomerIds?.includes(customer.potentialCustomerId)"
                :aria-label="`${customer.customerName} 선택`"
                @change="toggleCustomer(customer)"
              />
            </td>

            <td>
              <div class="contact-table__customer-name-wrap">
                <RouterLink
                  class="contact-table__customer-name"
                  :to="{
                    name: 'user-detail',
                    params: { customerId: customer.potentialCustomerId },
                    query: { 
                      conversionStatusCode: '01',
                      from: 'dashboard',
                    },
                  }"
                >
                  {{ customer.customerName }}
                </RouterLink>

                <span
                  v-if="contactReasonText(customer)"
                  class="contact-table__reason-tooltip"
                >
                    {{ contactReasonText(customer) }}
                </span>
              </div>
            </td>

            <td>{{ genderLabel(customer.gender) }}</td>
            <td>{{ customer.age }}</td>
            <td>{{ customer.birthDate }}</td>

            <td
              class="contact-table__age-label"
              :class="{ 'contact-table__age-label--today': customer.ageChangeLabel === 'D-DAY' }"
            >
              {{ customer.ageChangeLabel || '-' }}
            </td>

            <td>
              <span
                v-if="customer.badgeName"
                class="contact-table__badge"
                :class="badgeClass(customer.badgeColor)"
                :title="customer.badgeName"
              ></span>
              <span v-else>-</span>
            </td>

            <td>
              <span
                class="contact-table__status"
                :class="{
                  'contact-table__status--waiting':
                    customer.consultStatusName === '미상담',
            
                  'contact-table__status--progress':
                    customer.consultStatusName === '상담중',
            
                  'contact-table__status--complete':
                    customer.consultStatusName === '상담완료',
                }"
              >
                {{ customer.consultStatusName }}
              </span>
            </td>

            <td>
              <button
                class="contact-table__button"
                type="button"
                :disabled="!customer.webFormSendEnabled || isWebFormSending(customer)"
                :class="{
                  'contact-table__button--sent': isWebFormSent(customer), 
                  'contact-table__button--disabled':
                    !customer.webFormSendEnabled && !isWebFormSent(customer), 
                }"
                @click="emit('sendWebForm', customer)"
              >
                {{ webFormButtonLabel(customer) }}
              </button>
            </td>

            <td>
              <button
                class="contact-table__button"
                type="button"
                :disabled="!customer.reportSendEnabled || isReportSending(customer)"
                :class="{
                  'contact-table__button--sent': isReportSent(customer), 
                  'contact-table__button--disabled':
                    !customer.reportSendEnabled && !isReportSent(customer), 
                }"
                @click="emit('sendReport', customer)"
              >
                {{ reportButtonLabel(customer) }}
              </button>
            </td>
          </tr>

          <tr v-if="customers.length === 0">
            <td class="contact-table__empty" colspan="10">
              오늘 연락할 고객이 없습니다.
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.contact-table-wrapper {
  position: relative;
}

.contact-table-wrapper__actions {
  position: absolute;
  top: -34px;
  right: 12px;
  display: flex;
  gap: 8px;
}

.contact-table__bulk-button {
  min-width: 64px;
  height: 24px;
  border: 0;
  border-radius: 999px;
  background: #4e63e6;
  color: #ffffff;
  padding: 0 10px;
  font-size: 10px;
  font-weight: 800;
  cursor: pointer;
}

.contact-table__bulk-button:hover {
  background: #4055d4;
}

.contact-table {
  overflow-x: auto;
  overflow-y: visible;
  border: 1px solid #e1e7f0;
  border-radius: 6px;
}

.contact-table table {
  width: 100%;
  min-width:980px;
  table-layout: fixed;
  border-collapse: collapse;
}

.contact-table th,
.contact-table td {
  height: 34px;
  border-bottom: 1px solid #edf1f6;
  padding: 0 8px;
  text-align: center;
  font-size: 11px;
  white-space: nowrap;
}

.contact-table th {
  height: 32px;
  background: #f6f8fb;
  color: #4c586b;
  font-weight: 800;
}

.contact-table tr:last-child td {
  border-bottom: 0;
}

.contact-table th:first-child,
.contact-table td:first-child {
  width: 31px;
}

.contact-table input[type='checkbox'] {
  width: 12px;
  height: 12px;
  margin: 0;
  accent-color: var(--color-primary);
}

.contact-table__row--selected {
  background: #f1f3f7;
}

.contact-table__customer-name-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.contact-table__customer-name {
  color: inherit;
  font-weight: 700;
}

.contact-table__customer-name:hover {
  color: var(--color-primary);
  text-decoration: underline;
}

.contact-table__reason-tooltip {
  position: absolute;
  left: 50%;
  bottom: calc(100% + 6px);
  z-index: 40;
  visibility: hidden;
  opacity: 0;
  max-width: 260px;
  border-radius: 6px;
  background: #263142;
  color: #ffffff;
  padding: 6px 8px;
  font-size: 10px;
  font-weight: 700;
  line-height: 1;
  white-space: nowrap;
  transform: translate(-50%, 4px);
  transition:
    opacity 120ms ease,
    transform 120ms ease,
    visibility 120ms ease;
  pointer-events: none;
}

.contact-table__reason-tooltip::after {
  position: absolute;
  top: 100%;
  left: 50%;
  border: 4px solid transparent;
  border-top-color: #263142;
  content: '';
  transform: translateX(-50%);
}

.contact-table__customer-name-wrap:hover .contact-table__reason-tooltip {
  visibility: visible;
  opacity: 1;
  transform: translate(-50%, 0);
}

.contact-table__age-label {
  font-weight: 800;
}

.contact-table__age-label--today {
  color: #ff0000;
}

.contact-table__badge {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  box-shadow: inset 0 -1px 2px rgb(0 0 0 / 20%);
}

.contact-table__badge--red {
  background: #e22b2f;
}

.contact-table__badge--orange {
  background: #f59e0b;
}

.contact-table__badge--yellow {
  background: #ffd319;
}

.contact-table__badge--muted {
  background: #c5cad3;
}

.contact-table__status {
  display: inline-flex;
  min-width: 46px;
  height: 20px;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #f1f3f7;
  color: #4c586b;
  padding: 0 8px;
  font-size: 10px;
  font-weight: 800;
}

.contact-table__button {
  min-width: 54px;
  height: 24px;
  border: 0;
  border-radius: 999px;
  background: #4e63e6;
  color: #ffffff;
  padding: 0 10px;
  font-size: 10px;
  font-weight: 800;
  cursor: pointer;
}

.contact-table__button:hover:not(:disabled) {
  background: #4055d4;
}

.contact-table__button--disabled,
.contact-table__button:disabled {
  background: #c5cad3;
  color: #ffffff;
  cursor: default;
}

.contact-table__button--sent,
.contact-table__button--sent:disabled {
  border: 1px solid #4e63e6;
  background: #e5e7eb;
  color: #4e63e6;
  cursor: default;
}

.contact-table__empty {
  height: 80px;
  color: var(--color-text-muted);
}

.contact-table__status--waiting {
  background: #fff4d7;
  color: #8a6412;
}

.contact-table__status--progress {
  background: #e3efff;
  color: #285fba;
}

.contact-table__status--complete {
  background: #ddf7e7;
  color: #24723b;
}

</style>
