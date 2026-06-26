<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  PointElement,
  LineElement,
  CategoryScale,
  LinearScale,
  Filler
} from 'chart.js'
import { getIndividualPerformance } from '@/api/admin'
import type { IndividualPerformance } from '@/api/admin'

ChartJS.register(Title, Tooltip, Legend, PointElement, LineElement, CategoryScale, LinearScale, Filler)

const props = defineProps<{
  isOpen: boolean
  userId: number | null
  branchId: number
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const info = ref<IndividualPerformance | null>(null)
const isLoading = ref(false)

const loadData = async () => {
  if (!props.isOpen || props.userId === null) return
  isLoading.value = true
  try {
    const data = await getIndividualPerformance(props.branchId, props.userId)
    info.value = data
  } catch (error) {
    console.error('Failed to load individual performance:', error)
    info.value = null
  } finally {
    isLoading.value = false
  }
}

watch(() => [props.isOpen, props.userId], () => {
  if (props.isOpen && props.userId !== null) {
    loadData()
  } else {
    info.value = null
  }
}, { immediate: true })

const chartData = computed(() => {
  if (!info.value || !info.value.monthlyTrends) {
    return { labels: [], datasets: [] }
  }
  
  const trends = info.value.monthlyTrends
  return {
    labels: trends.map(t => t.month),
    datasets: [
      {
        label: '월별 계약 건수',
        data: trends.map(t => t.count),
        borderColor: '#1a6dff',
        backgroundColor: 'rgba(26, 109, 255, 0.08)',
        fill: true,
        tension: 0.35,
        pointBackgroundColor: '#1a6dff',
        pointBorderColor: '#ffffff',
        pointBorderWidth: 2,
        pointRadius: 5,
        pointHoverRadius: 7
      }
    ]
  }
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      backgroundColor: '#1e293b',
      titleFont: { size: 12, weight: 'bold' as const },
      bodyFont: { size: 11 },
      padding: 8,
      cornerRadius: 4,
      displayColors: false,
      callbacks: {
        label: (context: any) => ` 계약 건수: ${context.parsed.y}건`
      }
    }
  },
  scales: {
    x: {
      grid: {
        display: false
      },
      ticks: {
        color: '#64748b',
        font: { size: 11, family: 'Pretendard, sans-serif' }
      }
    },
    y: {
      border: { dash: [4, 4] },
      grid: { color: '#f1f5f9' },
      ticks: {
        color: '#64748b',
        font: { size: 11, family: 'Pretendard, sans-serif' },
        precision: 0
      }
    }
  }
}
</script>

<template>
  <div v-if="isOpen" class="modal-backdrop" @click="$emit('close')">
    <div class="modal-card modal-card--medium" @click.stop>
      <div class="modal-header">
        <div>
          <h2 class="modal-title">
            <span class="user-name">{{ info?.employeeName || '영업사원' }}</span>
            <span class="position-badge">{{ info?.positionName || '' }}</span>
            상세 성과
          </h2>
          <p class="modal-subtitle">영업사원의 월간 성과 상세 분석 및 6개월 실적 추이</p>
        </div>
        <button class="close-button" type="button" @click="$emit('close')">
          &times;
        </button>
      </div>

      <div class="modal-body">
        <div v-if="isLoading" class="loading-state">
          <div class="spinner"></div>
          <p>상세 실적 데이터를 불러오는 중입니다...</p>
        </div>

        <div v-else-if="!info" class="empty-state">
          <p>데이터가 존재하지 않거나 정보를 불러올 수 없습니다.</p>
        </div>

        <div v-else class="modal-content">
          <!-- 상단 프로필 및 주요 실적 정보 -->
          <div class="profile-card">
            <div class="rank-badge-container">
              <span class="rank-label">지점 순위</span>
              <span class="rank-value">{{ info.branchRank }}위 <span class="rank-total">/ {{ info.totalBranchUsers }}명</span></span>
            </div>
            
            <div class="target-status">
              <span :class="['status-badge', info.isTargetAchieved ? 'status-success' : 'status-pending']">
                {{ info.isTargetAchieved ? '목표 달성' : '목표 진행 중' }}
              </span>
            </div>
          </div>

          <!-- 달성률 프로그레스 바 -->
          <div class="progress-section card">
            <div class="card-body">
              <div class="progress-header">
                <span class="progress-title">당월 목표 달성률</span>
                <span class="progress-percentage">{{ info.targetAchievementRate }}%</span>
              </div>
              
              <div class="progress-bar-container">
                <div class="progress-bar-track">
                  <div 
                    class="progress-bar-fill" 
                    :class="{ 'fill-success': info.isTargetAchieved }"
                    :style="{ width: Math.min(info.targetAchievementRate, 100) + '%' }"
                  ></div>
                </div>
              </div>
              
              <div class="progress-details">
                <div class="detail-item">
                  <span class="label">당월 실적</span>
                  <span class="value font-bold">{{ info.thisMonthCount }}건</span>
                </div>
                <div class="detail-separator">/</div>
                <div class="detail-item">
                  <span class="label">월 목표</span>
                  <span class="value">{{ info.monthlyTargetCount }}건</span>
                </div>
                <div class="detail-separator">|</div>
                <div class="detail-item">
                  <span class="label">목표 대비</span>
                  <span :class="['value', 'font-bold', info.targetDifference >= 0 ? 'text-success' : 'text-danger']">
                    {{ info.targetDifference >= 0 ? '+' : '' }}{{ info.targetDifference }}건
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 그리드형 세부 성과 데이터 -->
          <div class="stats-grid">
            <div class="stats-card">
              <span class="stats-label">전월 실적</span>
              <span class="stats-value">{{ info.previousMonthCount }}건</span>
              <span class="stats-sub" :class="info.momDifference >= 0 ? 'text-success' : 'text-danger'">
                전월 대비 {{ info.momDifference >= 0 ? '+' : '' }}{{ info.momDifference }}건
              </span>
            </div>
            <div class="stats-card">
              <span class="stats-label">연간 누적 실적</span>
              <span class="stats-value font-highlight">{{ info.annualCount }}건</span>
              <span class="stats-sub">올해 누적 체결 계약</span>
            </div>
          </div>

          <!-- 하단 실적 추이 그래프 -->
          <div class="chart-section card">
            <div class="card-body">
              <h4 class="chart-title">최근 6개월 판매 실적 추이</h4>
              <div class="chart-container">
                <Line :data="chartData" :options="chartOptions" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px 28px 16px;
  border-bottom: 1px solid var(--color-border);
}

