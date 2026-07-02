<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import AppSidebar from '../../components/common/Sidebar.vue'
import AppHeader from '../../components/common/Header.vue'
import KpiWidgets from '../../components/admin/dashboard/KpiWidgets.vue'
import RankingChart from '../../components/admin/dashboard/RankingChart.vue'
import PerformanceList from '../../components/admin/dashboard/PerformanceList.vue'
import AllPerformancePopup from '../../components/admin/dashboard/AllPerformancePopup.vue'
import IndividualPerformancePopup from '../../components/admin/dashboard/IndividualPerformancePopup.vue'
import {
  getAnnualContracts,
  getMonthlyContracts,
  getSalesRanking,
  getDashboardSalesUsers,
  pinSalesUser,
  unpinSalesUser
} from '@/api/admin'
import type {
  BranchAnnualContracts,
  BranchMonthlyContracts,
  BranchSalesRanking,
  DashboardSalesUser
} from '@/api/admin'

const authStore = useAuthStore()

const branchIdMap: Record<string, number> = {
  '1': 1,
  '2': 2,
  '3': 3,
  '강남지점': 1,
  '서울강남지점': 1,
  '마포지점': 2,
  '서울마포지점': 2,
  '수원지점': 3,
  '경기수원지점': 3,
}

// 지점 ID & 이름 바인딩 (Compatibility for dev/prod)
const branchId = computed(() => {
  if (authStore.branchId) {
    const num = Number(authStore.branchId)
    if (!isNaN(num)) return num
  }
  const b = authStore.branch
  if (!b) return 1
  return branchIdMap[b] ?? (Number(b) || 1)
})
const branchName = computed(() => {
  const b = authStore.branch || '강남지점'
  const r = authStore.region
  return r ? `${r} ${b}` : b
})

