<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale
} from 'chart.js'
import { getSalesPerformanceDetails } from '@/api/admin'
import type { SalesUserPerformanceDetail } from '@/api/admin'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

const props = defineProps<{
  isOpen: boolean
  branchId: number
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const performances = ref<SalesUserPerformanceDetail[]>([])
const isLoading = ref(false)
const sortKey = ref<string>('rank')
const sortDesc = ref<boolean>(false)

const loadData = async () => {
  if (!props.isOpen) return
  isLoading.value = true
  try {
    const data = await getSalesPerformanceDetails(props.branchId)
    performances.value = data.performances || []
  } catch (error) {
    console.error('Failed to load performance details:', error)
  } finally {
    isLoading.value = false
  }
}

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    sortKey.value = 'rank'
    sortDesc.value = false
    loadData()
  }
})

// 로컬 테이블 정렬 로직
const sortedPerformances = computed(() => {
  const key = sortKey.value
  const desc = sortDesc.value
  return [...performances.value].sort((a, b) => {
    let valA = (a as any)[key]
    let valB = (b as any)[key]

    if (typeof valA === 'string') {
      return desc ? valB.localeCompare(valA) : valA.localeCompare(valB)
    }
    return desc ? valB - valA : valA - valB
  })
})

const handleSort = (key: string) => {
  if (sortKey.value === key) {
    sortDesc.value = !sortDesc.value
  } else {
    sortKey.value = key
    sortDesc.value = false
  }
}

// 종합 막대 그래프용 데이터 (정렬 기준과 관계 없이 항상 순위순 고정하여 그래프가 튀는 현상 방지)
const chartData = computed(() => {
  const list = performances.value
  return {
    labels: list.map(p => p.employeeName),
    datasets: [
      {
        label: '당월 계약 건수',
        data: list.map(p => p.thisMonthCount),
        backgroundColor: list.map(p => {
          if (p.groupCode === 'TOP') return 'rgba(239, 68, 68, 0.85)'
          if (p.groupCode === 'BOTTOM') return 'rgba(59, 130, 246, 0.85)'
          return 'rgba(148, 163, 184, 0.7)'
        }),
        borderRadius: 4,
        barPercentage: 0.6
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
        label: (context: any) => ` 당월: ${context.parsed.y}건`
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
      ticks: { color: '#64748b', font: { size: 11, family: 'Pretendard, sans-serif' } }
    }
  }
}
</script>

<template>
  <div v-if="isOpen" class="modal-backdrop" @click="$emit('close')">
    <div class="modal-card modal-card--wide" @click.stop>
      <div class="modal-header">
        <div>
          <h2 class="modal-title">전체 영업사원 성과 조회</h2>
          <p class="modal-subtitle">지점 소속 전체 직원의 성과 분석 그래프 및 상세 현황</p>
        </div>
        <button class="close-button" type="button" @click="$emit('close')">
          &times;
        </button>
      </div>

      <div class="modal-body">
        <div v-if="isLoading" class="loading-state">
          <div class="spinner"></div>
          <p>전체 성과 데이터를 불러오는 중입니다...</p>
        </div>

        <div v-else class="modal-content">
          <!-- 상단 전체 막대 그래프 -->
          <div class="chart-section card">
            <div class="card-body">
              <h4 class="chart-title">지점원 당월 성과 분포</h4>
              <div class="chart-container">
                <Bar :data="chartData" :options="chartOptions" />
              </div>
            </div>
          </div>

          <!-- 하단 상세 데이터 테이블 -->
          <div class="table-section">
            <div class="table-wrapper">
              <table class="data-table">
                <thead>
                  <tr>
                    <th class="sortable-header" @click="handleSort('rank')">
                      순위 <span class="sort-indicator">{{ sortKey === 'rank' ? (sortDesc ? '▼' : '▲') : '' }}</span>
                    </th>
                    <th class="sortable-header" @click="handleSort('employeeName')">
                      이름 <span class="sort-indicator">{{ sortKey === 'employeeName' ? (sortDesc ? '▼' : '▲') : '' }}</span>
                    </th>
                    <th>직급</th>
                    <th class="text-right sortable-header" @click="handleSort('thisMonthCount')">
                      당월 실적 <span class="sort-indicator">{{ sortKey === 'thisMonthCount' ? (sortDesc ? '▼' : '▲') : '' }}</span>
                    </th>
                    <th class="text-right sortable-header" @click="handleSort('monthlyTargetCount')">
                      월 목표 <span class="sort-indicator">{{ sortKey === 'monthlyTargetCount' ? (sortDesc ? '▼' : '▲') : '' }}</span>
                    </th>
                    <th class="text-right sortable-header" @click="handleSort('targetDifference')">
                      목표 대비 <span class="sort-indicator">{{ sortKey === 'targetDifference' ? (sortDesc ? '▼' : '▲') : '' }}</span>
                    </th>
                    <th class="text-right sortable-header" @click="handleSort('annualCount')">
                      연간 누적 <span class="sort-indicator">{{ sortKey === 'annualCount' ? (sortDesc ? '▼' : '▲') : '' }}</span>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in sortedPerformances" :key="item.employeeId">
                    <td class="font-bold text-slate-500">{{ item.rank }}위</td>
                    <td>
                      <span class="badge badge-group" :class="{
                        'badge-top': item.groupCode === 'TOP',
                        'badge-bottom': item.groupCode === 'BOTTOM'
                      }">
                        {{ item.employeeName }}
                      </span>
                    </td>
                    <td class="text-slate-500">{{ item.positionName }}</td>
                    <td class="text-right font-bold text-slate-800">{{ item.thisMonthCount }}건</td>
                    <td class="text-right text-slate-600">{{ item.monthlyTargetCount }}건</td>
                    <td class="text-right font-bold">
                      <span :class="item.targetDifference >= 0 ? 'text-rose-500' : 'text-blue-500'">
                        {{ item.targetDifference >= 0 ? '+' : '' }}{{ item.targetDifference }}
                      </span>
                    </td>
                    <td class="text-right font-bold text-slate-800">{{ item.annualCount }}건</td>
                  </tr>
                </tbody>
              </table>
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

.modal-card--wide {
  width: min(860px, 95vw) !important;
  max-width: none !important;
}

.loading-state {
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
  gap: 24px;
}

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

.table-wrapper {
  overflow-x: auto;
  max-height: 320px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.sortable-header {
  cursor: pointer;
  user-select: none;
  transition: background-color 100ms ease;
}

.sortable-header:hover {
  background-color: #e2e8f0;
}

.sort-indicator {
  display: inline-block;
  width: 10px;
  margin-left: 2px;
  font-size: 10px;
  color: var(--color-primary);
}

.badge-group {
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 700;
}

.badge-top {
  border-left: 3px solid #ef4444;
}

.badge-bottom {
  border-left: 3px solid #3b82f6;
}

.text-right {
  text-align: right;
}

.text-center {
  text-align: center;
}

.font-bold {
  font-weight: 700;
}

.text-slate-500 {
  color: #64748b;
}

.text-slate-600 {
  color: #475569;
}

.text-slate-800 {
  color: #1e293b;
}
</style>
