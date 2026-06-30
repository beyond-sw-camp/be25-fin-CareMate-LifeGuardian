<script setup lang="ts">
import axios from 'axios'
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

import AppHeader from '../../components/common/Header.vue'
import AppSidebar from '../../components/common/Sidebar.vue'
import ChildRegisterModal from '../../components/potential/ChildRegisterModal.vue'
import ParentSelectModal from '../../components/potential/ParentSelectModal.vue'
import PotentialTable from '../../components/potential/PotentialTable.vue'
import SalesPagination from '../../components/sales/SalesPagination.vue'
import {
  deletePotentialCustomer,
  getPotentialCustomers,
  type ParentCustomerSearchResponse,
  type PotentialCustomerCreateResponse,
  type PotentialCustomerListItem,
} from '@/api/potential'

const potentialCustomers = ref<PotentialCustomerListItem[]>([])
const selectedCustomerIds = ref<number[]>([])
const isLoading = ref(false)
const errorMessage = ref('')
const noticeMessage = ref('')
const noticeMessageType = ref<'success' | 'error'>('success')
const isDeleting = ref(false)

const isParentSelectModalOpen = ref(false)
const isChildRegisterModalOpen = ref(false)
const selectedParent = ref<ParentCustomerSearchResponse | null>(null)

const POTENTIAL_PAGE_SIZE = 13
const currentPage = ref(1)

const searchKeyword = ref('')

const filteredPotentialCustomers = computed(() => {
  const keyword = searchKeyword.value.trim()

  if (!keyword) {
    return potentialCustomers.value
  }

  return potentialCustomers.value.filter((customer) =>
    customer.customerName.includes(keyword),
  )
})

const totalPages = computed(() =>
  Math.ceil(filteredPotentialCustomers.value.length / POTENTIAL_PAGE_SIZE),
)

const displayedPotentialCustomers = computed(() => {
  const start = (currentPage.value - 1) * POTENTIAL_PAGE_SIZE
  const end = start + POTENTIAL_PAGE_SIZE

  return filteredPotentialCustomers.value.slice(start, end)
})

watch(searchKeyword, () => {
  currentPage.value = 1
  selectedCustomerIds.value = []
})

const getErrorMessage = (
  error: unknown,
  fallbackMessage = '잠재고객 목록을 불러오지 못했습니다.',
) => {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? fallbackMessage
  }

  return fallbackMessage
}

const showNoticeMessage = (message: string, type: 'success' | 'error') => {
  noticeMessage.value = message
  noticeMessageType.value = type
}

const loadPotentialCustomers = async () => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    potentialCustomers.value = await getPotentialCustomers()
    currentPage.value = 1
  } catch (error) {
    potentialCustomers.value = []
    currentPage.value = 1
    errorMessage.value = getErrorMessage(error)
  } finally {
    isLoading.value = false
  }
}

const handleDeleteSelectedCustomers = async () => {
  if (selectedCustomerIds.value.length === 0 || isDeleting.value) return

  if (!window.confirm(`선택한 ${selectedCustomerIds.value.length}명의 잠재고객을 삭제하시겠습니까?`)) {
    return
  }

  isDeleting.value = true
  noticeMessage.value = ''

  try {
    await Promise.all(
      selectedCustomerIds.value.map((customerId) =>
        deletePotentialCustomer(customerId),
      ),
    )

    showNoticeMessage('선택한 잠재고객이 삭제되었습니다.', 'success')
    selectedCustomerIds.value = []
    await loadPotentialCustomers()
  } catch (error) {
    showNoticeMessage(
      getErrorMessage(error, '잠재고객 삭제에 실패했습니다.'),
      'error',
    )
  } finally {
    isDeleting.value = false
  }
}

const handleOpenRegisterModal = () => {
  selectedParent.value = null
  isChildRegisterModalOpen.value = false
  isParentSelectModalOpen.value = true
}

const handleCloseParentSelectModal = () => {
  isParentSelectModalOpen.value = false
}

const handleParentSelectNext = (parent: ParentCustomerSearchResponse) => {
  selectedParent.value = parent
  isParentSelectModalOpen.value = false
  isChildRegisterModalOpen.value = true
}

const handleCloseChildRegisterModal = () => {
  isChildRegisterModalOpen.value = false
}

const handleBackToParentSelectModal = () => {
  isChildRegisterModalOpen.value = false
  isParentSelectModalOpen.value = true
}

const handleChildRegistered = async (
  customer: PotentialCustomerCreateResponse,
) => {
  isChildRegisterModalOpen.value = false
  selectedParent.value = null

  showNoticeMessage(
    `${customer.name} 잠재고객이 등록되었습니다.`,
    'success',
  )

  await loadPotentialCustomers()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  selectedCustomerIds.value = []
}

const BASE_PAGE_WIDTH = 1240
const scale = ref(1)

const updateScale = () => {
  const sidebarWidth = 208
  const horizontalPadding = 50
  const availableWidth = window.innerWidth - sidebarWidth - horizontalPadding

  scale.value = Math.min(1, availableWidth / BASE_PAGE_WIDTH)
}

onMounted(() => {
  void loadPotentialCustomers()

  updateScale()
  window.addEventListener('resize', updateScale)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateScale)
})
</script>

