<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import type { PotentialCustomerListItem } from '@/api/potential'

const props = defineProps<{
  customers: PotentialCustomerListItem[]
  selectedCustomerIds?: number[]
}>()

const emit = defineEmits<{
  'update:selectedCustomerIds': [customerIds: number[]]
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

const toggleCustomer = (customer: PotentialCustomerListItem) => {
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
</script>

<template>
  <div class="potential-table">
    <table>
      <thead>
        <tr>
          <th>
            <input
              ref="selectAllCheckbox"
              type="checkbox"
              :checked="isAllSelected"
              :disabled="customers.length === 0"
              aria-label="현재 잠재고객 전체 선택"
              @change="toggleAllCustomers"
            />
          </th>
          <th>고객명</th>
          <th>성별</th>
          <th>만 나이</th>
          <th>생년월일</th>
          <th>법정 대리인</th>
          <th>관계</th>
          <th>만 나이</th>
          <th>연락처</th>
        </tr>
      </thead>

      <tbody>
        <tr
          v-for="customer in customers"
          :key="customer.potentialCustomerId"
          :class="{
            'potential-table__row--selected':
              selectedCustomerIds?.includes(customer.potentialCustomerId),
            'potential-table__row--graduated': customer.isActive,
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
          <td class="potential-table__customer-name">
            {{ customer.customerName }}
          </td>
          <td>{{ genderLabel(customer.gender) }}</td>
          <td>{{ customer.age }}</td>
          <td>{{ customer.birthDate }}</td>
          <td>{{ customer.guardianName }}</td>
          <td>{{ customer.guardianRelationshipName }}</td>
          <td>{{ customer.guardianAge }}</td>
          <td>{{ customer.guardianPhone }}</td>
        </tr>

        <tr v-if="customers.length === 0">
          <td class="potential-table__empty" colspan="9">
            등록된 잠재고객이 없습니다.
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.potential-table {
  overflow-x: auto;
  overflow-y: visible;
  border: 1px solid #e1e7f0;
  border-radius: 6px;
}

.potential-table table {
  width: 100%;
  min-width: 980px;
  table-layout: fixed;
  border-collapse: collapse;
}

.potential-table th,
.potential-table td {
  height: 36px;
  border-bottom: 1px solid #edf1f6;
  padding: 0 9px;
  text-align: center;
  font-size: 11px;
  white-space: nowrap;
}

.potential-table th {
  height: 32px;
  background: #f6f8fb;
  color: #4c586b;
  font-weight: 800;
}

.potential-table tr:last-child td {
  border-bottom: 0;
}

.potential-table th:first-child,
.potential-table td:first-child {
  width: 31px;
}

.potential-table input[type='checkbox'] {
  width: 12px;
  height: 12px;
  margin: 0;
  accent-color: var(--color-primary);
}

.potential-table__row--selected {
  background: color-mix(in srgb, var(--color-primary) 10%, white);
}

.potential-table__row--graduated {
  background: #f1f3f7;
  color: #7f8999;
}

.potential-table__row--selected.potential-table__row--graduated {
  background: color-mix(in srgb, var(--color-primary) 14%, white);
}

.potential-table__customer-name {
  font-weight: 700;
}

.potential-table__empty {
  height: 80px;
  color: var(--color-text-muted);
}
</style>
