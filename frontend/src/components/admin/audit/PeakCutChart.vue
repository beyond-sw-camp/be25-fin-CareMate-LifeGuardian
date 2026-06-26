<script setup lang="ts">
import { computed } from 'vue'
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
import type { HourlyPowerProfile } from '@/api/admin'

ChartJS.register(Title, Tooltip, Legend, PointElement, LineElement, CategoryScale, LinearScale, Filler)

const props = defineProps<{
  profiles: HourlyPowerProfile[]
  targetDate: string
  isLoading: boolean
}>()

const emit = defineEmits<{
  (e: 'changeDate', date: string): void
}>()

const handleDateChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.value) {
    emit('changeDate', input.value)
  }
}

const chartData = computed(() => {
  const list = props.profiles || []
  return {
    labels: list.map(p => p.hour),
    datasets: [
      {
        label: '피크컷 적용 후 실제 부하율 (%)',
        data: list.map(p => p.optimizedLoad),
        borderColor: '#10b981', // Emerald green
        backgroundColor: 'rgba(16, 185, 129, 0.08)',
        fill: true,
        tension: 0.35,
        borderWidth: 2.5,
        pointBackgroundColor: '#10b981',
        pointBorderColor: '#ffffff',
        pointBorderWidth: 2,
        pointRadius: 4,
        pointHoverRadius: 6
      },
      {
        label: '전력 최적화 전 예상 부하율 (%)',
        data: list.map(p => p.traditionalLoad),
        borderColor: '#f59e0b', // Amber/Yellow
        backgroundColor: 'transparent',
        fill: false,
        borderDash: [5, 5],
        tension: 0.35,
        borderWidth: 2,
        pointBackgroundColor: '#f59e0b',
        pointBorderColor: '#ffffff',
        pointBorderWidth: 1.5,
        pointRadius: 3,
        pointHoverRadius: 5
      }
    ]
  }
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top' as const,
      labels: {
        boxWidth: 12,
        font: {
          family: 'Pretendard, sans-serif',
          size: 11,
          weight: 'bold' as const
        },
        color: '#475569'
      }
    },
    tooltip: {
      backgroundColor: '#1e293b',
      titleFont: { size: 12, weight: 'bold' as const },
      bodyFont: { size: 11 },
      padding: 10,
      cornerRadius: 6,
      callbacks: {
        label: (context: any) => ` 부하율: ${context.parsed.y.toFixed(1)}%`
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
      min: 0,
      max: 100,
      border: { dash: [4, 4] },
      grid: { color: '#f1f5f9' },
      ticks: {
        color: '#64748b',
        font: { size: 11, family: 'Pretendard, sans-serif' },
        callback: (value: any) => `${value}%`
      }
    }
  }
}
</script>

<template>
  <div class="card chart-card">
    <div class="card-header chart-header">
      <div>
        <h3 class="section-title">24시간 인프라 부하 및 피크 컷(Peak-Cut) 프로파일</h3>
        <p class="section-desc">지점 내 서버 부하(CPU 사용량) 분산 전후 전력 효율성 비교 모니터링</p>
      </div>
      <div class="date-selector">
        <label for="peak-cut-date" class="date-label">조회 기준일:</label>
        <input 
          id="peak-cut-date" 
          type="date" 
          class="input date-input" 
          :value="targetDate" 
          @change="handleDateChange"
          :disabled="isLoading"
        />
      </div>
    </div>

    <div class="card-body chart-body-wrapper">
      <div v-if="isLoading" class="chart-loading-state">
        <div class="spinner"></div>
        <p>전력 프로파일 로딩 중...</p>
      </div>
      <div v-else class="chart-container">
        <Line :data="chartData" :options="chartOptions" />
      </div>

      <!-- 피크컷 원리 설명 배너 -->
      <div class="peak-cut-banner">
        <div class="banner-icon">⚡</div>
        <div class="banner-text">
          <strong>피크컷(Peak-Cut) 부하 분산 효과:</strong> 
          주간 시간대 전력 사용 피크를 방어하기 위해 대규모 통계 배치 및 데이터 무결성 검증 처리를 전력 소모량이 적은 심야 시간대(02:00 ~ 05:00)로 이송(Shifting)하여 인프라 가동 전력의 평탄화를 완수했습니다.
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chart-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 420px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.section-title {
  margin: 0;
  font-size: 15px;
  font-weight: 900;
  color: var(--color-text);
}

.section-desc {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--color-text-muted);
}

.date-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-label {
  font-size: 12px;
  font-weight: 800;
  color: var(--color-text-muted);
}

.date-input {
  width: 140px;
  height: 32px;
  font-size: 12px;
  padding: 0 8px;
}

.chart-body-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px;
}

.chart-loading-state {
  flex: 1;
  display: grid;
  place-items: center;
  padding: 60px 0;
  color: var(--color-text-muted);
  font-weight: 700;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.chart-container {
  position: relative;
  width: 100%;
  height: 240px;
}

.peak-cut-banner {
  display: flex;
  gap: 10px;
  background-color: #f0fdf4; /* Emerald light background */
  border: 1px solid #dcfce7;
  border-radius: var(--radius-sm);
  padding: 12px 14px;
}

.banner-icon {
  font-size: 18px;
  line-height: 1;
  color: #10b981;
}

.banner-text {
  font-size: 11.5px;
  line-height: 1.5;
  color: #065f46;
}

.banner-text strong {
  font-weight: 800;
  color: #047857;
}
</style>