<template>
  <div class="app-shell potential-page">
    <AppSidebar active-label="잠재고객 관리" />

    <main class="app-main potential-page__main">
      <div class="potential-page__header">
        <AppHeader title="잠재고객 관리" />
      </div>
      <section class="card potential-list">
        <div class="potential-list__header">
          <div class="potential-list__title">
            <h3 class="potential-section-title">
              목록
              <span class="potential-list__count">
                총 {{ filteredPotentialCustomers.length }}건
              </span>
            </h3>
          </div>

          <div class="potential-list__toolbar">
            <section class="potential-search">
              <label class="potential-search__field">
                <span>고객명 검색</span>
                <input
                  v-model="searchKeyword"
                  class="potential-search__input"
                  type="text"
                  placeholder="고객명을 입력하세요."
                />
              </label>
            </section>
            
            <div class="potential-list__selected-actions">
              <button
                class="potential-button potential-button--register"
                type="button"
                @click="handleOpenRegisterModal"
              >
                등록
              </button>
              <button
                class="potential-button potential-button--danger"
                type="button"
                :disabled="selectedCustomerIds.length === 0 || isDeleting"
                @click="handleDeleteSelectedCustomers"
              >
                {{ isDeleting ? '삭제 중' : '삭제' }}
              </button>
            </div>
          </div>
        </div>
        <p
          v-if="noticeMessage"
          class="potential-list__notice"
          :class="`potential-list__notice--${noticeMessageType}`"
          role="status"
        >
          {{ noticeMessage }}
        </p>
        <p
          v-if="errorMessage"
          class="potential-list__message potential-list__message--error"
        >
          {{ errorMessage }}
        </p>
        <p v-else-if="isLoading" class="potential-list__message">
          불러오는 중...
        </p>
        <div v-else class="potential-list__table-area">
          <PotentialTable
            v-model:selected-customer-ids="selectedCustomerIds"
            :customers="displayedPotentialCustomers"
          />
        </div>
        <div class="potential-list__footer">
          <SalesPagination
            :current-page="currentPage"
            :total-pages="totalPages"
            @change="handlePageChange"
          />
        </div>
      </section>
    </main>

    <ParentSelectModal
      v-if="isParentSelectModalOpen"
      @close="handleCloseParentSelectModal"
      @next="handleParentSelectNext"
    />

    <ChildRegisterModal
      v-if="isChildRegisterModalOpen && selectedParent"
      :parent="selectedParent"
      @close="handleCloseChildRegisterModal"
      @back="handleBackToParentSelectModal"
      @registered="handleChildRegistered"
    />
  </div>
</template>

<style scoped>
.potential-page__main {
  padding: 6px 26px 8px 24px;
  overflow-x: hidden;
}

.potential-page__header {
  padding-top: 10px;
}

.potential-search {
  display: flex;
  align-items: center;
  min-width: 0;
}

.potential-search__field {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.potential-search__field span {
  color: #263142;
  font-size: 13px;
  font-weight: 800;
  white-space: nowrap;
}

.potential-search__input {
  width: 200px;
  max-width: 100%;
  height: 28px;
  border: 1px solid #d7dde7;
  border-radius: 6px;
  background: #ffffff;
  padding: 0 11px;
  color: #172033;
  font-size: 12px;
  outline: none;
}

.potential-search__input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-primary) 14%, transparent);
}

.potential-list {
  display: flex;
  min-height: 500px;
  flex-direction: column;
  border: 1px solid #e3e8f0;
  box-shadow: none;
  padding: 2px 14px 12px;
  overflow: visible;
  margin-top: -12px;
}

.potential-list__header {
  display: grid;
  gap: 10px;
  margin-bottom: 12px;
}

.potential-list__toolbar {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.potential-section-title {
  margin: 10px 0 0 5px;
  color: #263142;
  font-size: 20px;
  font-weight: 900;
}

.potential-list__count {
  margin-left: 5px;
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 700;
}

.potential-list__selected-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
  flex-shrink: 0;
}

.potential-button {
  min-width: 52px;
  height: 26px;
  border: 0;
  border-radius: 5px;
  padding: 0 10px;
  font-size: 10px;
  font-weight: 800;
  cursor: pointer;
}

.potential-button--danger {
  border: 1px solid #d85a65;
  background: #d85a65;
  color: #ffffff;
}

.potential-button:disabled {
  background: #c5cad3;
  color: #ffffff;
  cursor: default;
}

.potential-button--danger:disabled {
  background: #ffffff;
  color: #d85a65;
  border: 1px solid #d85a65;
  cursor: default;
  opacity: 1;
}

.potential-list__notice {
  margin: 0 0 7px;
  border-radius: 6px;
  padding: 7px 9px;
  font-size: 11px;
  font-weight: 700;
}

.potential-list__notice--success {
  background: #eaf8ee;
  color: #24723b;
}

.potential-list__notice--error {
  background: #fff0f1;
  color: #c43e4b;
}

.potential-list__message {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 110px;
  margin: 0;
  color: var(--color-text-muted);
  font-size: 12px;
}

.potential-list__message--error {
  color: #d85a65;
}

.potential-list__table-area {
  flex: 1;
  overflow: auto;
}

.potential-list__footer {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}

.potential-button--register {
  border: 1.5px solid var(--color-primary);
  background: #ffffff;
  color: var(--color-primary);
}

.potential-button--register:hover {
  background: color-mix(in srgb, var(--color-primary) 8%, white);
}

@media (max-width: 760px) {
  .potential-list__toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .potential-search__field {
    width: 100%;
  }

  .potential-search__input {
    width: 100%;
  }

  .potential-list__selected-actions {
    justify-content: flex-end;
    margin-left: 0;
  }
}
</style>
