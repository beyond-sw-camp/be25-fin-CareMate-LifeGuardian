import api, { type ApiResponse } from '@/api/instance'

export type ReportHistorySendType = 'all' | 'report' | 'webform'
export type ReportHistorySendItemType = 'all' | 'report_lifecycle' | 'report_disease' | 'webform'
export type ReportHistorySendStatus =
  | 'all'
  | 'pending'
  | 'success'
  | 'failed'
  | 'collected'
export type ReportHistoryCustomerStageCode = 'all' | '01' | '02'

export interface ReportHistoryItem {
  id: number
  sendType: Exclude<ReportHistorySendType, 'all'>
  sendTypeName: string
  customerId: number
  customerName: string
  customerStageCode: string
  customerStageName: string
  sendItemCode?: string
  sendItemName: string
  sendStatusCode: string
  sendStatusName: string
  sentAt: string
}

export interface ReportHistoryPage {
  page: number
  size: number
  totalCount: number
  totalPages: number
  items: ReportHistoryItem[]
}

export interface ReportHistorySearchParams {
  sendType: ReportHistorySendType
  sendItemType: ReportHistorySendItemType
  sendStatus: ReportHistorySendStatus
  customerStageCode: ReportHistoryCustomerStageCode
  keyword?: string
  page: number
  size: number
}

export async function getReportHistory(params: ReportHistorySearchParams) {
  const response = await api.get<ApiResponse<ReportHistoryPage>>('/v1/report-history', {
    params,
  })

  return response.data.data
}
