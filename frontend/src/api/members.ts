import api from '@/api/instance'
import type { ApiResponse } from '@/api/instance'

export interface SalesUserSearchFilters {
  keyword?: string
  statusCode?: string
  page?: number
  size?: number
}

export interface SalesUserSummary {
  id: number
  employeeId: string
  name: string
  statusCode: string
  statusName: string
  customerCount: number
}

export interface SalesUserListResponse {
  totalElements: number
  totalPages: number
  content: SalesUserSummary[]
}

export interface SalesUserRegisterRequest {
  name: string
  birthDate: string
  branchId: number
  rankCode: string
  phone: string
  email: string
  joinedAt: string
  roleCode?: string
}

export interface SalesUserRegisterResponse {
  id: number
  employeeId: string
  temporaryPassword: string
}

export interface SalesUserStatusUpdateResponse {
  id: number
  statusCode: string
  statusName: string
}

export interface SalesUserRetireResponse {
  id: number
  statusCode: string
  statusName: string
  invalidatedTokenCount: number
  retiredAt: string
}

export interface SalesUserCustomerTransferResponse {
  fromUserId: number
  toUserId: number
  transferredPotentialCount: number
  transferredIntegratedCount: number
}

export interface PiiSecureSummary {
  employeeId: string
  retiredAt: string
  purgedAt: string
  remainingDays: number
  statusName: string
}

export interface SalesUserPiiSecureListResponse {
  totalElements: number
  totalPages: number
  content: PiiSecureSummary[]
}

// 1. 영업사원 목록 조회 (인사 관리용)
export async function getSalesUserList(params: SalesUserSearchFilters) {
  const response = await api.get<ApiResponse<SalesUserListResponse>>('/v1/sales-users', { params })
  return response.data.data
}

// 2. 신입 영업사원 등록
export async function registerSalesUser(data: SalesUserRegisterRequest) {
  const response = await api.post<ApiResponse<SalesUserRegisterResponse>>('/v1/sales-users', data)
  return response.data.data
}

// 3. 영업사원 상태 변경
export async function changeSalesUserStatus(userId: number, statusCode: string) {
  const response = await api.patch<ApiResponse<SalesUserStatusUpdateResponse>>(`/v1/sales-users/${userId}/status`, { statusCode })
  return response.data
}

// 4. 퇴사자 계정 비활성화 및 세션 파기
export async function retireSalesUser(userId: number) {
  const response = await api.delete<ApiResponse<SalesUserRetireResponse>>(`/v1/sales-users/${userId}`)
  return response.data
}

// 5. 퇴사자 고객 일괄 이관
export async function transferCustomers(userId: number, toUserId: number) {
  const response = await api.post<ApiResponse<SalesUserCustomerTransferResponse>>(`/v1/sales-users/${userId}/transfer-customers`, { toUserId })
  return response.data.data
}

// 6. 퇴사자 PII 분리 보관 현황 조회
export async function getPiiSecureList(params: { page?: number; size?: number }) {
  const response = await api.get<ApiResponse<SalesUserPiiSecureListResponse>>('/v1/sales-users/pii-secure', { params })
  return response.data.data
}
