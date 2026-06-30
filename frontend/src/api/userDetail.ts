import api, { type ApiResponse } from '@/api/instance'

export interface UserDetailBadge {
  code: string
  name: string
}

export interface UserDetail {
  customerId: number
  conversionStatusCode: string
  conversionStatusName: string
  reportId?: number
  childName: string
  childGender: string
  childAge: number
  childBirthDate: string
  consultStatusCode?: string
  consultStatusName?: string
  lifeStageCode?: string
  lifeStageName?: string
  insuranceAgeShiftDate?: string
  parentCustomerId?: number
  guardianName?: string
  relationshipCode?: string
  relationshipName?: string
  guardianPhone?: string
  guardianAddress?: string
  guardianAge?: number
  badges: UserDetailBadge[]
  ageChangeLabel?: string | null
  ageIncreaseDDay?: number | null
}

export interface ConsultationScript {
  scriptId?: number
  triggerCode?: string
  triggerName?: string
  title?: string
  content?: string
  lines?: string[]
  generatedAt?: string
}

export interface RecommendationCoverage {
  rank?: number
  coverageName: string
  categoryCode?: string
  tags?: string[]
  score?: number
  premium?: number
  reason?: string
  exclusionReasons?: string[]
}

export interface InsuranceRecommendation {
  recommendationId?: number
  sourceType?: 'RULE_ENGINE' | 'AI' | string
  sourceName?: string
  planName?: string
  title?: string
  summary?: string
  monthlyPremium?: number
  recommendationScore?: number
  generatedAt?: string
  coverages: RecommendationCoverage[]
}

export interface ReportPreviewUrlResponse {
  previewUrl: string
}

type AnyRecord = Record<string, unknown>

const isRecord = (value: unknown): value is AnyRecord => {
  return typeof value === 'object' && value !== null
}

const asString = (value: unknown) => {
  if (typeof value === 'string') return value
  if (typeof value === 'number') return String(value)
  return undefined
}

const asNumber = (value: unknown) => {
  if (typeof value === 'number') return value
  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value)
    return Number.isNaN(parsed) ? undefined : parsed
  }
  return undefined
}

const asStringArray = (value: unknown) => {
  if (!Array.isArray(value)) return undefined
  return value.map(asString).filter((item): item is string => Boolean(item))
}

const firstString = (...values: unknown[]) => {
  for (const value of values) {
    const result = asString(value)
    if (result) return result
  }

  return undefined
}

const firstNumber = (...values: unknown[]) => {
  for (const value of values) {
    const result = asNumber(value)
    if (typeof result === 'number') return result
  }

  return undefined
}

export const INSURANCE_CATEGORY_NAMES: Record<string, string> = {
  CAT_CRITICAL_BAL: '암, 뇌혈관, 심혈관 중증 질환 진단 보장',
  CAT_DENT_EYE: '치아 보존/보철 치료 및 시력 교정/치료 보장',
  CAT_HOSP_SURG: '입원 및 수술비 보장',
  CAT_INFECT_GROUP: '어린이 다빈도 질환, 독감, 수족구, 응급실 보장',
  CAT_SH_INJURY: '골절, 깁스, 화상 등 상해 치료 보장',
}

export const insuranceCategoryName = (categoryCode?: string) =>
  categoryCode ? INSURANCE_CATEGORY_NAMES[categoryCode] ?? categoryCode : undefined

const normalizeCategoryLabel = (value?: string) => {
  if (!value) return undefined
  return INSURANCE_CATEGORY_NAMES[value] ?? value
}

const normalizeCategoryLabels = (values?: string[]) =>
  values?.map(normalizeCategoryLabel).filter((value): value is string => Boolean(value))

const categoryTag = (coverage: AnyRecord) => {
  const categoryName = firstString(coverage.categoryName, coverage.category_name)
  if (categoryName) return categoryName

  return insuranceCategoryName(firstString(coverage.categoryCode, coverage.category_code))
}

const normalizeScript = (script: unknown): ConsultationScript | null => {
  if (!isRecord(script)) return null

  const context =
    isRecord(script.context)
      ? script.context
      : isRecord(script.scriptContext)
        ? script.scriptContext
        : isRecord(script.scriptContextDto)
          ? script.scriptContextDto
          : null

  const content =
    firstString(
      script.content,
      script.scriptContent,
      script.scriptText,
      script.script,
      script.message,
      script.generatedScript,
      script.recommendScript,
      script.consultationScript,
      script.talkScript,
    )

  const lines = asStringArray(script.lines) ?? (content ? content.split(/\n+/).filter(Boolean) : undefined)

  return {
    scriptId: firstNumber(script.scriptId, script.id),
    triggerCode:
      firstString(script.triggerCode, script.triggerTypeCode, context?.triggerTypeCode),
    triggerName:
      firstString(
        script.triggerName,
        script.actionItemName,
        script.actionItemTitle,
        script.title,
        context?.triggerName,
      ) ??
      '상담 스크립트',
    title: firstString(script.title, script.actionItemName, context?.triggerName),
    content,
    lines,
    generatedAt: firstString(script.generatedAt, script.createdAt, script.updatedAt),
  }
}

