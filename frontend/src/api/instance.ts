import axios, {
  type AxiosError,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from 'axios'

import router from '@/router'
import { ACCESS_TOKEN_STORAGE_KEY } from '@/constants/auth'
import { useAuthStore } from '@/stores/auth'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export type ApiError<T = null> = AxiosError<ApiResponse<T>>

export interface TokenReissueResponse {
  accessToken: string
}

type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean
}

const AUTH_LOGIN_URL = '/v1/auth/login'
const TOKEN_REISSUE_URL = '/v1/auth/reissue'
const TOKEN_LOGOUT_URL = '/v1/auth/logout'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10_000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

const tokenApi = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10_000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

let refreshTokenRequest: Promise<TokenReissueResponse> | null = null

const isAuthRequest = (url?: string) => {
  if (!url) return false

  return (
    url.includes(AUTH_LOGIN_URL) ||
    url.includes(TOKEN_REISSUE_URL) ||
    url.includes(TOKEN_LOGOUT_URL)
  )
}

const reissueToken = async () => {
  refreshTokenRequest ??= tokenApi
    .post<ApiResponse<TokenReissueResponse>>(TOKEN_REISSUE_URL)
    .then((response) => response.data.data)
    .finally(() => {
      refreshTokenRequest = null
    })

  return refreshTokenRequest
}

export const reissueAccessToken = async () => {
  const authStore = useAuthStore()
  const tokenInfo = await reissueToken()

  authStore.setAccessToken(tokenInfo.accessToken)

  return tokenInfo.accessToken
}

api.interceptors.request.use((config) => {
  const authStore = useAuthStore()

  const accessToken =
    authStore.accessToken ||
    sessionStorage.getItem(ACCESS_TOKEN_STORAGE_KEY)

  const isDevToken =
    import.meta.env.DEV &&
    accessToken?.startsWith('dev-')

  if (accessToken && !isDevToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }

  return config
})

api.interceptors.response.use(
  (response) => response,

  async (error: ApiError) => {
    const authStore = useAuthStore()
    const originalRequest =
      error.config as RetryableRequestConfig | undefined

    if (!originalRequest) {
      return Promise.reject(error)
    }

    const isUnauthorized = error.response?.status === 401

    if (
      !isUnauthorized ||
      originalRequest._retry ||
      isAuthRequest(originalRequest.url)
    ) {
      return Promise.reject(error)
    }

    originalRequest._retry = true

    try {
      const accessToken = await reissueAccessToken()

      originalRequest.headers.Authorization = `Bearer ${accessToken}`

      return api(originalRequest as AxiosRequestConfig)
    } catch (refreshError) {
      authStore.clearAuthInfo()
      await router.replace('/login')

      return Promise.reject(refreshError)
    }
  },
)

export default api
