<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'
import {
  getReportHistory,
  type ReportHistoryItem,
  type ReportHistorySendItemType,
  type ReportHistorySendStatus,
  type ReportHistorySendType,
} from '@/api/reportHistory'
import AppHeader from '../../components/common/Header.vue'
import AppSidebar from '../../components/common/Sidebar.vue'
import SalesPagination from '../../components/sales/SalesPagination.vue'

type StatusTone = 'success' | 'failed' | 'pending'
type OptionScope = ReportHistorySendType
type ItemTypeOption = {
  label: string
  value: ReportHistorySendItemType
  scopes: OptionScope[]
}
type StatusOption = {
  label: string
  value: ReportHistorySendStatus
  scopes: OptionScope[]
}

const activeType = ref<ReportHistorySendType>('all')
const activeItemType = ref<ReportHistorySendItemType>('all')
const activeStatus = ref<ReportHistorySendStatus>('all')
const keyword = ref('')
const historyItems = ref<ReportHistoryItem[]>([])
const currentPage = ref(1)
const totalPages = ref(0)
const totalCount = ref(0)
const isLoading = ref(false)
const errorMessage = ref('')
const HISTORY_PAGE_SIZE = 12



const typeTabs = [
  { label: '전체', value: 'all' },
  { label: '리포트', value: 'report' },
  { label: '웹폼', value: 'webform' },
] as const

const itemTypeOptions: ItemTypeOption[] = [
  { label: '전체 항목', value: 'all', scopes: ['all', 'report', 'webform'] },
  { label: '생애주기 리포트', value: 'report_lifecycle', scopes: ['all', 'report'] },
  { label: '질병 통계 리포트', value: 'report_disease', scopes: ['all', 'report'] },
  { label: '상담 웹폼', value: 'webform', scopes: ['all', 'webform'] },
]

const statusOptions: StatusOption[] = [
  { label: '전체 상태', value: 'all', scopes: ['all', 'report', 'webform'] },
  { label: '발송대기', value: 'pending', scopes: ['all', 'report', 'webform'] },
  { label: '발송성공', value: 'success', scopes: ['all', 'report', 'webform'] },
  { label: '발송실패', value: 'failed', scopes: ['all', 'report', 'webform'] },
  { label: '회수완료', value: 'collected', scopes: ['all', 'webform'] },
]

const visibleItemTypeOptions = computed(() =>
  itemTypeOptions.filter((option) => option.scopes.includes(activeType.value)),
)

const visibleStatusOptions = computed(() =>
  statusOptions.filter((option) => option.scopes.includes(activeType.value)),
)

const syncFiltersWithType = () => {
  if (!visibleItemTypeOptions.value.some((option) => option.value === activeItemType.value)) {
    activeItemType.value = 'all'
  }

  if (!visibleStatusOptions.value.some((option) => option.value === activeStatus.value)) {
    activeStatus.value = 'all'
  }
}

const getErrorMessage = (error: unknown) => {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? '발송 내역을 불러오지 못했습니다.'
  }

  return '발송 내역을 불러오지 못했습니다.'
}

const loadHistory = async (page = currentPage.value) => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const result = await getReportHistory({
      sendType: activeType.value,
      sendItemType: activeItemType.value,
      sendStatus: activeStatus.value,
      keyword: keyword.value.trim() || undefined,
      page,
      size: HISTORY_PAGE_SIZE,
    })

    historyItems.value = result.items
    currentPage.value = result.page
    totalPages.value = result.totalPages
    totalCount.value = result.totalCount
  } catch (error) {
    historyItems.value = []
    currentPage.value = 1
    totalPages.value = 0
    totalCount.value = 0
    errorMessage.value = getErrorMessage(error)
  } finally {
    isLoading.value = false
  }
}

const handleTypeChange = (sendType: ReportHistorySendType) => {
  activeType.value = sendType
  syncFiltersWithType()
  void loadHistory(1)
}

const handleSearch = () => {
  void loadHistory(1)
}

const formatSentAt = (value?: string) => {
  if (!value) return '-'

  return value.replace('T', ' ').slice(0, 16)
}

