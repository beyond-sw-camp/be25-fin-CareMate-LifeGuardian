import api, { type ApiResponse } from '@/api/instance'

export interface WebformResponseSubmitRequest {
  customerId: number
  conversionStatusCode: string
  uuidToken?: string
  height: number
  weight: number
  selectedPriorityCategory: string
  historyList: string[]
  activityList: string[]
  pastSurgeryOrHospitalization: boolean
  desiredBudgetCode: string
}

export const submitWebformResponse = async (data: WebformResponseSubmitRequest): Promise<void> => {
  await api.post<ApiResponse<void>>('/v1/webforms/response', data)
}

export const verifyCustomer = async (customerId: number, conversionStatusCode: string): Promise<string> => {
  const response = await api.get<ApiResponse<string>>('/v1/webforms/customer/verify', {
    params: { customerId, conversionStatusCode }
  })
  if (response.data.code === 404) {
    throw new Error(response.data.message)
  }
  return response.data.data
}

export interface WebformTokenVerifyResponse {
  customerId: number
  conversionStatusCode: string
  customerName: string
}

export const verifyWebformToken = async (token: string): Promise<WebformTokenVerifyResponse> => {
  const response = await api.get<ApiResponse<WebformTokenVerifyResponse>>('/v1/webforms/token/verify', {
    params: { token }
  })
  if (response.data.code !== 200) {
    throw new Error(response.data.message)
  }
  return response.data.data
}

