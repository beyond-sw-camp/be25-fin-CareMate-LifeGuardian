<script setup lang="ts">
import { computed } from 'vue'
import { insuranceCategoryName, type InsuranceRecommendation, type RecommendationCoverage } from '@/api/userDetail'

const props = defineProps<{
  ruleRecommendation: InsuranceRecommendation | null
  aiRecommendation: InsuranceRecommendation | null
  isLoading: boolean
  ruleErrorMessage?: string
  aiErrorMessage?: string
}>()

const ruleCandidateCoverages = computed(() => props.ruleRecommendation?.coverages ?? [])
const aiCandidateCoverages = computed(() => props.aiRecommendation?.coverages ?? [])

const formatCurrency = (value?: number) => {
  if (typeof value !== 'number' || Number.isNaN(value)) return '-'
  return `${value.toLocaleString('ko-KR')}원`
}

const categoryBadgeClass = (coverage: RecommendationCoverage) => {
  return coverage.categoryCode ? `coverage-tag--${coverage.categoryCode.toLowerCase().replaceAll('_', '-')}` : ''
}

const coverageCount = (recommendation: InsuranceRecommendation | null) =>
  recommendation?.coverages?.length ?? 0

const categoryNames = (recommendation: InsuranceRecommendation | null) => {
  const names =
    recommendation?.coverages.flatMap((coverage) => {
      if (coverage.tags?.length) return coverage.tags
      return coverage.categoryCode ? [insuranceCategoryName(coverage.categoryCode) ?? coverage.categoryCode] : []
    }) ?? []

  return [...new Set(names)].filter(Boolean)
}

const ruleCategoryNames = computed(() => categoryNames(props.ruleRecommendation))
const aiCategoryNames = computed(() => categoryNames(props.aiRecommendation))

const premiumSavings = computed(() => {
  const rulePremium = props.ruleRecommendation?.monthlyPremium
  const aiPremium = props.aiRecommendation?.monthlyPremium
  if (typeof rulePremium !== 'number' || typeof aiPremium !== 'number') return undefined
  return Math.max(0, rulePremium - aiPremium)
})

const premiumSavingsRate = computed(() => {
  const rulePremium = props.ruleRecommendation?.monthlyPremium
  if (!rulePremium || typeof premiumSavings.value !== 'number') return undefined
  return Math.round((premiumSavings.value / rulePremium) * 1000) / 10
})

const cheaperSummary = computed(() => {
  const rulePremium = props.ruleRecommendation?.monthlyPremium
  const aiPremium = props.aiRecommendation?.monthlyPremium

  if (typeof rulePremium !== 'number' || typeof aiPremium !== 'number') {
    return {
      title: '보험료 비교 준비 중',
      amount: '-',
      rate: '',
      description: '두 추천 결과의 보험료가 모두 확인되면 더 저렴한 추천을 안내합니다.',
    }
  }

  if (rulePremium === aiPremium) {
    return {
      title: '두 추천의 보험료가 같아요',
      amount: '차액 없음',
      rate: '',
      description: '보험료가 같아 담보 구성과 보장 범위를 중심으로 비교해 보세요.',
    }
  }

  const cheaperName = aiPremium < rulePremium ? 'AI 추천' : '룰 엔진 추천'
  const higherPremium = Math.max(rulePremium, aiPremium)
  const difference = Math.abs(rulePremium - aiPremium)
  const rate = higherPremium ? Math.round((difference / higherPremium) * 1000) / 10 : 0

  return {
    title: `${cheaperName}이 더 저렴해요`,
    amount: `${formatCurrency(difference)} 절약`,
    rate: `(${rate}% 절약)`,
    description: `${cheaperName} 기준으로 월 보험료가 더 낮습니다.`,
  }
})

const recommendationReasons = (recommendation: InsuranceRecommendation | null, fallback: string) => {
  if (recommendation?.summary) return [recommendation.summary]

  const coverageReasons =
    recommendation?.coverages
      ?.map((coverage) => coverage.reason)
      .filter((reason): reason is string => Boolean(reason))
      .slice(0, 3) ?? []

  return coverageReasons.length ? coverageReasons : [fallback]
}

const ruleReasons = computed(() =>
  recommendationReasons(props.ruleRecommendation, '룰 기반 기준으로 우선순위가 높은 보장을 추천했습니다.'),
)

const aiReasons = computed(() =>
  recommendationReasons(props.aiRecommendation, 'AI 문맥 분석으로 고객에게 적합한 보장 조합을 추천했습니다.'),
)
</script>

