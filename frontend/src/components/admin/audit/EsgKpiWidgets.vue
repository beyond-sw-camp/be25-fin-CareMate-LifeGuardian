<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  carbonKg: number | null
  costSaved: number | null
  totalAuditLogs: number | null
  totalPiiCount: number | null
}>()

const formatNumber = (num: number | null) => {
  if (num === null || num === undefined) return '-'
  return num.toLocaleString()
}

const carbonPineTrees = computed(() => {
  if (props.carbonKg === null || props.carbonKg === undefined) return 0
  // 1그루당 6.6kg의 이산화탄소를 흡수하는 것으로 환산
  return Math.round(props.carbonKg / 6.6)
})
</script>

<template>
  <div class="esg-kpi-grid">
    <!-- 1. 누적 탄소 절감량 카드 -->
    <div class="card esg-kpi-card">
      <div class="card-body">
        <div class="esg-kpi-card__header">
          <span class="esg-kpi-card__title">누적 탄소 절감량</span>
          <span class="esg-badge esg-badge-green">🌿 ESG 지표</span>
        </div>
        <div class="esg-kpi-card__value-wrapper">
          <h3 class="esg-kpi-card__value">
            {{ carbonKg !== null ? carbonKg.toFixed(1) : '-' }}
            <span class="esg-kpi-card__unit">kg CO₂</span>
          </h3>
          <div class="esg-kpi-card__subtext">
            <span>🌲 소나무 <strong>{{ carbonPineTrees }}그루</strong> 식재 효과</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. 누적 인프라 비용 절감 카드 -->
    <div class="card esg-kpi-card">
      <div class="card-body">
        <div class="esg-kpi-card__header">
          <span class="esg-kpi-card__title">누적 인프라 비용 절감</span>
          <span class="esg-badge esg-badge-blue">💡 전력 최적화</span>
        </div>
        <div class="esg-kpi-card__value-wrapper">
          <h3 class="esg-kpi-card__value">
            ₩ {{ formatNumber(costSaved) }}
          </h3>
          <div class="esg-kpi-card__subtext">
            <span>피크컷 시간대 배치 시프팅 효과 누적</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 총 보안 감사 로그 카드 -->
    <div class="card esg-kpi-card">
      <div class="card-body">
        <div class="esg-kpi-card__header">
          <span class="esg-kpi-card__title">총 보안 감사 로그</span>
          <span class="esg-badge esg-badge-orange">🔒 보안 통제</span>
        </div>
        <div class="esg-kpi-card__value-wrapper">
          <h3 class="esg-kpi-card__value">
            {{ formatNumber(totalAuditLogs) }}
            <span class="esg-kpi-card__unit">건</span>
          </h3>
          <div class="esg-kpi-card__subtext">
            <span>시스템 중요 행위 감시 로그 누적</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 4. 퇴사자 PII 보관 현황 카드 -->
    <div class="card esg-kpi-card">
      <div class="card-body">
        <div class="esg-kpi-card__header">
          <span class="esg-kpi-card__title">퇴사자 PII 보관 현황</span>
          <span class="esg-badge esg-badge-gray">📁 컴플라이언스</span>
        </div>
        <div class="esg-kpi-card__value-wrapper">
          <h3 class="esg-kpi-card__value">
            {{ formatNumber(totalPiiCount) }}
            <span class="esg-kpi-card__unit">건</span>
          </h3>
          <div class="esg-kpi-card__subtext">
            <span>안전한 보안 테이블로 격리 보관 중</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.esg-kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.esg-kpi-card {
  position: relative;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.esg-kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
}

.esg-kpi-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.esg-kpi-card__title {
  font-size: 13px;
  font-weight: 800;
  color: var(--color-text-muted);
}

.esg-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: var(--radius-pill);
  font-size: 10px;
  font-weight: 800;
}

.esg-badge-green {
  background-color: #ecfdf5;
  color: #059669;
}

.esg-badge-blue {
  background-color: color-mix(in srgb, var(--color-primary) 10%, white);
  color: var(--color-primary);
}

.esg-badge-orange {
  background-color: #fff7ed;
  color: #ea580c;
}

.esg-badge-gray {
  background-color: #f8fafc;
  color: #475569;
}

.esg-kpi-card__value-wrapper {
  display: flex;
  flex-direction: column;
}

.esg-kpi-card__value {
  margin: 0 0 4px;
  font-size: 26px;
  font-weight: 900;
  color: var(--color-text);
  line-height: 1.2;
}

.esg-kpi-card__unit {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-muted);
  margin-left: 2px;
}

.esg-kpi-card__subtext {
  font-size: 11px;
  color: var(--color-text-subtle);
  font-weight: 700;
}

.esg-kpi-card__subtext strong {
  color: #059669;
}

@media (max-width: 1024px) {
  .esg-kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .esg-kpi-grid {
    grid-template-columns: 1fr;
  }
}
</style>
