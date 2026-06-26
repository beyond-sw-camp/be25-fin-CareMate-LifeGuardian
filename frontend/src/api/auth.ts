import api, { reissueAccessToken, type ApiResponse } from '@/api/instance'
import type { UserRole } from '@/constants/auth'

export interface LoginRequest {
  loginId: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  userId: number
  name: string
  branchId: number
  branchName: string
  role: UserRole
  isFirstLogin: boolean
}

export interface ChangePasswordRequest {
  newPassword: string
  confirmPassword: string
  privacyPolicyAgreed: boolean
}

export async function login(request: LoginRequest) {
  const response = await api.post<ApiResponse<LoginResponse>>(
    '/v1/auth/login',
    request,
  )

  return response.data.data
}

export async function reissueToken() {
  const accessToken = await reissueAccessToken()

  return {
    accessToken,
  }
}

export async function logout() {
  await api.post('/v1/auth/logout')
}

export async function changePassword(request: ChangePasswordRequest) {
  await api.patch('/v1/auth/initial-password', request)
}