<template>
  <section class="recommendation-section">
    <div v-if="isLoading" class="recommendation-state card">
      보험 추천 정보를 분석하는 중입니다.
    </div>

    <template v-else>
    <div class="recommendation-summary-card card">
      <header class="summary-hero">
        <span class="summary-hero-icon" aria-hidden="true">✦</span>
        <div>
          <h2>추천 요약</h2>
          <p>AI 분석과 룰 엔진 결과를 종합하여 고객님께 최적화된 보장을 추천합니다.</p>
        </div>
      </header>

      <section class="ai-rule-compare-panel">
        <div class="premium-compare-visual">
          <article class="premium-result premium-result--rule">
            <span>룰 엔진 추천 <small>(통계 기반)</small></span>
            <strong>{{ formatCurrency(props.ruleRecommendation?.monthlyPremium) }}</strong>
            <small>추천 담보 {{ coverageCount(props.ruleRecommendation) }}개</small>
            <div class="premium-category-list">
              <b>카테고리</b>
              <div v-if="ruleCategoryNames.length" class="category-chip-list">
                <span v-for="category in ruleCategoryNames" :key="`rule-category-${category}`">
                  {{ category }}
                </span>
              </div>
              <p v-else>카테고리 정보 없음</p>
            </div>
          </article>

          <article class="premium-cheaper-summary">
            <span class="premium-compare-arrow premium-compare-arrow--left" aria-hidden="true">‹</span>
            <div class="premium-cheaper-bubble">
              <span aria-hidden="true">↓</span>
              <strong>{{ cheaperSummary.amount }}</strong>
              <em v-if="cheaperSummary.rate">{{ cheaperSummary.rate }}</em>
            </div>
            <span class="premium-compare-arrow premium-compare-arrow--right" aria-hidden="true">›</span>
            <p>{{ cheaperSummary.title }}</p>
          </article>

          <article class="premium-result premium-result--ai">
            <span>AI 추천 <small>(개인 맞춤)</small></span>
            <strong>{{ formatCurrency(props.aiRecommendation?.monthlyPremium) }}</strong>
            <b v-if="premiumSavings">▼ {{ formatCurrency(premiumSavings) }} 절약</b>
            <em v-if="premiumSavingsRate">({{ premiumSavingsRate }}% 절약)</em>
            <small>추천 담보 {{ coverageCount(props.aiRecommendation) }}개</small>
            <div class="premium-category-list">
              <b>카테고리</b>
              <div v-if="aiCategoryNames.length" class="category-chip-list">
                <span v-for="category in aiCategoryNames" :key="`ai-category-${category}`">
                  {{ category }}
                </span>
              </div>
              <p v-else>카테고리 정보 없음</p>
            </div>
          </article>
        </div>
      </section>
    </div>

    <div class="candidate-compare-grid">
      <article class="candidate-card candidate-card--rule card">
        <header class="candidate-header">
          <div class="candidate-title">
            <span class="source-icon" aria-hidden="true">RE</span>
            <strong>룰 엔진 추천</strong>
            <span class="analysis-chip analysis-chip--rule">통계 기반</span>
          </div>
          <div class="card-metrics">
            <span>월 보험료</span>
            <strong>{{ formatCurrency(props.ruleRecommendation?.monthlyPremium) }}</strong>
          </div>
        </header>
        <div v-if="!props.ruleRecommendation" class="card-empty">
          {{ ruleErrorMessage || '자체 룰 엔진 추천 결과가 아직 없습니다.' }}
        </div>
        <div v-else class="candidate-list">
          <section class="candidate-plan-summary">
            <h3>{{ props.ruleRecommendation.planName || props.ruleRecommendation.title || '입원 및 수술 특화 보장 맞춤 추천 플랜' }}</h3>
          </section>

          <section class="candidate-reason-box candidate-reason-box--rule">
            <span class="summary-icon" aria-hidden="true">i</span>
            <ul>
              <li v-for="reason in ruleReasons" :key="`rule-card-${reason}`">{{ reason }}</li>
            </ul>
          </section>

          <div class="candidate-table-head">
            <span>#</span>
            <span>담보명</span>
            <span>보험료</span>
          </div>

          <div
            v-for="(coverage, index) in ruleCandidateCoverages"
            :key="`rule-candidate-${coverage.coverageName}-${coverage.premium ?? ''}`"
            class="candidate-row"
          >
            <span class="coverage-rank">{{ coverage.rank ?? index + 1 }}</span>
            <div class="coverage-name">
              <strong :title="coverage.coverageName">{{ coverage.coverageName }}</strong>
              <div v-if="coverage.tags?.length" class="coverage-tags">
                <span v-for="tag in coverage.tags.slice(0, 1)" :key="tag" :class="categoryBadgeClass(coverage)">
                  {{ tag }}
                </span>
              </div>
            </div>
            <span class="coverage-premium">{{ formatCurrency(coverage.premium) }}</span>
          </div>
        </div>
      </article>

      <article class="candidate-card candidate-card--ai card">
        <header class="candidate-header">
          <div class="candidate-title">
            <span class="source-icon" aria-hidden="true">AI</span>
            <strong>AI RAG 맞춤 추천</strong>
            <span class="analysis-chip">개인 맞춤 분석 기반</span>
          </div>
          <div class="card-metrics">
            <span>월 보험료</span>
            <strong>{{ formatCurrency(props.aiRecommendation?.monthlyPremium) }}</strong>
          </div>
        </header>
        <div v-if="!props.aiRecommendation" class="card-empty">
          {{ aiErrorMessage || 'AI 추천 결과가 아직 없습니다.' }}
        </div>
        <div v-else class="candidate-list">
          <section class="candidate-plan-summary">
            <h3>{{ props.aiRecommendation.planName || props.aiRecommendation.title || '영유아 종합 케어 AI 맞춤 플랜' }}</h3>
          </section>

          <section class="candidate-reason-box candidate-reason-box--ai">
            <span class="summary-icon" aria-hidden="true">i</span>
            <ul>
              <li v-for="reason in aiReasons" :key="`ai-card-${reason}`">{{ reason }}</li>
            </ul>
          </section>

          <div class="candidate-table-head">
            <span>#</span>
            <span>담보명</span>
            <span>보험료</span>
          </div>

          <div
            v-for="(coverage, index) in aiCandidateCoverages"
            :key="`ai-candidate-${coverage.coverageName}-${coverage.premium ?? ''}`"
            class="candidate-row"
          >
            <span class="coverage-rank">{{ coverage.rank ?? index + 1 }}</span>
            <div class="coverage-name">
              <strong :title="coverage.coverageName">{{ coverage.coverageName }}</strong>
              <div v-if="coverage.tags?.length" class="coverage-tags">
                <span v-for="tag in coverage.tags.slice(0, 1)" :key="tag" :class="categoryBadgeClass(coverage)">
                  {{ tag }}
                </span>
              </div>
            </div>
            <span class="coverage-premium">{{ formatCurrency(coverage.premium) }}</span>
          </div>
        </div>
      </article>
    </div>
    </template>
  </section>
