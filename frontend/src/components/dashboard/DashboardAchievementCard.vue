<script setup lang="ts">
import { computed } from 'vue'
import { Doughnut } from 'vue-chartjs'
import {
  ArcElement,
  Chart as ChartJS,
  Legend,
  Tooltip,
} from 'chart.js'

import type { DashboardAchievement } from '@/api/dashboard'

ChartJS.register(
  ArcElement,
  Tooltip,
  Legend,
)

const props = defineProps<{
  achievement: DashboardAchievement | null
}>()

const primaryColor = () => {
  if (typeof window === 'undefined') return '#F37021'
  return getComputedStyle(document.documentElement).getPropertyValue('--color-primary').trim() || '#F37021'
}

const chartData = computed(() => {
  const completed = props.achievement?.completedContractCount ?? 0
  const target = props.achievement?.targetContractCount ?? 0

  const remaining = Math.max(target - completed, 0)

  const hasChartValue = completed > 0 || remaining > 0

  return {
    datasets: [
      {
        data: hasChartValue ? [completed, remaining] : [0, 1],
        backgroundColor: hasChartValue
          ? [primaryColor(), '#eceef3']
          : ['#eceef3', '#eceef3'],
        borderWidth: 0,
      },
    ],
  }
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  cutout: '72%',
  plugins: {
    legend: {
      display: false,
    },
    tooltip: {
      enabled: false,
    },
  },
}
</script>

<template>
  <section class="achievement-card">
    <div class="achievement-card__header">
      <h3>이달의 영업 달성률</h3>

      <div class="achievement-card__legend">
        <span class="achievement-card__legend-item">
          <span class="achievement-card__legend-dot achievement-card__legend-dot--target" />
          전체 고객
        </span>

        <span class="achievement-card__legend-item">
          <span class="achievement-card__legend-dot achievement-card__legend-dot--completed" />
          계약 고객
        </span>
      </div>
    </div>

    <div class="achievement-card__chart">
      <Doughnut
        :data="chartData"
        :options="chartOptions"
      />

      <div class="achievement-card__center">
        <span>{{ new Date().getMonth() + 1 }}월</span>

        <strong>
          {{ achievement?.achievementRate ?? 0 }}%
        </strong>
      </div>
    </div>

    <!--
    <div class="achievement-card__footer">
      <div class="achievement-card__item">
        <span>목표 계약</span>
        <strong>
          {{ achievement?.targetContractCount ?? 0 }}건
        </strong>
      </div>

      <div class="achievement-card__item">
        <span>완료 계약</span>
        <strong>
          {{ achievement?.completedContractCount ?? 0 }}건
        </strong>
      </div>
    </div>
-->
  </section>
</template>

<style scoped>
.achievement-card {
  display: flex;
  height: 100%;
  min-height: 250px;
  flex-direction: column;
  border: 1px solid #e3e8f0;
  border-radius: 8px;
  background: #ffffff;
  padding: 18px;
}

.achievement-card__header {
  margin-bottom: 12px;
}

.achievement-card__header h3 {
  margin: 0 0 10px;
  color: #263142;
  font-size: 18px;
  font-weight: 900;
}

.achievement-card__legend {
  display: flex;
  gap: 16px;
}

.achievement-card__legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #7f8999;
  font-size: 12px;
}

.achievement-card__legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.achievement-card__legend-dot--target {
  background: #eceef3;
}

.achievement-card__legend-dot--completed {
  background: var(--color-primary);
}

.achievement-card__chart {
  position: relative;
  width: 170px;
  height: 170px;
  margin: 9px auto 0;
}

.achievement-card__center {
  position: absolute;
  inset: 0;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  padding-top: 0px;
}

.achievement-card__center span {
  color: #7f8999;
  font-size: 15px;
  font-weight: 700;
}

.achievement-card__center strong {
  color: #172033;
  font-size: 25px;
  font-weight: 900;
}

/*
.achievement-card__footer {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 20px;
}

.achievement-card__item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.achievement-card__item span {
  color: #7f8999;
  font-size: 12px;
}

.achievement-card__item strong {
  color: #263142;
  font-size: 18px;
  font-weight: 900;
}
  */

</style>
