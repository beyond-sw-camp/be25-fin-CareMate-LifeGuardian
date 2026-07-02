<script setup lang="ts">
import axios from 'axios'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import AppHeader from '../../components/common/Header.vue'
import AppSidebar from '../../components/common/Sidebar.vue'
import DashboardAchievementCard from '../../components/dashboard/DashboardAchievementCard.vue'
import DashboardContactCustomerTable from '../../components/dashboard/DashboardContactCustomerTable.vue'
import DashboardSummaryCards from '../../components/dashboard/DashboardSummaryCards.vue'
import SalesPagination from '../../components/sales/SalesPagination.vue'
import {
  getDashboardAchievement,
  getDashboardSummary,
  getTodayContactCustomers,
  sendDashboardReport,
  sendDashboardReportsInBulk,
  sendDashboardWebform,
  sendDashboardWebformsInBulk,
  type ContactCustomer,
  type DashboardAchievement,
  type DashboardSummary,
} from '@/api/dashboard'

const router = useRouter()

const summary = ref<DashboardSummary | null>(null)
const achievement = ref<DashboardAchievement | null>(null)
const contactCustomers = ref<ContactCustomer[]>([])

const selectedCustomerIds = ref<number[]>([])
const isLoading = ref(false)
const errorMessage = ref('')
const noticeMessage = ref('')
const noticeMessageType = ref<'success' | 'error'>('success')
const noticeTimerId = ref<number | null>(null)

const sendingWebFormIds = ref<number[]>([])
const sendingReportIds = ref<number[]>([])

const isBulkWebFormSending = ref(false)
const isBulkReportSending = ref(false)

const CONTACT_PAGE_SIZE = 8
const contactCurrentPage = ref(1)

const contactTotalPages = computed(() =>
  Math.ceil(contactCustomers.value.length / CONTACT_PAGE_SIZE),
)

const displayedContactCustomers = computed(() => {
  const start = (contactCurrentPage.value - 1) * CONTACT_PAGE_SIZE
  const end = start + CONTACT_PAGE_SIZE

  return contactCustomers.value.slice(start, end)
})

const getErrorMessage = (
  error: unknown,
  fallbackMessage = '대시보드 정보를 불러오지 못했습니다.',
) => {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? fallbackMessage
  }

  return fallbackMessage
}

const showNoticeMessage = (message: string, type: 'success' | 'error') => {
  noticeMessage.value = message
  noticeMessageType.value = type

  if (noticeTimerId.value !== null) {
    window.clearTimeout(noticeTimerId.value)
  }

  noticeTimerId.value = window.setTimeout(() => {
    noticeMessage.value = ''
    noticeTimerId.value = null
  }, 5000)
}

const loadDashboard = async () => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const [summaryResult, achievementResult, contactCustomerResult] =
      await Promise.all([
        getDashboardSummary(),
        getDashboardAchievement(),
        getTodayContactCustomers(),
      ])

    summary.value = summaryResult
    achievement.value = achievementResult
    contactCustomers.value = contactCustomerResult
    contactCurrentPage.value = 1
  } catch (error) {
    summary.value = null
    achievement.value = null
    contactCustomers.value = []
    contactCurrentPage.value = 1
    errorMessage.value = getErrorMessage(error)
  } finally {
    isLoading.value = false
  }
}

const handleSendWebForm = async (customer: ContactCustomer) => {
  const customerId = customer.potentialCustomerId

  if (sendingWebFormIds.value.includes(customerId)) return

  try {
    sendingWebFormIds.value = [...sendingWebFormIds.value, customerId]

    const result = await sendDashboardWebform(customerId)

    const targetCustomer = contactCustomers.value.find(
      (item) => item.potentialCustomerId === customerId,
    )

    if (targetCustomer) {
      targetCustomer.webFormStatusCode = result.webformStatusCode
      targetCustomer.webFormStatusName = result.webformStatusName
      targetCustomer.webFormSendEnabled = false
    }

    showNoticeMessage(
      `${customer.customerName} 고객에게 웹폼을 발송했습니다.`,
      'success',
    )
    await loadDashboard()
  } catch (error) {
    showNoticeMessage(
      getErrorMessage(error, '웹폼 발송에 실패했습니다.'),
      'error',
    )
  } finally {
    sendingWebFormIds.value = sendingWebFormIds.value.filter(
      (id) => id !== customerId,
    )
  }
}