</template>

<style scoped>
.recommendation-section {
  --rule-color: rgb(243, 115, 33);
  --rule-soft-bg: rgba(243, 115, 33, 0.08);
  --rule-border: rgba(243, 115, 33, 0.38);
  --rule-chip-bg: rgba(243, 115, 33, 0.12);
  display: grid;
  gap: 12px;
}

.recommendation-dashboard {
  display: grid;
  gap: 16px;
  padding: 18px 20px;
}

.recommendation-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.draft-planner-grid {
  display: grid;
  grid-template-columns: minmax(280px, 0.95fr) minmax(420px, 1.1fr) minmax(280px, 0.95fr);
  gap: 18px;
}

.candidate-compare-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.recommendation-summary-card {
  display: grid;
  gap: 14px;
  padding: 16px 18px;
}

.summary-hero {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
  padding: 2px 0 12px;
}

.summary-hero-icon {
  display: grid;
  width: 42px;
  height: 42px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 999px;
  background: #eef2ff;
  color: #4f46e5;
  font-size: 20px;
  font-weight: 950;
  line-height: 1;
}

.summary-hero h2 {
  margin: 0;
  color: #111827;
  font-size: 22px;
  font-weight: 950;
  letter-spacing: 0;
}

.summary-hero p {
  margin: 5px 0 0;
  color: #64748b;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.45;
}

.summary-top-grid {
  display: grid;
  grid-template-columns: minmax(420px, 1fr) minmax(440px, 1fr);
  gap: 16px;
  border-top: 1px solid #edf0f5;
  padding-top: 16px;
}

.key-reasons-panel,
.ai-rule-compare-panel,
.summary-reason-compare {
  display: grid;
  align-content: start;
  gap: 12px;
}

.recommendation-summary-card > .ai-rule-compare-panel {
  width: 100%;
}

.key-reasons-panel h3,
.ai-rule-compare-panel h3,
.summary-reason-compare h3 {
  margin: 0;
  color: #111827;
  font-size: 14px;
  font-weight: 950;
}

.key-reasons-panel h3 span,
.ai-rule-compare-panel h3 span,
.summary-reason-compare h3 span {
  display: inline-grid;
  width: 14px;
  height: 14px;
  place-items: center;
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  color: #94a3b8;
  font-size: 10px;
  font-weight: 900;
}

.key-reason-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.key-reason-card {
  display: grid;
  gap: 6px;
  min-height: 84px;
  border: 1px solid #e7ebf3;
  border-radius: 8px;
  background: #ffffff;
  padding: 12px 10px;
}

.key-reason-icon {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border-radius: 999px;
  background: #f0e9ff;
  color: #6544ff;
  font-size: 17px;
  font-weight: 950;
  line-height: 1;
}

.key-reason-card--green .key-reason-icon {
  background: #e9fbf0;
  color: #16a34a;
}

.key-reason-card--orange .key-reason-icon {
  background: #fff2e6;
  color: #f97316;
}