const normalizeCoverage = (coverage: unknown, index: number): RecommendationCoverage | null => {
  if (!isRecord(coverage)) return null

  const coverageName =
    firstString(
      coverage.coverageName,
      coverage.guaranteeName,
      coverage.guaranteeItemName,
      coverage.coverageItemName,
      coverage.itemName,
      coverage.name,
      coverage.damboName,
    )

  if (!coverageName) return null

  const baseScore = asNumber(coverage.baseScore)
  const biasScore = asNumber(coverage.biasScore)
  const derivedScore =
    typeof baseScore === 'number' || typeof biasScore === 'number'
      ? (baseScore ?? 0) + (biasScore ?? 0)
      : undefined
  const score = firstNumber(
    coverage.score,
    coverage.recommendationScore,
    coverage.priorityScore,
    coverage.finalScore,
    coverage.totalScore,
    coverage.recommendScore,
    coverage.ruleScore,
    derivedScore,
  )

  return {
    rank:
      firstNumber(coverage.rank, coverage.priority, coverage.sortOrder, coverage.orderNo, coverage.selectedOrder) ??
      index + 1,
    coverageName,
    categoryCode: firstString(coverage.categoryCode, coverage.category_code),
    tags:
      normalizeCategoryLabels(asStringArray(coverage.tags)) ??
      normalizeCategoryLabels(asStringArray(coverage.categories)) ??
      normalizeCategoryLabels(asStringArray(coverage.categoryNames ?? coverage.category_names)) ??
      asStringArray(categoryTag(coverage) ? [categoryTag(coverage)] : undefined),
    score,
    premium: firstNumber(
      coverage.premium,
      coverage.monthlyPremium,
      coverage.unitPremium,
      coverage.insurancePremium,
      coverage.monthlyInsurancePremium,
      coverage.coveragePremium,
    ),
    reason: firstString(
      coverage.reason,
      coverage.recommendationReason,
      coverage.recommendReason,
      coverage.reasonText,
      coverage.reasonMessage,
      coverage.recommendationReasonText,
      coverage.coverageSummary,
      coverage.description,
    ),
    exclusionReasons: asStringArray(coverage.exclusionReasons),
  }
}

const normalizeRecommendation = (
  recommendation: unknown,
  sourceType: InsuranceRecommendation['sourceType'] = 'RULE_ENGINE',
  sourceName = '자체 룰 엔진',
): InsuranceRecommendation | null => {
  if (!isRecord(recommendation)) return null

  const coverageSource =
    recommendation.coverages ??
    recommendation.items ??
    recommendation.recommendationItems ??
    recommendation.recommendItems ??
    recommendation.coverageItems ??
    recommendation.details ??
    recommendation.guarantees ??
    recommendation.products

  const coverages = Array.isArray(coverageSource)
    ? coverageSource
        .map((coverage, index) => normalizeCoverage(coverage, index))
        .filter((coverage): coverage is RecommendationCoverage => Boolean(coverage))
    : []

  const derivedSummary = coverages
    .map((coverage) => coverage.reason)
    .filter((reason): reason is string => Boolean(reason))
    .join(' ')
  const derivedScore = coverages.length
    ? coverages.reduce((sum, coverage) => sum + (coverage.score ?? 0), 0)
    : undefined

  return {
    recommendationId: firstNumber(recommendation.recommendationId, recommendation.id, recommendation.planId),
    sourceType,
    sourceName,
    planName:
      firstString(
        recommendation.planName,
        recommendation.recommendationName,
        recommendation.productName,
        recommendation.title,
        recommendation.planTitle,
      ),
    title: asString(recommendation.title),
    summary:
      firstString(
        recommendation.summary,
        recommendation.reason,
        recommendation.recommendationReason,
        recommendation.recommendReason,
        recommendation.reasonText,
        recommendation.reasonMessage,
        recommendation.recommendationReasonText,
        recommendation.description,
      ) ?? derivedSummary,
    monthlyPremium: firstNumber(
      recommendation.monthlyPremium,
      recommendation.totalPremium,
      recommendation.premium,
      recommendation.monthlyInsurancePremium,
      recommendation.totalMonthlyPremium,
    ),
    recommendationScore:
      firstNumber(
        recommendation.recommendationScore,
        recommendation.score,
        recommendation.totalScore,
        recommendation.finalScore,
        recommendation.planScore,
        recommendation.recommendScore,
        recommendation.ruleScore,
      ) ?? derivedScore,
    generatedAt:
      asString(recommendation.generatedAt) ??
      asString(recommendation.createdAt) ??
      asString(recommendation.updatedAt),
    coverages,
  }
}

export async function getUserDetail(customerId: number, conversionStatusCode: string) {
  const response = await api.get<ApiResponse<UserDetail>>(`/v1/customers/${customerId}/detail`, {
    params: { conversionStatusCode },
  })

  return response.data.data
}

export async function getReportPreviewUrl(reportId: number) {
  const response = await api.get<ApiResponse<ReportPreviewUrlResponse>>(
    `/v1/reports/${reportId}/preview-url`,
  )

  return response.data.data.previewUrl
}

export async function getConsultationScript(customerId: number) {
  const response = await api.get<ApiResponse<unknown>>(`/v1/customers/${customerId}/scripts`)

  return normalizeScript(response.data.data)
}

export async function getRuleEngineRecommendation(customerId: number) {
  const response = await api.get<ApiResponse<unknown>>(`/v1/customers/${customerId}/recommendation`)

  return normalizeRecommendation(response.data.data)
}

export async function getAiRagRecommendation(customerId: number) {
  const response = await api.post<ApiResponse<unknown>>('/v1/recommendai', { customerId })

  return normalizeRecommendation(response.data.data, 'AI', 'AI RAG 맞춤 추천')
}
