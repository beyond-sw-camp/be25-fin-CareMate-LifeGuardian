import api from '@/api/instance'
import type { ApiResponse } from '@/api/instance'
// test test test
export interface BranchAnnualContracts {
  currentYearCount: number
  previousYearCount: number
  yoyGrowthRate: number
  annualTargetCount: number
  targetAchievementRate: number
}

export interface BranchMonthlyContracts {
  currentMonthCount: number
  previousMonthCount: number
  momDifferenceCount: number
  activeSalesUserCount: number
  averagePerUser: number
}

export interface PerformerInfo {
  rank: number
  employeeId: string
  employeeName: string
  contractCount: number
}

export interface BranchSalesRanking {
  appliedYearMonth: string
  topPerformers: PerformerInfo[]
  bottomPerformers: PerformerInfo[]
}

export interface SalesUserPerformanceDetail {
  rank: number
  groupCode: 'TOP' | 'MIDDLE' | 'BOTTOM'
  employeeId: string
  employeeName: string
  positionName: string
  thisMonthCount: number
  annualCount: number
  monthlyTargetCount: number
  targetDifference: number
}

export interface BranchPerformanceDetails {
  targetYearMonth: string
  performances: SalesUserPerformanceDetail[]
}

export interface MonthlyTrend {
  month: string
  count: number
}

export interface IndividualPerformance {
  employeeName: string
  positionName: string
  thisMonthCount: number
  annualCount: number
  monthlyTargetCount: number
  targetDifference: number
  monthlyTrends: MonthlyTrend[]
  previousMonthCount: number
  momDifference: number
  targetAchievementRate: number
  branchRank: number
  totalBranchUsers: number
  isTargetAchieved: boolean
}

export interface DashboardSalesUser {
  userId: number
  employeeName: string
  rank: number
  thisMonthCount: number
  targetDifference: number
  isPinned: boolean
}

export interface DashboardSalesUsersResponse {
  totalCount: number
  pinnedCount: number
  salesUsers: DashboardSalesUser[]
}

export interface EsgEnvironmentalScores {
  totalSavedCarbonKg: number
  totalSavedCostKrw: number
}

export interface HourlyPowerProfile {
  hour: string
  traditionalLoad: number
  optimizedLoad: number
}

export interface EsgPeakCutProfile {
  targetDate: string
  hourlyProfiles: HourlyPowerProfile[]
}

// 지점 연간 누적 계약 통계 조회
export async function getAnnualContracts(branchId: number, targetYear?: number) {
  const response = await api.get<ApiResponse<BranchAnnualContracts>>(`/v1/branches/${branchId}/statistics/annual-contracts`, { params: { targetYear } })
  return response.data.data
}

// 지점 월간 당월 계약 통계 조회
export async function getMonthlyContracts(branchId: number, targetYearMonth?: string) {
  const response = await api.get<ApiResponse<BranchMonthlyContracts>>(`/v1/branches/${branchId}/statistics/monthly-contracts`, { params: { targetYearMonth } })
  return response.data.data
}

// 지점 월간 판매 실적 상/하위 랭킹 차트 조회
export async function getSalesRanking(branchId: number, targetYearMonth?: string) {
  const response = await api.get<ApiResponse<BranchSalesRanking>>(`/v1/branches/${branchId}/statistics/sales-ranking`, { params: { targetYearMonth } })
  return response.data.data
}

// 지점 전체 영업사원 판매 성과 상세 조회
export async function getSalesPerformanceDetails(branchId: number) {
  const response = await api.get<ApiResponse<BranchPerformanceDetails>>(`/v1/branches/${branchId}/statistics/sales-performance/details`)
  return response.data.data
}

// 영업사원 개인 판매 실적 상세 조회
export async function getIndividualPerformance(branchId: number, targetUserId: number) {
  const response = await api.get<ApiResponse<IndividualPerformance>>(`/v1/branches/${branchId}/statistics/sales-users/${targetUserId}/performance`)
  return response.data.data
}

// 대시보드 영업사원 목록 조회
export async function getDashboardSalesUsers(branchId: number, keyword?: string) {
  const response = await api.get<ApiResponse<DashboardSalesUsersResponse>>(`/v1/branches/${branchId}/dashboard/sales-users`, { params: { keyword } })
  return response.data.data
}

// 대시보드 영업사원 핀 고정
export async function pinSalesUser(targetUserId: number) {
  const response = await api.post<ApiResponse<void>>(`/v1/dashboard/pinned-users/${targetUserId}`)
  return response.data
}

// 대시보드 영업사원 핀 해제
export async function unpinSalesUser(targetUserId: number) {
  const response = await api.delete<ApiResponse<void>>(`/v1/dashboard/pinned-users/${targetUserId}`)
  return response.data
}

// ESG 환경 점수 조회
export async function getEsgEnvironmentalScores() {
  const response = await api.get<ApiResponse<EsgEnvironmentalScores>>('/v1/admin/dashboard/esg/environmental-scores')
  return response.data.data
}

// ESG 피크 컷 차트 조회
export async function getEsgPeakCutProfile(targetDate?: string) {
  const response = await api.get<ApiResponse<EsgPeakCutProfile>>('/v1/admin/dashboard/esg/peak-cut-profile', { params: { targetDate } })
  return response.data.data
}

export interface AuditLogSearchFilters {
  startDate?: string
  endDate?: string
  actionTypeCode?: string
  page?: number
  size?: number
}

export interface AuditLogInfo {
  auditId: number
  createdAt: string
  salesUserId: number
  employeeName: string
  actionName: string
  customerFormattedId: string | null
  customerName: string | null
  ipAddress: string
}

export interface AuditLogResponse {
  totalElements: number
  totalPages: number
  content: AuditLogInfo[]
}

// 보안 감사 로그 조회
export async function getAuditLogs(params: AuditLogSearchFilters) {
  const response = await api.get<ApiResponse<AuditLogResponse>>('/v1/audit-logs', { params })
  return response.data.data
}