.key-reason-card--rose .key-reason-icon {
  background: #ffe8ee;
  color: #f43f5e;
}

.key-reason-card strong {
  color: #111827;
  font-size: 12px;
  font-weight: 950;
}

.key-reason-card p,
.summary-note {
  margin: 0;
  color: #64748b;
  font-size: 11px;
  font-weight: 800;
  line-height: 1.45;
}

.summary-note {
  border: 1px solid #edf0f5;
  border-radius: 8px;
  background: #fbfcff;
  padding: 10px 12px;
}

.premium-compare-visual {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(170px, 0.45fr) minmax(0, 1fr);
  align-items: stretch;
  gap: 14px;
}

.premium-result {
  display: grid;
  align-content: center;
  justify-items: center;
  height: 100%;
  min-height: 158px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  padding: 16px;
  text-align: center;
}

.premium-result--ai {
  border-color: #c8b6ff;
  background: #fbf9ff;
}

.premium-result--rule {
  border-color: var(--rule-border);
  background: var(--rule-soft-bg);
}

.premium-result span {
  color: #111827;
  font-size: 12px;
  font-weight: 950;
}

.premium-result span small {
  color: #64748b;
  font-weight: 800;
}

.premium-result strong {
  margin-top: 10px;
  color: #6d28ff;
  font-size: 24px;
  font-weight: 950;
  line-height: 1;
}

.premium-result--rule strong {
  color: var(--rule-color);
}

.premium-result b {
  margin-top: 8px;
  color: #16a34a;
  font-size: 13px;
  font-weight: 950;
}

.premium-result em {
  margin-top: 3px;
  color: #64748b;
  font-size: 11px;
  font-style: normal;
  font-weight: 900;
}

.premium-result > small {
  margin-top: 16px;
  color: #475569;
  font-size: 12px;
  font-weight: 900;
}

.premium-cheaper-summary {
  position: relative;
  display: grid;
  align-content: center;
  justify-items: center;
  gap: 10px;
  min-height: 160px;
  min-width: 0;
  color: #11935b;
  padding: 10px 28px;
  text-align: center;
  overflow: hidden;
}

.premium-cheaper-bubble {
  display: grid;
  align-content: center;
  justify-items: center;
  width: min(128px, 100%);
  aspect-ratio: 1;
  border-radius: 999px;
  background: #eafaf2;
  padding: 16px;
}

.premium-cheaper-bubble span {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  border-radius: 999px;
  color: #16a34a;
  font-size: 28px;
  font-weight: 950;
  line-height: 1;
}

.premium-cheaper-bubble strong {
  margin-top: 4px;
  color: #0f9f63;
  font-size: 16px;
  font-weight: 950;
  line-height: 1.15;
}

.premium-cheaper-bubble em {
  margin-top: 3px;
  color: #64748b;
  font-size: 13px;
  font-style: normal;
  font-weight: 900;
}

.premium-cheaper-summary p {
  margin: 0;
  color: #11935b;
  font-size: 13px;
  font-weight: 950;
  line-height: 1.35;
  word-break: keep-all;
}

.premium-cheaper-summary small {
  color: #64748b;
  font-size: 10px;
  font-weight: 800;
  line-height: 1.35;
  word-break: keep-all;
}

.premium-compare-arrow {
  position: absolute;
  top: calc(50% - 18px);
  color: #a7b4c8;
  font-size: 34px;
  font-weight: 500;
  line-height: 1;
}

.premium-compare-arrow--left {
  left: 6px;
}

.premium-compare-arrow--right {
  right: 6px;
}

.premium-category-list {
  display: grid;
  gap: 7px;
  width: 100%;
  margin-top: 14px;
  border-top: 1px solid rgba(148, 163, 184, 0.22);
  padding-top: 12px;
  justify-items: center;
  text-align: center;
}

.premium-category-list b {
  margin: 0;
  color: #64748b;
  font-size: 11px;
  font-weight: 950;
}

.category-chip-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px;
}

.category-chip-list span {
  border-radius: 999px;
  background: #eef2f7;
  color: #334155;
  padding: 5px 9px;
  font-size: 11px;
  font-weight: 900;
  line-height: 1.2;
}

.premium-result--ai .category-chip-list span {
  background: #ebe4ff;
  color: #5b21ff;
}

.premium-category-list p {
  margin: 0;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 800;
}

.summary-meta-strip {
  display: grid;
  grid-template-columns: minmax(180px, 0.8fr) minmax(280px, 1.3fr) minmax(220px, 0.9fr);
  gap: 0;
  overflow: hidden;
  border: 1px solid #edf0f5;
  border-radius: 8px;
  background: #ffffff;
}

.summary-meta-strip > div {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  padding: 12px 16px;
}

.summary-meta-strip > div + div {
  border-left: 1px solid #edf0f5;
}

.summary-meta-strip p {
  display: grid;
  gap: 2px;
  min-width: 0;
  margin: 0;
}

