<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppHeader from '../../components/common/Header.vue'
import AppSidebar from '../../components/common/Sidebar.vue'
import EsgKpiWidgets from '../../components/admin/audit/EsgKpiWidgets.vue'
import PeakCutChart from '../../components/admin/audit/PeakCutChart.vue'
import AuditLogTable from '../../components/admin/audit/AuditLogTable.vue'
import PiiSecureTable from '../../components/admin/audit/PiiSecureTable.vue'
import {
  getEsgEnvironmentalScores,
  getEsgPeakCutProfile,
  getAuditLogs,
} from '@/api/admin'
import { getPiiSecureList } from '@/api/members'
import type { HourlyPowerProfile, AuditLogInfo } from '@/api/admin'
import type { PiiSecureSummary } from '@/api/members'

// Helper date methods
const getTodayString = () => new Date().toISOString().slice(0, 10)
const getYesterdayString = () => {
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  return yesterday.toISOString().slice(0, 10)
}

// 1. ESG KPI 데이터
const carbonKg = ref<number | null>(null)
const costSaved = ref<number | null>(null)

// 2. 피크컷 프로파일 차트 데이터
const targetChartDate = ref(getYesterdayString())
const hourlyProfiles = ref<HourlyPowerProfile[]>([])
const isLoadingChart = ref(false)

// 3. 감사 로그 데이터
const auditLogs = ref<AuditLogInfo[]>([])
const auditTotalElements = ref(0)
const auditTotalPages = ref(1)
const auditCurrentPage = ref(1)
const isLoadingAudit = ref(false)
const activeFilters = ref({
  startDate: getTodayString(),
  endDate: getTodayString(),
  actionTypeCode: ''
})

// 4. PII 보관 데이터
const piiList = ref<PiiSecureSummary[]>([])
const piiTotalElements = ref(0)
const piiTotalPages = ref(1)
const piiCurrentPage = ref(1)
const isLoadingPii = ref(false)

// API 호출: ESG 요약 점수
const loadEsgScores = async () => {
  try {
    const data = await getEsgEnvironmentalScores()
    if (data) {
      carbonKg.value = data.totalSavedCarbonKg
      costSaved.value = data.totalSavedCostKrw
    }
  } catch (error) {
    console.error('Failed to load ESG scores:', error)
  }
}

// API 호출: 전력 피크컷 프로파일
const loadPeakCutProfile = async (date: string) => {
  isLoadingChart.value = true
  targetChartDate.value = date
  try {
    const data = await getEsgPeakCutProfile(date)
    if (data && data.hourlyProfiles) {
      hourlyProfiles.value = data.hourlyProfiles
    }
  } catch (error) {
    console.error('Failed to load Peak-Cut profile:', error)
    hourlyProfiles.value = []
  } finally {
    isLoadingChart.value = false
  }
}

// API 호출: 감사 로그
const loadAuditLogs = async () => {
  isLoadingAudit.value = true
  try {
    const data = await getAuditLogs({
      startDate: activeFilters.value.startDate,
      endDate: activeFilters.value.endDate,
      actionTypeCode: activeFilters.value.actionTypeCode,
      page: auditCurrentPage.value,
      size: 10
    })
    if (data) {
      auditLogs.value = data.content || []
      auditTotalElements.value = data.totalElements || 0
      auditTotalPages.value = data.totalPages || 1
    }
  } catch (error) {
    console.error('Failed to load audit logs:', error)
    auditLogs.value = []
  } finally {
    isLoadingAudit.value = false
  }
}

// API 호출: PII 분리 보존 현황
const loadPiiList = async () => {
  isLoadingPii.value = true
  try {
    const data = await getPiiSecureList({
      page: piiCurrentPage.value,
      size: 10
    })
    if (data) {
      piiList.value = data.content || []
      piiTotalElements.value = data.totalElements || 0
      piiTotalPages.value = data.totalPages || 1
    }
  } catch (error) {
    console.error('Failed to load PII list:', error)
    piiList.value = []
  } finally {
    isLoadingPii.value = false
  }
}

// 필터 변경 핸들러
const handleFilterChange = (filters: { startDate: string; endDate: string; actionTypeCode: string }) => {
  activeFilters.value = filters
  auditCurrentPage.value = 1
  loadAuditLogs()
}

// 감사 로그 페이지 변경
const handleAuditPageChange = (page: number) => {
  auditCurrentPage.value = page
  loadAuditLogs()
}

// PII 페이지 변경
const handlePiiPageChange = (page: number) => {
  piiCurrentPage.value = page
  loadPiiList()
}

// 날짜 변경 핸들러
const handleChartDateChange = (date: string) => {
  loadPeakCutProfile(date)
}

// 초기 병렬 로드
onMounted(() => {
  loadEsgScores()
  loadPeakCutProfile(targetChartDate.value)
  loadAuditLogs()
  loadPiiList()
})
</script>

<template>
  <div class="app-shell">
    <AppSidebar active-label="시스템 감사 및 ESG" />

    <main class="app-main">
      <AppHeader 
        title="시스템 감사 및 ESG 관제" 
        description="시스템 보안 감사 이력을 실시간 모니터링하고, Peak-Cut 스케줄링을 통한 전력 사용 최적화 및 비용 절감 효과를 종합 분석합니다." 
      />

      <!-- 1. ESG KPI 지표 보드 -->
      <EsgKpiWidgets 
        :carbon-kg="carbonKg" 
        :cost-saved="costSaved" 
        :total-audit-logs="auditTotalElements"
        :total-pii-count="piiTotalElements"
      />

      <!-- 2. Peak-Cut 24시간 부하 비교 차트 -->
      <div class="chart-section-wrapper">
        <PeakCutChart 
          :profiles="hourlyProfiles" 
          :target-date="targetChartDate"
          :is-loading="isLoadingChart"
          @change-date="handleChartDateChange"
        />
      </div>

      <!-- 3. 하단 감사 로그 및 PII 관리 리스트 2열 그리드 -->
      <div class="audit-pii-grid">
        <!-- 감사 로그 테이블 -->
        <div class="grid-col-left">
          <AuditLogTable 
            :logs="auditLogs"
            :total-elements="auditTotalElements"
            :total-pages="auditTotalPages"
            :current-page="auditCurrentPage"
            :is-loading="isLoadingAudit"
            @filter-change="handleFilterChange"
            @page-change="handleAuditPageChange"
          />
        </div>

        <!-- PII 보존 현황 테이블 -->
        <div class="grid-col-right">
          <PiiSecureTable 
            :pii-list="piiList"
            :total-elements="piiTotalElements"
            :total-pages="piiTotalPages"
            :current-page="piiCurrentPage"
            :is-loading="isLoadingPii"
            @page-change="handlePiiPageChange"
          />
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.app-main {
  flex: 1;
  min-width: 0;
  padding: 24px 28px;
  background-color: #f8fafc;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-section-wrapper {
  margin-bottom: 4px;
}

.audit-pii-grid {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 20px;
  align-items: stretch;
  margin-bottom: 20px;
}

.grid-col-left, .grid-col-right {
  min-width: 0; /* Prevents flex/grid blowouts */
}

@media (max-width: 1280px) {
  .audit-pii-grid {
    grid-template-columns: 1fr;
  }
}
</style>
