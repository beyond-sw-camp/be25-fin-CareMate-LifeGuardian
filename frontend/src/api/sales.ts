import api, { type ApiResponse } from '@/api/instance'

// 영업현황 상단 KPI 요약 응답입니다.
export interface SalesSummary {
  year: number
  month: number
  targetCount: number
  contractCount: number
  achievementRate: number
}

// 영업현황 목록의 고객 1건입니다.
// 고객 단계에 따라 상담/계약/리포트/웹폼 정보가 없을 수 있어 일부 필드는 optional로 둡니다.
export interface SalesCustomer {
  // 고객 기본 정보
  customerId: number
  customerName: string
  conversionStatusCode?: SalesCustomerStageCode
  conversionStatusName?: string
  customerStageCode: SalesCustomerStageCode
  customerStageName: string
  gender: string
  age: number
  birthDate: string
  parentId?: number | null

  // 보험 나이 변경 정보입니다. 서버 D-Day 값이 없으면 화면에서 날짜로 계산합니다.
  insuranceAgeShiftDate?: string
  ageIncreaseDDay?: number
  ageChangeLabel?: string

  // 3-Step 진단 정보입니다. 미진단 고객은 코드가 없을 수 있습니다.
  threeStepCode?: string
  threeStepName: string

  // 잠재고객 상담 상태입니다. 통합고객 중심 데이터에서는 코드가 없을 수 있습니다.
  consultStatusCode?: string
  consultStatusName: string

  // 계약 정보입니다. 아직 계약이 생성되지 않은 고객은 contractId/statusCode가 없습니다.
  contractId?: number
  contractStatusCode?: string
  contractStatusName: string
  insuranceName: string
  insuredName: string

  // 웹폼 발송/수신 상태입니다. 발송 전에는 ID나 상태 코드가 없을 수 있습니다.
  webFormId?: number
  webformReceivedAt: string
  webFormStatusCode?: string
  webFormStatusName?: string
  webformStatusCode?: string
  webformStatusName?: string

  // 리포트 생성/발송 상태입니다. 리포트가 없거나 발송 전이면 관련 값이 비어 있을 수 있습니다.
  graduated?: boolean | null
  reportId?: number
  reportUrl?: string | null
  hasReport: boolean
  reportStatusCode?: string
  reportStatusName: string
  reportSentAt?: string
  canSendReport: boolean
  sortRank: number
}

// 영업현황 고객 단계 코드입니다. 01은 잠재고객, 02는 통합고객입니다.
export type SalesCustomerStageCode = '01' | '02'

const CUSTOMER_STAGE_NAMES: Record<SalesCustomerStageCode, string> = {
  '01': '잠재 고객',
  '02': '통합 고객',
}

// 고객 단계 코드를 그대로 반환해 테이블/상세 이동 로직에서 동일한 기준을 사용하게 합니다.
export const resolveSalesCustomerStageCode = (customer: SalesCustomer): SalesCustomerStageCode | '' => {
  return customer.customerStageCode
}

// 서버 단계명이 비어 있으면 코드 기준 기본 라벨로 보완합니다.
export const resolveSalesCustomerStageName = (customer: SalesCustomer) => {
  return customer.customerStageName || CUSTOMER_STAGE_NAMES[customer.customerStageCode] || '-'
}

// 영업현황 목록 페이지네이션 응답입니다.
export interface SalesPage {
  page: number
  size: number
  totalCount: number
  totalPages: number
  items: SalesCustomer[]
}

type RawSalesCustomer = Partial<SalesCustomer> & Record<string, unknown>

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

const asBoolean = (value: unknown) => {
  if (typeof value === 'boolean') return value
  if (typeof value === 'number') return value !== 0
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase()
    if (normalized === 'true') return true
    if (normalized === 'false') return false
  }
  return undefined
}

const asStageCode = (value: unknown): SalesCustomerStageCode | undefined => {
  const code = asString(value)
  return code === '01' || code === '02' ? code : undefined
}

const normalizeSalesCustomer = (customer: RawSalesCustomer): SalesCustomer => {
  const stageCode =
    asStageCode(customer.customerStageCode) ??
    asStageCode(customer.conversionStatusCode) ??
    '01'
  const stageName =
    asString(customer.customerStageName) ??
    asString(customer.conversionStatusName) ??
    CUSTOMER_STAGE_NAMES[stageCode]

  const contractStatusCode = asString(customer.contractStatusCode)
  const contractStatusName =
    asString(customer.contractStatusName) ??
    asString(customer.contractStatus) ??
    ''
  const graduated = asBoolean(customer.graduated)
  const isGraduated = graduated === true
  const reportUrl = asString(customer.reportUrl) ?? null
  const rawReportStatusName = asString(customer.reportStatusName) ?? ''
  const reportStatusName =
    rawReportStatusName === '졸업' && !isGraduated ? '' : rawReportStatusName

  return {
    ...(customer as SalesCustomer),
    conversionStatusCode: stageCode,
    conversionStatusName: stageName,
    customerStageCode: stageCode,
    customerStageName: stageName,
    contractStatusCode,
    contractStatusName,
    parentId: asNumber(customer.parentId) ?? null,
    graduated: isGraduated,
    reportUrl,
    reportStatusName: isGraduated ? '졸업' : (reportUrl ? reportStatusName : '미생성'),
    canSendReport: isGraduated ? false : Boolean(customer.canSendReport),
    webformStatusCode: asString(customer.webformStatusCode) ?? asString(customer.webFormStatusCode),
    webformStatusName: asString(customer.webformStatusName) ?? asString(customer.webFormStatusName),
  }
}