.summary-meta-strip b {
  color: #94a3b8;
  font-size: 11px;
  font-weight: 900;
}

.summary-meta-strip strong {
  color: #111827;
  font-size: 12px;
  font-weight: 950;
  line-height: 1.4;
}

.meta-icon {
  display: grid;
  width: 34px;
  height: 34px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 950;
  line-height: 1;
}

.meta-icon--green {
  background: #e9fbf0;
  color: #16a34a;
}

.meta-icon--purple {
  background: #f0e9ff;
  color: #6544ff;
}

.meta-icon--blue {
  background: #eef2ff;
  color: #4f46e5;
}

.candidate-card,
.draft-card {
  overflow: hidden;
  padding: 0;
  background: #ffffff;
}

.candidate-card--rule {
  border-color: var(--rule-border);
  box-shadow: 0 8px 18px rgba(243, 115, 33, 0.08);
}

.candidate-card--ai {
  border-color: #d8c8ff;
  box-shadow: 0 8px 18px rgba(111, 65, 255, 0.08);
}

.candidate-header,
.draft-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 82px;
  border-bottom: 1px solid #fed7aa;
  background: #fffaf4;
  padding: 16px 18px;
}

.candidate-header {
  gap: 14px;
  min-height: 74px;
  border-bottom-color: #e2e8f0;
  background: #f8fafc;
  padding: 14px 20px;
}

.candidate-card--ai .candidate-header {
  border-bottom-color: #d8c8ff;
  background: #f2edff;
}

.candidate-card--rule .candidate-header {
  border-bottom-color: var(--rule-border);
  background: var(--rule-soft-bg);
}

.draft-header {
  border-bottom-color: #bbf7d0;
  background: #f8fffb;
}

.candidate-title,
.draft-title {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.candidate-title {
  flex: 1;
}

.candidate-title strong,
.draft-title strong {
  color: #111827;
  font-size: 14px;
  font-weight: 950;
}

.candidate-title strong {
  white-space: nowrap;
}

.draft-title span:last-child,
.candidate-card h3 {
  color: #16a34a;
  font-size: 11px;
  font-weight: 900;
}

.candidate-card--rule h3 {
  color: #0f172a;
}

.candidate-card--ai h3 {
  color: #5b21ff;
}

.draft-header p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 12px;
  font-weight: 800;
}

.draft-icon {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: 10px;
  background: #dcfce7;
  color: #16a34a;
  font-weight: 950;
}

.candidate-list {
  display: grid;
  padding: 0 20px 16px;
}

.candidate-plan-summary h3 {
  margin: 0;
  padding: 16px 0 12px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 950;
}

.candidate-reason-box {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  padding: 14px 16px;
}

.candidate-reason-box--ai {
  border-color: #d8c8ff;
  background: #f8f5ff;
}

.candidate-reason-box--rule {
  border-color: var(--rule-border);
  background: rgba(243, 115, 33, 0.05);
}

.candidate-reason-box--ai strong {
  color: #5b21ff;
}

.candidate-reason-box ul {
  display: grid;
  gap: 6px;
  flex: 1;
  margin: 0;
  padding: 0;
  list-style: none;
}

.candidate-reason-box li {
  position: relative;
  color: #475569;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.55;
}

.candidate-table-head,
.candidate-row {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) 80px;
  align-items: center;
  gap: 10px;
}

.candidate-table-head {
  border-bottom: 1px solid #e2e8f0;
  color: #94a3b8;
  font-size: 11px;
  font-weight: 900;
  padding: 0 0 9px;
}

.candidate-table-head span:nth-child(3) {
  justify-self: end;
}

.candidate-row {
  min-height: 58px;
  border-bottom: 1px solid #edf0f5;
  padding: 12px 0;
}

.candidate-row:last-child {
  border-bottom: 0;
}

.candidate-row .coverage-premium {
  justify-self: end;
}

.coverage-action,
.coverage-remove {
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  border: 0;
  border-radius: 999px;
  background: #f0e9ff;
  color: #6544ff;
  cursor: pointer;
  font-size: 17px;
  font-weight: 950;
  line-height: 1;
}

.coverage-action:disabled {
  cursor: default;
  opacity: 0.35;
}

.coverage-remove {
  background: #ffe5e7;
  color: #ef4444;
  font-size: 15px;
}

.draft-table {
  display: grid;
  margin: 12px 18px;
  overflow: hidden;
  border: 1px solid #edf0f5;
  border-radius: 8px;
}

.draft-table-head,
.draft-row {
  display: grid;
  grid-template-columns: 34px minmax(120px, 1fr) minmax(120px, 1fr) 76px 42px;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #edf0f5;
  padding: 10px 12px;
}

.draft-table-head {
  background: #f8fafc;
  color: var(--color-text-subtle);
  font-size: 11px;
  font-weight: 900;
}