const getLocalYearMonth = () => {
  const d = new Date()
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

// 조회 날짜 관리 (현재 연도 및 연월 기본값 설정)
const targetYearMonth = ref(getLocalYearMonth())
const targetYear = computed(() => Number(targetYearMonth.value.split('-')[0]))

// API 데이터 수신 상태
const annualData = ref<BranchAnnualContracts | null>(null)
const monthlyData = ref<BranchMonthlyContracts | null>(null)
const rankingData = ref<BranchSalesRanking | null>(null)
const salesUsers = ref<DashboardSalesUser[]>([])

const isLoading = ref(false)
const searchKeyword = ref('')

// 팝업 제어 상태
const isAllPerformanceOpen = ref(false)
const isIndividualPerformanceOpen = ref(false)
const selectedUserId = ref<number | null>(null)

// API 호출부 정의
const loadAnnualData = async () => {
  try {
    annualData.value = await getAnnualContracts(branchId.value, targetYear.value)
  } catch (error) {
    console.error('Failed to load annual contracts statistics:', error)
    annualData.value = null
  }
}

const loadMonthlyData = async () => {
  try {
    monthlyData.value = await getMonthlyContracts(branchId.value, targetYearMonth.value)
  } catch (error) {
    console.error('Failed to load monthly contracts statistics:', error)
    monthlyData.value = null
  }
}

const loadRankingData = async () => {
  try {
    rankingData.value = await getSalesRanking(branchId.value, targetYearMonth.value)
  } catch (error) {
    console.error('Failed to load sales ranking statistics:', error)
    rankingData.value = null
  }
}

const loadSalesUsers = async (keyword?: string) => {
  try {
    const data = await getDashboardSalesUsers(branchId.value, keyword)
    salesUsers.value = data.salesUsers || []
  } catch (error) {
    console.error('Failed to load sales users list:', error)
    salesUsers.value = []
  }
}

// 초기화 데이터 로드 함수
const initDashboard = async () => {
  isLoading.value = true
  try {
    await Promise.all([
      loadAnnualData(),
      loadMonthlyData(),
      loadRankingData(),
      loadSalesUsers()
    ])
  } catch (error) {
    console.error('Dashboard data initialization failed:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  initDashboard()
})

// 날짜 변경 감지 시 지점 통계 수치 재조회
watch(targetYearMonth, async () => {
  isLoading.value = true
  try {
    await Promise.all([
      loadAnnualData(),
      loadMonthlyData(),
      loadRankingData()
    ])
  } catch (error) {
    console.error('Failed to reload dashboard statistics on date change:', error)
  } finally {
    isLoading.value = false
  }
})

// 검색어 입력 이벤트 처리 (Debounced from PerformanceList)
const handleSearch = (keyword: string) => {
  searchKeyword.value = keyword
  loadSalesUsers(keyword)
}

// 핀 고정 토글 처리 (Optimistic UI 적용)
const handleTogglePin = async (userId: number, currentPinned: boolean) => {
  // 상태 롤백 대비 원본 복사
  const backupList = JSON.parse(JSON.stringify(salesUsers.value))

  // 1. UI 낙관적 업데이트 수행
  const user = salesUsers.value.find(u => u.userId === userId)
  if (user) {
    user.isPinned = !currentPinned
  }

  // 2. 서버 요청 전송 및 롤백 대비 처리
  try {
    if (currentPinned) {
      await unpinSalesUser(userId)
    } else {
      await pinSalesUser(userId)
    }
  } catch (error) {
    console.error('Failed to toggle pin state for sales user:', error)
    // 에러 발생 시 원래 상태로 복원
    salesUsers.value = backupList
    alert('핀 설정 변경에 실패했습니다. 네트워크 연결 상태를 확인하고 다시 시도해주세요.')
  }
}

// 영업사원 개별 행 클릭 시 상세 조회 팝업 호출
const handleSelectUser = (userId: number) => {
  selectedUserId.value = userId
  isIndividualPerformanceOpen.value = true
}

// 핀 고정 최상단 정렬 및 2순위 실적 순위별 정렬
const sortedSalesUsers = computed(() => {
  return [...salesUsers.value].sort((a, b) => {
    if (a.isPinned !== b.isPinned) {
      return a.isPinned ? -1 : 1
    }
    return a.rank - b.rank
  })
})
</script>

<template>
  <div class="app-shell">
    <AppSidebar active-label="대시보드" />

    <main class="app-main">
      <AppHeader 
        title="지점 실적 대시보드" 
        description="지점의 전체 영업 실적, 월별 랭킹 차트, 영업사원 성과 지표를 실시간으로 통합 모니터링합니다." 
      />

      <!-- 상단 연월 선택 컨트롤 영역 -->
      <div class="dashboard-controls card">
        <div class="branch-badge">
          <span class="badge-dot"></span>
          <span class="branch-text">{{ branchName }} 실시간 관제</span>
        </div>
        
        <div class="month-selector">
          <span class="selector-label">조회 기준월:</span>
          <input 
            type="month" 
            v-model="targetYearMonth" 
            class="input month-input"
            aria-label="조회 기준월 선택"
          />
        </div>
      </div>

      <!-- 로딩 인디케이터 -->
      <div v-if="isLoading && !annualData && !monthlyData" class="page-loading">
        <div class="spinner"></div>
        <p>실적 현황 데이터를 로딩 중입니다...</p>
      </div>

      <div v-else class="dashboard-content">
        <!-- 1. 상단 KPI 카드 요약 위젯 -->
        <KpiWidgets 
          :annual-data="annualData" 
          :monthly-data="monthlyData" 
        />

        <!-- 2. 하단 데이터 비주얼라이제이션 그리드 -->
        <div class="dashboard-grid">
          <!-- 좌측: 상/하위 비교 막대 그래프 -->
          <div class="grid-left">
            <RankingChart 
              :ranking-data="rankingData" 
              @show-all-performances="isAllPerformanceOpen = true"
            />
          </div>

          <!-- 우측: 영업사원 성과 리스트 및 검색/핀고정 -->
          <div class="grid-right">
            <PerformanceList 
              :sales-users="sortedSalesUsers" 
              @search="handleSearch"
              @toggle-pin="handleTogglePin"
              @select-user="handleSelectUser"
            />
          </div>
        </div>
      </div>
    </main>

    <!-- 전체 영업사원 성과 모달 팝업 -->
    <AllPerformancePopup 
      :is-open="isAllPerformanceOpen" 
      :branch-id="branchId" 
      :default-year-month="targetYearMonth"
      @close="isAllPerformanceOpen = false"
    />

    <!-- 개별 영업사원 상세 정보 모달 팝업 -->
    <IndividualPerformancePopup 
      :is-open="isIndividualPerformanceOpen" 
      :branch-id="branchId" 
      :user-id="selectedUserId" 
      @close="isIndividualPerformanceOpen = false"
      @target-updated="initDashboard"
    />
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

/* 상단 컨트롤 영역 */
.dashboard-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  margin-bottom: 4px;
}

.branch-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #eff6ff;
  border: 1px solid #dbeafe;
  border-radius: var(--radius-pill);
  padding: 4px 12px;
}

.badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--color-primary);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(26, 109, 255, 0.7); }
  70% { transform: scale(1); box-shadow: 0 0 0 6px rgba(26, 109, 255, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(26, 109, 255, 0); }
}

.branch-text {
  font-size: 13px;
  font-weight: 800;
  color: var(--color-primary);
}

.month-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.selector-label {
  font-size: 13px;
  font-weight: 800;
  color: var(--color-text-muted);
}

.month-input {
  width: 140px;
  font-size: 13px;
  font-weight: 700;
  border: 1px solid var(--color-border);
  background-color: #ffffff;
  padding: 0 10px;
  height: 32px;
}

.month-input:focus {
  border-color: var(--color-primary);
}

/* 대시보드 세부 레이아웃 */
.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 20px;
  align-items: stretch;
}

.grid-left, .grid-right {
  min-width: 0;
}

/* 로딩 화면 */
.page-loading {
  display: grid;
  place-items: center;
  gap: 16px;
  padding: 120px 0;
  color: var(--color-text-muted);
  font-weight: 700;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 5px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 1024px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

:deep(.modal-backdrop) {
  z-index: 1000;
}
</style>