const resolveStatusTone = (item: ReportHistoryItem): StatusTone => {
  if (item.sendStatusCode === '02' || item.sendStatusCode === '04') return 'success'
  if (item.sendStatusCode === '03') return 'failed'
  return 'pending'
}

onMounted(() => {
  void loadHistory(1)
})
</script>

<template>
  <div class="app-shell send-history-page">
    <AppSidebar active-label="발송 내역" />

    <main class="app-main send-history-page__main">
      <AppHeader title="발송 내역" />

      <section class="send-history-toolbar card">
        <div class="send-history-tabs" aria-label="발송 유형">
          <button
            v-for="tab in typeTabs"
            :key="tab.value"
            class="send-history-tabs__button"
            :class="{ 'is-active': activeType === tab.value }"
            type="button"
            @click="handleTypeChange(tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="send-history-filters">
          <label>
            <span>항목</span>
            <select v-model="activeItemType" @change="handleSearch">
              <option v-for="option in visibleItemTypeOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>

          <label>
            <span>상태</span>
            <select v-model="activeStatus" @change="handleSearch">
              <option v-for="option in visibleStatusOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>

          <div class="send-history-search" role="search">
            <label for="send-history-keyword">검색</label>
            <input
              id="send-history-keyword"
              v-model="keyword"
              placeholder="고객명, 상태, 발송 항목"
              @keyup.enter="handleSearch"
            />
            <button type="button" @click="handleSearch">조회</button>
          </div>
        </div>
      </section>

      <section class="card send-history-list">
        <div class="send-history-list__header">
          <h2>내역 <span>총 {{ totalCount }}건</span></h2>
        </div>

        <p v-if="errorMessage" class="send-history-message send-history-message--error">{{ errorMessage }}</p>
        <p v-else-if="isLoading" class="send-history-message">불러오는 중...</p>

        <div v-else class="send-history-table">
          <table>
            <thead>
              <tr>
                <th>유형</th>
                <th>고객명</th>
                <th>고객 유형</th>
                <th>발송 항목</th>
                <th>발송 여부</th>
                <th>발송 일시</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in historyItems" :key="`${item.sendType}-${item.id}`">
                <td>
                  <span class="type-badge" :class="`type-badge--${item.sendType}`">
                    {{ item.sendTypeName }}
                  </span>
                </td>
                <td class="customer-name">{{ item.customerName }}</td>
                <td>{{ item.customerStageName }}</td>
                <td>{{ item.sendItemName }}</td>
                <td>
                  <span class="status-badge" :class="`status-badge--${resolveStatusTone(item)}`">
                    {{ item.sendStatusName }}
                  </span>
                </td>
                <td>{{ formatSentAt(item.sentAt) }}</td>
              </tr>
              <tr v-if="historyItems.length === 0">
                <td class="send-history-table__empty" colspan="6">조회된 발송 내역이 없습니다.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <SalesPagination
          :current-page="currentPage"
          :total-pages="totalPages"
          @change="loadHistory"
        />
      </section>
    </main>
  </div>
</template>

<style scoped>
.send-history-page__main {
  display: flex;
  height: 100vh;
  flex-direction: column;
  overflow: hidden;
  padding: 8px 20px 7px 20px;
}

.send-history-page__main :deep(.app-header) {
  min-height: 46px;
  margin-bottom: 7px;
}

.send-history-page__main :deep(.app-header__title) {
  padding-top: 2px;
}

.send-history-page__main :deep(.page-title) {
  font-size: 22px;
}

.send-history-toolbar {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin: 0 0 8px;
  border: 1px solid #e3e8f0;
  box-shadow: none;
  padding: 9px 12px;
}

.send-history-tabs {
  display: inline-flex;
  align-items: center;
  flex: 0 0 auto;
  border: 1px solid #dfe5ee;
  border-radius: 6px;
  background: #f7f9fc;
  padding: 2px;
}

.send-history-tabs__button {
  min-width: 58px;
  height: 26px;
  border: 0;
  border-radius: 5px;
  background: transparent;
  color: #6b7483;
  padding: 0 12px;
  font-size: 11px;
  font-weight: 850;
}

.send-history-tabs__button.is-active {
  background: #ffffff;
  color: var(--color-primary);
  box-shadow: 0 1px 3px rgb(15 23 42 / 10%);
}

