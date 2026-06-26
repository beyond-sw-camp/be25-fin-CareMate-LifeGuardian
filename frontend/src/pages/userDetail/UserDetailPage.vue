<script setup lang="ts">
import axios from 'axios'
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/common/Header.vue'
import AppSidebar from '@/components/common/Sidebar.vue'
import UserInfoGrid from '@/components/userDetail/UserInfoGrid.vue'
import UserProfileCard from '@/components/userDetail/UserProfileCard.vue'
import UserRecommendationCard from '@/components/userDetail/UserRecommendationCard.vue'
import UserScriptCard from '@/components/userDetail/UserScriptCard.vue'
import {
  getConsultationScript,
  getAiRagRecommendation,
  getRuleEngineRecommendation,
  getUserDetail,
  type ConsultationScript,
  type InsuranceRecommendation,
  type UserDetail,
} from '@/api/userDetail'
import {
  buildChildInfo,
  buildGuardianInfo,
  resolveDetailConversionStatusCode,
  resolveReportUrl,
} from '@/utils/userDetail'

const route = useRoute()
const router = useRouter()

const user = ref<UserDetail | null>(null)
const consultationScript = ref<ConsultationScript | null>(null)
const ruleRecommendation = ref<InsuranceRecommendation | null>(null)
const aiRecommendation = ref<InsuranceRecommendation | null>(null)
const isLoading = ref(true)
const isInsightLoading = ref(false)
const errorMessage = ref('')
const scriptErrorMessage = ref('')
const ruleRecommendationErrorMessage = ref('')
const aiRecommendationErrorMessage = ref('')
let activeLoadId = 0

const customerId = computed(() => Number(route.params.customerId))
const conversionStatusCode = computed(() => String(route.query.conversionStatusCode ?? ''))

const isFromDashboard = computed(() => route.query.from === 'dashboard')

const activeSidebarLabel = computed(() =>
  isFromDashboard.value ? '대시보드' : '영업현황',
)

const backButtonLabel = computed(() =>
  isFromDashboard.value ? '← 대시보드' : '← 영업현황',
)

const backRoutePath = computed(() =>
  isFromDashboard.value ? '/sales/dashboard' : '/sales',
)

const resolvedConversionStatusCode = computed(() =>
  resolveDetailConversionStatusCode(user.value, conversionStatusCode.value),
)
const isPotentialCustomer = computed(() => resolvedConversionStatusCode.value === '01')
const reportUrl = computed(() => resolveReportUrl(user.value))
const childInfo = computed(() => buildChildInfo(user.value, isPotentialCustomer.value))
const guardianInfo = computed(() => buildGuardianInfo(user.value))

const goBackToOrigin = () => {
  void router.push(backRoutePath.value)
}

const openReport = () => {
  if (!reportUrl.value) return
  window.open(reportUrl.value, '_blank', 'noopener,noreferrer')
}

const buildFallbackConsultationScript = (targetUser: UserDetail): ConsultationScript => {
  const childName = targetUser.childName || '고객'
  const guardianName = targetUser.guardianName || '보호자'
  const dDay = targetUser.insuranceAgeShiftDate
    ? `보험나이 변경 기준일(${targetUser.insuranceAgeShiftDate})`
    : '보험나이 변경 시점'

  return {
    triggerName: '기본 상담 템플릿',
    title: '상담 도입 안내',
    lines: [
      `안녕하세요, ${guardianName}님. ${childName}님의 보장 내용을 함께 점검해보려고 연락드렸습니다.`,
      `${dDay} 전에 현재 가입 보장과 필요한 보장 범위를 확인해두시면 이후 보험료와 보장 공백을 줄이는 데 도움이 됩니다.`,
      '오늘은 생활주기와 기존 계약 정보를 기준으로 우선 확인이 필요한 보장 항목을 짧게 안내드리겠습니다.',
    ],
  }
}

const getErrorMessage = (error: unknown, fallbackMessage = '고객 정보를 불러오지 못했습니다.') => {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status
    const serverMessage = error.response?.data?.message

    if (serverMessage) return serverMessage
    if (status === 500) return `${fallbackMessage} 백엔드 서버 오류(500)가 발생했습니다.`

    return fallbackMessage
  }

  return fallbackMessage
}

const resetCustomerInsights = () => {
  isInsightLoading.value = false
  scriptErrorMessage.value = ''
  ruleRecommendationErrorMessage.value = ''
  aiRecommendationErrorMessage.value = ''
  consultationScript.value = null
  ruleRecommendation.value = null
  aiRecommendation.value = null
}