.draft-row:last-child {
  border-bottom: 0;
}

.draft-rank {
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  border-radius: 999px;
  background: #dcfce7;
  color: #16a34a;
  font-size: 12px;
  font-weight: 950;
}

.draft-summary {
  color: #475569;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.45;
}

.draft-add-button {
  min-height: 36px;
  margin: 0 18px 12px;
  border: 1px dashed #86efac;
  border-radius: 8px;
  background: #f8fffb;
  color: #16a34a;
  font-size: 13px;
  font-weight: 950;
}

.draft-footer {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  border-top: 1px solid #edf0f5;
}

.draft-footer div {
  display: grid;
  gap: 4px;
  justify-items: center;
  padding: 12px;
}

.draft-footer div + div {
  border-left: 1px solid #edf0f5;
}

.draft-footer span,
.draft-action-summary span {
  color: var(--color-text-subtle);
  font-size: 11px;
  font-weight: 800;
}

.draft-footer strong,
.draft-action-summary strong {
  color: #111827;
  font-size: 16px;
  font-weight: 950;
}

.draft-footer div:last-child strong,
.draft-action-summary small {
  color: #16a34a;
}

.draft-action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
}

.draft-action-summary {
  display: flex;
  align-items: center;
  gap: 24px;
  min-width: 0;
}

.draft-action-summary > div {
  display: grid;
  gap: 3px;
}

.draft-action-summary small {
  font-size: 12px;
  font-weight: 900;
}

.draft-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.draft-actions button {
  min-height: 38px;
  border: 1px solid #c7d2fe;
  border-radius: 8px;
  background: #ffffff;
  color: #5138ff;
  padding: 0 22px;
  font-size: 13px;
  font-weight: 950;
}

.draft-actions .primary {
  border-color: #5b2eff;
  background: #5b2eff;
  color: #ffffff;
}

.recommendation-overview-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px 20px;
  border-top: 1px solid #edf0f5;
  padding-top: 14px;
}

.recommendation-summary-strip {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) auto;
  align-items: center;
  gap: 18px;
  padding: 14px 18px;
}

.summary-copy,
.card-header,
.card-source,
.card-metrics {
  display: flex;
  align-items: center;
}

.summary-copy {
  gap: 10px;
  min-width: 0;
}

.summary-icon-badge {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  border-radius: 999px;
  background: #eef2ff;
  color: #4f46e5;
  font-weight: 950;
}

.summary-copy strong,
.comparison-card h3,
.guide-card h3 {
  color: #111827;
  font-size: 15px;
  font-weight: 950;
}

.summary-copy p,
.comparison-card p {
  margin: 2px 0 0;
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 800;
}

.reason-comparison {
  display: grid;
  gap: 10px;
  border: 1px solid #edf0f5;
  border-radius: 8px;
  background: #fbfcff;
  padding: 12px 14px;
}

.reason-comparison h4 {
  margin: 0;
  color: #111827;
  font-size: 12px;
  font-weight: 950;
}

.reason-comparison-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.reason-comparison-grid section {
  display: grid;
  align-content: start;
  gap: 8px;
  min-width: 0;
}

.reason-card {
  border: 1px solid #fed7aa;
  border-radius: 8px;
  background: #fffaf7;
  padding: 14px 16px;
}

.summary-reason-compare .reason-card {
  min-width: 0;
}

.summary-reason-compare .reason-comparison-grid {
  grid-template-columns: 1fr;
}

.reason-card--ai {
  border-color: #ddd6fe;
  background: #fbf9ff;
}

.reason-comparison-grid strong {
  color: #ff8a00;
  font-size: 12px;
  font-weight: 950;
}

.reason-comparison-grid section:last-child strong {
  color: #4f46e5;
}

.reason-comparison-grid ul {
  display: grid;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.reason-comparison-grid li {
  position: relative;
  color: #475569;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.55;
  padding-left: 18px;
}

.reason-comparison-grid li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #ff8a00;
  font-weight: 950;
}

.reason-comparison-grid section:last-child li::before {
  color: #4f46e5;
}

.reason-card button {
  min-height: 34px;
  margin-top: 6px;
  border: 0;
  border-top: 1px solid rgba(249, 115, 22, 0.18);
  background: transparent;
  color: #f97316;
  cursor: pointer;
  font-size: 12px;
  font-weight: 950;
}

.reason-card--ai button {
  border-top-color: rgba(101, 68, 255, 0.18);
  color: #6544ff;
}

.recommendation-summary h3,
.recommendation-summary p {
  margin: 0;
}

.recommendation-main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) 320px;
  gap: 12px;
}

.recommendation-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.recommendation-card {
  overflow: hidden;
  padding: 0;
}

.card-header {
  gap: 12px;
  border-bottom: 1px solid #fed7aa;
  background: #fffaf4;
  padding: 16px 18px;
}

.recommendation-card--rule {
  border-color: #fed7aa;
  order: 1;
}

