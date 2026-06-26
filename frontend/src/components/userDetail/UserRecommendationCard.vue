<script setup lang="ts">
import { computed } from 'vue'
import type { InsuranceRecommendation, RecommendationCoverage } from '@/api/userDetail'

const props = defineProps<{
  ruleRecommendation: InsuranceRecommendation | null
  aiRecommendation: InsuranceRecommendation | null
  isLoading: boolean
  ruleErrorMessage?: string
  aiErrorMessage?: string
}>()

const recommendationCards = computed(() => [
  {
    key: 'rule',
    label: '자체 룰 엔진',
    accent: 'rule',
    recommendation: props.ruleRecommendation,
    emptyText: '자체 룰 엔진 추천 결과가 아직 없습니다.',
  },
  {
    key: 'ai',
    label: props.aiRecommendation?.sourceName ?? 'AI 추천',
    accent: 'ai',
    recommendation: props.aiRecommendation,
    emptyText: 'AI 추천 결과가 아직 없습니다.',
  },
])

const formatCurrency = (value?: number) => {
  if (typeof value !== 'number' || Number.isNaN(value)) return '-'
  return `${value.toLocaleString('ko-KR')}원`
}

const coverageRank = (coverage: RecommendationCoverage, index: number) => coverage.rank ?? index + 1

const formatScore = (value?: number) => {
  if (typeof value !== 'number' || Number.isNaN(value)) return '-'
  return `${value.toLocaleString('ko-KR')}점`
}

const coverageScoreWidth = (coverage: RecommendationCoverage) => {
  const score = coverage.score ?? 0
  return `${Math.max(8, Math.min(100, score))}%`
}

const categoryBadgeClass = (coverage: RecommendationCoverage) => {
  return coverage.categoryCode ? `coverage-tag--${coverage.categoryCode.toLowerCase().replaceAll('_', '-')}` : ''
}
</script>

<template>
  <section class="recommendation-section">
    <header class="section-header">
      <div class="section-title">
        <span class="section-icon" aria-hidden="true">☆</span>
        <h2>보험 추천</h2>
        <p>자체 룰 엔진으로 분석한 맞춤 추천 결과입니다</p>
      </div>
    </header>

    <div v-if="isLoading" class="recommendation-state card">
      보험 추천 정보를 분석하는 중입니다.
    </div>

    <div v-else class="recommendation-grid">
      <article
        v-for="card in recommendationCards"
        :key="card.key"
        class="recommendation-card card"
        :class="`recommendation-card--${card.accent}`"
      >
        <header class="card-header">
          <div class="card-source">
            <span class="source-icon" aria-hidden="true">{{ card.key === 'ai' ? 'AI' : 'RE' }}</span>
            <span>{{ card.label }}</span>
          </div>
          <div class="card-metrics">
            <span>월 보험료</span>
            <strong>{{ formatCurrency(card.recommendation?.monthlyPremium) }}</strong>
          </div>
          <div v-if="card.key !== 'ai'" class="card-metrics">
            <span>추천 점수</span>
            <strong>{{ formatScore(card.recommendation?.recommendationScore) }}</strong>
          </div>
        </header>

        <div v-if="!card.recommendation" class="card-empty">
          {{ card.key === 'rule' ? ruleErrorMessage || card.emptyText : aiErrorMessage || card.emptyText }}
        </div>

        <template v-else>
          <div class="recommendation-summary">
            <h3>{{ card.recommendation.planName || card.recommendation.title || '맞춤 추천 플랜' }}</h3>
            <p v-if="card.recommendation.summary">
              <span class="summary-icon" aria-hidden="true">i</span>
              <span>{{ card.recommendation.summary }}</span>
            </p>
          </div>

          <div class="coverage-table">
            <div class="coverage-head" :class="{ 'coverage-head--no-score': card.key === 'ai' }">
              <span>#</span>
              <span>담보명</span>
              <span v-if="card.key !== 'ai'">점수</span>
              <span>보험료</span>
            </div>

            <div
              v-for="(coverage, index) in card.recommendation.coverages"
              :key="`${coverageRank(coverage, index)}-${coverage.coverageName}`"
              class="coverage-row"
              :class="{ 'coverage-row--no-score': card.key === 'ai' }"
            >
              <span class="coverage-rank">{{ coverageRank(coverage, index) }}</span>
              <div class="coverage-name">
                <strong :title="coverage.coverageName">{{ coverage.coverageName }}</strong>
                <div v-if="coverage.tags?.length" class="coverage-tags">
                  <span v-for="tag in coverage.tags" :key="tag" :class="categoryBadgeClass(coverage)">
                    {{ tag }}
                  </span>
                </div>
              </div>
              <div v-if="card.key !== 'ai'" class="score-cell">
                <span class="score-bar">
                  <i :style="{ width: coverageScoreWidth(coverage) }"></i>
                </span>
                <b>{{ formatScore(coverage.score) }}</b>
              </div>
              <span class="premium-cell">{{ formatCurrency(coverage.premium) }}</span>
            </div>
          </div>
        </template>
      </article>
    </div>
  </section>
</template>

<style scoped>
.recommendation-section {
  display: grid;
  gap: 12px;
}

.section-header,
.section-title,
.card-header,
.card-source,
.card-metrics {
  display: flex;
  align-items: center;
}

.section-header {
  justify-content: space-between;
}

.section-title {
  gap: 8px;
  min-width: 0;
}

.section-icon {
  color: #ff9d00;
  font-size: 18px;
  font-weight: 900;
}