const handleSendBulkWebForms = async () => {
  if (isBulkWebFormSending.value) return

  try {
    isBulkWebFormSending.value = true

    const results = await sendDashboardWebformsInBulk()

    const sentCustomerIds = new Set(
      results.map((item) => item.customerId),
    )

    contactCustomers.value.forEach((customer) => {
      if (sentCustomerIds.has(customer.potentialCustomerId)) {
        customer.webFormStatusCode = '02'
        customer.webFormStatusName = '발송완료'
        customer.webFormSendEnabled = false
      }
    })

    showNoticeMessage(
      `${results.length}명의 고객에게 웹폼을 발송했습니다.`,
      'success',
    )
    await loadDashboard()
  } catch (error) {
    showNoticeMessage(
      getErrorMessage(error, '웹폼 일괄 발송에 실패했습니다.'),
      'error',
    )
  } finally {
    isBulkWebFormSending.value = false
  }
}

const handleSendReport = async (customer: ContactCustomer) => {
  const customerId = customer.potentialCustomerId

  if (sendingReportIds.value.includes(customerId)) return

  try {
    sendingReportIds.value = [...sendingReportIds.value, customerId]

    const result = await sendDashboardReport(customerId)

    const targetCustomer = contactCustomers.value.find(
      (item) => item.potentialCustomerId === customerId,
    )

    if (targetCustomer) {
      targetCustomer.reportSendStatusCode = result.sendStatusCode
      targetCustomer.reportSendStatusName = '발송완료'
      targetCustomer.reportSendEnabled = false
    }

    showNoticeMessage(
      `${result.customerName || customer.customerName} 고객에게 리포트를 발송했습니다.`,
      'success',
    )
  } catch (error) {
    showNoticeMessage(
      getErrorMessage(error, '리포트 발송에 실패했습니다.'),
      'error',
    )
  } finally {
    sendingReportIds.value = sendingReportIds.value.filter(
      (id) => id !== customerId,
    )
  }
}

const handleSendBulkReports = async () => {
  if (isBulkReportSending.value) return

  const reportIds = contactCustomers.value
    .filter((customer) => customer.reportSendEnabled)
    .map((customer) => customer.reportId)
    .filter((reportId): reportId is number => typeof reportId === 'number')

  if (reportIds.length === 0) {
    showNoticeMessage(
      '발송 가능한 리포트가 없습니다.',
      'error',
    )
    return
  }

  try {
    isBulkReportSending.value = true

    const result = await sendDashboardReportsInBulk(reportIds)

    showNoticeMessage(
      `리포트 발송 완료: 성공 ${result.successCount}건, 제외 ${result.skippedCount}건, 실패 ${result.failedCount}건`,
      'success',
    )

    await loadDashboard()
  } catch (error) {
    showNoticeMessage(
      getErrorMessage(error, '리포트 일괄 발송에 실패했습니다.'),
      'error',
    )
  } finally {
    isBulkReportSending.value = false
  }
}

const handleContactPageChange = (page: number) => {
  contactCurrentPage.value = page
  selectedCustomerIds.value = []
}

const handleGoSalesFilter = (filter: {
  consultStatusCode?: string[]
  contractStatusCode?: string[]
}) => {
  void router.push({
    path: '/sales',
    query: filter,
  })
}

onMounted(() => {
  void loadDashboard()
})

onBeforeUnmount(() => {
  if (noticeTimerId.value !== null) {
    window.clearTimeout(noticeTimerId.value)
  }
})
</script>