.recommendation-card--ai .card-header {
  border-bottom-color: #ded2ff;
  background: #f4f0ff;
}

.recommendation-card--ai {
  border-color: #ddd6fe;
  order: 2;
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
  border-radius: 8px;
  background: #ff7a1a;
  color: #ffffff;
  font-size: 11px;
  font-weight: 950;
}

.candidate-card .source-icon {
  width: 30px;
  height: 30px;
  border-radius: 7px;
  font-size: 10px;
}

.candidate-card--rule .source-icon {
  background: var(--rule-color);
  color: #ffffff;
}

.recommendation-card--ai .source-icon,
.candidate-card--ai .source-icon {
  background: #6d3cff;
  color: #ffffff;
}

.analysis-chip {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  border-radius: 999px;
  background: #f0e9ff;
  color: #6941c6;
  padding: 0 9px;
  font-size: 10px;
  font-weight: 900;
  white-space: nowrap;
}

.candidate-card .analysis-chip {
  min-height: 20px;
  padding: 0 8px;
  font-size: 10px;
}

.recommendation-card--rule .analysis-chip,
.candidate-card--rule .analysis-chip {
  background: var(--rule-chip-bg);
  color: var(--rule-color);
}

.candidate-card--ai .analysis-chip {
  background: #ebe4ff;
  color: #5b21ff;
}

.card-metrics {
  flex-direction: column;
  align-items: flex-end;
  gap: 3px;
  min-width: 84px;
}

.candidate-card .card-metrics {
  gap: 2px;
  min-width: 92px;
}

.card-metrics span {
  color: var(--color-text-subtle);
  font-size: 11px;
  font-weight: 700;
}

.candidate-card .card-metrics span {
  color: #94a3b8;
  font-size: 10px;
  font-weight: 800;
}

.card-metrics strong {
  color: #ff8a00;
  font-size: 18px;
  font-weight: 950;
}

.candidate-card .card-metrics strong {
  line-height: 1;
}

.candidate-card--rule .card-metrics strong {
  color: var(--rule-color);
}

.recommendation-card--ai .card-metrics strong,
.candidate-card--ai .card-metrics strong {
  color: #6d28ff;
}

.recommendation-summary {
  display: grid;
  gap: 12px;
  padding: 14px 18px;
}

.recommendation-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.plan-chip {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  border-radius: 999px;
  background: #f4ecff;
  color: #7c1dff;
  padding: 0 9px;
  font-size: 11px;
  font-weight: 900;
  white-space: nowrap;
}

.recommendation-summary h3 {
  color: #121826;
  font-size: 15px;
  font-weight: 900;
  letter-spacing: 0;
}

.recommendation-summary p,
.card-empty {
  border: 1px solid #fee7d4;
  border-radius: 8px;
  background: #fffaf7;
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

.recommendation-card--ai .recommendation-summary p {
  border-color: #e9ddff;
  background: #fbf9ff;
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
  padding: 0 18px 12px;
}

.detail-body {
  display: grid;
  padding: 10px 18px 18px;
}

.coverage-list {
  display: grid;
  align-content: start;
  gap: 12px;
}

.coverage-list {
  min-width: 0;
}

.coverage-list-head {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) 74px;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #edf0f5;
  color: var(--color-text-subtle);
  font-size: 11px;
  font-weight: 900;
  padding: 0 0 8px;
}

.coverage-list h3 {
  margin: 0;
  color: inherit;
  font-size: inherit;
  font-weight: inherit;
}

.coverage-list-head span:nth-child(3) {
  justify-self: end;
}

.coverage-pill-row {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) 74px;
  align-items: center;
  gap: 10px;
  min-height: 58px;
  border-bottom: 1px solid #edf0f5;
  padding: 10px 0;
}

.coverage-pill-row:last-child {
  border-bottom: 0;
}