.section-title h2,
.recommendation-summary h3,
.section-title p {
  margin: 0;
}

.section-title h2 {
  color: var(--color-text);
  font-size: 17px;
  font-weight: 900;
  letter-spacing: 0;
}

.section-title p {
  color: var(--color-text-subtle);
  font-size: 12px;
  font-weight: 700;
}

.recommendation-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.recommendation-card {
  overflow: hidden;
  padding: 0;
}

.card-header {
  gap: 18px;
  border-bottom: 1px solid #e4e8ef;
  background: #f8fbff;
  padding: 16px 22px;
}

.recommendation-card--ai .card-header {
  border-bottom-color: #ded2ff;
  background: #f4f0ff;
}

.card-source {
  flex: 1;
  gap: 10px;
  min-width: 0;
  color: #172033;
  font-size: 13px;
  font-weight: 900;
}

.source-icon {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: var(--radius-pill);
  background: #e9eff7;
  color: #526070;
  font-size: 11px;
  font-weight: 950;
}

.recommendation-card--ai .source-icon {
  background: #e9ddff;
  color: #7c1dff;
}

.card-metrics {
  flex-direction: column;
  align-items: flex-end;
  gap: 3px;
  min-width: 84px;
}

.card-metrics span {
  color: var(--color-text-subtle);
  font-size: 11px;
  font-weight: 700;
}

.card-metrics strong {
  color: #ff8a00;
  font-size: 16px;
  font-weight: 950;
}

.recommendation-card--ai .card-metrics strong {
  color: #7c1dff;
}

.recommendation-summary {
  display: grid;
  gap: 12px;
  padding: 16px 22px;
}

.recommendation-summary h3 {
  color: #121826;
  font-size: 15px;
  font-weight: 900;
  letter-spacing: 0;
}

.recommendation-summary p,
.card-empty {
  border: 1px solid #edf0f5;
  border-radius: 8px;
  background: #f8fafc;
  color: #566173;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.6;
  padding: 12px 14px;
}

.recommendation-summary p {
  display: grid;
  grid-template-columns: 16px minmax(0, 1fr);
  gap: 8px;
}

.summary-icon {
  display: grid;
  width: 14px;
  height: 14px;
  place-items: center;
  border: 1px solid #a7b2c4;
  border-radius: var(--radius-pill);
  color: #8793a5;
  font-size: 10px;
  font-weight: 900;
  line-height: 1;
}

.card-empty {
  display: grid;
  min-height: 180px;
  place-items: center;
  margin: 18px 20px;
  text-align: center;
}

.coverage-table {
  display: grid;
  padding: 0 22px 16px;
}

.coverage-head,
.coverage-row {
  display: grid;
  grid-template-columns: 30px minmax(180px, 1fr) 130px 86px;
  align-items: center;
  gap: 10px;
}

.coverage-head--no-score,
.coverage-row--no-score {
  grid-template-columns: 30px minmax(180px, 1fr) 86px;
}

.coverage-head {
  border-bottom: 1px solid #edf0f5;
  color: var(--color-text-subtle);
  font-size: 11px;
  font-weight: 800;
  padding: 0 0 8px;
}

.coverage-row {
  min-height: 66px;
  border-bottom: 1px solid #edf0f5;
  color: #172033;
  font-size: 13px;
  padding: 10px 0;
}

.coverage-rank {
  color: #8a94a6;
  font-size: 12px;
}

.coverage-name {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.coverage-name strong {
  overflow: hidden;
  max-width: 100%;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.coverage-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.coverage-tags span {
  border-radius: 5px;
  background: #e9f8ef;
  color: #057647;
  padding: 3px 7px;
  font-size: 11px;
  font-weight: 800;
}

.coverage-tags .coverage-tag--cat-critical-bal {
  background: #ffe8ec;
  color: #b4233b;
}

.coverage-tags .coverage-tag--cat-dent-eye {
  background: #e7f0ff;
  color: #1d5db8;
}

.coverage-tags .coverage-tag--cat-hosp-surg {
  background: #e8f7ee;
  color: #057647;
}

.coverage-tags .coverage-tag--cat-infect-group {
  background: #fff3d9;
  color: #a15c00;
}

.coverage-tags .coverage-tag--cat-sh-injury {
  background: #f0e9ff;
  color: #6941c6;
}

.premium-cell {
  justify-self: end;
  color: #263244;
  font-size: 12px;
}

.score-cell {
  display: grid;
  justify-items: end;
  gap: 4px;
}

.score-bar {
  display: block;
  width: 90px;
  height: 7px;
  overflow: hidden;
  border-radius: var(--radius-pill);
  background: #edf1f6;
}

.score-bar i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #ffb000;
}

.recommendation-card--ai .score-bar i {
  background: #8b4dff;
}

.score-cell b {
  color: #8a94a6;
  font-size: 11px;
  font-weight: 700;
}

.recommendation-state {
  display: grid;
  min-height: 180px;
  place-items: center;
  color: var(--color-text-muted);
  font-size: 14px;
  font-weight: 800;
}

@media (max-width: 1180px) {
  .recommendation-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .section-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .card-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .card-metrics {
    align-items: flex-start;
  }

  .coverage-head {
    display: none;
  }

  .coverage-row {
    grid-template-columns: 26px minmax(0, 1fr);
  }

  .coverage-head--no-score,
  .coverage-row--no-score {
    grid-template-columns: 26px minmax(0, 1fr);
  }

  .score-cell,
  .premium-cell {
    grid-column: 2;
    justify-self: start;
  }

  .score-cell {
    justify-items: start;
  }
}
</style>