// 개별 리포트 발송 결과입니다.
export interface ReportSendResult {
  customerId: number
  customerName: string
  sendStatusCode: string
  sendStatusName: string
  sentAt: string
}

// 리포트 일괄 발송 결과입니다.
export interface BulkSendResult {
  requestedCount: number
  successCount: number
  skippedCount: number
  failedCount: number
  sentAt: string
}

// 웹폼 발송 결과입니다.
export interface WebformSendResult {
  customerId: number
  conversionStatusCode: string
  uuidToken: string
  webformStatusCode: string
  webformStatusName: string
  issuedAt: string
  success?: boolean
  failed?: boolean
  reportProcessFailed?: boolean
  reportProcessed?: boolean
  reportProcessStatus?: string
  reportProcessingStatus?: string
  message?: string
}

// reportIds가 비어 있으면 서버가 현재 조건 기준 전체 발송으로 처리합니다.
export interface ReportBulkSendRequest {
  reportIds?: number[]
}

// 영업현황 검색 API 파라미터입니다.
// 사용자가 선택하지 않은 조건은 undefined로 보내지 않기 위해 optional로 둡니다.
export interface SalesSearchParams {
  customerName?: string
  age?: number
  gender?: 'Male' | 'Female'
  customerStageCode?: SalesCustomerStageCode
  consultStatusCode?: string[]
  contractStatusCode?: string[]
  hasReport?: boolean
  hasThreeStep?: boolean
  webformSent?: boolean
  reportSent?: boolean
  page: number
  size: number
}

// 화면 검색 폼에서는 page/size를 페이지 컴포넌트가 별도로 붙이므로 필터 타입에서 제외합니다.
export type SalesSearchFilters = Omit<SalesSearchParams, 'page' | 'size'>

// 배열 필터는 같은 key를 여러 번 붙여 백엔드가 다중 조건으로 받을 수 있게 직렬화합니다.
const serializeSalesSearchParams = (params: Record<string, unknown>) => {
  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined) return

    if (Array.isArray(value)) {
      value.forEach((item) => searchParams.append(key, item))
      return
    }

    searchParams.append(key, String(value))
  })

  return searchParams.toString()
}

// 현재 월 기준 영업 KPI 요약을 조회합니다. targetYearMonth는 yyyyMM 형식입니다.
export async function getSalesSummary(targetYearMonth: string) {
  const response = await api.get<ApiResponse<SalesSummary>>('/v1/sales/performance/summary', {
    params: { targetYearMonth },
  })

  return response.data.data
}

// 검색 조건과 페이지 정보를 함께 보내 영업현황 고객 목록을 조회합니다.
export async function getSalesList(params: SalesSearchParams) {
  const response = await api.get<ApiResponse<SalesPage>>('/v1/sales/performance/contracts', {
    params,
    paramsSerializer: serializeSalesSearchParams,
  })

  const data = response.data.data

  return {
    ...data,
    items: data.items.map((customer) => normalizeSalesCustomer(customer as RawSalesCustomer)),
  }
}

// 고객 1명에게 생성된 리포트를 발송하거나 재발송합니다.
export async function sendCustomerReport(customerId: number) {
  const response = await api.post<ApiResponse<ReportSendResult>>(
    `/v1/reports/${customerId}/send`,
  )

  return response.data.data
}

// 선택된 reportIds가 있으면 선택 건만, 없으면 서버 기준 발송 대상 전체를 일괄 발송합니다.
export async function sendCustomerReportsInBulk(reportIds?: number[]) {
  const data: ReportBulkSendRequest = reportIds?.length ? { reportIds } : {}
  const response = await api.post<ApiResponse<BulkSendResult>>(
    '/v1/reports/send/bulk',
    data,
  )

  return response.data.data
}

// 고객 단계 코드와 고객 ID를 기준으로 상담 웹폼을 발송합니다.
export async function sendCustomerWebform(
  sendSource: 'sales-status',
  conversionStatusCode: string,
  customerId: number,
) {
  const response = await api.post<ApiResponse<WebformSendResult>>(
    `/v1/webforms/${sendSource}/${conversionStatusCode}/${customerId}/send`,
  )

  return response.data.data
}

// 서버 기준 웹폼 발송 대상 전체에 일괄 발송을 요청합니다.
export async function sendCustomerWebformsInBulk() {
  const response = await api.post<ApiResponse<WebformSendResult[]>>(
    '/v1/webforms/sales-status/send/bulk',
  )

  return response.data.data
}