.coverage-premium {
  justify-self: end;
  color: #334155;
  font-size: 12px;
  font-weight: 900;
  white-space: nowrap;
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
  min-height: 58px;
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
  max-width: 100%;
  font-weight: 800;
  line-height: 1.45;
  white-space: normal;
  word-break: keep-all;
  overflow-wrap: anywhere;
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
  line-height: 1.35;
  white-space: normal;
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

.recommendation-footer {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 8px;
  border-top: 1px solid #edf0f5;
  padding: 14px 18px;
}

.recommendation-footer span {
  color: var(--color-text-subtle);
  font-size: 11px;
  font-weight: 800;
}

.recommendation-footer strong {
  color: #4f46e5;
  font-size: 18px;
  font-weight: 950;
}

.footer-stars {
  color: #ffad00 !important;
  letter-spacing: 1px;
}

.comparison-card,
.guide-card {
  display: grid;
  align-content: start;
  gap: 12px;
  padding: 18px;
}

.comparison-card {
  padding: 0;
}

.comparison-card h3,
.guide-card h3 {
  margin: 0;
}

.recommendation-guide {
  display: grid;
  gap: 10px;
  border: 1px solid #edf0f5;
  border-radius: 8px;
  background: #fbfcff;
  padding: 12px 14px;
}

.recommendation-guide h4 {
  margin: 0;
  color: #111827;
  font-size: 12px;
  font-weight: 950;
}

.recommendation-guide ul {
  display: grid;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.recommendation-guide li {
  position: relative;
  color: #475569;
  font-size: 11px;
  font-weight: 800;
  line-height: 1.55;
  padding-left: 18px;
}

.recommendation-guide li::before {
  content: '✓';
  position: absolute;
  top: 0;
  left: 0;
  color: #4f46e5;
  font-weight: 950;
}

.comparison-table {
  display: grid;
  overflow: hidden;
  border: 1px solid #edf0f5;
  border-radius: 8px;
}

.comparison-head,
.comparison-row {
  display: grid;
  grid-template-columns: minmax(82px, 1fr) minmax(80px, 1fr) minmax(80px, 1fr);
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid #edf0f5;
  padding: 10px 12px;
}

.comparison-row:last-child {
  border-bottom: 0;
}

.comparison-head {
  background: #f8fafc;
  color: var(--color-text-subtle);
  font-size: 11px;
  font-weight: 900;
}

.comparison-row {
  color: #172033;
  font-size: 12px;
  font-weight: 800;
}

.comparison-row strong {
  color: #263244;
  font-size: 12px;
}

.comparison-row .is-highlight {
  color: #7c1dff;
}

.comparison-row .is-rule {
  color: #ff8a00;
}

.recommendation-bottom-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(280px, 1fr);
  gap: 12px;
}

.guide-card ul {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.guide-card li {
  position: relative;
  color: #334155;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.5;
  padding-left: 24px;
}

.guide-card li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #4f46e5;
  font-weight: 950;
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
  .recommendation-summary-strip,
  .recommendation-main-grid,
  .recommendation-bottom-grid,
  .candidate-compare-grid,
  .summary-top-grid,
  .summary-meta-strip,
  .recommendation-detail-grid,
  .recommendation-overview-grid {
    grid-template-columns: 1fr;
  }

  .key-reason-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .summary-meta-strip > div + div {
    border-top: 1px solid #edf0f5;
    border-left: 0;
  }

  .recommendation-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .comparison-card {
    border-top: 1px solid #edf0f5;
    border-left: 0;
    padding: 16px 0 0;
  }

  .draft-action-bar,
  .draft-action-summary {
    align-items: flex-start;
    flex-direction: column;
  }

  .draft-actions {
    justify-content: flex-start;
    width: 100%;
  }
}

@media (max-width: 980px) {
  .premium-compare-visual {
    grid-template-columns: 1fr;
  }

  .premium-cheaper-summary {
    min-height: auto;
    padding: 14px 42px;
  }

  .premium-cheaper-bubble {
    width: 132px;
  }

  .premium-compare-arrow {
    top: 52px;
  }
}

@media (max-width: 720px) {
  .recommendation-summary-strip {
    padding: 14px;
  }

  .recommendation-summary-card {
    padding: 14px;
  }

  .summary-hero {
    align-items: flex-start;
  }

  .summary-hero h2 {
    font-size: 19px;
  }

  .key-reason-grid,
  .premium-compare-visual {
    grid-template-columns: 1fr;
  }

  .card-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .card-metrics {
    align-items: flex-start;
  }

  .detail-body {
    grid-template-columns: 1fr;
  }

  .coverage-list {
    padding: 0;
  }

  .coverage-head {
    display: none;
  }

  .coverage-row {
    grid-template-columns: 26px minmax(0, 1fr);
  }

  .coverage-pill-row {
    grid-template-columns: 26px minmax(0, 1fr);
  }

  .candidate-table-head,
  .candidate-row,
  .draft-row {
    grid-template-columns: 28px minmax(0, 1fr);
  }

  .candidate-table-head {
    display: none;
  }

  .draft-table-head {
    display: none;
  }

  .draft-summary,
  .coverage-premium,
  .coverage-remove {
    grid-column: 2;
    justify-self: start;
  }

  .draft-footer {
    grid-template-columns: 1fr;
  }

  .draft-footer div + div {
    border-top: 1px solid #edf0f5;
    border-left: 0;
  }

  .draft-actions button {
    width: 100%;
  }

  .coverage-list-head {
    display: none;
  }

  .reason-comparison-grid {
    grid-template-columns: 1fr;
  }

  .coverage-premium {
    grid-column: 2;
    justify-self: start;
  }

  .coverage-head--no-score,
  .coverage-row--no-score {
    grid-template-columns: 26px minmax(0, 1fr);
  }

  .premium-cell {
    grid-column: 2;
    justify-self: start;
  }
}
</style>
