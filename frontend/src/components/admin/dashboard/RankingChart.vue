<script setup lang="ts">
import { computed } from 'vue'
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
import type { BranchSalesRanking } from '@/api/admin'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

const props = defineProps<{
  rankingData: BranchSalesRanking | null
}>()

defineEmits<{
  (e: 'showAllPerformances'): void
}>()

const performers = computed(() => {
  if (!props.rankingData) return []
  const tops = props.rankingData.topPerformers || []
  const bottoms = props.rankingData.bottomPerformers || []
  return [...tops, ...bottoms]
})

const hasData = computed(() => performers.value.length > 0)

const chartData = computed(() => {
  const list = performers.value
  return {
    labels: list.map(p => p.employeeName),
    datasets: [
      {
        label: '계약 건수',
        data: list.map(p => p.contractCount),
        backgroundColor: list.map(p => {
          const isTop = props.rankingData?.topPerformers.some(t => t.employeeId === p.employeeId)
          return isTop ? 'rgba(239, 68, 68, 0.85)' : 'rgba(59, 130, 246, 0.85)'
        }),
        borderRadius: 6,
        borderWidth: 0,
        barPercentage: 0.55
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
      titleFont: { size: 13, weight: 'bold' as const },
      bodyFont: { size: 12 },
      padding: 10,
      cornerRadius: 6,
      displayColors: false,
      callbacks: {
        label: (context: any) => ` 당월 계약: ${context.parsed.y}건`
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
        font: {
          family: 'Pretendard, sans-serif',
          weight: 'bold' as const
        }
      }
    },
    y: {
      border: {
        dash: [4, 4]
      },
      grid: {
        color: '#f1f5f9'
      },
      ticks: {
        color: '#64748b',
        font: {
          family: 'Pretendard, sans-serif'
        }
      }
    }
  }
}
</script>

<template>
  <div class="card ranking-card">
    <div class="card-header">
      <div class="ranking-header__title">
        <h3 class="section-title">당월 실적 상/하위 비교</h3>
        <p class="section-desc">지점 내 상위 3명(빨간색) vs 하위 3명(파란색) 실적 현황</p>
      </div>
      <button class="button-text" @click="$emit('showAllPerformances')">
        전체 영업사원 성과 보기 &gt;
      </button>
    </div>

    <div class="card-body ranking-body">
      <div v-if="!hasData" class="empty-state">
        <div class="empty-state__icon">📊</div>
        <p class="empty-state__text">이번 달 판매 실적이 아직 없습니다.</p>
      </div>
      <div v-else class="chart-container">
        <Bar :data="chartData" :options="chartOptions" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.ranking-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 380px;
}

.ranking-header__title {
  display: grid;
  gap: 2px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
  color: var(--color-text);
}

.section-desc {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-muted);
}

.button-text {
  border: none;
  background: none;
  color: var(--color-primary);
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  padding: 0;
  transition: opacity 120ms ease;
}

.button-text:hover {
  opacity: 0.8;
  text-decoration: underline;
}

.ranking-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 20px 20px;
}

.chart-container {
  position: relative;
  width: 100%;
  height: 280px;
}

.empty-state {
  display: grid;
  place-items: center;
  gap: 12px;
  text-align: center;
  color: var(--color-text-subtle);
}

.empty-state__icon {
  font-size: 48px;
}

.empty-state__text {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
}
</style>