const loadCustomerInsights = async (loadId: number) => {
  if (!Number.isInteger(customerId.value) || customerId.value <= 0 || !conversionStatusCode.value) {
    return
  }

  isInsightLoading.value = true
  scriptErrorMessage.value = ''
  ruleRecommendationErrorMessage.value = ''
  aiRecommendationErrorMessage.value = ''

  const [scriptResult, ruleResult, aiResult] = await Promise.allSettled([
    getConsultationScript(customerId.value),
    getRuleEngineRecommendation(customerId.value),
    getAiRagRecommendation(customerId.value),
  ])

  if (loadId !== activeLoadId) return

  if (scriptResult.status === 'fulfilled') {
    consultationScript.value = scriptResult.value
  } else {
    if (user.value) {
      consultationScript.value = buildFallbackConsultationScript(user.value)
    }

    scriptErrorMessage.value = getErrorMessage(
      scriptResult.reason,
      '상담 스크립트를 불러오지 못했습니다.',
    )
  }

  if (ruleResult.status === 'fulfilled') {
    ruleRecommendation.value = ruleResult.value
  } else {
    ruleRecommendationErrorMessage.value = getErrorMessage(
      ruleResult.reason,
      '자체 룰 엔진 추천 정보를 불러오지 못했습니다.',
    )
  }

  if (aiResult.status === 'fulfilled') {
    aiRecommendation.value = aiResult.value
  } else {
    aiRecommendationErrorMessage.value = getErrorMessage(
      aiResult.reason,
      'AI 추천 정보를 불러오지 못했습니다.',
    )
  }

  isInsightLoading.value = false
}

const loadUser = async () => {
  const loadId = activeLoadId + 1
  activeLoadId = loadId
  isLoading.value = true
  errorMessage.value = ''
  user.value = null
  resetCustomerInsights()

  if (!Number.isInteger(customerId.value) || customerId.value <= 0) {
    errorMessage.value = '유효하지 않은 고객 번호입니다.'
    isLoading.value = false
    return
  }

  if (!conversionStatusCode.value) {
    errorMessage.value = '고객 구분 정보가 없습니다.'
    isLoading.value = false
    return
  }

  try {
    const nextUser = await getUserDetail(customerId.value, conversionStatusCode.value)
    if (loadId !== activeLoadId) return

    user.value = nextUser
    isLoading.value = false
    void loadCustomerInsights(loadId)
  } catch (error) {
    if (loadId !== activeLoadId) return
    errorMessage.value = getErrorMessage(error)
  } finally {
    if (loadId === activeLoadId) {
      isLoading.value = false
    }
  }
}

watch(
  () => [route.params.customerId, route.query.conversionStatusCode],
  () => {
    void loadUser()
  },
  { immediate: true },
)
</script>

<template>
  <div class="app-shell user-detail-shell">
    <AppSidebar :active-label="activeSidebarLabel" />

    <main class="app-main user-detail-page">
      <AppHeader title="고객 상세" />

      <div class="detail-toolbar">
        <button class="back-button" type="button" @click="goBackToOrigin">
          {{ backButtonLabel }}
        </button>

        <button class="report-button" type="button" :disabled="!reportUrl" @click="openReport">
          생활주기 성장 리포트 보기
        </button>
      </div>

      <section v-if="isLoading" class="detail-state card">고객 정보를 불러오는 중입니다.</section>
      <section v-else-if="errorMessage" class="detail-state detail-state--error card">
        <strong>{{ errorMessage }}</strong>

        <button class="button button-secondary" type="button" @click="goBackToOrigin">
          목록으로 돌아가기
        </button>
      </section>

      <div v-else-if="user" class="detail-content">
        <UserProfileCard :user="user" :is-potential-customer="isPotentialCustomer" />
        <UserInfoGrid :child-info="childInfo" :guardian-info="guardianInfo" />
        <UserScriptCard
          :script="consultationScript"
          :is-loading="isInsightLoading"
          :error-message="scriptErrorMessage"
        />
        <UserRecommendationCard
          :rule-recommendation="ruleRecommendation"
          :ai-recommendation="aiRecommendation"
          :is-loading="isInsightLoading"
          :rule-error-message="ruleRecommendationErrorMessage"
          :ai-error-message="aiRecommendationErrorMessage"
        />
      </div>
    </main>
  </div>
</template>

<style scoped>
.user-detail-shell {
  background: #f4f7fb;
}

.user-detail-page {
  padding: 16px 28px 48px 25px;
  overflow-x: hidden;
}

.detail-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 6px 0 14px;
}

.back-button {
  border: 0;
  background: transparent;
  color: var(--color-text-muted);
  padding: 0;
  font-size: 12px;
  font-weight: 800;
}

.report-button {
  min-height: 34px;
  border: 0;
  border-radius: 6px;
  background: var(--color-primary);
  color: #ffffff;
  padding: 0 14px;
  font-size: 12px;
  font-weight: 800;
}

.report-button:disabled {
  background: #b9c1cf;
}

.detail-content {
  display: grid;
  gap: 14px;
}

.detail-state {
  display: grid;
  min-height: 240px;
  place-items: center;
  color: var(--color-text-muted);
}

.detail-state--error {
  align-content: center;
  gap: 14px;
  color: #d85a65;
}

@media (max-width: 640px) {
  .user-detail-page {
    padding: 16px;
  }

  .detail-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .report-button {
    width: 100%;
  }
}

</style>
