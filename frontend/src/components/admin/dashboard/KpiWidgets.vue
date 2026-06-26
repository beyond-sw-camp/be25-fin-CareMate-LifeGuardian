<script setup lang="ts">
import type { BranchAnnualContracts, BranchMonthlyContracts } from '@/api/admin'

defineProps<{
  annualData: BranchAnnualContracts | null
  monthlyData: BranchMonthlyContracts | null
}>()

const formatNumber = (num: number | undefined) => {
  if (num === undefined) return '0'
  return num.toLocaleString()
}
</script>

<template>
  <div class="kpi-grid">
    <!-- 연간 실적 카드 -->
    <div class="card kpi-card">
      <div class="card-body">
        <div class="kpi-card__header">
          <span class="kpi-card__title">지점 연간 계약 현황</span>
          <span v-if="annualData" class="badge badge-success">
            목표 달성률 {{ annualData.targetAchievementRate }}%
          </span>
        </div>
        <div class="kpi-card__value-wrapper">
          <h3 class="kpi-card__value">
            {{ annualData ? formatNumber(annualData.currentYearCount) : '-' }}
            <span class="kpi-card__unit">건</span>
          </h3>
          <div class="kpi-card__sub-metrics" v-if="annualData">
            <span class="sub-metric-label">연간 목표:</span>
            <span class="sub-metric-value">{{ formatNumber(annualData.annualTargetCount) }}건</span>
            <span class="sub-divider">|</span>
            <span class="sub-metric-label">전년 누적:</span>
            <span class="sub-metric-value">{{ formatNumber(annualData.previousYearCount) }}건</span>
            <span class="sub-divider">|</span>
            <span class="sub-metric-label">YoY:</span>
            <span class="sub-metric-value" :class="annualData.yoyGrowthRate >= 0 ? 'yoy-up' : 'yoy-down'">
              {{ annualData.yoyGrowthRate >= 0 ? '+' : '' }}{{ annualData.yoyGrowthRate }}%
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 월간 실적 카드 -->
    <div class="card kpi-card">
      <div class="card-body">
        <div class="kpi-card__header">
          <span class="kpi-card__title">지점 월간 계약 현황</span>
          <span v-if="monthlyData" class="badge" :class="monthlyData.momDifferenceCount >= 0 ? 'badge-success' : 'badge-danger'">
            전월 대비 {{ monthlyData.momDifferenceCount >= 0 ? '+' : '' }}{{ monthlyData.momDifferenceCount }}건
          </span>
        </div>
        <div class="kpi-card__value-wrapper">
          <h3 class="kpi-card__value">
            {{ monthlyData ? formatNumber(monthlyData.currentMonthCount) : '-' }}
            <span class="kpi-card__unit">건</span>
          </h3>
          <div class="kpi-card__sub-metrics" v-if="monthlyData">
            <span class="sub-metric-label">활성 사원:</span>
            <span class="sub-metric-value">{{ monthlyData.activeSalesUserCount }}명</span>
            <span class="sub-divider">|</span>
            <span class="sub-metric-label">인당 평균 실적:</span>
            <span class="sub-metric-value">{{ monthlyData.averagePerUser.toFixed(1) }}건</span>
            <span class="sub-divider">|</span>
            <span class="sub-metric-label">전월 실적:</span>
            <span class="sub-metric-value">{{ formatNumber(monthlyData.previousMonthCount) }}건</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.kpi-card {
  position: relative;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
}

.kpi-card__header {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.kpi-card__title {
  font-size: 15px;
  font-weight: 800;
  color: var(--color-text-muted);
}

.kpi-card__value-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.kpi-card__value {
  margin: 0 0 10px;
  font-size: 44px;
  font-weight: 900;
  color: var(--color-text);
  line-height: 1;
}

.kpi-card__unit {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-muted);
  margin-left: 4px;
}

.kpi-card__sub-metrics {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.sub-metric-label {
  font-weight: 700;
}

.sub-metric-value {
  font-weight: 800;
  color: var(--color-text);
}

.sub-divider {
  color: var(--color-border);
  font-size: 10px;
}

.yoy-up {
  color: #ef4444;
}

.yoy-down {
  color: #3b82f6;
}

@media (max-width: 768px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }
}
</style>