.modal-title {
  margin: 0;
  font-size: 20px;
  font-weight: 900;
  color: var(--color-text);
  letter-spacing: -0.04em;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  color: var(--color-text);
}

.position-badge {
  font-size: 12px;
  font-weight: 700;
  background-color: #f1f5f9;
  color: #475569;
  padding: 2px 8px;
  border-radius: 4px;
}

.modal-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--color-text-muted);
}

.close-button {
  background: none;
  border: none;
  font-size: 28px;
  line-height: 1;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 0 4px;
}

.close-button:hover {
  color: var(--color-text);
}

.modal-body {
  padding: 24px 28px 28px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-card--medium {
  width: min(640px, 95vw) !important;
  max-width: none !important;
}

.loading-state, .empty-state {
  display: grid;
  place-items: center;
  gap: 16px;
  padding: 60px 0;
  color: var(--color-text-muted);
  font-weight: 700;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 4px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.modal-content {
  display: grid;
  gap: 20px;
}

/* 프로필 요약 스타일 */
.profile-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8fafc;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 16px 20px;
}

.rank-badge-container {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.rank-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-muted);
  text-transform: uppercase;
}

.rank-value {
  font-size: 22px;
  font-weight: 900;
  color: var(--color-text);
  letter-spacing: -0.02em;
}

.rank-total {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-muted);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: var(--radius-pill);
  font-size: 12px;
  font-weight: 800;
}

.status-success {
  background-color: #ecfdf5;
  color: var(--color-success);
  border: 1px solid #a7f3d0;
}

.status-pending {
  background-color: #fffbeb;
  color: #d97706;
  border: 1px solid #fde68a;
}

/* 달성률 프로그레스 섹션 */
.progress-section {
  background: var(--color-surface);
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 10px;
}

.progress-title {
  font-size: 14px;
  font-weight: 800;
  color: var(--color-text);
}

.progress-percentage {
  font-size: 24px;
  font-weight: 900;
  color: var(--color-primary);
}

.progress-bar-container {
  margin-bottom: 16px;
}

.progress-bar-track {
  height: 8px;
  background-color: #f1f5f9;
  border-radius: var(--radius-pill);
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background-color: var(--color-primary);
  border-radius: var(--radius-pill);
  transition: width 0.6s ease-out;
}

.fill-success {
  background-color: var(--color-success);
}

.progress-details {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  font-size: 13px;
  color: #475569;
  border-top: 1px solid #f1f5f9;
  padding-top: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.detail-separator {
  color: #cbd5e1;
}

.label {
  color: var(--color-text-muted);
}

.value {
  color: var(--color-text);
}

/* 그리드 세부 정보 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.stats-card {
  display: flex;
  flex-direction: column;
  padding: 16px 20px;
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.stats-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--color-text-muted);
  margin-bottom: 4px;
}

.stats-value {
  font-size: 20px;
  font-weight: 800;
  color: var(--color-text);
  margin-bottom: 6px;
}

.font-highlight {
  color: var(--color-primary);
}

.stats-sub {
  font-size: 11px;
  color: var(--color-text-muted);
  font-weight: 500;
}

.text-success {
  color: var(--color-success);
  font-weight: 700;
}

.text-danger {
  color: #ef4444;
  font-weight: 700;
}

.font-bold {
  font-weight: 700;
}

/* 그래프 섹션 */
.chart-section {
  background: var(--color-surface);
}

.chart-title {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 800;
  color: var(--color-text);
}

.chart-container {
  position: relative;
  width: 100%;
  height: 200px;
}
</style>
