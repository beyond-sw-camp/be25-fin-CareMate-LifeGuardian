<script setup lang="ts">
import type { SalesSummary } from '@/api/sales'

// 영업현황 상단 KPI 카드에 필요한 월 실적 요약 데이터입니다.
defineProps<{
  summary: SalesSummary | null
}>()
</script>

<template>
  <section class="sales-summary" aria-label="영업 실적 요약">
    <article class="card sales-summary__count-card">
      <p class="sales-summary__label">월 계약 수</p>
      <strong class="sales-summary__count">{{ summary?.contractCount ?? 0 }}<span>건</span></strong>
    </article>

    <article class="card sales-summary__target-card">
      <p class="sales-summary__label">{{ summary?.month ?? '-' }}월 실적 목표 {{ summary?.targetCount ?? 0 }}건</p>
      <div class="sales-summary__progress-track">
        <div
          class="sales-summary__progress-fill"
          :style="{ width: `${Math.min(summary?.achievementRate ?? 0, 100)}%` }"
        ></div>
      </div>
      <p class="sales-summary__caption">목표 달성률 {{ summary?.achievementRate ?? 0 }}%</p>
    </article>
  </section>
</template>

<style scoped>
.sales-summary {
  display: grid;
  grid-template-columns: 118px 1fr;
  gap: 10px;
  margin-bottom: 12px;
}

.sales-summary__count-card,
.sales-summary__target-card {
  min-height: 64px;
  border: 1px solid #e3e8f0;
  box-shadow: none;
  padding: 8px 12px;
}

.sales-summary__label,
.sales-summary__caption {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 10px;
  font-weight: 700;
}

.sales-summary__count {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 3px;
  color: #1d2738;
  font-size: 32px;
  font-weight: 800;
  line-height: 1.08;
}

.sales-summary__count span {
  font-size: 13px;
  font-weight: 800;
}

.sales-summary__progress-track {
  height: 10px;
  margin: 10px 20px 7px 0;
  overflow: hidden;
  border-radius: var(--radius-pill);
  background: #e9eef5;
}

.sales-summary__progress-fill {
  width: 78%;
  height: 100%;
  border-radius: inherit;
  background: var(--color-primary);
}
</style>