.send-history-filters {
  display: flex;
  align-items: center;
  gap: 12px;
}

.send-history-filters > label {
  display: grid;
  grid-template-columns: max-content minmax(132px, 170px);
  align-items: center;
  gap: 8px;
  color: #394252;
  font-size: 11px;
  font-weight: 800;
}

.send-history-search {
  display: grid;
  grid-template-columns: max-content 320px 54px;
  align-items: center;
  gap: 8px;
  color: #394252;
  font-size: 11px;
  font-weight: 800;
  min-width: 0;
}

.send-history-search label {
  margin-right: 0;
  white-space: nowrap;
}

.send-history-filters select,
.send-history-search input {
  min-width: 0;
  height: 30px;
  border: 1px solid #d9e0ea;
  border-radius: 5px;
  background: #ffffff;
  padding: 0 10px;
  color: #273244;
  font-size: 12px;
  outline: none;
}

.send-history-search button {
  width: 54px;
  height: 30px;
  border: 0;
  border-radius: 5px;
  background: var(--color-primary);
  color: #ffffff;
  padding: 0;
  font-size: 11px;
  font-weight: 850;
  white-space: nowrap;
}

.send-history-filters select:focus,
.send-history-search input:focus {
  border-color: #8db5ff;
  box-shadow: 0 0 0 3px rgb(26 109 255 / 10%);
}

.send-history-list {
  display: flex;
  flex: 0 0 auto;
  flex-direction: column;
  border: 1px solid #e3e8f0;
  box-shadow: none;
  padding: 8px 11px 6px;
}

.send-history-list__header {
  flex: 0 0 auto;
  margin-bottom: 5px;
}

.send-history-list h2 {
  margin: 0;
  color: #263142;
  font-size: 14px;
  font-weight: 900;
  letter-spacing: 0;
}

.send-history-list h2 span {
  margin-left: 5px;
  color: var(--color-text-muted);
  font-size: 10px;
  font-weight: 700;
}

.send-history-message {
  display: flex;
  min-height: 112px;
  align-items: center;
  justify-content: center;
  margin: 0;
  color: var(--color-text-muted);
  font-size: 12px;
}

.send-history-message--error {
  color: #d85a65;
}

.send-history-table {
  overflow-x: auto;
  overflow-y: visible;
  border: 1px solid #e1e7f0;
  border-radius: 6px;
}

.send-history-table table {
  width: 100%;
  min-width: 760px;
  table-layout: fixed;
  border-collapse: collapse;
}

.send-history-table th,
.send-history-table td {
  height: 38px;
  border-bottom: 1px solid #edf1f6;
  padding: 0 10px;
  text-align: center;
  font-size: 11px;
  white-space: nowrap;
}

.send-history-table th {
  height: 34px;
  background: #f6f8fb;
  color: #4c586b;
  font-weight: 800;
}

.send-history-table tr:last-child td {
  border-bottom: 0;
}

.customer-name {
  color: var(--color-text);
  font-weight: 900;
}

.type-badge,
.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 52px;
  height: 22px;
  border-radius: 5px;
  padding: 0 8px;
  font-size: 10px;
  font-weight: 850;
}

.type-badge--report {
  background: #eef3ff;
  color: #4055d4;
}

.type-badge--webform {
  background: #eaf8ee;
  color: #24723b;
}

.status-badge--success {
  background: #ddf7e7;
  color: #24723b;
}

.status-badge--failed {
  background: #ffe4e6;
  color: #b23b49;
}

.status-badge--pending {
  background: #fff4d7;
  color: #8a6412;
}

.send-history-table__empty {
  height: 96px;
  color: var(--color-text-muted);
}

.send-history-list :deep(.sales-pagination) {
  flex: 0 0 auto;
  margin-top: 2px;
  padding-top: 0;
}

@media (max-width: 1120px) {
  .send-history-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .send-history-filters {
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}

@media (max-width: 760px) {
  .send-history-filters,
  .send-history-filters label,
  .send-history-search {
    display: grid;
    grid-template-columns: 1fr;
  }

  .send-history-search button {
    width: 100%;
  }
}
</style>