<template>
  <div class="app-shell">
    <AppSidebar active-label="대시보드" />

    <main class="app-main page-placeholder">
      <AppHeader title="영업사원 대시보드" />
      <p
        v-if="errorMessage"
        class="dashboard-state dashboard-state--error"
      >
        {{ errorMessage }}
      </p>
      <p
        v-else-if="isLoading"
        class="dashboard-state"
      >
        불러오는 중...
      </p>
      <template v-else>
        <section class="dashboard-top">
          <DashboardSummaryCards 
            :summary="summary"
            @go-sales-filter="handleGoSalesFilter"
          />
          <DashboardAchievementCard :achievement="achievement" />
        </section>
        <section class="card dashboard-contact">
          <div class="dashboard-contact__header">
            <h3>
              오늘 연락 고객
              <span>총 {{ contactCustomers.length }}명</span>
            </h3>

            <div class="dashboard-contact__actions">
              <button
                class="dashboard-contact__bulk-button"
                type="button"
                @click="handleSendBulkWebForms"
              >
                웹폼 일괄발송
              </button>

              <button
                class="dashboard-contact__bulk-button"
                type="button"
                @click="handleSendBulkReports"
              >
                리포트 일괄발송
              </button>
            </div>
          </div>
          
          <p
            v-if="noticeMessage"
            class="dashboard-message"
            :class="`dashboard-message--${noticeMessageType}`"
          >
            {{ noticeMessage }}
          </p>

          <DashboardContactCustomerTable
            v-model:selected-customer-ids="selectedCustomerIds"
            :customers="displayedContactCustomers"
            :sending-web-form-ids="sendingWebFormIds"
            :sending-report-ids="sendingReportIds"
            @send-web-form="handleSendWebForm"
            @send-report="handleSendReport"
          />
          <div class="dashboard-contact__footer">
            <SalesPagination
              :current-page="contactCurrentPage"
              :total-pages="contactTotalPages"
              @change="handleContactPageChange"
            />
          </div>
        </section>
      </template>
    </main>
  </div>
</template>

<style scoped>
.page-placeholder {
  width: 100%;
  padding: 24px 28px;
  overflow-x: auto;
}

.dashboard-message {
  margin: 0 0 12px;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 11px;
  font-weight: 700;
}

.dashboard-message--success {
  background: #eaf8ee;
  color: #24723b;
}

.dashboard-message--error {
  background: #fff0f1;
  color: #c43e4b;
}

.dashboard-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  margin: 0;
  color: var(--color-text-muted);
  font-size: 12px;
}

.dashboard-state--error {
  color: #d85a65;
}


.dashboard-top {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(300px, 360px);
  gap: 24px;
  align-items: start;
}

.dashboard-contact {
  display: flex;
  width: 100%;
  flex-direction: column;
  margin-top: 24px;
  border: 1px solid #e3e8f0;
  box-shadow: none;
  padding: 14px 16px 18px;
  min-height: 410px;
}

.dashboard-contact__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.dashboard-contact__header h3 {
  margin: 0;
  color: #263142;
  font-size: 14px;
  font-weight: 900;
}

.dashboard-contact__header span {
  margin-left: 4px;
  color: var(--color-text-muted);
  font-size: 11px;
  font-weight: 700;
}

.dashboard-contact__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dashboard-contact__bulk-button {
  width: 83px;
  height: 25px;
  border: 0;
  border-radius: 999px;
  background: var(--color-primary);
  color: #ffffff;
  padding: 0 10px;
  font-size: 10px;
  font-weight: 800;
  cursor: pointer;
  white-space: nowrap;
}

.dashboard-contact__bulk-button:hover {
  background: color-mix(in srgb, var(--color-primary) 84%, black);
}


.dashboard-contact__footer {
  display: flex;
  justify-content: center;
  margin-top: auto;
  padding-top: 10px;
}

@media (max-width: 1180px) {
  .dashboard-top {
    grid-template-columns: 1fr;
  }
}

</style>
